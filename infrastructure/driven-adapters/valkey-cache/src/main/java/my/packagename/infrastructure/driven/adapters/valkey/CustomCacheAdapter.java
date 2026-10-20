package my.packagename.infrastructure.driven.adapters.valkey;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cache-aside decorator over {@link CustomRepositoryPort}. Fail-open: any Valkey problem degrades
 * to the delegate. Writes evict twice — now and after commit — so a reader racing the transaction
 * cannot re-cache the pre-commit row.
 */
@Component
@Order(CustomCacheAdapter.ORDER)
public class CustomCacheAdapter
    implements CustomRepositoryPort, ChainablePort<CustomRepositoryPort> {

  public static final int ORDER = 50;
  public static final String GETS = "cache.gets";
  public static final String ERRORS = "cache.errors";

  private static final Logger log = LoggerFactory.getLogger(CustomCacheAdapter.class);
  private static final String CACHE_NAME = "custom";

  private final StringRedisTemplate redis;
  private final JsonMapper json;
  private final CustomCacheProperties properties;
  private final MeterRegistry registry;
  private CustomRepositoryPort delegate;

  public CustomCacheAdapter(
      StringRedisTemplate redis,
      JsonMapper json,
      CustomCacheProperties properties,
      MeterRegistry registry) {
    this.redis = redis;
    this.json = json;
    this.properties = properties;
    this.registry = registry;
  }

  @Override
  public void setDelegate(CustomRepositoryPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Optional<Custom> findById(CustomId id) {
    Optional<Custom> cached = read(id);
    countGet(cached.isPresent() ? "hit" : "miss");
    return cached.isPresent() ? cached : loadAndStore(id);
  }

  @Override
  public Custom insert(Custom custom) {
    return delegate.insert(custom);
  }

  @Override
  public Custom update(Custom custom) {
    Custom updated = delegate.update(custom);
    evictNowAndAfterCommit(custom.id());
    return updated;
  }

  @Override
  public PageResult<Custom> findAll(PageQuery query) {
    return delegate.findAll(query);
  }

  @Override
  public boolean deleteById(CustomId id) {
    boolean deleted = delegate.deleteById(id);
    evictNowAndAfterCommit(id);
    return deleted;
  }

  private Optional<Custom> loadAndStore(CustomId id) {
    Optional<Custom> loaded = delegate.findById(id);
    loaded.ifPresent(this::store);
    return loaded;
  }

  private Optional<Custom> read(CustomId id) {
    try {
      return Optional.ofNullable(redis.opsForValue().get(key(id)))
          .map(raw -> json.readValue(raw, CachedCustom.class).toDomain());
    } catch (DataAccessException | JacksonException ex) {
      failOpen("read", id, ex);
      return Optional.empty();
    }
  }

  private void store(Custom custom) {
    try {
      String raw = json.writeValueAsString(CachedCustom.from(custom));
      redis.opsForValue().set(key(custom.id()), raw, properties.ttl());
    } catch (DataAccessException | JacksonException ex) {
      failOpen("write", custom.id(), ex);
    }
  }

  private void evictNowAndAfterCommit(CustomId id) {
    evict(id);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              evict(id);
            }
          });
    }
  }

  private void evict(CustomId id) {
    try {
      redis.delete(key(id));
    } catch (DataAccessException ex) {
      failOpen("evict", id, ex);
    }
  }

  private void failOpen(String operation, CustomId id, RuntimeException ex) {
    log.warn("Valkey {} failed for custom {}; falling back to source", operation, id, ex);
    registry.counter(ERRORS, "cache", CACHE_NAME, "operation", operation).increment();
  }

  private void countGet(String result) {
    registry.counter(GETS, "cache", CACHE_NAME, "result", result).increment();
  }

  private String key(CustomId id) {
    return properties.keyPrefix() + id;
  }
}

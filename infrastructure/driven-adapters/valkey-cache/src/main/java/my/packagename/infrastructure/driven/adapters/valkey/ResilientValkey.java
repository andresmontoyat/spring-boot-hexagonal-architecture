package my.packagename.infrastructure.driven.adapters.valkey;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Valkey I/O behind a circuit breaker, fail-open. While the breaker is open, calls return at once
 * instead of paying the command timeout on every request (resilience.md §3).
 */
@Component
public class ResilientValkey {

  public static final String BREAKER = "valkey";
  public static final String ERRORS = "cache.errors";
  public static final String SHORT_CIRCUITED = "cache.short_circuited";

  private static final Logger log = LoggerFactory.getLogger(ResilientValkey.class);

  private final StringRedisTemplate redis;
  private final CircuitBreaker breaker;
  private final MeterRegistry registry;

  @Autowired
  public ResilientValkey(
      StringRedisTemplate redis, CircuitBreakerRegistry breakers, MeterRegistry registry) {
    this(redis, breakers.circuitBreaker(BREAKER), registry);
  }

  ResilientValkey(StringRedisTemplate redis, CircuitBreaker breaker, MeterRegistry registry) {
    this.redis = redis;
    this.breaker = breaker;
    this.registry = registry;
  }

  public Optional<String> get(String key) {
    return call("get", () -> Optional.ofNullable(redis.opsForValue().get(key)))
        .flatMap(value -> value);
  }

  public void set(String key, String value, Duration ttl) {
    call(
        "set",
        () -> {
          redis.opsForValue().set(key, value, ttl);
          return Boolean.TRUE;
        });
  }

  public void delete(String key) {
    call("delete", () -> redis.delete(key));
  }

  private <T> Optional<T> call(String operation, Supplier<T> io) {
    try {
      return Optional.ofNullable(breaker.executeSupplier(io));
    } catch (CallNotPermittedException ex) {
      registry.counter(SHORT_CIRCUITED, "cache", BREAKER).increment();
      return Optional.empty();
    } catch (DataAccessException ex) {
      log.warn("Valkey {} failed; falling back to source", operation, ex);
      registry.counter(ERRORS, "cache", BREAKER, "operation", operation).increment();
      return Optional.empty();
    }
  }
}

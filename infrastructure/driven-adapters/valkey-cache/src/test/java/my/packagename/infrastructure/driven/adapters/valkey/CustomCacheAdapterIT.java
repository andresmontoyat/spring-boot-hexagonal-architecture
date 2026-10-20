package my.packagename.infrastructure.driven.adapters.valkey;

import static my.packagename.application.fake.CustomFixtures.FIXED_ID;
import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import my.packagename.application.fake.InMemoryCustomRepository;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

@DataRedisTest
@Import(ValkeyContainerConfiguration.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomCacheAdapterIT {

  private static final CustomCacheProperties PROPERTIES =
      new CustomCacheProperties(Duration.ofMinutes(5), "test:custom:");

  @Autowired private StringRedisTemplate redis;

  private InMemoryCustomRepository database;
  private MeterRegistry registry;
  private CustomCacheAdapter cache;

  @BeforeEach
  void setUp() {
    redis.delete(PROPERTIES.keyPrefix() + FIXED_ID);
    database = new InMemoryCustomRepository();
    registry = new SimpleMeterRegistry();
    cache = new CustomCacheAdapter(redis, JsonMapper.builder().build(), PROPERTIES, registry);
    cache.setDelegate(database);
    database.insert(Custom.create(FIXED_ID, new CustomName("Demo")));
  }

  @Test
  void should_serve_second_read_from_valkey_when_first_read_missed() {
    // When
    cache.findById(FIXED_ID);
    Custom second = cache.findById(FIXED_ID).orElseThrow();

    // Then
    assertThat(second.name().value()).isEqualTo("Demo");
    assertThat(database.findByIdCalls()).isOne();
    assertThat(gets("hit")).isEqualTo(1.0);
    assertThat(gets("miss")).isEqualTo(1.0);
  }

  @Test
  void should_store_entry_with_ttl_when_read_misses() {
    // When
    cache.findById(FIXED_ID);

    // Then
    assertThat(redis.getExpire(PROPERTIES.keyPrefix() + FIXED_ID)).isPositive();
  }

  @Test
  void should_evict_entry_when_custom_is_updated() {
    // Given
    Custom cached = cache.findById(FIXED_ID).orElseThrow();

    // When
    cache.update(cached.rename(new CustomName("Renamed")));

    // Then
    assertThat(cache.findById(FIXED_ID).orElseThrow().name().value()).isEqualTo("Renamed");
    assertThat(database.findByIdCalls()).isEqualTo(2);
  }

  @Test
  void should_evict_entry_when_custom_is_deleted() {
    // Given
    cache.findById(FIXED_ID);

    // When
    boolean deleted = cache.deleteById(FIXED_ID);

    // Then
    assertThat(deleted).isTrue();
    assertThat(cache.findById(FIXED_ID)).isEmpty();
  }

  @Test
  void should_fall_back_to_delegate_when_entry_is_corrupt() {
    // Given
    redis.opsForValue().set(PROPERTIES.keyPrefix() + FIXED_ID, "{not json");

    // When / Then
    assertThat(cache.findById(FIXED_ID)).isPresent();
    assertThat(database.findByIdCalls()).isOne();
  }

  private double gets(String result) {
    return registry.get(CustomCacheAdapter.GETS).tag("result", result).counter().count();
  }
}

package my.packagename.infrastructure.driven.adapters.valkey;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Valkey unreachable: calls degrade, and once the breaker opens they stop paying the timeout. */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ResilientValkeyTest {

  private static final int CLOSED_PORT = 1;
  private static final int WINDOW = 2;
  private static final String KEY = "test:key";

  private LettuceConnectionFactory deadValkey;
  private CircuitBreaker breaker;
  private MeterRegistry registry;
  private ResilientValkey valkey;

  @BeforeEach
  void setUp() {
    deadValkey =
        new LettuceConnectionFactory(
            new RedisStandaloneConfiguration("localhost", CLOSED_PORT),
            LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(200)).build());
    deadValkey.start();
    breaker =
        CircuitBreaker.of(
            "valkey",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(WINDOW)
                .minimumNumberOfCalls(WINDOW)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build());
    registry = new SimpleMeterRegistry();
    valkey = new ResilientValkey(new StringRedisTemplate(deadValkey), breaker, registry);
  }

  @AfterEach
  void tearDown() {
    deadValkey.destroy();
  }

  @Test
  void should_return_empty_instead_of_failing_when_valkey_is_unreachable() {
    assertThat(valkey.get(KEY)).isEmpty();
    assertThat(errors("get")).isEqualTo(1.0);
  }

  @Test
  void should_open_circuit_and_skip_valkey_when_failures_fill_the_window() {
    // Given
    valkey.get(KEY);
    valkey.delete(KEY);

    // When
    valkey.set(KEY, "value", Duration.ofMinutes(1));

    // Then
    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    assertThat(registry.get(ResilientValkey.SHORT_CIRCUITED).counter().count()).isEqualTo(1.0);
  }

  private double errors(String operation) {
    return registry.get(ResilientValkey.ERRORS).tag("operation", operation).counter().count();
  }
}

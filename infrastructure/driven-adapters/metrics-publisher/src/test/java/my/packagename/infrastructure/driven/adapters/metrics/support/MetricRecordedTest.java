package my.packagename.infrastructure.driven.adapters.metrics.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MetricRecordedTest {

  private static final String ENTITY = "custom";
  private static final String OPERATION = "probe";

  private MeterRegistry registry;
  private Probe probe;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    probe = new Probe(registry);
  }

  @Test
  void should_time_and_count_success_when_operation_completes() {
    // When
    String result = probe.call(() -> "ok");

    // Then
    assertThat(result).isEqualTo("ok");
    assertThat(registry.get(MetricRecorded.TIMER).tags(tags()).timer().count()).isOne();
    assertThat(calls("true")).isEqualTo(1.0);
  }

  @Test
  void should_count_failure_and_error_type_when_operation_throws() {
    // When
    assertThatThrownBy(() -> probe.call(this::boom)).isInstanceOf(IllegalStateException.class);

    // Then
    assertThat(calls("false")).isEqualTo(1.0);
    assertThat(
            registry
                .get(MetricRecorded.ERRORS)
                .tags(tags())
                .tag("error", "IllegalStateException")
                .counter()
                .count())
        .isEqualTo(1.0);
    assertThat(registry.get(MetricRecorded.TIMER).tags(tags()).timer().count()).isOne();
  }

  @Test
  void should_run_first_in_chain_when_ordered() {
    assertThat(probe.getOrder()).isEqualTo(MetricRecorded.ORDER);
  }

  private double calls(String success) {
    return registry
        .get(MetricRecorded.CALLS)
        .tags(tags())
        .tag("success", success)
        .counter()
        .count();
  }

  private String[] tags() {
    return new String[] {"entity", ENTITY, "operation", OPERATION};
  }

  private String boom() {
    throw new IllegalStateException("boom");
  }

  private static final class Probe extends MetricRecorded {

    Probe(MeterRegistry registry) {
      super(registry);
    }

    String call(Supplier<String> work) {
      return record(ENTITY, OPERATION, work);
    }
  }
}

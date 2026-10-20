package my.packagename.infrastructure.driven.adapters.metrics.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import org.springframework.core.Ordered;

/**
 * Base for use case metrics decorators. Tags stay low-cardinality (entity, operation, success,
 * error class) — never ids — so the OTLP backend does not explode.
 */
public abstract class MetricRecorded implements Ordered {

  public static final String TIMER = "usecase.operation";
  public static final String CALLS = "usecase.operation.calls";
  public static final String ERRORS = "usecase.operation.errors";

  /** First in the chain so the timer covers cache and persistence too. */
  public static final int ORDER = 10;

  private final MeterRegistry registry;

  protected MetricRecorded(MeterRegistry registry) {
    this.registry = registry;
  }

  protected <T> T record(String entity, String operation, Supplier<T> work) {
    Tags tags = Tags.of("entity", entity, "operation", operation);
    Timer.Sample sample = Timer.start(registry);
    try {
      T result = work.get();
      registry.counter(CALLS, tags.and("success", "true")).increment();
      return result;
    } catch (RuntimeException ex) {
      registry.counter(CALLS, tags.and("success", "false")).increment();
      registry.counter(ERRORS, tags.and("error", ex.getClass().getSimpleName())).increment();
      throw ex;
    } finally {
      sample.stop(registry.timer(TIMER, tags));
    }
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}

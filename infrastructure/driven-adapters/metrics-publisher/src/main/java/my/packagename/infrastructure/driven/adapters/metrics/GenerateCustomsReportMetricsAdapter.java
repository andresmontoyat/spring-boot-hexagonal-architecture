package my.packagename.infrastructure.driven.adapters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Locale;
import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.GenerateCustomsReportPort;
import my.packagename.application.result.ReportOutcome;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.springframework.stereotype.Component;

/**
 * Besides the usual timer, counts outcomes: any {@code already_generated} means two runs reached
 * the same period, so the scheduler lock was bypassed or expired. Alert on it.
 */
@Component
public class GenerateCustomsReportMetricsAdapter extends MetricRecorded
    implements GenerateCustomsReportPort, ChainablePort<GenerateCustomsReportPort> {

  public static final String OUTCOMES = "customs.report.outcomes";

  private final MeterRegistry registry;
  private GenerateCustomsReportPort delegate;

  public GenerateCustomsReportMetricsAdapter(MeterRegistry registry) {
    super(registry);
    this.registry = registry;
  }

  @Override
  public void setDelegate(GenerateCustomsReportPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public ReportOutcome execute(GenerateCustomsReportCommand command) {
    ReportOutcome outcome =
        record("customs_report", "generate_customs_report", () -> delegate.execute(command));
    registry.counter(OUTCOMES, "outcome", outcome.name().toLowerCase(Locale.ROOT)).increment();
    return outcome;
  }
}

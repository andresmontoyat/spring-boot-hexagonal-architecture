package my.packagename.infrastructure.driven.adapters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import my.packagename.application.command.RenameCustomCommand;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.RenameCustomPort;
import my.packagename.domain.model.Custom;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.springframework.stereotype.Component;

@Component
public class RenameCustomMetricsAdapter extends MetricRecorded
    implements RenameCustomPort, ChainablePort<RenameCustomPort> {

  private RenameCustomPort delegate;

  public RenameCustomMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(RenameCustomPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Custom execute(RenameCustomCommand command) {
    return record("custom", "rename_custom", () -> delegate.execute(command));
  }
}

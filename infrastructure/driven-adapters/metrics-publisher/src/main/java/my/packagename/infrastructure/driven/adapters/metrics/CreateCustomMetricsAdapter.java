package my.packagename.infrastructure.driven.adapters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import my.packagename.application.command.CreateCustomCommand;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.CreateCustomPort;
import my.packagename.domain.model.Custom;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.springframework.stereotype.Component;

@Component
public class CreateCustomMetricsAdapter extends MetricRecorded
    implements CreateCustomPort, ChainablePort<CreateCustomPort> {

  private CreateCustomPort delegate;

  public CreateCustomMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(CreateCustomPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Custom execute(CreateCustomCommand command) {
    return record("custom", "create_custom", () -> delegate.execute(command));
  }
}

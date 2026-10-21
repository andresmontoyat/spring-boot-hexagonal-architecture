package my.packagename.infrastructure.driven.adapters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.DeleteCustomPort;
import my.packagename.domain.model.CustomId;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.springframework.stereotype.Component;

@Component
public class DeleteCustomMetricsAdapter extends MetricRecorded
    implements DeleteCustomPort, ChainablePort<DeleteCustomPort> {

  private DeleteCustomPort delegate;

  public DeleteCustomMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(DeleteCustomPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public void execute(CustomId id) {
    record(
        "custom",
        "delete_custom",
        () -> {
          delegate.execute(id);
          return null;
        });
  }
}

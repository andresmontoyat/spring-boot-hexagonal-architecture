package my.packagename.infrastructure.driven.adapters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.FindCustomPort;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.springframework.stereotype.Component;

@Component
public class FindCustomMetricsAdapter extends MetricRecorded
    implements FindCustomPort, ChainablePort<FindCustomPort> {

  private FindCustomPort delegate;

  public FindCustomMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(FindCustomPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Custom execute(CustomId id) {
    return record("custom", "find_custom", () -> delegate.execute(id));
  }
}

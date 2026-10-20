package my.packagename.infrastructure.driven.adapters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.ListCustomsPort;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.springframework.stereotype.Component;

@Component
public class ListCustomsMetricsAdapter extends MetricRecorded
    implements ListCustomsPort, ChainablePort<ListCustomsPort> {

  private ListCustomsPort delegate;

  public ListCustomsMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(ListCustomsPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public PageResult<Custom> execute(PageQuery query) {
    return record("custom", "list_customs", () -> delegate.execute(query));
  }
}

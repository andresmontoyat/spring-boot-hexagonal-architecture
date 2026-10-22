package my.packagename.application.fake;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import my.packagename.application.port.outbound.CustomsReportRepositoryPort;
import my.packagename.domain.model.CustomsReport;
import my.packagename.domain.model.ReportPeriod;

/** Same contract as the JDBC adapter: first report of a period wins, atomically. */
public final class InMemoryCustomsReportRepository implements CustomsReportRepositoryPort {

  private final Map<ReportPeriod, CustomsReport> store = new ConcurrentHashMap<>();

  @Override
  public boolean insertIfAbsent(CustomsReport report) {
    return store.putIfAbsent(report.period(), report) == null;
  }

  public Optional<CustomsReport> find(ReportPeriod period) {
    return Optional.ofNullable(store.get(period));
  }

  public int size() {
    return store.size();
  }
}

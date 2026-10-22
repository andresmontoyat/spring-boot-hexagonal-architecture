package my.packagename.infrastructure.driven.adapters.jdbc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import my.packagename.application.port.outbound.CustomsReportRepositoryPort;
import my.packagename.domain.model.CustomsReport;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * {@code ON CONFLICT DO NOTHING} rather than check-then-insert: a SELECT followed by an INSERT lets
 * two concurrent runs both see "absent". Here the second waits for the first to commit, then
 * inserts nothing.
 */
@Component
public class CustomsReportRepositoryAdapter implements CustomsReportRepositoryPort {

  private static final String INSERT_IF_ABSENT =
      """
      INSERT INTO customs_report (period_start, total_customs)
      VALUES (:periodStart, :totalCustoms)
      ON CONFLICT (period_start) DO NOTHING
      """;

  private final JdbcClient jdbc;

  public CustomsReportRepositoryAdapter(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public boolean insertIfAbsent(CustomsReport report) {
    return jdbc.sql(INSERT_IF_ABSENT)
            .param("periodStart", OffsetDateTime.ofInstant(report.period().start(), ZoneOffset.UTC))
            .param("totalCustoms", report.totalCustoms())
            .update()
        == 1;
  }
}

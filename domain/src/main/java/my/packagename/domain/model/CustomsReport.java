package my.packagename.domain.model;

import java.util.Objects;

/** Snapshot of how many customs existed during a period. At most one per period. */
public record CustomsReport(ReportPeriod period, long totalCustoms) {

  public CustomsReport {
    Objects.requireNonNull(period, "period must not be null");
    if (totalCustoms < 0) {
      throw new IllegalArgumentException("totalCustoms must be non-negative");
    }
  }
}

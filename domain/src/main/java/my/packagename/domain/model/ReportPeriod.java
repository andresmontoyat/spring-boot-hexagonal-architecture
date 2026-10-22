package my.packagename.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * The minute a customs report covers. Every instance that fires inside the same minute computes the
 * same period, which is what lets storage reject the duplicate.
 */
public record ReportPeriod(Instant start) {

  public ReportPeriod {
    Objects.requireNonNull(start, "start must not be null");
    if (!start.truncatedTo(ChronoUnit.MINUTES).equals(start)) {
      throw new IllegalArgumentException("start must be at the beginning of a minute");
    }
  }

  public static ReportPeriod containing(Instant instant) {
    return new ReportPeriod(instant.truncatedTo(ChronoUnit.MINUTES));
  }
}

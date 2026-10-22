package my.packagename.infrastructure.entry.points.scheduler;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param lockName per service ({@code customs-report-<app>}): one name shared by every service
 *     would serialize them all
 * @param lockAtMostFor safety net for a crashed holder; must exceed the slowest run, or the lock
 *     expires mid-run and a second instance overlaps
 * @param lockAtLeastFor keeps the lock after a fast run, so an instance that fires a little later
 *     (scheduler jitter, clock skew) skips instead of running the same minute again
 */
@ConfigurationProperties("jobs.customs-report")
public record CustomsReportJobProperties(
    String lockName, Duration lockAtMostFor, Duration lockAtLeastFor) {

  public CustomsReportJobProperties {
    Objects.requireNonNull(lockName, "lockName must not be null");
    Objects.requireNonNull(lockAtMostFor, "lockAtMostFor must not be null");
    Objects.requireNonNull(lockAtLeastFor, "lockAtLeastFor must not be null");
    if (lockAtLeastFor.compareTo(lockAtMostFor) > 0) {
      throw new IllegalArgumentException("lockAtLeastFor must not exceed lockAtMostFor");
    }
  }
}

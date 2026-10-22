package my.packagename.infrastructure.entry.points.scheduler;

import java.time.Clock;
import java.util.Optional;
import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.port.inbound.GenerateCustomsReportPort;
import my.packagename.application.result.ReportOutcome;
import net.javacrumbs.shedlock.core.ClockProvider;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Every instance fires this cron; the ShedLock lock lets one of them run it. The lock is taken
 * programmatically, like the outbox SDK does, so the skip is visible here and the lock windows are
 * typed properties rather than annotation strings.
 */
@Component
public class CustomsReportJob {

  private static final Logger log = LoggerFactory.getLogger(CustomsReportJob.class);

  private final GenerateCustomsReportPort generateReport;
  private final LockProvider lockProvider;
  private final CustomsReportJobProperties properties;
  private final Clock clock = Clock.systemUTC();

  public CustomsReportJob(
      GenerateCustomsReportPort generateReport,
      LockProvider lockProvider,
      CustomsReportJobProperties properties) {
    this.generateReport = generateReport;
    this.lockProvider = lockProvider;
    this.properties = properties;
  }

  @Scheduled(cron = "${jobs.customs-report.cron}", zone = "UTC")
  public void run() {
    Optional<SimpleLock> lock = lockProvider.lock(lockConfiguration());
    if (lock.isEmpty()) {
      log.info("Customs report skipped: lock {} is held by another run", properties.lockName());
      return;
    }
    try {
      ReportOutcome outcome =
          generateReport.execute(new GenerateCustomsReportCommand(clock.instant()));
      log.info("Customs report run finished: {}", outcome);
    } finally {
      lock.get().unlock();
    }
  }

  private LockConfiguration lockConfiguration() {
    return new LockConfiguration(
        ClockProvider.now(),
        properties.lockName(),
        properties.lockAtMostFor(),
        properties.lockAtLeastFor());
  }
}

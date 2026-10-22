package my.packagename.infrastructure.entry.points.scheduler;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.port.inbound.GenerateCustomsReportPort;
import my.packagename.application.result.ReportOutcome;

/**
 * Stands in for the use case and records how many runs happened and how many overlapped. A blocking
 * port keeps every run inside {@code execute} until {@link #release()}, which is how the tests hold
 * a lock for as long as they need.
 */
final class ObservedReportPort implements GenerateCustomsReportPort {

  private static final Duration MAX_BLOCK = Duration.ofSeconds(30);

  private final CountDownLatch release;
  private final AtomicInteger executions = new AtomicInteger();
  private final AtomicInteger running = new AtomicInteger();
  private final AtomicInteger maxOverlap = new AtomicInteger();

  private ObservedReportPort(int blockers) {
    this.release = new CountDownLatch(blockers);
  }

  static ObservedReportPort blocking() {
    return new ObservedReportPort(1);
  }

  static ObservedReportPort immediate() {
    return new ObservedReportPort(0);
  }

  @Override
  public ReportOutcome execute(GenerateCustomsReportCommand command) {
    executions.incrementAndGet();
    maxOverlap.accumulateAndGet(running.incrementAndGet(), Math::max);
    try {
      awaitRelease();
      return ReportOutcome.GENERATED;
    } finally {
      running.decrementAndGet();
    }
  }

  void release() {
    release.countDown();
  }

  int executions() {
    return executions.get();
  }

  /** Highest number of runs that were inside the job at the same moment. */
  int maxOverlap() {
    return maxOverlap.get();
  }

  private void awaitRelease() {
    try {
      if (!release.await(MAX_BLOCK.toMillis(), TimeUnit.MILLISECONDS)) {
        throw new IllegalStateException("test never released the job");
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("interrupted while holding the job", ex);
    }
  }
}

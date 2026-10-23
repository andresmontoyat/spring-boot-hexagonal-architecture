package my.packagename;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.port.inbound.GenerateCustomsReportPort;
import my.packagename.application.result.ReportOutcome;
import my.packagename.infrastructure.driven.adapters.metrics.GenerateCustomsReportMetricsAdapter;
import my.packagename.infrastructure.entry.points.scheduler.CustomsReportJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * The two layers of protection, wired for real: the ShedLock lock (through the job) and the
 * per-period primary key (through the use case, for runs that get past an expired lock).
 */
@IntegrationTestContainers
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomsReportJobAcceptanceTest {

  private static final int CONCURRENT_RUNS = 4;
  private static final Instant FIRED_AT = Instant.parse("2026-10-22T10:15:07Z");

  @Autowired private CustomsReportJob job;
  @Autowired private GenerateCustomsReportPort generateReport;
  @Autowired private JdbcClient jdbc;
  @Autowired private MeterRegistry registry;

  @BeforeEach
  void clean() {
    jdbc.sql("DELETE FROM customs_report").update();
    jdbc.sql("DELETE FROM shedlock WHERE name = 'customs-report-test'").update();
  }

  @Test
  void should_store_one_report_when_several_instances_fire_together() {
    // When
    runConcurrently(job::run);

    // Then
    assertThat(storedReports()).isOne();
    assertThat(lockHolder()).as("the winner's lock row").isNotBlank();
  }

  @Test
  void should_keep_one_report_per_period_when_runs_get_past_the_lock() {
    // Given
    double duplicatesBefore = duplicates();

    // When
    List<ReportOutcome> outcomes =
        collectConcurrently(
            () -> generateReport.execute(new GenerateCustomsReportCommand(FIRED_AT)));

    // Then
    assertThat(outcomes)
        .containsOnlyOnce(ReportOutcome.GENERATED)
        .containsOnly(ReportOutcome.GENERATED, ReportOutcome.ALREADY_GENERATED);
    assertThat(storedReports()).isOne();
    assertThat(duplicates() - duplicatesBefore).isEqualTo(CONCURRENT_RUNS - 1);
  }

  private void runConcurrently(Runnable run) {
    collectConcurrently(
        () -> {
          run.run();
          return Boolean.TRUE;
        });
  }

  private <T> List<T> collectConcurrently(Supplier<T> run) {
    try (ExecutorService pods = Executors.newFixedThreadPool(CONCURRENT_RUNS)) {
      List<CompletableFuture<T>> runs =
          IntStream.range(0, CONCURRENT_RUNS)
              .mapToObj(i -> CompletableFuture.supplyAsync(run, pods))
              .toList();
      return runs.stream().map(CompletableFuture::join).toList();
    }
  }

  private long storedReports() {
    return jdbc.sql("SELECT count(*) FROM customs_report").query(Long.class).single();
  }

  private String lockHolder() {
    return jdbc.sql("SELECT locked_by FROM shedlock WHERE name = 'customs-report-test'")
        .query(String.class)
        .single();
  }

  private double duplicates() {
    Counter counter =
        registry
            .find(GenerateCustomsReportMetricsAdapter.OUTCOMES)
            .tag("outcome", "already_generated")
            .counter();
    return counter == null ? 0 : counter.count();
  }
}

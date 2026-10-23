package my.packagename.infrastructure.entry.points.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * What goes wrong when several instances run the same scheduled job, and what each ShedLock setting
 * fixes. Each "instance" is a {@link CustomsReportJob} with its own lock provider over a shared
 * PostgreSQL {@code shedlock} table, exactly as separate pods would have; they differ from pods
 * only in running as threads of one JVM.
 */
@Testcontainers
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomsReportJobConcurrencyIT {

  private static final int INSTANCES = 3;
  private static final String LOCK_NAME = "customs-report-test";
  private static final Duration WAIT = Duration.ofSeconds(10);
  private static final Duration LONG_LOCK = Duration.ofMinutes(1);
  private static final Duration SHORT_LOCK = Duration.ofMillis(500);

  /** Without ShedLock every instance's lock attempt simply succeeds. */
  private static final LockProvider NO_LOCK = configuration -> Optional.of(() -> {});

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

  private static DataSource dataSource;

  private ExecutorService pods;
  private ObservedReportPort port;

  @BeforeAll
  static void createLockTable() {
    dataSource =
        new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    // Same DDL as the service changelog (20261020150000-create-shedlock).
    JdbcClient.create(dataSource)
        .sql(
            """
            CREATE TABLE shedlock (
                name VARCHAR(64) PRIMARY KEY,
                lock_until TIMESTAMP NOT NULL,
                locked_at TIMESTAMP NOT NULL,
                locked_by VARCHAR(255) NOT NULL)
            """)
        .update();
  }

  @BeforeEach
  void setUp() {
    JdbcClient.create(dataSource).sql("DELETE FROM shedlock").update();
    pods = Executors.newFixedThreadPool(INSTANCES);
  }

  @AfterEach
  void tearDown() {
    port.release();
    pods.close();
  }

  // Problem 1: every instance fires the cron, so every instance runs the job.

  @Test
  void without_a_lock_every_instance_runs_the_same_tick_at_the_same_time() {
    // Given
    port = ObservedReportPort.blocking();
    List<CustomsReportJob> instances = instances(() -> NO_LOCK, LONG_LOCK, Duration.ZERO);

    // When
    List<CompletableFuture<Void>> runs = fireAll(instances);
    await().atMost(WAIT).until(() -> port.executions() == INSTANCES);
    port.release();
    joinAll(runs);

    // Then
    assertThat(port.executions()).as("one run per instance").isEqualTo(INSTANCES);
    assertThat(port.maxOverlap()).as("all of them at once").isEqualTo(INSTANCES);
  }

  @Test
  void with_shedlock_one_instance_runs_the_tick_and_the_others_skip() {
    // Given
    port = ObservedReportPort.blocking();
    List<CustomsReportJob> instances =
        instances(CustomsReportJobConcurrencyIT::shedLock, LONG_LOCK, Duration.ZERO);

    // When
    List<CompletableFuture<Void>> runs = fireAll(instances);
    await().atMost(WAIT).until(() -> finished(runs) == INSTANCES - 1);
    port.release();
    joinAll(runs);

    // Then
    assertThat(port.executions()).isOne();
    assertThat(port.maxOverlap()).isOne();
  }

  // Problem 2: lockAtMostFor shorter than the run. The lock expires while the holder is still
  // working, and the next instance takes it: two runs overlap even with ShedLock.

  @Test
  void a_lock_that_expires_before_the_run_ends_lets_a_second_instance_overlap() {
    // Given
    port = ObservedReportPort.blocking();
    CustomsReportJob first = instance(shedLock(), SHORT_LOCK, Duration.ZERO);
    CustomsReportJob second = instance(shedLock(), SHORT_LOCK, Duration.ZERO);
    CompletableFuture<Void> slowRun = fire(first);
    await().atMost(WAIT).until(() -> port.executions() == 1);

    // When
    await().atMost(WAIT).until(this::lockExpired);
    CompletableFuture<Void> overlappingRun = fire(second);
    await().atMost(WAIT).until(() -> port.executions() == 2);
    port.release();
    joinAll(List.of(slowRun, overlappingRun));

    // Then
    assertThat(port.maxOverlap()).as("both inside the job together").isEqualTo(2);
  }

  @Test
  void a_lock_longer_than_the_run_keeps_the_second_instance_out() {
    // Given
    port = ObservedReportPort.blocking();
    CustomsReportJob first = instance(shedLock(), LONG_LOCK, Duration.ZERO);
    CustomsReportJob second = instance(shedLock(), LONG_LOCK, Duration.ZERO);
    CompletableFuture<Void> slowRun = fire(first);
    await().atMost(WAIT).until(() -> port.executions() == 1);

    // When
    second.run();
    port.release();
    slowRun.join();

    // Then
    assertThat(port.executions()).isOne();
  }

  // Problem 3: a fast run releases the lock at once. An instance whose trigger fires a moment later
  // (scheduler jitter, a slower pod, clock skew) finds it free and runs the same minute again.

  @Test
  void a_fast_run_that_releases_at_once_lets_a_late_instance_repeat_the_tick() {
    // Given
    port = ObservedReportPort.immediate();
    CustomsReportJob onTime = instance(shedLock(), LONG_LOCK, Duration.ZERO);
    CustomsReportJob late = instance(shedLock(), LONG_LOCK, Duration.ZERO);

    // When
    onTime.run();
    late.run();

    // Then
    assertThat(port.executions()).as("same tick, twice").isEqualTo(2);
  }

  @Test
  void lock_at_least_for_keeps_a_late_instance_out_after_a_fast_run() {
    // Given
    port = ObservedReportPort.immediate();
    Duration minimumHold = Duration.ofSeconds(30);
    CustomsReportJob onTime = instance(shedLock(), LONG_LOCK, minimumHold);
    CustomsReportJob late = instance(shedLock(), LONG_LOCK, minimumHold);

    // When
    onTime.run();
    late.run();

    // Then
    assertThat(port.executions()).isOne();
  }

  private List<CustomsReportJob> instances(
      Supplier<LockProvider> locks, Duration atMost, Duration atLeast) {
    return IntStream.range(0, INSTANCES)
        .mapToObj(i -> instance(locks.get(), atMost, atLeast))
        .toList();
  }

  private CustomsReportJob instance(LockProvider locks, Duration atMost, Duration atLeast) {
    return new CustomsReportJob(
        port, locks, new CustomsReportJobProperties(LOCK_NAME, atMost, atLeast));
  }

  /** One provider per instance, as each pod builds its own; they share only the table. */
  private static LockProvider shedLock() {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime()
            .build());
  }

  private List<CompletableFuture<Void>> fireAll(List<CustomsReportJob> instances) {
    return instances.stream().map(this::fire).toList();
  }

  private CompletableFuture<Void> fire(CustomsReportJob instance) {
    return CompletableFuture.runAsync(instance::run, pods);
  }

  private static long finished(List<CompletableFuture<Void>> runs) {
    return runs.stream().filter(CompletableFuture::isDone).count();
  }

  private static void joinAll(List<CompletableFuture<Void>> runs) {
    runs.forEach(CompletableFuture::join);
  }

  private boolean lockExpired() {
    return JdbcClient.create(dataSource)
        .sql("SELECT lock_until < timezone('utc', now()) FROM shedlock WHERE name = :name")
        .param("name", LOCK_NAME)
        .query(Boolean.class)
        .single();
  }
}

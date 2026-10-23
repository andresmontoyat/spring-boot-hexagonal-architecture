package my.packagename.infrastructure.driven.adapters.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import my.packagename.domain.model.CustomsReport;
import my.packagename.domain.model.ReportPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresContainerConfiguration.class, CustomsReportRepositoryAdapter.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomsReportRepositoryAdapterIT {

  private static final ReportPeriod PERIOD =
      ReportPeriod.containing(Instant.parse("2026-10-22T10:15:00Z"));
  private static final int CONCURRENT_WRITERS = 8;

  @Autowired private CustomsReportRepositoryAdapter adapter;
  @Autowired private JdbcClient jdbc;

  @BeforeEach
  void clean() {
    jdbc.sql("DELETE FROM customs_report").update();
  }

  @Test
  void should_store_the_first_report_of_a_period_and_ignore_the_rest() {
    assertThat(adapter.insertIfAbsent(new CustomsReport(PERIOD, 3))).isTrue();
    assertThat(adapter.insertIfAbsent(new CustomsReport(PERIOD, 9))).isFalse();

    assertThat(storedTotals()).containsExactly(3L);
  }

  /** Each writer commits on its own connection, as separate instances would. */
  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void should_let_exactly_one_concurrent_writer_win_a_period() throws Exception {
    // Given
    assertThat(TestTransaction.isActive()).isFalse();
    Callable<Boolean> write = () -> adapter.insertIfAbsent(new CustomsReport(PERIOD, 1));

    // When
    List<Boolean> results;
    try (ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_WRITERS)) {
      List<Future<Boolean>> futures =
          pool.invokeAll(IntStream.range(0, CONCURRENT_WRITERS).mapToObj(i -> write).toList());
      results = futures.stream().map(CustomsReportRepositoryAdapterIT::join).toList();
    }

    // Then
    assertThat(results).containsOnlyOnce(true);
    assertThat(storedTotals()).hasSize(1);
  }

  private List<Long> storedTotals() {
    return jdbc.sql("SELECT total_customs FROM customs_report").query(Long.class).list();
  }

  private static Boolean join(Future<Boolean> future) {
    try {
      return future.get();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}

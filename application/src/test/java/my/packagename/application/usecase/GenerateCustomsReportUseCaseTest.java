package my.packagename.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.fake.InMemoryCustomRepository;
import my.packagename.application.fake.InMemoryCustomsReportRepository;
import my.packagename.application.fake.RecordingTransaction;
import my.packagename.application.result.ReportOutcome;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;
import my.packagename.domain.model.CustomsReport;
import my.packagename.domain.model.ReportPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenerateCustomsReportUseCaseTest {

  private static final Instant FIRED_AT = Instant.parse("2026-10-22T10:15:07Z");
  private static final ReportPeriod PERIOD = ReportPeriod.containing(FIRED_AT);

  private InMemoryCustomRepository customs;
  private InMemoryCustomsReportRepository reports;
  private RecordingTransaction<ReportOutcome> transaction;
  private GenerateCustomsReportUseCase useCase;

  @BeforeEach
  void setUp() {
    customs = new InMemoryCustomRepository();
    reports = new InMemoryCustomsReportRepository();
    transaction = new RecordingTransaction<>();
    useCase = new GenerateCustomsReportUseCase(transaction, customs, reports);
  }

  @Test
  void should_store_a_snapshot_of_the_customs_count_for_the_fired_minute() {
    // Given
    customs.insert(Custom.create(new CustomId(UUID.randomUUID()), new CustomName("One")));
    customs.insert(Custom.create(new CustomId(UUID.randomUUID()), new CustomName("Two")));

    // When
    ReportOutcome outcome = useCase.execute(new GenerateCustomsReportCommand(FIRED_AT));

    // Then
    assertThat(outcome).isEqualTo(ReportOutcome.GENERATED);
    assertThat(reports.find(PERIOD)).contains(new CustomsReport(PERIOD, 2));
    assertThat(transaction.boundaries()).isOne();
  }

  @Test
  void should_keep_the_first_report_when_the_same_minute_runs_again() {
    // Given
    useCase.execute(new GenerateCustomsReportCommand(FIRED_AT));
    customs.insert(Custom.create(new CustomId(UUID.randomUUID()), new CustomName("Late")));

    // When
    ReportOutcome outcome =
        useCase.execute(new GenerateCustomsReportCommand(FIRED_AT.plusSeconds(30)));

    // Then
    assertThat(outcome).isEqualTo(ReportOutcome.ALREADY_GENERATED);
    assertThat(reports.find(PERIOD)).contains(new CustomsReport(PERIOD, 0));
  }

  @Test
  void should_generate_again_when_the_next_minute_fires() {
    // Given
    useCase.execute(new GenerateCustomsReportCommand(FIRED_AT));

    // When
    ReportOutcome outcome =
        useCase.execute(new GenerateCustomsReportCommand(FIRED_AT.plusSeconds(60)));

    // Then
    assertThat(outcome).isEqualTo(ReportOutcome.GENERATED);
    assertThat(reports.size()).isEqualTo(2);
  }
}

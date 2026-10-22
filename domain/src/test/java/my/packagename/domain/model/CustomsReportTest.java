package my.packagename.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomsReportTest {

  private static final ReportPeriod PERIOD =
      ReportPeriod.containing(Instant.parse("2026-10-22T10:15:00Z"));

  @Test
  void should_accept_zero_customs() {
    assertThat(new CustomsReport(PERIOD, 0).totalCustoms()).isZero();
  }

  @Test
  void should_reject_negative_total() {
    assertThatThrownBy(() -> new CustomsReport(PERIOD, -1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_reject_missing_period() {
    assertThatThrownBy(() -> new CustomsReport(null, 1)).isInstanceOf(NullPointerException.class);
  }
}

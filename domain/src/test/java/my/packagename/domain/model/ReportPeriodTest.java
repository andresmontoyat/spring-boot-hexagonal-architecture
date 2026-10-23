package my.packagename.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReportPeriodTest {

  private static final Instant MINUTE_START = Instant.parse("2026-10-22T10:15:00Z");

  @Test
  void should_start_at_the_minute_when_instant_is_inside_it() {
    assertThat(ReportPeriod.containing(Instant.parse("2026-10-22T10:15:42.123Z")).start())
        .isEqualTo(MINUTE_START);
  }

  @Test
  void should_be_equal_when_two_instants_share_the_minute() {
    assertThat(ReportPeriod.containing(MINUTE_START.plusMillis(1)))
        .isEqualTo(ReportPeriod.containing(MINUTE_START.plusSeconds(59)));
  }

  @Test
  void should_reject_start_when_not_at_the_beginning_of_a_minute() {
    assertThatThrownBy(() -> new ReportPeriod(MINUTE_START.plusSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_reject_null_start() {
    assertThatThrownBy(() -> new ReportPeriod(null)).isInstanceOf(NullPointerException.class);
  }
}

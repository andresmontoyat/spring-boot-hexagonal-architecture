package my.packagename.infrastructure.entry.points.scheduler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomsReportJobPropertiesTest {

  @Test
  void should_reject_a_minimum_hold_longer_than_the_maximum() {
    assertThatThrownBy(
            () ->
                new CustomsReportJobProperties(
                    "customs-report", Duration.ofSeconds(10), Duration.ofSeconds(11)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_reject_a_missing_lock_name() {
    assertThatThrownBy(
            () -> new CustomsReportJobProperties(null, Duration.ofMinutes(1), Duration.ZERO))
        .isInstanceOf(NullPointerException.class);
  }
}

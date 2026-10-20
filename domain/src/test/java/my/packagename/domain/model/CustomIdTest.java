package my.packagename.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomIdTest {

  private static final UUID RAW_ID = UUID.fromString("7f1c2a52-3c1e-4b5e-9b7a-1f2d3c4b5a69");

  @Test
  void should_be_equal_when_wrapping_the_same_uuid() {
    assertThat(new CustomId(RAW_ID)).isEqualTo(new CustomId(RAW_ID));
  }

  @Test
  void should_reject_id_when_value_is_null() {
    assertThatThrownBy(() -> new CustomId(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_render_raw_uuid_when_converted_to_string() {
    assertThat(new CustomId(RAW_ID)).hasToString(RAW_ID.toString());
  }
}

package my.packagename.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import my.packagename.domain.exception.InvalidCustomNameException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomNameTest {

  @Test
  void should_strip_surrounding_whitespace_when_created() {
    // When
    CustomName name = new CustomName("  Demo  ");

    // Then
    assertThat(name.value()).isEqualTo("Demo");
  }

  @Test
  void should_accept_name_when_length_is_exactly_the_maximum() {
    // Given
    String twentyChars = "a".repeat(CustomName.MAX_LENGTH);

    // When
    CustomName name = new CustomName(twentyChars);

    // Then
    assertThat(name.value()).hasSize(CustomName.MAX_LENGTH);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  void should_reject_name_when_blank(String blank) {
    assertThatThrownBy(() -> new CustomName(blank))
        .isInstanceOf(InvalidCustomNameException.class)
        .hasMessageContaining("blank");
  }

  @Test
  void should_reject_name_when_longer_than_maximum() {
    // Given
    String tooLong = "a".repeat(CustomName.MAX_LENGTH + 1);

    // When / Then
    assertThatThrownBy(() -> new CustomName(tooLong))
        .isInstanceOf(InvalidCustomNameException.class)
        .hasMessageContaining(String.valueOf(CustomName.MAX_LENGTH));
  }
}

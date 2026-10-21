package my.packagename.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomTest {

  private static final CustomId ID =
      new CustomId(UUID.fromString("7f1c2a52-3c1e-4b5e-9b7a-1f2d3c4b5a69"));
  private static final CustomName NAME = new CustomName("Demo");
  private static final CustomName OTHER_NAME = new CustomName("Renamed");

  @Test
  void should_start_at_version_zero_when_created() {
    // When
    Custom custom = Custom.create(ID, NAME);

    // Then
    assertThat(custom.id()).isEqualTo(ID);
    assertThat(custom.name()).isEqualTo(NAME);
    assertThat(custom.version()).isZero();
  }

  @Test
  void should_return_new_instance_with_new_name_when_renamed() {
    // Given
    Custom original = Custom.rehydrate(ID, NAME, 3L);

    // When
    Custom renamed = original.rename(OTHER_NAME);

    // Then
    assertThat(renamed).isNotSameAs(original);
    assertThat(renamed.name()).isEqualTo(OTHER_NAME);
    assertThat(renamed.version()).isEqualTo(3L);
    assertThat(original.name()).isEqualTo(NAME);
  }

  @Test
  void should_return_same_instance_when_renamed_to_current_name() {
    // Given
    Custom original = Custom.create(ID, NAME);

    // When
    Custom renamed = original.rename(new CustomName("Demo"));

    // Then
    assertThat(renamed).isSameAs(original);
  }

  @Test
  void should_be_equal_when_ids_match_regardless_of_state() {
    assertThat(Custom.create(ID, NAME)).isEqualTo(Custom.rehydrate(ID, OTHER_NAME, 7L));
    assertThat(Custom.create(ID, NAME)).hasSameHashCodeAs(Custom.rehydrate(ID, OTHER_NAME, 7L));
  }

  @Test
  void should_reject_rehydration_when_version_is_negative() {
    assertThatThrownBy(() -> Custom.rehydrate(ID, NAME, -1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_reject_creation_when_name_is_missing() {
    assertThatThrownBy(() -> Custom.create(ID, null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_not_expose_name_when_rendered_as_string() {
    assertThat(Custom.create(ID, NAME).toString()).contains(ID.toString()).doesNotContain("Demo");
  }
}

package my.packagename.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import my.packagename.domain.model.CustomId;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DomainExceptionTest {

  private static final CustomId ID =
      new CustomId(UUID.fromString("7f1c2a52-3c1e-4b5e-9b7a-1f2d3c4b5a69"));

  @Test
  void should_expose_missing_id_when_custom_is_not_found() {
    CustomNotFoundException exception = new CustomNotFoundException(ID);

    assertThat(exception.id()).isEqualTo(ID);
    assertThat(exception).isInstanceOf(DomainException.class).hasMessageContaining(ID.toString());
  }

  @Test
  void should_keep_cause_when_modification_is_concurrent() {
    IllegalStateException cause = new IllegalStateException("stale version");

    CustomConcurrentModificationException exception =
        new CustomConcurrentModificationException(ID, cause);

    assertThat(exception).hasCause(cause).hasMessageContaining(ID.toString());
  }
}

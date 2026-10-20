package my.packagename.application.usecase;

import static my.packagename.application.fake.CustomFixtures.FIXED_ID;
import static my.packagename.application.fake.CustomFixtures.UNKNOWN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import my.packagename.application.fake.InMemoryCustomRepository;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DeleteCustomUseCaseTest {

  private InMemoryCustomRepository repository;
  private DeleteCustomUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryCustomRepository();
    useCase = new DeleteCustomUseCase(repository);
  }

  @Test
  void should_remove_custom_when_it_exists() {
    // Given
    repository.insert(Custom.create(FIXED_ID, new CustomName("Demo")));

    // When
    useCase.execute(FIXED_ID);

    // Then
    assertThat(repository.findById(FIXED_ID)).isEmpty();
  }

  @Test
  void should_fail_with_not_found_when_custom_does_not_exist() {
    assertThatThrownBy(() -> useCase.execute(UNKNOWN_ID))
        .isInstanceOf(CustomNotFoundException.class);
  }
}

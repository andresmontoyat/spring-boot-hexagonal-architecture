package my.packagename.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import my.packagename.application.fake.InMemoryCustomRepository;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ListCustomsUseCaseTest {

  private InMemoryCustomRepository repository;
  private ListCustomsUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryCustomRepository();
    useCase = new ListCustomsUseCase(repository);
    store("Charlie", 1);
    store("Alpha", 2);
    store("Bravo", 3);
  }

  @Test
  void should_return_requested_page_when_listing() {
    // When
    PageResult<Custom> page = useCase.execute(new PageQuery(1, 2));

    // Then
    assertThat(page.items()).extracting(custom -> custom.name().value()).containsExactly("Charlie");
    assertThat(page.totalElements()).isEqualTo(3);
    assertThat(page.totalPages()).isEqualTo(2);
  }

  private void store(String name, int seed) {
    CustomId id = new CustomId(new UUID(0L, seed));
    repository.insert(Custom.create(id, new CustomName(name)));
  }
}

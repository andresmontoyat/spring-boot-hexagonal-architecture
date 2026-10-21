package my.packagename.application.usecase;

import static my.packagename.application.fake.CustomFixtures.FIXED_ID;
import static my.packagename.application.fake.CustomFixtures.UNKNOWN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import my.packagename.application.event.CustomDeleted;
import my.packagename.application.event.CustomEventOutbox;
import my.packagename.application.fake.InMemoryCustomRepository;
import my.packagename.application.fake.OutboxFixtures;
import my.packagename.application.fake.RecordingOutboxWriter;
import my.packagename.application.fake.RecordingTransaction;
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
  private RecordingTransaction<Boolean> transaction;
  private RecordingOutboxWriter outbox;
  private DeleteCustomUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryCustomRepository();
    transaction = new RecordingTransaction<>();
    outbox = new RecordingOutboxWriter(transaction::isOpen);
    CustomEventOutbox events = new CustomEventOutbox(outbox, OutboxFixtures.eventFactory());
    useCase = new DeleteCustomUseCase(transaction, repository, events);
  }

  @Test
  void should_remove_custom_and_enqueue_deleted_event_in_one_transaction() {
    // Given
    repository.insert(Custom.create(FIXED_ID, new CustomName("Demo")));

    // When
    useCase.execute(FIXED_ID);

    // Then
    assertThat(repository.findById(FIXED_ID)).isEmpty();
    assertThat(transaction.boundaries()).isOne();
    assertThat(outbox.saved())
        .singleElement()
        .satisfies(
            saved -> {
              assertThat(saved.insideTransaction()).isTrue();
              assertThat(saved.event().body().payload())
                  .isEqualTo(new CustomDeleted(FIXED_ID.value()));
            });
  }

  @Test
  void should_fail_without_publishing_when_custom_does_not_exist() {
    assertThatThrownBy(() -> useCase.execute(UNKNOWN_ID))
        .isInstanceOf(CustomNotFoundException.class);
    assertThat(outbox.saved()).isEmpty();
  }
}

package my.packagename.application.usecase;

import static my.packagename.application.fake.CustomFixtures.FIXED_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import my.packagename.application.command.CreateCustomCommand;
import my.packagename.application.event.CustomCreated;
import my.packagename.application.event.CustomEventOutbox;
import my.packagename.application.fake.InMemoryCustomRepository;
import my.packagename.application.fake.OutboxFixtures;
import my.packagename.application.fake.RecordingOutboxWriter;
import my.packagename.application.fake.RecordingTransaction;
import my.packagename.domain.exception.InvalidCustomNameException;
import my.packagename.domain.model.Custom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CreateCustomUseCaseTest {

  private InMemoryCustomRepository repository;
  private RecordingTransaction<Custom> transaction;
  private RecordingOutboxWriter outbox;
  private CreateCustomUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryCustomRepository();
    transaction = new RecordingTransaction<>();
    outbox = new RecordingOutboxWriter(transaction::isOpen);
    CustomEventOutbox events = new CustomEventOutbox(outbox, OutboxFixtures.eventFactory());
    useCase = new CreateCustomUseCase(transaction, repository, () -> FIXED_ID, events);
  }

  @Test
  void should_persist_custom_with_generated_id_when_name_is_valid() {
    // When
    Custom created = useCase.execute(new CreateCustomCommand("Demo"));

    // Then
    assertThat(created.id()).isEqualTo(FIXED_ID);
    assertThat(created.name().value()).isEqualTo("Demo");
    assertThat(repository.findById(FIXED_ID)).contains(created);
  }

  @Test
  void should_enqueue_created_event_inside_the_same_transaction() {
    // When
    useCase.execute(new CreateCustomCommand("Demo"));

    // Then
    assertThat(transaction.boundaries()).isOne();
    assertThat(outbox.saved())
        .singleElement()
        .satisfies(
            saved -> {
              assertThat(saved.insideTransaction()).isTrue();
              assertThat(saved.message().topic()).isEqualTo(CustomEventOutbox.TOPIC);
              assertThat(saved.message().key()).isEqualTo(FIXED_ID.toString());
              assertThat(saved.event().body().eventName()).isEqualTo("CustomCreated");
              assertThat(saved.event().body().payload())
                  .isEqualTo(new CustomCreated(FIXED_ID.value(), "Demo"));
            });
  }

  @Test
  void should_neither_persist_nor_publish_when_name_is_invalid() {
    // When / Then
    assertThatThrownBy(() -> useCase.execute(new CreateCustomCommand(" ")))
        .isInstanceOf(InvalidCustomNameException.class);
    assertThat(repository.findById(FIXED_ID)).isEmpty();
    assertThat(outbox.saved()).isEmpty();
  }
}

package my.packagename.application.usecase;

import static my.packagename.application.fake.CustomFixtures.FIXED_ID;
import static my.packagename.application.fake.CustomFixtures.UNKNOWN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import my.packagename.application.command.RenameCustomCommand;
import my.packagename.application.event.CustomEventOutbox;
import my.packagename.application.event.CustomRenamed;
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
class RenameCustomUseCaseTest {

  private InMemoryCustomRepository repository;
  private RecordingTransaction<Custom> transaction;
  private RecordingOutboxWriter outbox;
  private RenameCustomUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryCustomRepository();
    transaction = new RecordingTransaction<>();
    outbox = new RecordingOutboxWriter(transaction::isOpen);
    CustomEventOutbox events = new CustomEventOutbox(outbox, OutboxFixtures.eventFactory());
    useCase = new RenameCustomUseCase(transaction, repository, events);
  }

  @Test
  void should_persist_new_name_and_bump_version_when_custom_exists() {
    // Given
    Custom stored = repository.insert(Custom.create(FIXED_ID, new CustomName("Demo")));

    // When
    Custom renamed = useCase.execute(new RenameCustomCommand(FIXED_ID, "Renamed"));

    // Then
    assertThat(renamed.name().value()).isEqualTo("Renamed");
    assertThat(renamed.version()).isEqualTo(stored.version() + 1);
    assertThat(transaction.boundaries()).isOne();
  }

  @Test
  void should_enqueue_renamed_event_with_new_version_inside_the_transaction() {
    // Given
    repository.insert(Custom.create(FIXED_ID, new CustomName("Demo")));

    // When
    useCase.execute(new RenameCustomCommand(FIXED_ID, "Renamed"));

    // Then
    assertThat(outbox.saved())
        .singleElement()
        .satisfies(
            saved -> {
              assertThat(saved.insideTransaction()).isTrue();
              assertThat(saved.message().key()).isEqualTo(FIXED_ID.toString());
              assertThat(saved.event().body().payload())
                  .isEqualTo(new CustomRenamed(FIXED_ID.value(), "Renamed", 1L));
            });
  }

  @Test
  void should_fail_without_publishing_when_custom_does_not_exist() {
    assertThatThrownBy(() -> useCase.execute(new RenameCustomCommand(UNKNOWN_ID, "Renamed")))
        .isInstanceOf(CustomNotFoundException.class);
    assertThat(outbox.saved()).isEmpty();
  }
}

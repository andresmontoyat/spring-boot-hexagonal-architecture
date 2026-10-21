package my.packagename.application.fake;

import io.codehunters.messaging.EventMessage;
import io.codehunters.messaging.OutboxMessage;
import io.codehunters.messaging.OutboxWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Keeps every saved outbox row and whether a transaction was open at that moment, so tests prove
 * the event is written atomically with the business change.
 */
public final class RecordingOutboxWriter implements OutboxWriter {

  public record Saved(OutboxMessage<?> message, boolean insideTransaction) {

    public EventMessage<?> event() {
      return (EventMessage<?>) message.message();
    }
  }

  private final BooleanSupplier transactionOpen;
  private final List<Saved> saved = new ArrayList<>();

  public RecordingOutboxWriter(BooleanSupplier transactionOpen) {
    this.transactionOpen = transactionOpen;
  }

  @Override
  public <T> void save(OutboxMessage<T> outboxMessage) {
    saved.add(new Saved(outboxMessage, transactionOpen.getAsBoolean()));
  }

  public List<Saved> saved() {
    return List.copyOf(saved);
  }
}

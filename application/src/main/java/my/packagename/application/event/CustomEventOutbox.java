package my.packagename.application.event;

import io.codehunters.messaging.OutboxEvent;
import io.codehunters.messaging.OutboxWriter;
import io.codehunters.messaging.factory.OutboxEventFactory;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;

/**
 * Writes Custom integration events to the outbox. Must be called inside the use case transaction:
 * the row commits or rolls back with the business change, and the SDK publishes it after commit.
 * Keyed by aggregate id, so events of one custom keep their order.
 */
public final class CustomEventOutbox {

  public static final String TOPIC = "customs.custom.events";

  private final OutboxWriter writer;
  private final OutboxEventFactory factory;

  public CustomEventOutbox(OutboxWriter writer, OutboxEventFactory factory) {
    this.writer = writer;
    this.factory = factory;
  }

  public void created(Custom custom) {
    write(
        custom.id(),
        "CustomCreated",
        new CustomCreated(custom.id().value(), custom.name().value()));
  }

  public void renamed(Custom custom) {
    write(
        custom.id(),
        "CustomRenamed",
        new CustomRenamed(custom.id().value(), custom.name().value(), custom.version()));
  }

  public void deleted(CustomId id) {
    write(id, "CustomDeleted", new CustomDeleted(id.value()));
  }

  private <T> void write(CustomId id, String eventName, T payload) {
    writer.save(
        factory.create(OutboxEvent.to(TOPIC).key(id.toString()).type(eventName).payload(payload)));
  }
}

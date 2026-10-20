package my.packagename.application.usecase;

import my.packagename.application.event.CustomEventOutbox;
import my.packagename.application.port.inbound.DeleteCustomPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.port.outbound.TransactionPort;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.CustomId;

public final class DeleteCustomUseCase implements DeleteCustomPort {

  private final TransactionPort<Boolean> transaction;
  private final CustomRepositoryPort repository;
  private final CustomEventOutbox events;

  public DeleteCustomUseCase(
      TransactionPort<Boolean> transaction,
      CustomRepositoryPort repository,
      CustomEventOutbox events) {
    this.transaction = transaction;
    this.repository = repository;
    this.events = events;
  }

  @Override
  public void execute(CustomId id) {
    boolean deleted = transaction.run(() -> deleteAndRecord(id));
    if (!deleted) {
      throw new CustomNotFoundException(id);
    }
  }

  private boolean deleteAndRecord(CustomId id) {
    boolean deleted = repository.deleteById(id);
    if (deleted) {
      events.deleted(id);
    }
    return deleted;
  }
}

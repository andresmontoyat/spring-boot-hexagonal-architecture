package my.packagename.application.usecase;

import my.packagename.application.command.RenameCustomCommand;
import my.packagename.application.event.CustomEventOutbox;
import my.packagename.application.port.inbound.RenameCustomPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.port.outbound.TransactionPort;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomName;

/**
 * The optimistic version check on update makes this the single writer of the aggregate key, which
 * is what keeps its outbox events in order.
 */
public final class RenameCustomUseCase implements RenameCustomPort {

  private final TransactionPort<Custom> transaction;
  private final CustomRepositoryPort repository;
  private final CustomEventOutbox events;

  public RenameCustomUseCase(
      TransactionPort<Custom> transaction,
      CustomRepositoryPort repository,
      CustomEventOutbox events) {
    this.transaction = transaction;
    this.repository = repository;
    this.events = events;
  }

  @Override
  public Custom execute(RenameCustomCommand command) {
    CustomName newName = new CustomName(command.name());
    return transaction.run(
        () -> {
          Custom renamed = repository.update(load(command).rename(newName));
          events.renamed(renamed);
          return renamed;
        });
  }

  private Custom load(RenameCustomCommand command) {
    return repository
        .findById(command.id())
        .orElseThrow(() -> new CustomNotFoundException(command.id()));
  }
}

package my.packagename.application.usecase;

import my.packagename.application.command.RenameCustomCommand;
import my.packagename.application.port.inbound.RenameCustomPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.port.outbound.TransactionPort;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomName;

public final class RenameCustomUseCase implements RenameCustomPort {

  private final TransactionPort<Custom> transaction;
  private final CustomRepositoryPort repository;

  public RenameCustomUseCase(TransactionPort<Custom> transaction, CustomRepositoryPort repository) {
    this.transaction = transaction;
    this.repository = repository;
  }

  @Override
  public Custom execute(RenameCustomCommand command) {
    CustomName newName = new CustomName(command.name());
    return transaction.run(() -> repository.update(load(command).rename(newName)));
  }

  private Custom load(RenameCustomCommand command) {
    return repository
        .findById(command.id())
        .orElseThrow(() -> new CustomNotFoundException(command.id()));
  }
}

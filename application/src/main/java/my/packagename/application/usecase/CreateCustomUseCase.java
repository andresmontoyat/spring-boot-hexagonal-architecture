package my.packagename.application.usecase;

import my.packagename.application.command.CreateCustomCommand;
import my.packagename.application.mapper.CommandMapper;
import my.packagename.application.port.inbound.CreateCustomPort;
import my.packagename.application.port.outbound.CustomIdGeneratorPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.port.outbound.TransactionPort;
import my.packagename.domain.model.Custom;

public final class CreateCustomUseCase implements CreateCustomPort {

  private final TransactionPort<Custom> transaction;
  private final CustomRepositoryPort repository;
  private final CustomIdGeneratorPort idGenerator;

  public CreateCustomUseCase(
      TransactionPort<Custom> transaction,
      CustomRepositoryPort repository,
      CustomIdGeneratorPort idGenerator) {
    this.transaction = transaction;
    this.repository = repository;
    this.idGenerator = idGenerator;
  }

  @Override
  public Custom execute(CreateCustomCommand command) {
    Custom custom = CommandMapper.toCustom(command, idGenerator.next());
    return transaction.run(() -> repository.insert(custom));
  }
}

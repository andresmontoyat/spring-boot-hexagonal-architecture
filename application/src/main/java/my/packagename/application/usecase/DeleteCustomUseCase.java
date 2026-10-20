package my.packagename.application.usecase;

import my.packagename.application.port.inbound.DeleteCustomPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.CustomId;

public final class DeleteCustomUseCase implements DeleteCustomPort {

  private final CustomRepositoryPort repository;

  public DeleteCustomUseCase(CustomRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public void execute(CustomId id) {
    if (!repository.deleteById(id)) {
      throw new CustomNotFoundException(id);
    }
  }
}

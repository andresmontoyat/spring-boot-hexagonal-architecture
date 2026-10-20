package my.packagename.application.usecase;

import my.packagename.application.port.inbound.FindCustomPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;

public final class FindCustomUseCase implements FindCustomPort {

  private final CustomRepositoryPort repository;

  public FindCustomUseCase(CustomRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public Custom execute(CustomId id) {
    return repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
  }
}

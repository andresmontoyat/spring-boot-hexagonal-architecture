package my.packagename.application.usecase;

import my.packagename.application.port.inbound.ListCustomsPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;

public final class ListCustomsUseCase implements ListCustomsPort {

  private final CustomRepositoryPort repository;

  public ListCustomsUseCase(CustomRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public PageResult<Custom> execute(PageQuery query) {
    return repository.findAll(query);
  }
}

package my.packagename.infrastructure.driven.adapters.jdbc;

import java.util.Optional;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.exception.CustomConcurrentModificationException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.infrastructure.driven.adapters.jdbc.entity.CustomEntity;
import my.packagename.infrastructure.driven.adapters.jdbc.mapper.CustomEntityMapper;
import my.packagename.infrastructure.driven.adapters.jdbc.repository.CustomJdbcRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Component;

@Component
public class CustomRepositoryAdapter implements CustomRepositoryPort {

  private static final Sort DEFAULT_SORT = Sort.by("name", "id");

  private final CustomJdbcRepository repository;
  private final JdbcAggregateOperations aggregates;

  public CustomRepositoryAdapter(
      CustomJdbcRepository repository, JdbcAggregateOperations aggregates) {
    this.repository = repository;
    this.aggregates = aggregates;
  }

  @Override
  public Custom insert(Custom custom) {
    return CustomEntityMapper.toDomain(aggregates.insert(CustomEntityMapper.toNewEntity(custom)));
  }

  @Override
  public Custom update(Custom custom) {
    try {
      return CustomEntityMapper.toDomain(aggregates.update(CustomEntityMapper.toEntity(custom)));
    } catch (OptimisticLockingFailureException ex) {
      throw new CustomConcurrentModificationException(custom.id(), ex);
    }
  }

  @Override
  public Optional<Custom> findById(CustomId id) {
    return repository.findById(id.value()).map(CustomEntityMapper::toDomain);
  }

  @Override
  public PageResult<Custom> findAll(PageQuery query) {
    Page<CustomEntity> page =
        repository.findAll(PageRequest.of(query.page(), query.size(), DEFAULT_SORT));
    return new PageResult<>(
        page.map(CustomEntityMapper::toDomain).getContent(),
        query.page(),
        query.size(),
        page.getTotalElements());
  }

  @Override
  public boolean deleteById(CustomId id) {
    return repository.deleteReturningCount(id.value()) > 0;
  }
}

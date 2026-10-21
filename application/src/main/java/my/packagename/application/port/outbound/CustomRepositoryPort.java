package my.packagename.application.port.outbound;

import java.util.Optional;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;

public interface CustomRepositoryPort {

  Custom insert(Custom custom);

  /**
   * Persists a changed aggregate.
   *
   * @throws my.packagename.domain.exception.CustomConcurrentModificationException when {@code
   *     custom.version()} no longer matches the stored version
   */
  Custom update(Custom custom);

  Optional<Custom> findById(CustomId id);

  PageResult<Custom> findAll(PageQuery query);

  /** Returns {@code true} when a row was removed. */
  boolean deleteById(CustomId id);
}

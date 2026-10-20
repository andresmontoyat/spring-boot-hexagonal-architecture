package my.packagename.infrastructure.driven.adapters.jdbc.repository;

import java.util.UUID;
import my.packagename.infrastructure.driven.adapters.jdbc.entity.CustomEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface CustomJdbcRepository
    extends ListCrudRepository<CustomEntity, UUID>,
        ListPagingAndSortingRepository<CustomEntity, UUID> {

  /** Single statement, so callers learn whether the row existed without a racy exists-check. */
  @Modifying
  @Query("DELETE FROM custom WHERE id = :id")
  int deleteReturningCount(@Param("id") UUID id);
}

package my.packagename.infrastracture.repository;

import my.packagename.infrastracture.repository.entities.CustomEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomJpaRepository extends CrudRepository<CustomEntity, Long> {
}

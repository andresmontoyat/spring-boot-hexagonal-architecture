package my.packagename.infrastracture.adapter.mapper.support;

import lombok.extern.slf4j.Slf4j;
import my.packagename.commons.util.ex.CustomException;
import my.packagename.domain.model.support.Model;
import my.packagename.infrastracture.repository.entities.support.Entities;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
public class JpaMapper<D extends Model, E extends Entities> {

    @PersistenceContext
    protected EntityManager em;

    @ObjectFactory
    public E reference(D dto, @TargetType Class<E> entityClass) {
        try {
            return (dto.getId() != null) ? em.getReference(entityClass, dto.getId()) : entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CustomException("Error message", e);
        }
    }
}

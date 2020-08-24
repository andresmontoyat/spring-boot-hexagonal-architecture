package my.packagename.infrastracture.repository;

import lombok.extern.slf4j.Slf4j;
import my.packagename.domain.CustomEntity;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CustomRepositoryImpl implements CustomRepository {

    @Override
    public CustomEntity defaultRepositoryMethod() {
        log.info("this is a repository method");
        CustomEntity customEntity = new CustomEntity();
        customEntity.setName("Name");
        customEntity.setOperation("Operation");
        return customEntity;
    }
}

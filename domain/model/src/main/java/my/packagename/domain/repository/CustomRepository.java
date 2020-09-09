package my.packagename.domain.repository;

import my.packagename.domain.model.Custom;

import java.util.List;

public interface CustomRepository {

    List<Custom> findAll();

    Custom findById(Long id);

    Custom save(Custom custom);

    Custom update(Custom custom);

    void delete(Long id);

    boolean exists(Long id);

}

package my.packagename.domain.service;

import my.packagename.domain.model.Custom;

import java.util.List;

public interface CustomService {

    List<Custom> findAll();

    Custom findById(Long id);

    boolean exist(Long id);

    void delete(Long id);

    Custom defaultServiceMethod();

    Custom save(Custom custom);

    Custom update(Custom custom);
}

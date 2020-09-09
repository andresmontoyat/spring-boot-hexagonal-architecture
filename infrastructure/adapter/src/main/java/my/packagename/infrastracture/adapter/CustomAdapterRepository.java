package my.packagename.infrastracture.adapter;

import my.packagename.domain.model.Custom;
import my.packagename.domain.repository.CustomRepository;
import my.packagename.infrastracture.adapter.mapper.CustomAdapterMapper;
import my.packagename.infrastracture.repository.CustomJpaRepository;
import my.packagename.infrastracture.repository.entities.CustomEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomAdapterRepository implements CustomRepository {

    private CustomJpaRepository customJpaRepository;

    private CustomAdapterMapper mapper;

    public CustomAdapterRepository(CustomJpaRepository customJpaRepository, CustomAdapterMapper mapper) {
        this.customJpaRepository = customJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Custom> findAll() {
        return mapper.toCustoms((List<CustomEntity>) customJpaRepository.findAll());
    }

    @Override
    public Custom findById(Long id) {
        return mapper.toCustom(customJpaRepository.findById(id).get());
    }

    @Override
    public Custom save(Custom custom) {
        CustomEntity customEntity = mapper.toCustomEntity(custom);
        return mapper.toCustom(customJpaRepository.save(customEntity));
    }

    @Override
    public Custom update(Custom custom) {
        CustomEntity customEntity = mapper.toCustomEntity(custom);
        return mapper.toCustom(customJpaRepository.save(customEntity));
    }

    @Override
    public void delete(Long id) {
        customJpaRepository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        return customJpaRepository.existsById(id);
    }
}

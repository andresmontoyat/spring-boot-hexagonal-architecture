package my.packagename.domain.service.impl;

import lombok.extern.slf4j.Slf4j;
import my.packagename.domain.model.Custom;
import my.packagename.domain.repository.CustomRepository;
import my.packagename.domain.service.CustomService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CustomServiceImpl implements CustomService {

    private CustomRepository customRepository;

    public CustomServiceImpl(CustomRepository customRepository) {
        this.customRepository = customRepository;
    }

    @Override
    public List<Custom> findAll() {
        return customRepository.findAll();
    }

    @Override
    public Custom findById(Long id) {
        return customRepository.findById(id);
    }

    @Override
    public boolean exist(Long id) {
        return customRepository.exists(id);
    }

    @Override
    public void delete(Long id) {
        customRepository.delete(id);
    }

    @Override
    public Custom defaultServiceMethod() {
        log.info("this is a message from service layer");
        return null;
    }

    @Override
    public Custom save(Custom custom) {
        return customRepository.save(custom);
    }

    @Override
    public Custom update(Custom custom) {
        return customRepository.update(custom);
    }
}

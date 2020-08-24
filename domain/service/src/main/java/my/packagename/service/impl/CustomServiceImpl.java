package my.packagename.service.impl;

import lombok.extern.slf4j.Slf4j;
import my.packagename.dto.CustomDTO;
import my.packagename.dto.mapper.CustomDTOMapper;
import my.packagename.infrastracture.repository.CustomRepository;
import my.packagename.service.CustomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomServiceImpl implements CustomService {

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private CustomDTOMapper mapper;

    @Override
    public CustomDTO defaultServiceMethod() {
        log.info("this is a message from service layer");
        return mapper.toCustomModelDTO(customRepository.defaultRepositoryMethod());
    }
}

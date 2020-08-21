package my.packagename.rest.impl;

import lombok.extern.slf4j.Slf4j;
import my.packagename.rest.CustomRestController;
import my.packagename.rest.model.CustomModelMapper;
import my.packagename.rest.model.response.CustomResponse;
import my.packagename.service.CustomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/operations")
public class CustomRestControllerImpl implements CustomRestController {

    @Autowired
    private CustomService customService;

    @Autowired
    private CustomModelMapper mapper;

    @GetMapping
    @Override
    public ResponseEntity<CustomResponse> getOperationSupport() {
        log.info("This is a rest controller");
        return ResponseEntity.ok(mapper.toCustomResponse(customService.defaultServiceMethod()));
    }
}

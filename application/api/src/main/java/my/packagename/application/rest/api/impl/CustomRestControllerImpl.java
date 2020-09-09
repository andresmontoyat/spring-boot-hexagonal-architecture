package my.packagename.application.rest.api.impl;

import lombok.extern.slf4j.Slf4j;
import my.packagename.application.rest.api.CustomRestController;
import my.packagename.application.rest.api.model.CustomModelMapper;
import my.packagename.application.rest.api.model.request.CustomCreateRequest;
import my.packagename.application.rest.api.model.request.CustomUpdateRequest;
import my.packagename.application.rest.api.model.response.CustomResponse;
import my.packagename.application.rest.handler.ApiMessage;
import my.packagename.application.rest.handler.ex.RestException;
import my.packagename.domain.service.CustomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiMessage> findAll() {
        ApiMessage message = ApiMessage.builder()
                .code("00x1")
                .message("Message")
                .data(mapper.toCustomResponses(customService.findAll()))
                .build();
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiMessage> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiMessage.builder()
                .code("00x1")
                .message("Message")
                .data(mapper.toCustomResponse(customService.findById(id)))
                .build());
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiMessage> save(CustomCreateRequest customCreateRequest, BindingResult result) {
        if (result.hasErrors()) {
            CustomResponse customResponse = mapper.toCustomResponse(customService.save(mapper.toCustom(customCreateRequest)));
            return ResponseEntity.ok(ApiMessage.builder().status(HttpStatus.CREATED).data(customResponse).build());
        }

        throw new RestException("Error bean validation", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiMessage> update(@PathVariable Long id, CustomUpdateRequest customUpdateRequest, BindingResult result) {
        if(customService.exist(id)) {
            if (result.hasErrors()) {
                CustomResponse customResponse = mapper.toCustomResponse(customService.update(mapper.toCustom(id, customUpdateRequest)));
                return ResponseEntity.ok(ApiMessage.builder().data(customResponse).build());
            }
            throw new RestException("Error bean validation", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        throw new RestException("Error not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity delete(@PathVariable Long id) {
        if(customService.exist(id)) {
            customService.delete(id);
            return ResponseEntity.ok().build();
        }
        throw new RestException("Error not found", HttpStatus.NOT_FOUND);
    }

}

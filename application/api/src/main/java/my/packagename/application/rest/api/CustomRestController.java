package my.packagename.application.rest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import my.packagename.application.rest.api.model.request.CustomCreateRequest;
import my.packagename.application.rest.api.model.request.CustomUpdateRequest;
import my.packagename.application.rest.handler.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

@Api(tags = "Custom API")
public interface CustomRestController {

    @ApiOperation(value = "Find All")
    ResponseEntity<ApiMessage> findAll();

    @ApiOperation(value = "Find by @Id")
    ResponseEntity<ApiMessage> findById(Long id);

    @ApiOperation(value = "Create a new custom record")
    ResponseEntity<ApiMessage> save(CustomCreateRequest customCreateRequest, BindingResult result);

    @ApiOperation(value = "Update a custom record")
    ResponseEntity<ApiMessage> update(Long id, CustomUpdateRequest customUpdateRequest, BindingResult result);

    @ApiOperation(value = "Delete a custom record")
    ResponseEntity delete(Long id);


}

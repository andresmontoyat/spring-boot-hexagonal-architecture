package my.packagename.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import my.packagename.rest.model.response.CustomResponse;
import org.springframework.http.ResponseEntity;

@Api(tags = "Custom API")
public interface CustomRestController {

    @ApiOperation(value = "GET Operation summary")
    ResponseEntity<CustomResponse> getOperationSupport();
}

package my.packagename.application.rest.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import my.packagename.commons.util.ex.Message;
import org.springframework.http.HttpStatus;

@Getter
public class ApiError<T> extends Message {

    @ApiModelProperty(name = "status")
    @JsonProperty("status")
    private HttpStatus status;

    @ApiModelProperty(name = "errors")
    @JsonProperty("errors")
    private T errors;

    @Builder
    public ApiError(String code, String message, HttpStatus status, T errors) {
        super(code, message);
        this.status = status;
        this.errors = errors;
    }
}

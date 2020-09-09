package my.packagename.application.rest.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import my.packagename.commons.util.ex.Message;
import org.springframework.http.HttpStatus;

@Getter
public class ApiMessage<T> extends Message {

    @ApiModelProperty(name = "status")
    @JsonProperty("status")
    private HttpStatus status;

    @ApiModelProperty(name = "data")
    @JsonProperty("data")
    private T data;

    @Builder
    public ApiMessage(String code, String message, HttpStatus status, T data) {
        super(code, message);
        this.status = status;
        this.data = data;
    }

}

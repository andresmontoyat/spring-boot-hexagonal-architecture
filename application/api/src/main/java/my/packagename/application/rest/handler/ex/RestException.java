package my.packagename.application.rest.handler.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestException extends RuntimeException{

    private HttpStatus status;

    public RestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public RestException(String message, Throwable cause,  HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}

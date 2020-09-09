package my.packagename.application.rest.handler;

import lombok.extern.slf4j.Slf4j;
import my.packagename.application.rest.handler.ex.RestException;
import my.packagename.commons.util.ex.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiError> resolveCustomException(HttpServletRequest request, HttpServletResponse response, CustomException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError
                .builder()
                .code("code")
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ApiError> resolveGeneralException(HttpServletRequest request, HttpServletResponse response, RestException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(e.getStatus()).body(ApiError
                .builder()
                .code("code")
                .message(e.getMessage())
                .status(e.getStatus())
                .build());
    }
}

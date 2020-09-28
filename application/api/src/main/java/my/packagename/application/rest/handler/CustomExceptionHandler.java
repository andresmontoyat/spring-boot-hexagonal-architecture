package my.packagename.application.rest.handler;

import lombok.extern.slf4j.Slf4j;
import my.packagename.application.rest.handler.ex.RestException;
import my.packagename.commons.util.ex.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiError> resolveCustomException(HttpServletRequest request, HttpServletResponse response, CustomException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError
                .builder()
                .code("code")
                .message(messageSource.getMessage("internal.error", null, LocaleContextHolder.getLocale()))
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ApiError> resolveRestException(HttpServletRequest request, HttpServletResponse response, RestException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(e.getStatus()).body(ApiError
                .builder()
                .code("code")
                .message(messageSource.getMessage("ret.error", null, LocaleContextHolder.getLocale()))
                .status(e.getStatus())
                .build());
    }
}

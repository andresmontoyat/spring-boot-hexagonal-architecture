package my.packagename.infrastructure.entry.points.rest;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import my.packagename.domain.exception.CustomConcurrentModificationException;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.exception.InvalidCustomNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * RFC 9457 problem details. {@code code} is the stable machine-readable contract; {@code title} and
 * {@code detail} may change.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  static final String PROBLEM_BASE_URI = "https://api.my.packagename/problems/";

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(CustomNotFoundException.class)
  ProblemDetail handleNotFound(CustomNotFoundException ex, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, "CUSTOM_NOT_FOUND", ex.getMessage(), request);
  }

  @ExceptionHandler(InvalidCustomNameException.class)
  ProblemDetail handleInvalidName(InvalidCustomNameException ex, HttpServletRequest request) {
    return problem(
        HttpStatus.UNPROCESSABLE_CONTENT, "CUSTOM_INVALID_NAME", ex.getMessage(), request);
  }

  @ExceptionHandler(CustomConcurrentModificationException.class)
  ProblemDetail handleConflict(
      CustomConcurrentModificationException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, "CUSTOM_CONCURRENT_MODIFICATION", ex.getMessage(), request);
  }

  /**
   * {@code @PreAuthorize} denials surface here before the security filter sees them; rethrow so
   * Spring Security renders the single 401/403 problem shape instead of the 500 fallback below.
   */
  @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
  void rethrowSecurityFailure(RuntimeException ex) {
    throw ex;
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled failure on {} {}", request.getMethod(), request.getRequestURI(), ex);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<Map<String, String>> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> fieldError(error.getField(), error.getDefaultMessage()))
            .toList();
    return validationFailed(ex.getBody(), errors);
  }

  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<Map<String, String>> errors =
        ex.getParameterValidationResults().stream()
            .flatMap(
                result ->
                    result.getResolvableErrors().stream()
                        .map(
                            error ->
                                fieldError(
                                    result.getMethodParameter().getParameterName(),
                                    error.getDefaultMessage())))
            .toList();
    return validationFailed(ex.getBody(), errors);
  }

  private static ResponseEntity<Object> validationFailed(
      ProblemDetail body, List<Map<String, String>> errors) {
    body.setType(URI.create(PROBLEM_BASE_URI + "validation-failed"));
    body.setTitle("Validation failed");
    body.setProperty("code", "VALIDATION_FAILED");
    body.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(body);
  }

  private static Map<String, String> fieldError(String field, String message) {
    return Map.of("field", String.valueOf(field), "message", String.valueOf(message));
  }

  private static ProblemDetail problem(
      HttpStatus status, String code, String detail, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(URI.create(PROBLEM_BASE_URI + code.toLowerCase().replace('_', '-')));
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", code);
    return problem;
  }
}

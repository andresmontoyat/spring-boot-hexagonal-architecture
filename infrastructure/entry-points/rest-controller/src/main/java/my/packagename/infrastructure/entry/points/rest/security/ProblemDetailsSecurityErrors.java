package my.packagename.infrastructure.entry.points.rest.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

/**
 * 401 and 403 rendered as RFC 9457 problem details, like every other API error. The bearer
 * delegates still set the RFC 6750 {@code WWW-Authenticate} header; no exception text leaks.
 */
public final class ProblemDetailsSecurityErrors
    implements AuthenticationEntryPoint, AccessDeniedHandler {

  public static final String PROBLEM_BASE_URI = "https://api.my.packagename/problems/";

  private final AuthenticationEntryPoint bearerEntryPoint =
      new BearerTokenAuthenticationEntryPoint();
  private final AccessDeniedHandler bearerAccessDenied = new BearerTokenAccessDeniedHandler();
  private final JsonMapper json;

  public ProblemDetailsSecurityErrors(JsonMapper json) {
    this.json = json;
  }

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException, ServletException {
    bearerEntryPoint.commence(request, response, ex);
    write(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
  }

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException, ServletException {
    bearerAccessDenied.handle(request, response, ex);
    write(request, response, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
  }

  private void write(
      HttpServletRequest request, HttpServletResponse response, HttpStatus status, String code)
      throws IOException {
    Map<String, Object> problem = new LinkedHashMap<>();
    problem.put("type", PROBLEM_BASE_URI + code.toLowerCase().replace('_', '-'));
    problem.put("title", status.getReasonPhrase());
    problem.put("status", status.value());
    problem.put("instance", request.getRequestURI());
    problem.put("code", code);
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    json.writeValue(response.getOutputStream(), problem);
  }
}

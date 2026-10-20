package my.packagename.infrastructure.entry.points.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void should_hide_internal_message_when_failure_is_unexpected() {
    // Given
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customs");

    // When
    ProblemDetail problem =
        handler.handleUnexpected(new IllegalStateException("db password=secret"), request);

    // Then
    assertThat(problem.getStatus()).isEqualTo(500);
    assertThat(problem.getDetail()).doesNotContain("secret");
    assertThat(problem.getProperties()).containsEntry("code", "INTERNAL_ERROR");
  }
}

package my.packagename.infrastructure.entry.points.rest;

import static my.packagename.infrastructure.entry.points.rest.FakeCustomPorts.KNOWN_ID;
import static my.packagename.infrastructure.entry.points.rest.SecurityTestSupport.reader;
import static my.packagename.infrastructure.entry.points.rest.SecurityTestSupport.withoutRoles;
import static my.packagename.infrastructure.entry.points.rest.SecurityTestSupport.writer;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Mandatory negative paths (keycloak.md §13.4): anonymous, invalid token, wrong role. */
@WebMvcTest(CustomController.class)
@Import({FakeCustomPorts.class, GlobalExceptionHandler.class, SecurityTestSupport.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomControllerSecurityTest {

  private static final String BASE = "/api/v1/customs";
  private static final String BODY = "{\"name\":\"Fresh\"}";

  @Autowired private MockMvcTester mvc;

  @Test
  void should_return_401_problem_when_request_is_anonymous() {
    MvcTestResult result = mvc.get().uri(BASE).exchange();

    assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("UNAUTHENTICATED");
    assertThat(result).headers().containsHeader(HttpHeaders.WWW_AUTHENTICATE);
  }

  @Test
  void should_return_401_when_bearer_token_cannot_be_verified() {
    MvcTestResult result =
        mvc.get()
            .uri(BASE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer forged.token.value")
            .exchange();

    assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void should_return_403_problem_when_reader_tries_to_create() {
    MvcTestResult result =
        mvc.post()
            .uri(BASE)
            .with(reader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY)
            .exchange();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("ACCESS_DENIED");
  }

  @Test
  void should_return_403_when_reader_tries_to_delete() {
    MvcTestResult result =
        mvc.delete().uri(BASE + "/{id}", KNOWN_ID.value()).with(reader()).exchange();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_return_403_when_token_carries_no_platform_role() {
    MvcTestResult result = mvc.get().uri(BASE).with(withoutRoles()).exchange();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_let_writer_read_too() {
    MvcTestResult result =
        mvc.get().uri(BASE + "/{id}", KNOWN_ID.value()).with(writer()).exchange();

    assertThat(result).hasStatusOk();
  }
}

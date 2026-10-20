package my.packagename.infrastructure.entry.points.rest;

import static my.packagename.infrastructure.entry.points.rest.FakeCustomPorts.CONTENDED_ID;
import static my.packagename.infrastructure.entry.points.rest.FakeCustomPorts.KNOWN_ID;
import static my.packagename.infrastructure.entry.points.rest.FakeCustomPorts.NEW_ID;
import static my.packagename.infrastructure.entry.points.rest.SecurityTestSupport.reader;
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

@WebMvcTest(CustomController.class)
@Import({FakeCustomPorts.class, GlobalExceptionHandler.class, SecurityTestSupport.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomControllerTest {

  private static final String BASE = "/api/v1/customs";

  @Autowired private MockMvcTester mvc;

  @Test
  void should_return_201_with_location_when_custom_is_created() {
    MvcTestResult result = post(BASE, "{\"name\":\"Fresh\"}");

    assertThat(result).hasStatus(HttpStatus.CREATED);
    assertThat(result)
        .headers()
        .hasValue(HttpHeaders.LOCATION, "http://localhost" + BASE + "/" + NEW_ID);
    assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Fresh");
  }

  @Test
  void should_return_400_problem_with_field_errors_when_name_is_blank() {
    MvcTestResult result = post(BASE, "{\"name\":\"\"}");

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
    assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("name");
  }

  @Test
  void should_return_custom_when_it_exists() {
    MvcTestResult result =
        mvc.get().uri(BASE + "/{id}", KNOWN_ID.value()).with(reader()).exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(KNOWN_ID.toString());
    assertThat(result).bodyJson().extractingPath("$.version").isEqualTo(2);
  }

  @Test
  void should_return_404_problem_when_custom_does_not_exist() {
    MvcTestResult result = mvc.get().uri(BASE + "/{id}", NEW_ID.value()).with(reader()).exchange();

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CUSTOM_NOT_FOUND");
    assertThat(result).bodyJson().extractingPath("$.instance").isEqualTo(BASE + "/" + NEW_ID);
  }

  @Test
  void should_return_400_problem_when_id_is_not_a_uuid() {
    MvcTestResult result = mvc.get().uri(BASE + "/not-a-uuid").with(reader()).exchange();

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void should_return_page_envelope_when_listing() {
    MvcTestResult result = mvc.get().uri(BASE + "?page=0&size=5").with(reader()).exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.items[0].name").isEqualTo("Demo");
    assertThat(result).bodyJson().extractingPath("$.totalPages").isEqualTo(1);
  }

  @Test
  void should_return_400_problem_when_page_size_is_out_of_range() {
    MvcTestResult result = mvc.get().uri(BASE + "?size=101").with(reader()).exchange();

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
  }

  @Test
  void should_return_renamed_custom_when_renaming() {
    MvcTestResult result = put(BASE + "/" + KNOWN_ID, "{\"name\":\"Renamed\"}");

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Renamed");
  }

  @Test
  void should_return_409_problem_when_rename_races() {
    MvcTestResult result = put(BASE + "/" + CONTENDED_ID, "{\"name\":\"Renamed\"}");

    assertThat(result).hasStatus(HttpStatus.CONFLICT);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("CUSTOM_CONCURRENT_MODIFICATION");
  }

  @Test
  void should_return_422_problem_when_name_breaks_domain_rule() {
    MvcTestResult result = put(BASE + "/" + KNOWN_ID, "{\"name\":\"   \"}");

    assertThat(result).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CUSTOM_INVALID_NAME");
  }

  @Test
  void should_return_204_when_custom_is_deleted() {
    MvcTestResult result =
        mvc.delete().uri(BASE + "/{id}", KNOWN_ID.value()).with(writer()).exchange();

    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
  }

  @Test
  void should_return_404_problem_when_deleting_unknown_custom() {
    MvcTestResult result =
        mvc.delete().uri(BASE + "/{id}", NEW_ID.value()).with(writer()).exchange();

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_return_415_when_content_type_is_not_json() {
    MvcTestResult result = post(BASE, "{\"name\":\"Fresh\"}", "broken/json+type");

    assertThat(result).hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }

  private MvcTestResult post(String uri, String json) {
    return post(uri, json, MediaType.APPLICATION_JSON_VALUE);
  }

  private MvcTestResult post(String uri, String json, String contentType) {
    return mvc.post().uri(uri).with(writer()).contentType(contentType).content(json).exchange();
  }

  private MvcTestResult put(String uri, String json) {
    return mvc.put()
        .uri(uri)
        .with(writer())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange();
  }
}

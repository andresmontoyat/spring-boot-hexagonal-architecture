package my.packagename;

import static org.assertj.core.api.Assertions.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * The committed KrakenD config in front of the running service, with real Keycloak tokens. Proves
 * the edge rejects bad callers before the service sees them, routes good ones, passes RFC 9457
 * errors through untouched and throttles per client.
 */
@IntegrationTestContainers
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GatewaySecurityAcceptanceTest {

  private static final String KRAKEND_IMAGE = "krakend:2.13.11";
  private static final int GATEWAY_PORT = 8080;
  private static final String CUSTOMS = "/api/v1/customs";
  private static final String ALLOWED_ORIGIN = "http://localhost:3000";
  private static final int BURST = 25;

  private static GenericContainer<?> gateway;

  private final HttpClient http = HttpClient.newHttpClient();

  @LocalServerPort private int servicePort;

  @Autowired private KeycloakContainer keycloak;

  private KeycloakTokens tokens;

  @BeforeEach
  void startGatewayOnce() {
    tokens = new KeycloakTokens(keycloak);
    if (gateway == null) {
      gateway = krakend();
      gateway.start();
    }
  }

  @Test
  void should_reject_at_the_edge_when_token_is_missing() throws Exception {
    HttpResponse<String> response = send(get(CUSTOMS).build());

    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(response.body())
        .as("rejected by KrakenD, not the service")
        .doesNotContain("\"code\"");
  }

  @Test
  void should_reject_at_the_edge_when_reader_tries_to_create() throws Exception {
    HttpResponse<String> response = send(post(CUSTOMS, "{\"name\":\"Nope\"}", tokens.reader()));

    assertThat(response.statusCode()).isEqualTo(403);
    assertThat(response.body()).doesNotContain("\"code\"");
  }

  @Test
  void should_route_writer_and_return_a_location_that_resolves_against_the_gateway()
      throws Exception {
    HttpResponse<String> created =
        send(post(CUSTOMS, "{\"name\":\"ViaGateway\"}", tokens.writer()));
    String location = created.headers().firstValue("Location").orElseThrow();

    HttpResponse<String> fetched =
        send(get(URI.create(gatewayUrl(CUSTOMS)).resolve(location).getPath(), tokens.reader()));

    assertThat(created.statusCode()).isEqualTo(201);
    assertThat(location)
        .doesNotContain("host.testcontainers.internal")
        .doesNotContain("custom-service");
    assertThat(fetched.statusCode()).isEqualTo(200);
    assertThat(fetched.body()).contains("ViaGateway");
  }

  @Test
  void should_pass_service_problem_details_through_unchanged() throws Exception {
    HttpResponse<String> response =
        send(get(CUSTOMS + "/00000000-0000-0000-0000-000000000000", tokens.reader()));

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.headers().firstValue("Content-Type")).hasValue("application/problem+json");
    assertThat(response.body()).contains("\"code\":\"CUSTOM_NOT_FOUND\"");
  }

  @Test
  void should_throttle_a_client_that_exceeds_its_rate() {
    String token = tokens.reader();

    List<Integer> statuses =
        IntStream.range(0, BURST).mapToObj(i -> statusOf(get(CUSTOMS, token))).toList();

    assertThat(statuses).contains(429);
  }

  @Test
  void should_answer_cors_preflight_for_an_allowed_origin() throws Exception {
    HttpRequest preflight =
        HttpRequest.newBuilder(URI.create(gatewayUrl(CUSTOMS)))
            .method("OPTIONS", BodyPublishers.noBody())
            .header("Origin", ALLOWED_ORIGIN)
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "authorization,content-type")
            .build();

    HttpResponse<String> response = send(preflight);

    assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
        .hasValue(ALLOWED_ORIGIN);
  }

  private GenericContainer<?> krakend() {
    int keycloakPort = URI.create(keycloak.getAuthServerUrl()).getPort();
    Testcontainers.exposeHostPorts(servicePort, keycloakPort);
    String realmPath = "/realms/" + KeycloakContainerConfiguration.REALM;
    return new GenericContainer<>(KRAKEND_IMAGE)
        .withCopyFileToContainer(
            MountableFile.forHostPath(System.getProperty("krakend.config.dir")), "/etc/krakend")
        .withEnv("FC_ENABLE", "1")
        .withEnv("FC_SETTINGS", "/etc/krakend/settings")
        .withEnv("CUSTOMS_HOST", "http://host.testcontainers.internal:" + servicePort)
        // Tokens are minted through the host-mapped port, so that is the `iss` they carry.
        .withEnv("KEYCLOAK_ISSUER", KeycloakContainerConfiguration.realmUrl(keycloak))
        .withEnv(
            "KEYCLOAK_JWKS_URL",
            "http://host.testcontainers.internal:"
                + keycloakPort
                + realmPath
                + "/protocol/openid-connect/certs")
        .withEnv("KRAKEND_JWK_INSECURE", "true")
        .withCommand("run", "-c", "/etc/krakend/krakend.tmpl")
        .withExposedPorts(GATEWAY_PORT)
        .waitingFor(Wait.forHttp("/__health").forPort(GATEWAY_PORT));
  }

  private String gatewayUrl(String path) {
    return "http://" + gateway.getHost() + ":" + gateway.getMappedPort(GATEWAY_PORT) + path;
  }

  private HttpRequest.Builder get(String path) {
    return HttpRequest.newBuilder(URI.create(gatewayUrl(path))).GET();
  }

  private HttpRequest get(String path, String token) {
    return get(path).header("Authorization", "Bearer " + token).build();
  }

  private HttpRequest post(String path, String json, String token) {
    return HttpRequest.newBuilder(URI.create(gatewayUrl(path)))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(json))
        .build();
  }

  private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
    return http.send(request, BodyHandlers.ofString());
  }

  private int statusOf(HttpRequest request) {
    try {
      return send(request).statusCode();
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(ex);
    }
  }
}

package my.packagename;

import static my.packagename.KeycloakContainerConfiguration.READER_BOT;
import static my.packagename.KeycloakContainerConfiguration.READER_SECRET;
import static my.packagename.KeycloakContainerConfiguration.WRITER_BOT;
import static my.packagename.KeycloakContainerConfiguration.WRITER_SECRET;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Machine tokens via client_credentials — the grant keycloak.md §9 prescribes for automation. */
public final class KeycloakTokens {

  private final RestClient tokenEndpoint;

  public KeycloakTokens(KeycloakContainer keycloak) {
    this.tokenEndpoint =
        RestClient.create(
            KeycloakContainerConfiguration.realmUrl(keycloak) + "/protocol/openid-connect/token");
  }

  public String writer() {
    return clientCredentials(WRITER_BOT, WRITER_SECRET);
  }

  public String reader() {
    return clientCredentials(READER_BOT, READER_SECRET);
  }

  private String clientCredentials(String clientId, String secret) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "client_credentials");
    form.add("client_id", clientId);
    form.add("client_secret", secret);
    return tokenEndpoint
        .post()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(TokenResponse.class)
        .access_token();
  }

  @SuppressWarnings("checkstyle:RecordComponentName") // mirrors the OAuth2 wire field
  private record TokenResponse(String access_token) {}
}

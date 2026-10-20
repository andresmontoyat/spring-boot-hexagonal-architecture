package my.packagename.infrastructure.entry.points.rest.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class KeycloakJwtAuthenticationConverterTest {

  private static final String CLIENT_ID = "customs-api";

  private final KeycloakJwtAuthenticationConverter converter =
      new KeycloakJwtAuthenticationConverter(
          new KeycloakSecurityProperties(CLIENT_ID, "platform-"));

  @Test
  void should_map_client_roles_stripping_prefix_and_uppercasing() {
    Jwt jwt =
        token(
            Map.of(
                "resource_access", Map.of(CLIENT_ID, Map.of("roles", List.of("platform-writer")))));

    assertThat(authorities(jwt)).contains("ROLE_WRITER");
  }

  @Test
  void should_map_realm_roles_and_scopes() {
    Jwt jwt =
        token(Map.of("realm_access", Map.of("roles", List.of("admin")), "scope", "profile email"));

    assertThat(authorities(jwt)).contains("ROLE_ADMIN", "SCOPE_profile", "SCOPE_email");
  }

  @Test
  void should_ignore_roles_of_other_clients() {
    Jwt jwt =
        token(
            Map.of(
                "resource_access",
                Map.of("billing-api", Map.of("roles", List.of("platform-writer")))));

    assertThat(authorities(jwt)).doesNotContain("ROLE_WRITER");
  }

  @Test
  void should_use_subject_as_principal_name() {
    Jwt jwt = token(Map.of());

    assertThat(converter.convert(jwt).getName()).isEqualTo("service-account-bot");
  }

  private List<String> authorities(Jwt jwt) {
    return converter.convert(jwt).getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
  }

  private static Jwt token(Map<String, Object> claims) {
    Jwt.Builder builder =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("service-account-bot")
            .issuedAt(Instant.parse("2026-10-20T12:00:00Z"))
            .expiresAt(Instant.parse("2026-10-20T12:05:00Z"));
    claims.forEach(builder::claim);
    return builder.build();
  }
}

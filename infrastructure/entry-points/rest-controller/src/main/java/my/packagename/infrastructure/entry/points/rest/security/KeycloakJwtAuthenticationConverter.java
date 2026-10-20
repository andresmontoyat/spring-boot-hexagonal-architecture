package my.packagename.infrastructure.entry.points.rest.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Keycloak nests roles under {@code realm_access.roles} and {@code resource_access.<client>.roles},
 * which Spring's default converter ignores (keycloak.md §5.1). Only this API's client roles count.
 */
public final class KeycloakJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLE = "ROLE_";
  private static final String SCOPE = "SCOPE_";

  private final KeycloakSecurityProperties properties;

  public KeycloakJwtAuthenticationConverter(KeycloakSecurityProperties properties) {
    this.properties = properties;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Set<GrantedAuthority> authorities =
        Stream.of(realmRoles(jwt), clientRoles(jwt), scopes(jwt))
            .flatMap(Collection::stream)
            .collect(Collectors.toUnmodifiableSet());
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }

  private static List<GrantedAuthority> realmRoles(Jwt jwt) {
    return rolesOf(jwt.getClaimAsMap("realm_access")).stream()
        .map(role -> authority(ROLE + role.toUpperCase(Locale.ROOT)))
        .toList();
  }

  private List<GrantedAuthority> clientRoles(Jwt jwt) {
    Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
    Object client = resourceAccess == null ? null : resourceAccess.get(properties.clientId());
    return rolesOf(client).stream()
        .map(role -> role.replaceFirst("^" + properties.rolePrefix(), ""))
        .map(role -> authority(ROLE + role.toUpperCase(Locale.ROOT)))
        .toList();
  }

  private static List<GrantedAuthority> scopes(Jwt jwt) {
    String scope = jwt.getClaimAsString("scope");
    return scope == null
        ? List.of()
        : Arrays.stream(scope.split(" ")).map(value -> authority(SCOPE + value)).toList();
  }

  private static List<String> rolesOf(Object access) {
    if (access instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> roles) {
      return roles.stream().map(String::valueOf).toList();
    }
    return List.of();
  }

  private static GrantedAuthority authority(String name) {
    return new SimpleGrantedAuthority(name);
  }
}

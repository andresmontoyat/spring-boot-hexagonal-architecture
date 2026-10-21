package my.packagename.infrastructure.entry.points.rest.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * @param clientId Keycloak client whose {@code resource_access} roles this API honours
 * @param rolePrefix stripped from client roles: {@code platform-writer} → {@code ROLE_WRITER}
 */
@Validated
@ConfigurationProperties("security.keycloak")
public record KeycloakSecurityProperties(
    @NotBlank String clientId, @DefaultValue("platform-") String rolePrefix) {}

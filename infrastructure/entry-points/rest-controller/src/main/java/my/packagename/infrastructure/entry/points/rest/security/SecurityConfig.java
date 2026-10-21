package my.packagename.infrastructure.entry.points.rest.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

/**
 * Topology B (keycloak.md §5): the service re-validates every JWT even behind KrakenD, so it stays
 * safe when reached directly. Coarse rule here (authenticated); roles per endpoint with
 * {@code @PreAuthorize} on the controller.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@EnableConfigurationProperties(KeycloakSecurityProperties.class)
public class SecurityConfig {

  private static final String[] PUBLIC_PATHS = {
    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health/**", "/actuator/info"
  };

  @Bean
  SecurityFilterChain apiSecurity(
      HttpSecurity http, KeycloakSecurityProperties properties, JsonMapper json) {
    ProblemDetailsSecurityErrors errors = new ProblemDetailsSecurityErrors(json);
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(
            server ->
                server
                    .jwt(
                        jwt ->
                            jwt.jwtAuthenticationConverter(
                                new KeycloakJwtAuthenticationConverter(properties)))
                    .authenticationEntryPoint(errors)
                    .accessDeniedHandler(errors))
        .exceptionHandling(
            handling -> handling.authenticationEntryPoint(errors).accessDeniedHandler(errors))
        .build();
  }
}

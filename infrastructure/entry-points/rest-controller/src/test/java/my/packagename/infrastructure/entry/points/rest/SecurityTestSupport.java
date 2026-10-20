package my.packagename.infrastructure.entry.points.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import my.packagename.infrastructure.entry.points.rest.security.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Real {@link SecurityConfig}; tokens are forged with {@code jwt()} so no Keycloak is needed. The
 * decoder rejects everything, proving tests never pass through an unchecked bearer token.
 */
@TestConfiguration(proxyBeanMethods = false)
@Import(SecurityConfig.class)
public class SecurityTestSupport {

  @Bean
  JwtDecoder rejectingJwtDecoder() {
    return token -> {
      throw new BadJwtException("no real tokens in slice tests");
    };
  }

  public static RequestPostProcessor reader() {
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_READER"));
  }

  public static RequestPostProcessor writer() {
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_WRITER"));
  }

  public static RequestPostProcessor withoutRoles() {
    return jwt().authorities(new SimpleGrantedAuthority("SCOPE_profile"));
  }
}

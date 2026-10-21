package my.packagename.infrastructure.driven.adapters.valkey;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ValkeyContainerConfiguration {

  private static final DockerImageName VALKEY_IMAGE =
      DockerImageName.parse("valkey/valkey:8-alpine");
  private static final int VALKEY_PORT = 6379;

  /** Valkey is wire-compatible with Redis, so Boot's "redis" service connection applies. */
  @Bean
  @ServiceConnection(name = "redis")
  GenericContainer<?> valkey() {
    return new GenericContainer<>(VALKEY_IMAGE).withExposedPorts(VALKEY_PORT);
  }
}

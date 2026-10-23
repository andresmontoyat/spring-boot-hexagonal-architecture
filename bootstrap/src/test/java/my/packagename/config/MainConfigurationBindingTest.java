package my.packagename.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import io.codehunters.messaging.config.EventMessagingProperties;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import my.packagename.infrastructure.driven.adapters.valkey.CustomCacheProperties;
import my.packagename.infrastructure.entry.points.rest.security.KeycloakSecurityProperties;
import my.packagename.infrastructure.entry.points.scheduler.CustomsReportJobProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;

/**
 * Context tests load the standalone test application.yml (application-yml.md R10), which shadows
 * the production file on the classpath. This binds the production file's defaults to the real
 * types, so a value of the wrong type fails here instead of at the first deployment.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MainConfigurationBindingTest {

  private static final Path MAIN_YAML = Path.of("src/main/resources/application.yml");

  private Binder binder;

  @BeforeEach
  void loadProductionDefaults() throws IOException {
    StandardEnvironment environment = new StandardEnvironment();
    new YamlPropertySourceLoader()
        .load("main", new FileSystemResource(MAIN_YAML))
        .forEach(environment.getPropertySources()::addLast);
    binder = Binder.get(environment);
  }

  @Test
  void should_bind_hikari_settings_to_their_real_types() {
    HikariConfig hikari =
        binder.bind("spring.datasource.hikari", HikariConfig.class).orElseGet(HikariConfig::new);

    assertThat(hikari.getConnectionTimeout()).isEqualTo(3_000L);
  }

  @Test
  void should_bind_outbox_settings_as_durations() {
    EventMessagingProperties outbox =
        binder.bind("codehunters.outbox", EventMessagingProperties.class).get();

    assertThat(outbox.claim().ttl()).isEqualTo(Duration.ofMinutes(5));
  }

  @Test
  void should_bind_service_specific_properties() {
    assertThat(binder.bind("cache.custom", CustomCacheProperties.class).get().ttl())
        .isEqualTo(Duration.ofMinutes(10));
    assertThat(binder.bind("security.keycloak", KeycloakSecurityProperties.class).get().clientId())
        .isEqualTo("customs-api");
  }

  @Test
  void should_bind_job_lock_windows_as_durations() {
    CustomsReportJobProperties job =
        binder.bind("jobs.customs-report", CustomsReportJobProperties.class).get();

    assertThat(job.lockAtMostFor()).isEqualTo(Duration.ofMinutes(2));
    assertThat(job.lockAtLeastFor()).isEqualTo(Duration.ofSeconds(30));
    assertThat(job.lockName()).isEqualTo("customs-report-custom-service");
  }
}

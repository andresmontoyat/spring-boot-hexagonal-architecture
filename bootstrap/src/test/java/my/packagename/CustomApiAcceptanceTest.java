package my.packagename;

import static org.assertj.core.api.Assertions.assertThat;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/**
 * Black-box BDD suite: Karate drives the real HTTP stack against real PostgreSQL and Valkey. After
 * the run, the meters prove the decorator chain (metrics → cache → JDBC) is actually wired.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import({PostgresContainerConfiguration.class, ValkeyContainerConfiguration.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomApiAcceptanceTest {

  @LocalServerPort private int port;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Autowired private MeterRegistry registry;

  @Test
  void should_satisfy_every_customs_scenario_through_the_decorator_chain() {
    Results results =
        Runner.path("classpath:karate/customs")
            .systemProperty("baseUrl", "http://localhost:" + port + contextPath)
            .outputCucumberJson(false)
            .parallel(1);

    assertThat(results.getScenariosTotal()).as("an empty suite must not pass").isPositive();
    assertThat(results.getFailCount()).as(results.getErrorMessages()).isZero();
    assertThat(registry.find("usecase.operation").tag("operation", "create_custom").timer())
        .as("metrics decorator in front of the use case")
        .isNotNull();
    assertThat(registry.find("cache.gets").tag("result", "hit").counter())
        .as("cache decorator in front of JDBC")
        .isNotNull();
  }
}

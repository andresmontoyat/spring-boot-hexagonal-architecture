package my.packagename;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Real Keycloak with the committed realm (keycloak.md §13.3): real token endpoint, real JWKS,
 * nothing mocked. Secrets reach the realm through its ${ENV} placeholders, never the JSON.
 *
 * <p>An initializer rather than a container bean: the resource-server auto-configuration decides on
 * {@code jwk-set-uri} while parsing configuration, before any bean (or dynamic property registrar)
 * exists, so the URLs must be in the environment before the context refreshes.
 */
public class KeycloakContainerConfiguration
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  public static final String REALM = "customs";
  public static final String WRITER_BOT = "customs-writer-bot";
  public static final String READER_BOT = "customs-reader-bot";
  public static final String WRITER_SECRET = "test-writer-secret";
  public static final String READER_SECRET = "test-reader-secret";

  private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.7.4";

  private static final KeycloakContainer KEYCLOAK =
      new KeycloakContainer(KEYCLOAK_IMAGE)
          .withRealmImportFile("customs-realm.json")
          .withEnv("CUSTOMS_WRITER_BOT_SECRET", WRITER_SECRET)
          .withEnv("CUSTOMS_READER_BOT_SECRET", READER_SECRET)
          .withEnv("CUSTOMS_DEMO_USER_PASSWORD", "unused-in-tests");

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    if (!KEYCLOAK.isRunning()) {
      KEYCLOAK.start();
    }
    String realmUrl = realmUrl(KEYCLOAK);
    TestPropertyValues.of(
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + realmUrl,
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri="
                + realmUrl
                + "/protocol/openid-connect/certs")
        .applyTo(context.getEnvironment());
    context.getBeanFactory().registerSingleton("keycloakContainer", KEYCLOAK);
  }

  public static String realmUrl(KeycloakContainer keycloak) {
    return keycloak.getAuthServerUrl() + "/realms/" + REALM;
  }
}

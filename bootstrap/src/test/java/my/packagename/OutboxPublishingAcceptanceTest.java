package my.packagename;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import my.packagename.application.event.CustomEventOutbox;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.web.client.RestClient;

/**
 * The write and the event are one transaction; the SDK publishes after commit. Proven end to end:
 * HTTP → PostgreSQL (custom + outbox_message) → Kafka record keyed by the aggregate id.
 */
@IntegrationTestContainers
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OutboxPublishingAcceptanceTest {

  private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(20);
  private static final String OUTBOX_ID_HEADER = "outbox-message-id";

  @LocalServerPort private int port;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Autowired private ConsumerFactory<String, String> consumerFactory;

  @Autowired private KeycloakContainer keycloak;

  private Consumer<String, String> consumer;
  private final List<ConsumerRecord<String, String>> received = new CopyOnWriteArrayList<>();

  @BeforeEach
  void subscribe() {
    Properties overrides = new Properties();
    overrides.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumer =
        consumerFactory.createConsumer("acceptance-" + UUID.randomUUID(), null, null, overrides);
    consumer.subscribe(List.of(CustomEventOutbox.TOPIC));
  }

  @AfterEach
  void close() {
    consumer.close();
  }

  @Test
  void should_publish_created_event_to_kafka_keyed_by_custom_id() {
    // When
    String id = createCustom("Published");

    // Then
    await().atMost(DELIVERY_TIMEOUT).untilAsserted(() -> assertThat(recordsFor(id)).isNotEmpty());
    ConsumerRecord<String, String> record = recordsFor(id).getFirst();
    assertThat(record.value()).contains("\"eventName\":\"CustomCreated\"").contains("Published");
    assertThat(record.headers().lastHeader(OUTBOX_ID_HEADER))
        .as("consumers deduplicate on this header")
        .isNotNull();
    assertThat(
            new String(
                record.headers().lastHeader(OUTBOX_ID_HEADER).value(), StandardCharsets.UTF_8))
        .matches("[0-9a-f-]{36}");
  }

  private List<ConsumerRecord<String, String>> recordsFor(String key) {
    consumer.poll(Duration.ofMillis(200)).forEach(received::add);
    return received.stream().filter(record -> key.equals(record.key())).toList();
  }

  private String createCustom(String name) {
    return RestClient.create("http://localhost:" + port + contextPath)
        .post()
        .uri("/api/v1/customs")
        .headers(headers -> headers.setBearerAuth(new KeycloakTokens(keycloak).writer()))
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"name\":\"" + name + "\"}")
        .retrieve()
        .body(CreatedCustom.class)
        .id();
  }

  private record CreatedCustom(String id) {}
}

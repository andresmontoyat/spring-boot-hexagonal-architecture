package my.packagename;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaContainerConfiguration {

  private static final String KAFKA_IMAGE = "apache/kafka:3.9.1";

  @Bean
  @ServiceConnection
  KafkaContainer kafka() {
    return new KafkaContainer(KAFKA_IMAGE);
  }
}

package my.packagename;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

/**
 * One shared configuration for every full-context test, so Spring's context cache starts the
 * containers once per run.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import({
  PostgresContainerConfiguration.class,
  ValkeyContainerConfiguration.class,
  KafkaContainerConfiguration.class
})
public @interface IntegrationTestContainers {}

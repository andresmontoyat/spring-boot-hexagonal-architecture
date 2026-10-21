package my.packagename.infrastructure.entry.points.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import my.packagename.application.port.inbound.CreateCustomPort;
import my.packagename.application.port.inbound.DeleteCustomPort;
import my.packagename.application.port.inbound.FindCustomPort;
import my.packagename.application.port.inbound.ListCustomsPort;
import my.packagename.application.port.inbound.RenameCustomPort;
import my.packagename.application.query.PageResult;
import my.packagename.domain.exception.CustomConcurrentModificationException;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/** Inbound ports backed by a map: the controller is exercised without any use case class. */
@TestConfiguration(proxyBeanMethods = false)
class FakeCustomPorts {

  static final CustomId KNOWN_ID =
      new CustomId(UUID.fromString("7f1c2a52-3c1e-4b5e-9b7a-1f2d3c4b5a69"));
  static final CustomId NEW_ID =
      new CustomId(UUID.fromString("0b6f3e7c-8d1a-4c2b-9e5f-6a7b8c9d0e1f"));
  static final CustomId CONTENDED_ID =
      new CustomId(UUID.fromString("c0ffee00-0000-4000-8000-000000000000"));

  private final Map<CustomId, Custom> store = new LinkedHashMap<>();

  FakeCustomPorts() {
    store.put(KNOWN_ID, Custom.rehydrate(KNOWN_ID, new CustomName("Demo"), 2L));
  }

  @Bean
  CreateCustomPort createCustomPort() {
    return command -> Custom.create(NEW_ID, new CustomName(command.name()));
  }

  @Bean
  FindCustomPort findCustomPort() {
    return this::load;
  }

  @Bean
  ListCustomsPort listCustomsPort() {
    return query ->
        new PageResult<>(List.copyOf(store.values()), query.page(), query.size(), store.size());
  }

  @Bean
  RenameCustomPort renameCustomPort() {
    return command -> {
      if (command.id().equals(CONTENDED_ID)) {
        throw new CustomConcurrentModificationException(CONTENDED_ID, null);
      }
      return load(command.id()).rename(new CustomName(command.name()));
    };
  }

  @Bean
  DeleteCustomPort deleteCustomPort() {
    return this::load;
  }

  private Custom load(CustomId id) {
    Custom custom = store.get(id);
    if (custom == null) {
      throw new CustomNotFoundException(id);
    }
    return custom;
  }
}

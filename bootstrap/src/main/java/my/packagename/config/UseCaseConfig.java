package my.packagename.config;

import io.codehunters.messaging.OutboxWriter;
import io.codehunters.messaging.factory.OutboxEventFactory;
import java.util.UUID;
import my.packagename.application.event.CustomEventOutbox;
import my.packagename.application.port.ChainablePort;
import my.packagename.application.port.inbound.CreateCustomPort;
import my.packagename.application.port.inbound.DeleteCustomPort;
import my.packagename.application.port.inbound.FindCustomPort;
import my.packagename.application.port.inbound.GenerateCustomsReportPort;
import my.packagename.application.port.inbound.ListCustomsPort;
import my.packagename.application.port.inbound.RenameCustomPort;
import my.packagename.application.port.outbound.CustomIdGeneratorPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.port.outbound.CustomsReportRepositoryPort;
import my.packagename.application.port.outbound.TransactionPort;
import my.packagename.application.result.ReportOutcome;
import my.packagename.application.usecase.CreateCustomUseCase;
import my.packagename.application.usecase.DeleteCustomUseCase;
import my.packagename.application.usecase.FindCustomUseCase;
import my.packagename.application.usecase.GenerateCustomsReportUseCase;
import my.packagename.application.usecase.ListCustomsUseCase;
import my.packagename.application.usecase.RenameCustomUseCase;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.infrastructure.driven.adapters.jdbc.CustomRepositoryAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * The only place that names use case implementations. Every port bean is {@code @Primary} and is
 * the head of its decorator chain (metrics → cache → core), so consumers never see a decorator or
 * the core directly.
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  CustomIdGeneratorPort customIdGenerator() {
    return () -> new CustomId(UUID.randomUUID());
  }

  /** OutboxWriter and OutboxEventFactory come from the outbox SDK auto-configuration. */
  @Bean
  CustomEventOutbox customEventOutbox(OutboxWriter writer, OutboxEventFactory factory) {
    return new CustomEventOutbox(writer, factory);
  }

  @Bean
  @Primary
  CustomRepositoryPort customRepositoryPort(
      CustomRepositoryAdapter jdbc,
      ObjectProvider<ChainablePort<CustomRepositoryPort>> decorators) {
    return PortChain.link(jdbc, decorators.stream().toList());
  }

  @Bean
  @Primary
  CreateCustomPort createCustomPort(
      TransactionPort<Custom> transaction,
      CustomRepositoryPort repository,
      CustomIdGeneratorPort idGenerator,
      CustomEventOutbox events,
      ObjectProvider<ChainablePort<CreateCustomPort>> decorators) {
    return PortChain.link(
        new CreateCustomUseCase(transaction, repository, idGenerator, events),
        decorators.stream().toList());
  }

  @Bean
  @Primary
  FindCustomPort findCustomPort(
      CustomRepositoryPort repository, ObjectProvider<ChainablePort<FindCustomPort>> decorators) {
    return PortChain.link(new FindCustomUseCase(repository), decorators.stream().toList());
  }

  @Bean
  @Primary
  ListCustomsPort listCustomsPort(
      CustomRepositoryPort repository, ObjectProvider<ChainablePort<ListCustomsPort>> decorators) {
    return PortChain.link(new ListCustomsUseCase(repository), decorators.stream().toList());
  }

  @Bean
  @Primary
  RenameCustomPort renameCustomPort(
      TransactionPort<Custom> transaction,
      CustomRepositoryPort repository,
      CustomEventOutbox events,
      ObjectProvider<ChainablePort<RenameCustomPort>> decorators) {
    return PortChain.link(
        new RenameCustomUseCase(transaction, repository, events), decorators.stream().toList());
  }

  @Bean
  @Primary
  DeleteCustomPort deleteCustomPort(
      TransactionPort<Boolean> transaction,
      CustomRepositoryPort repository,
      CustomEventOutbox events,
      ObjectProvider<ChainablePort<DeleteCustomPort>> decorators) {
    return PortChain.link(
        new DeleteCustomUseCase(transaction, repository, events), decorators.stream().toList());
  }

  @Bean
  @Primary
  GenerateCustomsReportPort generateCustomsReportPort(
      TransactionPort<ReportOutcome> transaction,
      CustomRepositoryPort customs,
      CustomsReportRepositoryPort reports,
      ObjectProvider<ChainablePort<GenerateCustomsReportPort>> decorators) {
    return PortChain.link(
        new GenerateCustomsReportUseCase(transaction, customs, reports),
        decorators.stream().toList());
  }
}

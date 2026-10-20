package my.packagename.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "my.packagename", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  private static final String DOMAIN = "my.packagename.domain..";
  private static final String APPLICATION = "my.packagename.application..";
  private static final String USE_CASES = "my.packagename.application.usecase..";
  private static final String INBOUND_PORTS = "my.packagename.application.port.inbound..";
  private static final String PORTS = "my.packagename.application.port..";
  private static final String INFRASTRUCTURE = "my.packagename.infrastructure..";
  private static final String ENTRY_POINTS = "my.packagename.infrastructure.entry.points..";
  private static final String DRIVEN_ADAPTERS = "my.packagename.infrastructure.driven.adapters..";
  private static final String USE_CASE_DECORATORS =
      "my.packagename.infrastructure.driven.adapters.metrics..";

  @ArchTest
  static final ArchRule domainShouldOnlyDependOnJdkAndItself =
      classes()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", DOMAIN)
          .because("The domain is pure Java: no framework, no application, no infrastructure");

  @ArchTest
  static final ArchRule applicationShouldOnlyDependOnJdkDomainAndItself =
      classes()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", DOMAIN, APPLICATION)
          .because("Use cases and ports are framework-agnostic; adapters bring the frameworks");

  @ArchTest
  static final ArchRule portsShouldBeInterfaces =
      classes()
          .that()
          .resideInAPackage(PORTS)
          .should()
          .beInterfaces()
          .because("Ports are contracts");

  @ArchTest
  static final ArchRule useCasesShouldImplementInboundPort =
      classes()
          .that()
          .resideInAPackage(USE_CASES)
          .and()
          .haveSimpleNameEndingWith("UseCase")
          .should()
          .implement(implementedInterfaceIn(INBOUND_PORTS))
          .because("Direction rules cannot see a missing interface; entry points need a contract");

  @ArchTest
  static final ArchRule infrastructureShouldNotDependOnUseCases =
      noClasses()
          .that()
          .resideInAPackage(INFRASTRUCTURE)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(USE_CASES)
          .because("Adapters see ports; only bootstrap wiring names the implementation");

  @ArchTest
  static final ArchRule entryPointsShouldNotDependOnDrivenAdapters =
      noClasses()
          .that()
          .resideInAPackage(ENTRY_POINTS)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(DRIVEN_ADAPTERS)
          .because("Entry points talk to inbound ports, never to persistence");

  @ArchTest
  static final ArchRule drivenAdaptersShouldNotDependOnEntryPoints =
      noClasses()
          .that()
          .resideInAPackage(DRIVEN_ADAPTERS)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(ENTRY_POINTS)
          .because("Driven adapters must not know how they are invoked");

  /**
   * Use case metrics decorators implement inbound ports, whose signatures carry commands; they are
   * the one driven-adapter package allowed to see them.
   */
  @ArchTest
  static final ArchRule drivenAdaptersShouldNotDependOnCommands =
      noClasses()
          .that()
          .resideInAPackage(DRIVEN_ADAPTERS)
          .and()
          .resideOutsideOfPackage(USE_CASE_DECORATORS)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("my.packagename.application.command..")
          .because("Commands flow from entry points to use cases; persistence works on aggregates");

  @ArchTest
  static final ArchRule useCaseDecoratorsShouldOnlyDecorateInboundPorts =
      noClasses()
          .that()
          .resideInAPackage(USE_CASE_DECORATORS)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("my.packagename.application.port.outbound..")
          .because("Use case decorators wrap inbound ports; they must not reach persistence");

  private static DescribedPredicate<JavaClass> implementedInterfaceIn(String packageIdentifier) {
    return JavaClass.Predicates.resideInAPackage(packageIdentifier);
  }
}

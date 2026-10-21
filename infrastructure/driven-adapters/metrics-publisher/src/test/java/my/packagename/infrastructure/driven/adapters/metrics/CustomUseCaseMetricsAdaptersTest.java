package my.packagename.infrastructure.driven.adapters.metrics;

import static my.packagename.application.fake.CustomFixtures.FIXED_ID;
import static my.packagename.application.fake.CustomFixtures.UNKNOWN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import my.packagename.application.command.CreateCustomCommand;
import my.packagename.application.command.RenameCustomCommand;
import my.packagename.application.query.PageQuery;
import my.packagename.domain.exception.CustomNotFoundException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomName;
import my.packagename.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomUseCaseMetricsAdaptersTest {

  private static final Custom DEMO = Custom.create(FIXED_ID, new CustomName("Demo"));

  private MeterRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
  }

  @Test
  void should_delegate_and_record_when_creating() {
    CreateCustomMetricsAdapter adapter = new CreateCustomMetricsAdapter(registry);
    adapter.setDelegate(command -> DEMO);

    assertThat(adapter.execute(new CreateCustomCommand("Demo"))).isSameAs(DEMO);
    assertThat(timerCount("create_custom")).isOne();
  }

  @Test
  void should_delegate_and_record_when_finding() {
    FindCustomMetricsAdapter adapter = new FindCustomMetricsAdapter(registry);
    adapter.setDelegate(id -> DEMO);

    assertThat(adapter.execute(FIXED_ID)).isSameAs(DEMO);
    assertThat(timerCount("find_custom")).isOne();
  }

  @Test
  void should_delegate_and_record_when_listing() {
    ListCustomsMetricsAdapter adapter = new ListCustomsMetricsAdapter(registry);
    adapter.setDelegate(query -> null);

    adapter.execute(new PageQuery(0, 1));

    assertThat(timerCount("list_customs")).isOne();
  }

  @Test
  void should_delegate_and_record_when_renaming() {
    RenameCustomMetricsAdapter adapter = new RenameCustomMetricsAdapter(registry);
    adapter.setDelegate(command -> DEMO);

    assertThat(adapter.execute(new RenameCustomCommand(FIXED_ID, "Demo"))).isSameAs(DEMO);
    assertThat(timerCount("rename_custom")).isOne();
  }

  @Test
  void should_record_failure_and_rethrow_when_delete_fails() {
    DeleteCustomMetricsAdapter adapter = new DeleteCustomMetricsAdapter(registry);
    adapter.setDelegate(
        id -> {
          throw new CustomNotFoundException(id);
        });

    assertThatThrownBy(() -> adapter.execute(UNKNOWN_ID))
        .isInstanceOf(CustomNotFoundException.class);
    assertThat(timerCount("delete_custom")).isOne();
  }

  private long timerCount(String operation) {
    return registry
        .get(MetricRecorded.TIMER)
        .tags("entity", "custom", "operation", operation)
        .timer()
        .count();
  }
}

package my.packagename.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import my.packagename.application.port.ChainablePort;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PortChainTest {

  private final List<String> trail = new ArrayList<>();

  @Test
  void should_return_core_when_there_are_no_decorators() {
    UnaryOperator<String> core = value -> value;

    assertThat(PortChain.link(core, List.of())).isSameAs(core);
  }

  @Test
  void should_call_decorators_by_ascending_order_then_core() {
    // Given — registered out of order on purpose
    UnaryOperator<String> core =
        value -> {
          trail.add("core");
          return value;
        };
    List<ChainablePort<UnaryOperator<String>>> decorators =
        List.of(new Cache(trail), new Metrics(trail));

    // When
    UnaryOperator<String> head = PortChain.link(core, decorators);
    head.apply("x");

    // Then
    assertThat(trail).containsExactly("metrics", "cache", "core");
  }

  @Order(10)
  private static final class Metrics extends Recording {
    Metrics(List<String> trail) {
      super("metrics", trail);
    }
  }

  @Order(50)
  private static final class Cache extends Recording {
    Cache(List<String> trail) {
      super("cache", trail);
    }
  }

  private abstract static class Recording
      implements UnaryOperator<String>, ChainablePort<UnaryOperator<String>> {

    private final String name;
    private final List<String> trail;
    private UnaryOperator<String> delegate;

    Recording(String name, List<String> trail) {
      this.name = name;
      this.trail = trail;
    }

    @Override
    public void setDelegate(UnaryOperator<String> delegate) {
      this.delegate = delegate;
    }

    @Override
    public String apply(String value) {
      trail.add(name);
      return delegate.apply(value);
    }
  }
}

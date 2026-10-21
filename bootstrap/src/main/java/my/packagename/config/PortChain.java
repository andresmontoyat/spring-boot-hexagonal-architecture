package my.packagename.config;

import java.util.ArrayList;
import java.util.List;
import my.packagename.application.port.ChainablePort;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * Links decorators in front of a core port implementation. Lowest {@code @Order} runs first; the
 * core (use case or real adapter) is always last.
 */
final class PortChain {

  private PortChain() {}

  @SuppressWarnings("unchecked")
  static <T> T link(T core, List<? extends ChainablePort<T>> decorators) {
    List<ChainablePort<T>> ordered = new ArrayList<>(decorators);
    AnnotationAwareOrderComparator.sort(ordered);
    T next = core;
    for (int i = ordered.size() - 1; i >= 0; i--) {
      ordered.get(i).setDelegate(next);
      next = (T) ordered.get(i);
    }
    return next;
  }
}

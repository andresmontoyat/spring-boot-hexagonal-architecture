package my.packagename.application.fake;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.exception.CustomConcurrentModificationException;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;

/** Fake with the same optimistic-locking contract as the JDBC adapter. */
public final class InMemoryCustomRepository implements CustomRepositoryPort {

  private final Map<CustomId, Custom> store = new LinkedHashMap<>();
  private int findByIdCalls;

  /** Lets decorator tests (e.g. cache) prove whether a read reached the store. */
  public int findByIdCalls() {
    return findByIdCalls;
  }

  @Override
  public Custom insert(Custom custom) {
    store.put(custom.id(), custom);
    return custom;
  }

  @Override
  public Custom update(Custom custom) {
    Custom current = store.get(custom.id());
    if (current == null || current.version() != custom.version()) {
      throw new CustomConcurrentModificationException(custom.id(), null);
    }
    Custom stored = Custom.rehydrate(custom.id(), custom.name(), custom.version() + 1);
    store.put(stored.id(), stored);
    return stored;
  }

  @Override
  public Optional<Custom> findById(CustomId id) {
    findByIdCalls++;
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PageResult<Custom> findAll(PageQuery query) {
    List<Custom> items =
        store.values().stream()
            .sorted(Comparator.comparing(custom -> custom.name().value()))
            .skip((long) query.page() * query.size())
            .limit(query.size())
            .toList();
    return new PageResult<>(items, query.page(), query.size(), store.size());
  }

  @Override
  public boolean deleteById(CustomId id) {
    return store.remove(id) != null;
  }

  @Override
  public long count() {
    return store.size();
  }
}

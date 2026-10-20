package my.packagename.domain.model;

import java.util.Objects;

/**
 * Aggregate root. A class rather than a record so the canonical constructor stays private and every
 * state change goes through {@link #rename}.
 */
public final class Custom {

  private final CustomId id;
  private final CustomName name;
  private final long version;

  private Custom(CustomId id, CustomName name, long version) {
    if (version < 0) {
      throw new IllegalArgumentException("version must be non-negative");
    }
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.name = Objects.requireNonNull(name, "name must not be null");
    this.version = version;
  }

  public static Custom create(CustomId id, CustomName name) {
    return new Custom(id, name, 0L);
  }

  /** For persistence rehydration only. */
  public static Custom rehydrate(CustomId id, CustomName name, long version) {
    return new Custom(id, name, version);
  }

  public Custom rename(CustomName newName) {
    return name.equals(newName) ? this : new Custom(id, newName, version);
  }

  public CustomId id() {
    return id;
  }

  public CustomName name() {
    return name;
  }

  /** Optimistic-lock token; incremented by the persistence adapter, never by the domain. */
  public long version() {
    return version;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Custom that && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Custom[id=" + id + ", version=" + version + "]";
  }
}

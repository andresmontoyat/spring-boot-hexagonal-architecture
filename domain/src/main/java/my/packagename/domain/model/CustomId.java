package my.packagename.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CustomId(UUID value) {

  public CustomId {
    Objects.requireNonNull(value, "value must not be null");
  }

  @Override
  public String toString() {
    return value.toString();
  }
}

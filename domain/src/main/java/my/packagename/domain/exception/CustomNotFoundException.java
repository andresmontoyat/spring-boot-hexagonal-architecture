package my.packagename.domain.exception;

import my.packagename.domain.model.CustomId;

public final class CustomNotFoundException extends DomainException {

  private final CustomId id;

  public CustomNotFoundException(CustomId id) {
    super("Custom not found with id: " + id);
    this.id = id;
  }

  public CustomId id() {
    return id;
  }
}

package my.packagename.domain.exception;

import my.packagename.domain.model.CustomId;

public final class CustomConcurrentModificationException extends DomainException {

  public CustomConcurrentModificationException(CustomId id, Throwable cause) {
    super("Custom was modified concurrently: " + id, cause);
  }
}

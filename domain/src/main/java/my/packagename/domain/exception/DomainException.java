package my.packagename.domain.exception;

/** Root of every business-rule violation. Unchecked so it never leaks into port signatures. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}

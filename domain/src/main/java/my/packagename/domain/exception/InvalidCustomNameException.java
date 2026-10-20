package my.packagename.domain.exception;

public final class InvalidCustomNameException extends DomainException {

  public InvalidCustomNameException(String reason) {
    super("Invalid custom name: " + reason);
  }
}

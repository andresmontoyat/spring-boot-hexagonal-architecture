package my.packagename.domain.model;

import my.packagename.domain.exception.InvalidCustomNameException;

public record CustomName(String value) {

  public static final int MAX_LENGTH = 20;

  public CustomName {
    if (value == null || value.isBlank()) {
      throw new InvalidCustomNameException("must not be blank");
    }
    value = value.strip();
    if (value.length() > MAX_LENGTH) {
      throw new InvalidCustomNameException("must be at most " + MAX_LENGTH + " characters");
    }
  }
}

package my.packagename.application.fake;

import java.util.UUID;
import my.packagename.domain.model.CustomId;

public final class CustomFixtures {

  public static final CustomId FIXED_ID =
      new CustomId(UUID.fromString("7f1c2a52-3c1e-4b5e-9b7a-1f2d3c4b5a69"));
  public static final CustomId UNKNOWN_ID =
      new CustomId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

  private CustomFixtures() {}
}

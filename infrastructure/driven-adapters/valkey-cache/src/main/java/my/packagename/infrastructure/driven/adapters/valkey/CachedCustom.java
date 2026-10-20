package my.packagename.infrastructure.driven.adapters.valkey;

import java.util.UUID;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;

/** Cache payload. Decoupled from the domain so a domain refactor never corrupts stored entries. */
record CachedCustom(UUID id, String name, long version) {

  static CachedCustom from(Custom custom) {
    return new CachedCustom(custom.id().value(), custom.name().value(), custom.version());
  }

  Custom toDomain() {
    return Custom.rehydrate(new CustomId(id), new CustomName(name), version);
  }
}

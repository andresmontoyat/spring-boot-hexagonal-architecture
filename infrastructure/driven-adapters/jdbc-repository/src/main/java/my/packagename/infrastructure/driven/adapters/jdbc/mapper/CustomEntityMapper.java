package my.packagename.infrastructure.driven.adapters.jdbc.mapper;

import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;
import my.packagename.infrastructure.driven.adapters.jdbc.entity.CustomEntity;

public final class CustomEntityMapper {

  private CustomEntityMapper() {}

  /** Null version tells Spring Data JDBC to INSERT and seed the optimistic-lock counter. */
  public static CustomEntity toNewEntity(Custom custom) {
    return new CustomEntity(custom.id().value(), custom.name().value(), null);
  }

  public static CustomEntity toEntity(Custom custom) {
    return new CustomEntity(custom.id().value(), custom.name().value(), custom.version());
  }

  public static Custom toDomain(CustomEntity entity) {
    return Custom.rehydrate(
        new CustomId(entity.id()), new CustomName(entity.name()), entity.version());
  }
}

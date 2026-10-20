package my.packagename.application.mapper;

import my.packagename.application.command.CreateCustomCommand;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;

public final class CommandMapper {

  private CommandMapper() {}

  public static Custom toCustom(CreateCustomCommand command, CustomId id) {
    return Custom.create(id, new CustomName(command.name()));
  }
}

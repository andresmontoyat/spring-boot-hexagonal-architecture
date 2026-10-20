package my.packagename.application.command;

import my.packagename.domain.model.CustomId;

public record RenameCustomCommand(CustomId id, String name) {}

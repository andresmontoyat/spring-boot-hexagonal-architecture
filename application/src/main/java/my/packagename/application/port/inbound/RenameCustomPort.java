package my.packagename.application.port.inbound;

import my.packagename.application.command.RenameCustomCommand;
import my.packagename.domain.model.Custom;

@FunctionalInterface
public interface RenameCustomPort {
  Custom execute(RenameCustomCommand command);
}

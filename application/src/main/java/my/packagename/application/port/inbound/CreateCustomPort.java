package my.packagename.application.port.inbound;

import my.packagename.application.command.CreateCustomCommand;
import my.packagename.domain.model.Custom;

@FunctionalInterface
public interface CreateCustomPort {
  Custom execute(CreateCustomCommand command);
}

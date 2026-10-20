package my.packagename.application.port.inbound;

import my.packagename.domain.model.CustomId;

@FunctionalInterface
public interface DeleteCustomPort {
  void execute(CustomId id);
}

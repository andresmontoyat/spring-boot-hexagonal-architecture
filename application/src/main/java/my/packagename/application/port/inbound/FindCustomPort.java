package my.packagename.application.port.inbound;

import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;

@FunctionalInterface
public interface FindCustomPort {
  /**
   * @throws my.packagename.domain.exception.CustomNotFoundException when no custom has that id
   */
  Custom execute(CustomId id);
}

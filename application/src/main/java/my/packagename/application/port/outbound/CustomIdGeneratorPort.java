package my.packagename.application.port.outbound;

import my.packagename.domain.model.CustomId;

@FunctionalInterface
public interface CustomIdGeneratorPort {
  CustomId next();
}

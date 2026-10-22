package my.packagename.application.port.inbound;

import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.result.ReportOutcome;

@FunctionalInterface
public interface GenerateCustomsReportPort {
  ReportOutcome execute(GenerateCustomsReportCommand command);
}

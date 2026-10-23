package my.packagename.application.port.outbound;

import my.packagename.domain.model.CustomsReport;

@FunctionalInterface
public interface CustomsReportRepositoryPort {

  /**
   * Stores the report unless its period already has one, in a single atomic step: two concurrent
   * callers for the same period get exactly one {@code true}.
   */
  boolean insertIfAbsent(CustomsReport report);
}

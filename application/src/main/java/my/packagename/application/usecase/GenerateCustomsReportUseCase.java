package my.packagename.application.usecase;

import my.packagename.application.command.GenerateCustomsReportCommand;
import my.packagename.application.port.inbound.GenerateCustomsReportPort;
import my.packagename.application.port.outbound.CustomRepositoryPort;
import my.packagename.application.port.outbound.CustomsReportRepositoryPort;
import my.packagename.application.port.outbound.TransactionPort;
import my.packagename.application.result.ReportOutcome;
import my.packagename.domain.model.CustomsReport;
import my.packagename.domain.model.ReportPeriod;

/**
 * Idempotent per period on purpose. The scheduler's lock keeps runs from overlapping, but a lock
 * that expires mid-run or a late instance can still execute twice; the store then keeps the first.
 */
public final class GenerateCustomsReportUseCase implements GenerateCustomsReportPort {

  private final TransactionPort<ReportOutcome> transaction;
  private final CustomRepositoryPort customs;
  private final CustomsReportRepositoryPort reports;

  public GenerateCustomsReportUseCase(
      TransactionPort<ReportOutcome> transaction,
      CustomRepositoryPort customs,
      CustomsReportRepositoryPort reports) {
    this.transaction = transaction;
    this.customs = customs;
    this.reports = reports;
  }

  @Override
  public ReportOutcome execute(GenerateCustomsReportCommand command) {
    ReportPeriod period = ReportPeriod.containing(command.firedAt());
    return transaction.run(
        () ->
            reports.insertIfAbsent(new CustomsReport(period, customs.count()))
                ? ReportOutcome.GENERATED
                : ReportOutcome.ALREADY_GENERATED);
  }
}

package my.packagename.application.result;

public enum ReportOutcome {
  GENERATED,
  /** Another run already stored this period: a duplicate execution that storage absorbed. */
  ALREADY_GENERATED
}

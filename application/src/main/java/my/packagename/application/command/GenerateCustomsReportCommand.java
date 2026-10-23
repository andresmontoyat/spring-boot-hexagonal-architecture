package my.packagename.application.command;

import java.time.Instant;

/** {@code firedAt} is when the trigger fired; the report covers the minute that contains it. */
public record GenerateCustomsReportCommand(Instant firedAt) {}

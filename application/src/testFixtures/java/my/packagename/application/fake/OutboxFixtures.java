package my.packagename.application.fake;

import io.codehunters.messaging.factory.EventMessageFactory;
import io.codehunters.messaging.factory.OutboxEventFactory;
import io.codehunters.messaging.factory.TraceIdSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class OutboxFixtures {

  public static final String ORIGIN = "custom-service";
  public static final Instant NOW = Instant.parse("2026-10-20T12:00:00Z");

  private OutboxFixtures() {}

  /** The SDK's real factory with a pinned clock and no tracing. */
  public static OutboxEventFactory eventFactory() {
    return new OutboxEventFactory(
        new EventMessageFactory(ORIGIN, TraceIdSource.none(), Clock.fixed(NOW, ZoneOffset.UTC)));
  }
}

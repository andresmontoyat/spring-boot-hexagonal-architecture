---
status: accepted
date: 2026-10-20
decision-makers: [andresmontoyat]
---

# 0005. Transactional outbox through the io.codehunters event-messaging SDK, on Kafka

## Context and Problem Statement

Customs changes must be published as integration events without dual writes: a database commit
and a broker send cannot be atomic, so either events get lost or phantom events appear.

## Decision Drivers

- Event written in the same local transaction as the business change.
- Per-aggregate ordering and at-least-once delivery with a consumer dedup key.
- Do not re-implement claim, retry, purge and tracing per service.

## Considered Options

- A — `io.codehunters:sdk-event-messaging-{core,jdbc,kafka}` 9.1.0 (platform reference
  implementation).
- B — Hand-written outbox following the platform messaging guide.
- C — Debezium CDC.

## Decision Outcome

Chosen option: **A** with Kafka. Use cases write through `OutboxWriter` + `OutboxEventFactory`
(framework-free `-core`); `-jdbc` and `-kafka` live only in `bootstrap`. Events are keyed by the
custom id; the optimistic version check makes each use case the single writer of its key.

### Consequences

- **Good** — dispatch on commit, claim with `SKIP LOCKED`, backoff, purge, `traceparent` and
  `outbox-message-id` headers come from the SDK.
- **Bad** — the SDK is published on GitHub Packages (`Codehunters-IO/java-sdk`): building needs
  `GH_PACKAGES_USERNAME` / `GH_PACKAGES_TOKEN`. Anyone without access cannot build this public repo.
- **Neutral** — the SDK's reference DDL is copied into this changelog and owned from here on.

### Confirmation

Use case tests assert the event is saved inside the transaction and never when the command is
rejected; ArchUnit forbids the dispatcher ports in `application`; `OutboxPublishingAcceptanceTest`
follows a POST to the Kafka record.

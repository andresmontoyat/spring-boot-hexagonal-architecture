---
status: accepted
date: 2026-10-20
decision-makers: [andresmontoyat]
---

# 0006. Fail-open Valkey cache behind a circuit breaker; no breaker on the outbox publisher

## Context and Problem Statement

Reads by id go through a Valkey cache-aside decorator. When Valkey is down or slow, every request
would wait for the command timeout before falling back. Separately, it is tempting to put the same
breaker on the broker publisher.

## Decision Drivers

- Serving from PostgreSQL is always correct; the cache only saves latency.
- An outage of an optional dependency must not add latency to every request.
- Outbox retries must not burn attempts during a broker outage.

## Considered Options

- A — Fail-open cache, Resilience4j breaker `valkey` around Valkey I/O, no breaker on the publisher.
- B — Fail-closed cache (return 503 when Valkey is down).
- C — Breaker on both cache and publisher.

## Decision Outcome

Chosen option: **A**. `ResilientValkey` runs each command through the breaker and degrades to the
database. The breaker stays out of readiness. The outbox keeps only the SDK's exponential backoff: a
breaker there would fail each claimed row instantly, consume its attempts and turn rows `FAILED`,
blocking their key.

### Consequences

- **Good** — Valkey outages cost one fast database read, counted in `cache.errors` /
  `cache.short_circuited`.
- **Bad** — up to the TTL (10 min) of staleness if both evictions are lost.

### Confirmation

`ResilientValkeyTest` opens the breaker against an unreachable Valkey; `CustomCacheAdapterFailOpenTest`
and `CustomCacheAdapterIT` cover fallback, eviction and corrupt entries.

---
status: accepted
date: 2026-10-22
decision-makers: [andresmontoyat]
---

# 0007. Scheduled jobs: programmatic ShedLock lock, plus work that is idempotent per period

## Context and Problem Statement

The service runs as several instances, and every instance fires every `@Scheduled` trigger. The
first job, `customs-report`, stores a snapshot of the customs count each minute. Without
coordination, each instance writes its own snapshot for the same minute. A distributed lock reduces
that to one run, but a lock alone does not guarantee one run:

- if the lock expires (`lockAtMostFor`) while the holder is still working, a second instance takes
  it and the two runs overlap;
- if a fast run releases the lock at once, an instance whose trigger fires a moment later finds it
  free and repeats the tick.

## Decision Drivers

- One report per period, whatever the number of instances.
- Reuse what the service already has: PostgreSQL, the `shedlock` table and the JDBC `LockProvider`
  the outbox SDK registers.
- Keep the job testable and its lock windows visible as typed configuration.
- Never let our jobs share a scheduler thread with the outbox SDK.

## Considered Options

- A — Programmatic ShedLock in the job adapter, idempotent use case (unique key per period).
- B — `@SchedulerLock` annotation with `@EnableSchedulerLock`.
- C — Lock only, no idempotency.
- D — Idempotency only, no lock.

## Decision Outcome

Chosen option: **A**.

- `CustomsReportJob` (entry point, module `scheduler`) calls `LockProvider.lock(...)` with
  `CustomsReportJobProperties`. It runs the use case only if it gets the lock, logs the skip
  otherwise, and always unlocks. This is the same programmatic style as the outbox SDK.
- The lock name is per service (`customs-report-<app>`); the lock uses database time
  (`usingDbTime`), so clock skew between pods does not shorten or stretch it.
- `lockAtMostFor` (2 m) is set well above the slowest run. `lockAtLeastFor` (30 s) absorbs jitter
  and stays below the one-minute period, so the next tick is never skipped.
- The use case writes with `INSERT … ON CONFLICT (period_start) DO NOTHING`. Whatever gets past the
  lock is absorbed by the primary key and counted in `customs.report.outcomes{outcome=already_generated}`.
- Jobs run on their own one-thread `jobTaskScheduler`, so a slow run delays the next fire on its
  instance rather than overlapping with it.

B was rejected because the annotation needs an AOP proxy, keeps the windows in strings, and hides
the skip. C fails both scenarios in the context section. D does duplicate work and adds row
contention on every tick.

### Consequences

- **Good** — every instance can run the cron. There is no leader election, and losing the holder
  costs at most `lockAtMostFor`.
- **Good** — duplicate runs are measurable. Alert on any `already_generated`.
- **Bad** — each job needs its own idempotency key; the lock alone is never the guarantee.
- **Bad** — a run longer than `lockAtMostFor` still overlaps. For long jobs, use
  `KeepAliveLockProvider` or split the work.

### Confirmation

`CustomsReportJobConcurrencyIT` (real PostgreSQL `shedlock` table, one lock provider per simulated
instance) reproduces each problem and its fix:

| Test | Shows |
|---|---|
| `without_a_lock_every_instance_runs_the_same_tick_at_the_same_time` | N instances → N concurrent runs |
| `with_shedlock_one_instance_runs_the_tick_and_the_others_skip` | N instances → 1 run |
| `a_lock_that_expires_before_the_run_ends_lets_a_second_instance_overlap` | `lockAtMostFor` too short → 2 runs overlap |
| `a_lock_longer_than_the_run_keeps_the_second_instance_out` | the fix |
| `a_fast_run_that_releases_at_once_lets_a_late_instance_repeat_the_tick` | `lockAtLeastFor = 0` → same tick twice |
| `lock_at_least_for_keeps_a_late_instance_out_after_a_fast_run` | the fix |

`CustomsReportRepositoryAdapterIT` and `CustomsReportJobAcceptanceTest` prove that concurrent
writers for one period store exactly one report.

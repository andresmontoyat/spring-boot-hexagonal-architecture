---
status: accepted
date: 2026-10-20
decision-makers: [andresmontoyat]
---

# 0003. Spring Data JDBC with Liquibase instead of JPA with ddl-auto

## Context and Problem Statement

The template used JPA entities with `ddl-auto: update` and an `EntityManager`-based mapper. The
schema was whatever Hibernate generated, and the persistence model leaked lazy-loading and
session semantics into adapters.

## Decision Drivers

- Schema under version control, forward-only, reviewable.
- Explicit insert vs update and optimistic locking without session magic.
- Integration tests against the real database.

## Considered Options

- A — Spring Data JDBC (`JdbcAggregateOperations` insert/update, `@Version`) + Liquibase.
- B — Keep JPA, add Liquibase.

## Decision Outcome

Chosen option: **A**. The adapter maps records to an immutable aggregate, maps
`OptimisticLockingFailureException` to a domain exception (HTTP 409), and the outbox DDL lives in the
same changelog (ADR 0005).

### Consequences

- **Good** — no hidden queries; version check is the single-writer guarantee for event ordering.
- **Bad** — relationships must be modelled explicitly (fine for aggregate-sized writes).

### Confirmation

`CustomRepositoryAdapterIT` and `JdbcTransactionAdapterIT` on PostgreSQL 17 (Testcontainers),
including a stale-version update and a rollback.

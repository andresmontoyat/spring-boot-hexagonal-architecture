---
status: accepted
date: 2026-10-20
decision-makers: [andresmontoyat]
---

# 0001. Hexagonal Gradle modules with a decorator chain per port

## Context and Problem Statement

The original template split code into `domain/model`, `domain/service`, `application/api` and
`infrastructure/*`, but the domain depended on Spring (`@Service`), Lombok `@Data` made entities
mutable, and nothing stopped a layer from reaching outward. Cross-cutting concerns (metrics,
cache) had no defined home.

## Decision Drivers

- Dependencies must point inward and be enforced at build time, not by review.
- Use cases must stay framework-free and unit-testable with fakes.
- Cross-cutting concerns must be addable without editing use cases or adapters.

## Considered Options

- A — Gradle modules `domain`, `application`, `infrastructure/{entry-points,driven-adapters}/*`,
  `bootstrap`; ArchUnit rules; decorators linked per port by `@Order`.
- B — Single module with packages and ArchUnit only.
- C — Spring Modulith.

## Decision Outcome

Chosen option: **A**. Module boundaries make illegal imports fail compilation; ArchUnit covers what
modules cannot (ports are interfaces, use cases implement an inbound port, adapters never name a
use case). `bootstrap` is the only place that instantiates use cases; `PortChain` links
`ChainablePort` decorators (metrics `@Order(10)`, cache `@Order(50)`) in front of the core.

### Consequences

- **Good** — framework-free `domain`/`application`; tests run without Spring.
- **Good** — metrics and cache were added without touching a use case.
- **Bad** — more modules and build files than a single-module service.
- **Neutral** — `ChainablePort` has no Spring `Ordered`; order comes from `@Order` on the adapter.

### Confirmation

`HexagonalArchitectureTest` (10 rules) and `PortChainTest`; the Karate suite asserts the chain is
wired by checking the meters it produces.

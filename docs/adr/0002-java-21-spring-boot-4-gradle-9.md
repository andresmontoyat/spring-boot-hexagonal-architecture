---
status: accepted
date: 2026-10-20
decision-makers: [andresmontoyat]
---

# 0002. Java 21, Spring Boot 4 and Gradle 9 (Kotlin DSL)

## Context and Problem Statement

The template targeted Java 8, Spring Boot 2.3.3, Gradle 6.5 (Groovy) and resolved from the
shut-down JCenter; Springfox no longer works on current Spring. Every one of these is a major
version jump, which the platform requires to be recorded.

## Decision Drivers

- Supported, patched runtime and framework.
- Records, sealed types and pattern matching for immutable models.
- Platform baseline: Java 21 LTS, Spring Boot 4.0.x, Gradle 9.x.

## Considered Options

- A — Java 21 toolchain, Spring Boot 4.0.8, Gradle 9.7.1 Kotlin DSL, Jackson 3, Testcontainers 2.
- B — Stop at Spring Boot 3.5 / Java 17.

## Decision Outcome

Chosen option: **A**, the platform baseline. Springfox is replaced by springdoc 3.0.x, JPA by
Spring Data JDBC (ADR 0003), MapStruct/Lombok by records and static mappers.

### Consequences

- **Good** — supported stack; `-Werror` with all lint warnings enabled keeps deprecations visible.
- **Bad** — Boot 4 renamed modules and packages (`spring-boot-starter-webmvc`, test slices,
  `spring-boot-starter-security-oauth2-resource-server`); upgrades must track them.
- **Neutral** — Jackson 3 (`tools.jackson`) and `JsonMapper` everywhere.

### Confirmation

The toolchain is pinned in `build.gradle.kts`; `./gradlew check` runs tests, coverage (≥80% lines
per module), Spotless and ArchUnit.

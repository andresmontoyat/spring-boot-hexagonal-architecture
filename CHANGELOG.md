# Changelog

All notable changes to this project are documented here. Format: [Keep a Changelog](https://keepachangelog.com/),
versioning: [SemVer](https://semver.org/).

## [Unreleased] — 2.0.0

### Changed (breaking)

- Java 8 / Spring Boot 2.3 / Gradle 6 → Java 21 / Spring Boot 4.0.8 / Gradle 9.7.1 Kotlin DSL.
- New module layout: `domain`, `application`, `infrastructure/{entry-points,driven-adapters}/*`, `bootstrap`.
- JPA with `ddl-auto` → Spring Data JDBC with Liquibase migrations.
- Springfox → springdoc; errors are RFC 9457 problem details.
- Resource path `/api/v1/operations` → `/api/v1/customs`; ids are UUIDs; responses carry `version`.
- Every endpoint requires a Keycloak access token with the `platform-reader` or `platform-writer` role.

### Added

- Immutable domain model, use cases behind inbound ports, `TransactionPort`.
- Transactional outbox publishing `CustomCreated` / `CustomRenamed` / `CustomDeleted` to Kafka.
- Valkey cache-aside decorator with a Resilience4j circuit breaker (fail-open).
- Use case metrics decorators; OTLP export of metrics and traces.
- KrakenD gateway (JWT, roles, rate limit, CORS) and a Keycloak realm as code.
- ArchUnit rules, Testcontainers integration tests, Karate BDD suite, gateway E2E test.
- Local stack in `compose.yaml`; ADRs 0001–0006.

### Fixed

- Create/update ignored bean validation (inverted `hasErrors()` check).
- `isNew()` was inverted on the persistence base entity.
- `findById` threw `NoSuchElementException` instead of 404.

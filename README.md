# spring-boot-hexagonal-architecture

Reference service for hexagonal architecture (ports and adapters) on **Java 21** and **Spring Boot 4**:
a small `Custom` CRUD that shows the whole production path.

| Concern | How |
|---|---|
| Architecture | Gradle modules per layer, ArchUnit rules, decorator chain per port ([ADR 0001](docs/adr/0001-hexagonal-modules-and-decorator-chain.md)) |
| Persistence | Spring Data JDBC + Liquibase on PostgreSQL, optimistic locking ([ADR 0003](docs/adr/0003-spring-data-jdbc-and-liquibase.md)) |
| Events | Transactional outbox → Kafka via the `io.codehunters` SDK ([ADR 0005](docs/adr/0005-transactional-outbox-with-codehunters-sdk-on-kafka.md)) |
| Cache | Valkey cache-aside, fail-open, Resilience4j circuit breaker ([ADR 0006](docs/adr/0006-fail-open-valkey-cache-with-circuit-breaker.md)) |
| Security | Keycloak tokens checked by KrakenD at the edge and again by the service ([ADR 0004](docs/adr/0004-keycloak-resource-server-behind-krakend.md)) |
| Observability | Use case metrics as decorators; metrics and traces exported over OTLP |
| Errors | RFC 9457 problem details with a stable `code` |
| Tests | TDD unit tests with fakes, Testcontainers ITs, Karate BDD acceptance suite, gateway E2E |

## Architecture

```mermaid
flowchart LR
  client([Client]) -->|Bearer JWT| gw[KrakenD :8000]
  kc[(Keycloak)] -. JWKS .-> gw
  kc -. JWKS .-> rest
  gw --> rest[rest-controller<br/>@PreAuthorize]
  subgraph service [custom-service]
    rest --> m[metrics decorator] --> uc[use cases<br/>application]
    uc --> dom[domain]
    uc --> c[valkey-cache decorator] --> jdbc[jdbc-repository]
    uc --> ob[OutboxWriter]
  end
  c --> valkey[(Valkey)]
  jdbc --> pg[(PostgreSQL)]
  ob --> pg
  pg -. dispatch after commit .-> kafka[(Kafka)]
```

| Module | Layer | Depends on |
|---|---|---|
| `domain` | Aggregate `Custom`, value objects, domain exceptions | nothing |
| `application` | Use cases, inbound/outbound ports, commands, integration events | `domain`, `sdk-event-messaging-core` |
| `infrastructure/entry-points/rest-controller` | REST API, security, problem details | `application`, `domain` |
| `infrastructure/driven-adapters/jdbc-repository` | PostgreSQL adapter, transaction adapter, Liquibase | `application`, `domain` |
| `infrastructure/driven-adapters/valkey-cache` | Cache decorator + circuit breaker | `application`, `domain` |
| `infrastructure/driven-adapters/metrics-publisher` | Use case metrics decorators | `application`, `domain` |
| `bootstrap` | Spring Boot app, wiring, `application.yml`, acceptance tests | all |

## Prerequisites

- JDK 21 (Gradle provisions the toolchain), Docker.
- Access to the Codehunters SDK on GitHub Packages:

  ```bash
  export GH_PACKAGES_USERNAME=<github-user>
  export GH_PACKAGES_TOKEN=<token with read:packages>
  ```

## Build and test

```bash
./gradlew check          # unit + integration + acceptance tests, coverage ≥80%, Spotless, ArchUnit
./gradlew spotlessApply  # format
```

Integration and acceptance tests start PostgreSQL, Valkey, Kafka, Keycloak and KrakenD with
Testcontainers; nothing else needs to be running.

## Run locally

```bash
./gradlew :bootstrap:bootRun   # starts compose.yaml (PostgreSQL, Valkey, Kafka, Keycloak, KrakenD, Grafana LGTM)
```

| URL | What |
|---|---|
| http://localhost:8000/api/v1/customs | API through KrakenD |
| http://localhost:8080/custom-service/api/v1/customs | API directly (still requires a token) |
| http://localhost:8080/custom-service/swagger-ui.html | OpenAPI UI (disabled in `prod`) |
| http://localhost:8081/actuator/health | Actuator (separate port) |
| http://localhost:8180 | Keycloak (`admin` / `admin`, realm `customs`) |
| http://localhost:3001 | Grafana: metrics, traces, logs |

Get a token and call the API (lab secrets from `compose.yaml`; override them in `.env`):

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/customs/protocol/openid-connect/token \
  -d grant_type=client_credentials -d client_id=customs-writer-bot -d client_secret=local-writer-secret \
  | jq -r .access_token)

curl -i -X POST http://localhost:8000/api/v1/customs \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"name":"Demo"}'
```

| Caller | Role | Allowed |
|---|---|---|
| `customs-writer-bot`, user `alice` | `platform-writer` | read, create, rename, delete |
| `customs-reader-bot`, user `bob` | `platform-reader` | read |
| `customs-web` | public client, Authorization Code + PKCE | browser login |

## API

| Method | Path | Role | Success |
|---|---|---|---|
| `GET` | `/api/v1/customs?page=&size=` | reader | 200, page envelope |
| `GET` | `/api/v1/customs/{id}` | reader | 200 |
| `POST` | `/api/v1/customs` | writer | 201, `Location: customs/{id}` |
| `PUT` | `/api/v1/customs/{id}` | writer | 200 |
| `DELETE` | `/api/v1/customs/{id}` | writer | 204 |

Errors: `400 VALIDATION_FAILED`, `401 UNAUTHENTICATED`, `403 ACCESS_DENIED`, `404 CUSTOM_NOT_FOUND`,
`409 CUSTOM_CONCURRENT_MODIFICATION`, `422 CUSTOM_INVALID_NAME`.

Events on topic `customs.custom.events`, keyed by custom id: `CustomCreated`, `CustomRenamed`,
`CustomDeleted`. Consumers deduplicate on the `outbox-message-id` header.

## Configuration

One `bootstrap/src/main/resources/application.yml`; every value is `${ENV_VAR:default}` named after
its property path (`spring.data.redis.port` → `SPRING_DATA_REDIS_PORT`). Notable groups:
`codehunters.outbox.*`, `resilience4j.circuitbreaker.instances.valkey.*`, `cache.custom.*`,
`security.keycloak.*`, `spring.security.oauth2.resourceserver.jwt.*`, `management.otlp.*`.

## Decisions

See [docs/adr](docs/adr).

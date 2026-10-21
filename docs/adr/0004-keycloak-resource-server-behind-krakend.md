---
status: accepted
date: 2026-10-20
decision-makers: [andresmontoyat]
---

# 0004. Keycloak tokens validated at the KrakenD edge and again by the service (Topology B)

## Context and Problem Statement

The API must be protected with Keycloak and fronted by KrakenD. The platform offers two topologies:
A — the gateway validates and forwards trusted `x-user-*` headers; B — every service is an OAuth2
resource server. Topology A is only safe when the service can never be reached except through the
gateway (NetworkPolicy / mesh). This repository is a public template run with Docker Compose, where
the service port is reachable directly.

## Decision Drivers

- Safe even when the network boundary is not guaranteed.
- Reject bad callers as early as possible.
- One consistent role model: client roles `platform-reader` / `platform-writer`.

## Considered Options

- A — Gateway-first with trusted headers.
- B — Resource server in the service; gateway also validates and forwards the token unchanged.

## Decision Outcome

Chosen option: **B**. KrakenD validates signature, issuer, audience and per-endpoint roles, rate
limits per client and handles CORS; the service re-validates the same token, maps the client roles
to `ROLE_READER` / `ROLE_WRITER` and enforces `@PreAuthorize` on the controller. No identity headers
are trusted.

### Consequences

- **Good** — the service is self-defending; a direct call without a token gets 401.
- **Good** — roles are enforced twice (edge and service), as defense in depth.
- **Bad** — two places hold the role-per-endpoint rule (`endpoints.json` and the controller); the
  gateway acceptance test keeps them in step.
- **Neutral** — switching to Topology A later needs a superseding ADR and a NetworkPolicy.

### Confirmation

`CustomControllerSecurityTest` (anonymous, unverifiable token, wrong role, no role),
`KeycloakJwtAuthenticationConverterTest`, the Karate 401/403 scenarios against a real Keycloak, and
`GatewaySecurityAcceptanceTest` with the committed KrakenD config.

# US-040 Milestone Dispute API Docs Sync

## Status

implemented

## Lane

normal

## Product Contract

The Milestone Dispute v2 backend surface from `SPEC-MILESTONE-DISPUTER.md`
must be discoverable and testable through Swagger/OpenAPI-facing docs. The
runtime Swagger inventory, static OpenAPI snapshot, overview, Swagger test
guide, and README API list must include the new milestone escrow, progress
report, dispute settlement, termination request, case attachment, and
contract-review routes.

## Relevant Product Docs

- `SPEC-MILESTONE-DISPUTER.md`
- `README.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/openapi/openapi-v1.json`
- `docs/product/contract-management.md`

## Acceptance Criteria

- Runtime `/v3/api-docs` exposes the current Milestone Dispute v2 API routes.
- `docs/openapi/openapi-v1.json` is regenerated from runtime Swagger.
- `docs/swagger-api-overview.md` lists all runtime operations and calls out the
  US-040 Milestone Dispute v2 contract flow.
- `docs/swagger-api-test-guide.md` includes every runtime operation with params,
  body schema hints, and a Milestone Dispute v2 smoke flow.
- README API inventory includes the newly exposed v2 routes.
- Harness durable matrix records proof for the docs/API synchronization story.

## Design Notes

- Commands: runtime Swagger was fetched from `http://localhost:8081/v3/api-docs`.
- Queries: `scripts/bin/harness-cli query matrix --numeric`,
  `scripts/bin/harness-cli query traces`, and `scripts/bin/harness-cli query
  tools --capability ... --status present`.
- API: public Swagger inventory is now 170 operations.
- Tables: no schema changes in this story; existing V49/V50 migrations were
  validated by runtime boot.
- Domain rules: docs reflect Staff decision and settlement as separate steps,
  termination request lifecycle, single-release escrow guard, and reviews only
  after `CLOSED`.
- UI surfaces: Swagger UI grouping now places termination request, case
  attachment, and admin dispute operations under Contract Execution Flow.

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `.\mvnw.cmd -DskipTests compile` passes after Swagger tag mapping update. |
| Integration | Runtime Spring Boot on port 8081 returns `/v3/api-docs`; Flyway validates 50 migrations and reports schema up to date. |
| E2E | Not run; this is documentation/API inventory sync, not a browser user flow. |
| Platform | `docs/openapi/openapi-v1.json` parses and overview/test guide each list 170 operations. |
| Release | Not run. |

## Harness Delta

Created durable story US-040 for the documentation and Swagger synchronization
work that closes the remaining public-contract gap after traces #58-#60
completed the backend implementation.

## Evidence

- `docker compose ps` showed `aitasker-postgres` and `redis-otp` running.
- `.\mvnw.cmd -DskipTests compile` passed after `OpenApiConfig` tag mapping.
- Runtime boot on `SERVER_PORT=8081` returned `GET /v3/api-docs` with HTTP 200.
- Runtime boot log shows Flyway successfully validated 50 migrations and schema
  was up to date.
- `docs/openapi/openapi-v1.json` was regenerated from runtime `/v3/api-docs`.
- OpenAPI parse check passed:
  `Get-Content docs\openapi\openapi-v1.json -Raw | ConvertFrom-Json | Out-Null`.
- Route-count checks passed:
  `docs/swagger-api-overview.md` has 170 endpoint rows and
  `docs/swagger-api-test-guide.md` has 170 endpoint sections.
- Harness trace: #61.

# US-025 Sync Swagger API Inventory And DB Reference

## Status

implemented

## Lane

normal

## Product Contract

Swagger/OpenAPI, the Swagger overview/test guide, and the DB API reference
document must reflect the current controller source, security exposure, and
Flyway-backed database/runtime contract.

## Relevant Product Docs

- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Acceptance Criteria

- Swagger security metadata matches current public vs protected routes for the
  API surface touched by `SecurityConfig`.
- Swagger overview and Swagger test guide list the current API inventory from
  controller source, including newly added or previously omitted routes.
- `C:\Learning\School\Summer2026\SWP391\DB\AITASKER_BE_API_Database_Reference.docx`
  is updated to match the current API/database contract.

## Design Notes

- Commands: `rg` route inventory, focused `mvnw` compile/test, bundled Python for
  `.docx` edits.
- Queries: compare controller mappings with `SecurityConfig` public matchers and
  Flyway migration inventory.
- API: REST controllers under `src/main/java/com/aitasker/be/controller` plus
  `HealthController`.
- Tables: refresh DB reference sections around wallet/quota/recommendation and
  active/legacy notes.
- Domain rules: `/api/v1/jobs/my` must remain protected; public job detail and
  public job milestone routes must stay numeric-id scoped only.
- UI surfaces: Swagger UI route auth indicators and grouped endpoint inventory.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Route matcher regression test for public job routes. |
| Integration | N/A without Docker-backed Postgres/runtime boot in this environment. |
| E2E | N/A |
| Platform | `mvnw -DskipTests compile` and rendered `.docx` visual QA if LibreOffice is available. |
| Release | N/A |

## Harness Delta

Keep Swagger/docs inventory synchronized with controller source and security
matchers when public route exposure changes.

## Evidence

- `.\\mvnw.cmd "-Dtest=BusinessProfileRouteMatcherTest,PublicJobRouteMatcherTest" test`
  passed: 12 tests, 0 failures, 0 errors on 2026-06-23.
- `.\\mvnw.cmd -DskipTests compile` passed on 2026-06-23.
- `docs/swagger-api-overview.md` and `docs/swagger-api-test-guide.md` were
  regenerated from current controller source plus `SecurityConfig`.
- `C:\\Learning\\School\\Summer2026\\SWP391\\DB\\AITASKER_BE_API_Database_Reference.docx`
  was rebuilt to match current API/database contract.
- Runtime Swagger UI and rendered DOCX PNG QA were not completed because Docker
  Desktop/Postgres and LibreOffice `soffice` were unavailable in this
  environment.

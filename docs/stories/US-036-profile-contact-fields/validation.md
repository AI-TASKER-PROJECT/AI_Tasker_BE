# Validation

## Proof Strategy

Use focused service tests to prove every requested read path now enriches the
correct transient fields. Compile and full-suite checks provide broader
regression confidence. OpenAPI/docs checks prove Swagger-facing surfaces list
the fields and remain parseable.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | ProfileService read enrichment for Business current/by-id/by-job/list and Expert current/by-id/list. |
| Integration | Full Maven test suite when local Docker/Postgres is available. |
| E2E | Manual Swagger/Postman guide steps for profile read APIs. |
| Platform | OpenAPI JSON parse and runtime Swagger smoke when backend is running. |
| Performance | Not applicable. |
| Logs/Audit | No new read audit event expected. |

## Fixtures

Mockito repository fixtures with deterministic account/profile IDs and account
contact fields.

## Commands

```text
command: .\mvnw.cmd -Dtest=ProfileServiceTest test
result: passed, 21 tests, 0 failures, 0 errors
notes: Focused proof for all requested profile read enrichment paths.

command: Get-Content docs/openapi/openapi-v1.json -Raw | ConvertFrom-Json | Out-Null
result: passed
notes: Checked-in OpenAPI JSON remains parseable after schema field update.

command: .\mvnw.cmd -DskipTests compile
result: passed
notes: Compile goal completed successfully.

command: .\mvnw.cmd test
result: passed, 169 tests, 0 failures, 0 errors
notes: Full suite passed; Flyway validated 44 migrations against local PostgreSQL.

command: Invoke-RestMethod http://localhost:8081/v3/api-docs and compare runtime operations to docs
result: passed
notes: Runtime OpenAPI returned 134 operations; overview and Swagger test guide each list 134 matching operations. Runtime schemas include Business `fullName,email,phone` and Expert `fullName,email,phone,title`.
```

## Acceptance Evidence

Focused proof passed on 2026-06-30:

- `ProfileServiceTest` covers Business profile contact fields for `/me`,
  `/{businessId}`, `/by-job/{jobId}`, and list reads.
- `ProfileServiceTest` covers Expert profile contact fields for `/me`,
  `/{expertId}`, and list reads.
- `docs/openapi/openapi-v1.json` parses successfully after adding schema
  fields.

Full Maven suite and runtime Swagger smoke passed in this task. Completed
Harness trace: #48.

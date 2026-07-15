# Validation

## Proof Strategy

Prove source selection validation, upload authorization/delegation, additive
field persistence, focused contract regression coverage, Flyway/JPA alignment,
and documented OpenAPI shape.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | URL-only, file-only, missing both, upload delegation and path |
| Integration | Flyway V59 and Hibernate validation on PostgreSQL |
| E2E | Runtime OpenAPI exposes upload route and both fields |
| Platform | Not applicable; backend-only change |
| Performance | ZIP capped at 50 MB; no extraction |
| Logs/Audit | Successful upload records milestone audit action |

## Fixtures

- Approved Expert account assigned to an ACTIVE contract.
- IN_PROGRESS milestone with both NDA signatures.
- Mock multipart ZIP and mocked Firebase storage response.

## Commands

```text
command: .\mvnw.cmd -DskipTests compile
result: PASS - 249 source files compiled, BUILD SUCCESS.
notes: Confirms controller/service/entity/migration-facing code compiles.

command: .\mvnw.cmd "-Dtest=ContractExecutionServiceTest,FirebaseStorageServiceTest" test
result: PASS - 80 tests, 0 failures, 0 errors.
notes: Covers URL/file selection, path ownership, upload delegation/audit, ZIP extension/MIME/signature, and 50 MB cap.

command: .\scripts\bin\harness-cli.exe story verify US-061
result: PASS - focused 80-test verification command.
notes: Durable story verification recorded.

command: .\mvnw.cmd test
result: PASS - 347 tests, 0 failures, 0 errors.
notes: After starting Docker Desktop, PostgreSQL 16.14 accepted connections; Flyway validated/applied all 59 migrations and Hibernate/JPA initialized successfully.

command: runtime GET http://localhost:8082/v3/api-docs and inventory comparison
result: PASS - runtime OpenAPI 185 operations; overview 185 rows; Swagger guide 185 sections; Postman guide 185 sections.
notes: Runtime snapshot contains the upload path and sourceCodeFileUrl in DeliverableEntity, MilestoneProgressReportEntity, and ProgressReportRequest.

command: git diff --check
result: PASS - no whitespace errors; line-ending conversion warnings only.
notes: No formatting blocker.
```

## Acceptance Evidence

- URL-only and ZIP-only final deliverables are supported; missing both is rejected.
- Uploaded storage paths are bound to the current milestone and Expert account.
- ZIP validation checks extension, supported MIME, ZIP signature, and 50 MB cap.
- Progress reports preserve optional `sourceCodeFileUrl` without turning progress
  tracking into final-deliverable validation.
- Migration `V59__milestone_source_code_file.sql` additively adds both nullable
  columns.
- Docker-backed full suite passes 347 tests; Flyway validates/applies 59
  migrations and Hibernate validates the entity mapping.
- Runtime OpenAPI and product/API docs are synchronized at 185 operations.
- Completed detailed Harness trace: `#108`.

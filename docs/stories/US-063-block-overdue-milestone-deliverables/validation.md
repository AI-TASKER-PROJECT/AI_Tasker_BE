# Validation

## Expected Proof

| Layer | Proof |
| --- | --- |
| Unit | Deliverable and upload deadline guards, including no side effects |
| Regression | Full Maven suite |
| Contract | Product docs and OpenAPI descriptions state the deadline rule |
| Static | Harness story verification and `git diff --check` |

## Evidence

- `.\mvnw.cmd "-Dtest=ContractExecutionServiceTest" test` passed 79 tests,
  0 failures, and 0 errors.
- The focused suite proves a before-deadline submission and upload remain
  allowed, missing deadline data preserves existing behavior, an expired
  `IN_PROGRESS` snapshot is rejected, persisted `OVERDUE` is rejected, late
  resubmission is rejected, and no deliverable/upload/notification/audit side
  effects occur on the new failure paths.
- `.\mvnw.cmd test` passed 354 tests, 0 failures, and 0 errors.
- PostgreSQL 16.14 startup validated all 61 Flyway migrations and Hibernate
  initialized successfully; no migration was added.
- Runtime OpenAPI on port 8082 exposed 185 operations and includes the deadline
  contract for both deliverable submission and ZIP upload.
- `.\scripts\bin\harness-cli.exe story verify US-063` passed the focused
  79-test command.
- `git diff --check` passed with line-ending notices only.

# Validation

## Proof Strategy

Run the full Maven test suite against the local Docker-backed test context and
record pass/fail in the Harness story.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Contract signature to `PENDING`, rejection to `CANCELLED`/job `OPEN`, completion to `COMPLETED`/job `CLOSED`, deposit to `ACTIVE`/job `IN_PROGRESS`, disabled change request flow. |
| Integration | Flyway migration applies through application context tests. |
| E2E | Not run in this change. |
| Platform | `docker compose up -d` if database is needed. |
| Performance | Not applicable. |
| Logs/Audit | Existing audit calls remain covered by service paths. |

## Fixtures

Mockito service fixtures plus Docker PostgreSQL for context tests.

## Commands

```text
docker compose up -d
.\mvnw.cmd test
```

## Acceptance Evidence

2026-06-20:

- `docker compose up -d` passed; `aitasker-postgres` and `redis-otp` were
  running.
- First `.\mvnw.cmd test` run failed with one unit test setup issue in the new
  disabled change-request path.
- After moving the disabled-flow error before auth-dependent checks, the second
  `.\mvnw.cmd test` run passed: 55 tests, 0 failures, 0 errors, 0 skipped.

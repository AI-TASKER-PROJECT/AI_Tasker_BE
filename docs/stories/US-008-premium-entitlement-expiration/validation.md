# Validation

## Proof Strategy

Run the full Maven test suite and Harness story verification. Include focused
unit tests for Premium extension and active package fallback, plus a migration
test for the backfill/drop behavior.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Premium then lower tier preserves Premium; repeated Premium extends cumulatively; expired Premium with active Plus reports Plus and `premiumActive=false`; quota response excludes the old flag. |
| Integration | Migration SQL backfills `premium_expired_at` and drops the old flag in a temporary PostgreSQL schema; Spring context/Flyway still loads. |
| E2E | Not automated in this slice. |
| Platform | Docker PostgreSQL/Redis used by the Maven suite. |
| Performance | Not in scope. |
| Logs/Audit | Existing purchase audit behavior remains. |

## Fixtures

- Business account with approved status.
- Membership packages for Standard, Plus, and Premium.
- User quota rows with deterministic expiration timestamps.
- Temporary PostgreSQL schema for migration backfill validation.

## Commands

```text
docker compose up -d
.\mvnw.cmd test
.\scripts\bin\harness-cli.exe story verify US-006
```

## Acceptance Evidence

- `docker compose up -d` was attempted on 2026-06-21 and did not start the
  backing services because Docker Desktop's Linux engine pipe was unavailable:
  `//./pipe/dockerDesktopLinuxEngine` could not be found.
- `.\mvnw.cmd -Dtest=PaymentWalletServiceTest test` passed on 2026-06-21:
  8 tests, 0 failures, 0 errors.
- `.\mvnw.cmd test` was attempted on 2026-06-21 and failed because DB-backed
  Spring context tests and the migration test could not connect to
  `127.0.0.1:5433`.
- `.\scripts\bin\harness-cli.exe story verify US-006` was attempted on
  2026-06-21 and failed because it runs `.\mvnw.cmd test`, which hit the same
  unavailable DB connection.
- Static scan found no production or docs references to the removed
  recommendation visibility field.

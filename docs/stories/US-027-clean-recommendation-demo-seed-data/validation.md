# Validation

## Proof Strategy

Use SQL assertions inside the migration to prevent whitespace identifiers from
surviving, then run backend compile and targeted test coverage that boots the
Spring context and applies Flyway migrations when a database is available.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Existing recommendation/profile tests still compile and pass when run. |
| Integration | Flyway applies through V41 and migration assertions pass. |
| E2E | Not run; no frontend/API flow changed. |
| Platform | Not run; no deployment/platform change. |
| Performance | Not applicable; deterministic demo seed only. |
| Logs/Audit | Harness trace records the cleanup. |

## Fixtures

- Expert accounts `expert.recommend01@aitasker.local` through `expert.recommend20@aitasker.local`.
- Business accounts `business.recommend01@aitasker.local` through `business.recommend10@aitasker.local`.

## Commands

```text
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd "-Dtest=ProfileServiceTest,NotificationServiceTest" test
```

## Acceptance Evidence

- `docker compose up -d` passed on 2026-06-23; PostgreSQL and Redis containers
  were running.
- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-23.
- `.\mvnw.cmd "-Dtest=ProfileServiceTest,NotificationServiceTest" test`
  passed on 2026-06-23: 20 tests, 0 failures, 0 errors.
- `.\mvnw.cmd test` passed on 2026-06-23: 123 tests, 0 failures, 0 errors.
  Flyway validated 41 migrations and schema version 41 was current.
- `.\scripts\bin\harness-cli.exe story verify US-027` passed on 2026-06-23
  with `.\mvnw.cmd test`: 123 tests, 0 failures, 0 errors.
- Direct DB verification confirmed:
  - Bad seeded account email/phone identifiers: 0.
  - Bad seeded Expert national IDs: 0.
  - Bad seeded Business tax codes: 0.
  - Seeded recommendation accounts remain 20 Experts and 10 Businesses.
  - Sample fixed accounts:
    `expert.recommend01@aitasker.local`,
    `expert.recommend20@aitasker.local`,
    `business.recommend01@aitasker.local`,
    `business.recommend10@aitasker.local`.

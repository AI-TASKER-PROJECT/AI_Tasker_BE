# Validation

## Proof Strategy

Use focused unit tests around `MarketplaceService.createJob` for the new
validation rules, then run compile and a broader service test set.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Job duration pair validation; milestone total greater than job duration rejected; valid mixed-unit duration values normalized and saved. |
| Integration | Flyway/schema validation through Maven test when local PostgreSQL is available. |
| E2E | Not run; frontend form behavior is out of scope. |
| Platform | Not applicable. |
| Performance | Not applicable. |
| Logs/Audit | Existing create-job audit remains unchanged. |

## Fixtures

- Business account id `10`.
- Business profile id `20`.
- Draft job id `99`.
- Two nested milestones with durations `7 DAY` and `1 WEEK`.

## Commands

```text
.\mvnw.cmd "-Dtest=MarketplaceServiceTest" test
.\mvnw.cmd "-DskipTests" compile
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-23.
- `.\scripts\bin\harness-cli.exe story verify US-020` passed on 2026-06-23: `MarketplaceServiceTest` 8 tests, 0 failures, 0 errors.
- `.\mvnw.cmd test` passed on 2026-06-23: 108 tests, 0 failures, 0 errors.
- Flyway validated 37 migrations and reported schema `public` at version 37.

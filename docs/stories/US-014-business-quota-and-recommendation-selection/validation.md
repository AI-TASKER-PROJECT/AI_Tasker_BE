# Validation

## Proof Strategy

Use unit tests for quota grants, admin-create quota path, recommendation selection, and notification dispatch. Use compile/full Maven test for integration safety, including Flyway schema validation against local PostgreSQL when available.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Business initial quota grant, Expert existing quota grant, Admin-created Business quota, recommendation selection notification. |
| Integration | Full Maven suite with Flyway local PostgreSQL. |
| E2E | Not run; frontend button is out of scope. |
| Platform | Not applicable. |
| Performance | Not applicable. |
| Logs/Audit | Quota usage log records initial Business grant. Notification row/realtime push path is covered by service mock. |

## Fixtures

- Business account id `10`.
- Job id `1`.
- Expert id `2`, account id `22`.

## Commands

```text
.\mvnw.cmd "-Dtest=PaymentWalletServiceTest,ExpertRecommendationServiceTest,AdminServiceTest" test
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd "-Dtest=PaymentWalletServiceTest,ExpertRecommendationServiceTest,AdminServiceTest" test` passed on 2026-06-22: 15 tests, 0 failures, 0 errors.
- `.\scripts\bin\harness-cli.exe story verify US-014` passed on 2026-06-22.
- `.\mvnw.cmd test` passed on 2026-06-22: 73 tests, 0 failures, 0 errors.
- Flyway validated 34 migrations and local PostgreSQL schema is version 34.
- DB query confirmed `business@aitasker.local` has `job_post_quota_balance=3`, `expert@aitasker.local` has `proposal_quota_balance=3`, and both `proposals.business_selected` and `expert_recommendations.business_selected` are boolean `NOT NULL DEFAULT false`.

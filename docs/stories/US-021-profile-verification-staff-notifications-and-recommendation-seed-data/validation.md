# Validation

## Proof Strategy

Use focused unit tests for staff notification dispatch and full Maven testing for migration/Flyway safety.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Business profile submission notifies every staff account. |
| Unit | Expert profile submission notifies every staff account. |
| Unit | Existing profile review notifications continue to pass. |
| Integration | Full Maven suite applies Flyway migrations through V38 against local PostgreSQL. |
| E2E | Not run; frontend changes are out of scope. |

## Commands

```text
.\mvnw.cmd "-Dtest=ProfileServiceTest,NotificationServiceTest" test
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd "-Dtest=ProfileServiceTest,NotificationServiceTest" test` passed on 2026-06-23: 20 tests, 0 failures, 0 errors.
- `.\mvnw.cmd test` passed on 2026-06-23: 110 tests, 0 failures, 0 errors.
- Flyway validated 38 migrations and local PostgreSQL schema is version 38.
- `.\scripts\bin\harness-cli.exe story verify US-021` passed on 2026-06-23.
- DB verification confirmed:
  - 20 seeded Expert accounts and 20 approved Expert profiles.
  - 10 seeded Business accounts and 10 approved Business profiles.
  - 10 seeded OPEN jobs.
  - 30 seeded milestones.
  - 20 seeded proposals.
  - Each seeded job has 2 proposals and 3 milestones.
  - All 20 seeded portfolios contain recommendation-matching chatbot/RAG/e-commerce/customer-support keywords.

# Validation - US-045

## Proof Strategy

Map every v2.2 acceptance criterion to focused service tests, migration/JPA
validation, route inventory checks, or an explicit non-goal.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Dual deposit, rejection history, explicit dispute, SLA, Staff guards, settlement arithmetic, immediate penalty |
| Integration | Flyway V51 plus JPA validation on PostgreSQL |
| E2E | Main milestone, dispute, standard termination, immediate termination flows through service/controller contracts |
| Platform | All new public routes use `/api/v1`; OpenAPI inventory synchronized |
| Performance | Not required for this change |
| Logs/Audit | Required v2.2 events are asserted or inspected |

## Fixtures

Use deterministic Business/Expert/Admin/Staff accounts and a contract total of
100,000,000: Business deposit 20,000,000; Expert deposit 10,000,000; immediate
termination penalty 10,000,000.

## Commands

```text
command:
.\mvnw.cmd -DskipTests compile
result:
pass - 214 source files compiled.

command:
.\mvnw.cmd "-Dtest=ContractExecutionServiceTest,PaymentWalletServiceTest,WalletLedgerServiceTest" test
result:
pass - 72 tests, 0 failures, 0 errors.

command:
Critical v2.2 controller route scan
result:
pass - all critical new dual-deposit, progress SLA, Staff-candidate, and
termination routes found under /api/v1.

command:
Forbidden implementation vocabulary scan
result:
pass - no admin-final-decision, REPORT_REVISION_REQUESTED, or automatic
Business-rejection dispute helper remains in implementation.

command:
git diff --check
result:
pass - line-ending warnings only.

command:
docker compose up -d; .\mvnw.cmd test
result:
pass - Docker Desktop 4.79.0 / PostgreSQL 16.14 available; Flyway validated
51 migrations and the full suite passed with 205 tests, 0 failures, 0 errors.
```

## Acceptance Evidence

- Additive V51 covers participant deposits, rejection/submission history,
  progress request history, overdue statuses, Staff timing fields, and Business
  termination response fields.
- Focused tests prove existing escrow/dispute/termination regression plus
  Business-deposit waiting and dual-deposit activation.
- Rejection now persists feedback and returns `UNDER_REVIEW -> IN_PROGRESS`
  without creating dispute.
- SLA auto-approval now performs guarded wallet release instead of only changing
  status.
- Public documentation lists the v2.2 routes and behavior.
- Docker-backed Flyway/JPA/full-suite proof passes against PostgreSQL.

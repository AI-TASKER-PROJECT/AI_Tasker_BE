# Validation

## Proof Strategy

Prove deadline snapshotting, duration parsing, automatic due processing,
blocking states, concurrency/idempotency guards, API response shape, Admin UI,
workspace countdown, and removal of the manual trigger.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Duration validation/conversion; submission timestamps; due/not-due; dispute/termination blocking; exactly-once release; approve/reject state recheck |
| Integration | Flyway applies the new setting/columns/index and JPA validates |
| E2E | Workspace obtains server deadline and observes scheduled completion; Admin saves minute/hour/day setting |
| Platform | FE TypeScript build and lint/test suite |
| Performance | Due query uses a partial deadline index and avoids full milestone scan |
| Logs/Audit | Automatic completion emits the SLA audit action; empty cycles do not |

## Fixtures

- Active contract with funded `IN_PROGRESS` milestone.
- Expert final deliverable submission.
- Business and Expert wallets with held milestone escrow.
- Variants with active dispute and active termination request.

## Commands

```text
command: .\mvnw.cmd test (isolated PostgreSQL database migrated from V1 to V68)
result: PASS - 403 tests, 0 failures, 0 errors
notes: Flyway applied all 68 migrations and Hibernate schema validation passed.

command: .\mvnw.cmd "-Dtest=ContractExecutionServiceTest,AdminServiceTest,MilestoneReviewSlaSchedulerTest,NotificationServiceTest" test
result: PASS - 106 tests, 0 failures, 0 errors
notes: Includes deadline snapshot, automatic settlement, dispute blocking, settings validation, scheduler invocation, and automatic notification coverage.

command: npm run build
result: PASS - TypeScript and Vite production build completed.

command: npm test
result: PASS - 22 tests, 0 failures
notes: Two SLA-specific FE contract tests verify minute/hour/day configuration and absence of the manual settlement API/action.

command: npx eslint <changed SLA frontend files>
result: PASS - no lint findings.

command: runtime GET http://localhost:8082/v3/api-docs
result: PASS - 201 operations; removed manual SLA path is absent; ContractMilestoneViewResponse contains reviewStartedAt and reviewDueAt.

command: git diff --check (BE and FE)
result: PASS - no whitespace errors; repository line-ending warnings only.
```

## Acceptance Evidence

- Expert final-deliverable submission snapshots the active value/unit duration.
- Scheduler processing is backend-owned and no longer requires authenticated
  Admin state.
- Due selection uses the partial review deadline index plus pessimistic locks;
  ledger operations retain their existing idempotency keys.
- Business approval and rejection revalidate the locked contract milestone;
  rejection clears the current review deadline before a resubmission.
- Active dispute/termination and released-escrow guards skip auto settlement.
- Admin cannot disable/delete the automatic SLA and FE presents only duration
  value plus minute/hour/day unit.
- Workspace renders the server deadline/countdown, polls for server state, and
  contains no settlement trigger.
- Runtime OpenAPI and affected product/Swagger/Postman guides are current.
- Full interactive Admin-page proof was attempted on an isolated local dataset,
  but the browser-side login throttling state prevented completing that visual
  pass. Build, lint, focused FE contract tests, runtime API verification, and the
  complete backend suite provide the recorded proof; E2E remains marked false.
- Completed detailed Harness trace: `#143`.

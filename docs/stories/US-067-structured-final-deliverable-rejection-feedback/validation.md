# Validation

## Proof Strategy

Proof must show structured rejection feedback is validated, persisted, exposed
on deliverables, and remains backward compatible with legacy query-only
`reason` clients.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Reject with structured failed criteria; reject unknown/wrong milestone criteria; legacy query reason still works. |
| Integration | Flyway migration and JPA schema validation in full suite. |
| E2E | Not required for this backend slice. |
| Platform | Runtime OpenAPI/docs route and schema coverage. |
| Performance | Small criteria list validation uses milestone-scoped criteria lookup. |
| Logs/Audit | Existing `MILESTONE_REJECTED` audit remains covered by service tests. |

## Fixtures

- Business-owned active contract.
- Milestone in `UNDER_REVIEW`.
- Submitted deliverable.
- Milestone-owned acceptance criteria.

## Commands

```text
command: .\mvnw.cmd -Dtest=ContractExecutionServiceTest test
result: PASS
notes: 81 service tests passed, including structured failed criteria persistence, wrong-milestone criteria rejection, and legacy query reason compatibility.

command: .\mvnw.cmd -DskipTests compile
result: PASS
notes: Main source compiles after DTO/entity/controller/service changes.

command: .\mvnw.cmd test
result: PASS
notes: Full suite passed with 377 tests, 0 failures, 0 errors, 0 skipped after Docker Postgres became available.

command: Invoke-WebRequest http://localhost:8081/v3/api-docs
result: PASS
notes: Refreshed docs/openapi/openapi-v1.json from runtime; OpenAPI contains RejectMilestoneRequest, FailedCriterionFeedback, and DeliverableEntity.rejectedCriteriaFeedback.

command: git diff --check
result: PASS
notes: No whitespace errors; Git reported LF-to-CRLF warnings only.
```

## Acceptance Evidence

- Business rejection now accepts body feedback with overall `reason` and
  optional `failedCriteria`.
- Backend validates every failed criterion belongs to the rejected milestone,
  appears only once, and has a nonblank reason before saving.
- Failed-criterion feedback is persisted on `deliverables` as JSONB through
  `rejected_criteria_feedback`, without adding a new table.
- Legacy clients using query `reason` still work and store no structured
  criteria feedback.

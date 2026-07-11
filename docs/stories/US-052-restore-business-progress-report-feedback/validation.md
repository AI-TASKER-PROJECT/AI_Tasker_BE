# Validation

## Proof Strategy

Use focused service tests for authorization/state behavior, compile proof for
JPA/controller/DTO shape, and OpenAPI/doc sync checks for the restored public
route.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Business feedback stores fields, acknowledges pending report, notifies Expert; blank feedback rejected; ack gate opens after feedback. |
| Integration | Flyway/JPA validation through full Maven test when local Docker/Postgres is available. |
| E2E | Not required for this backend restoration. |
| Platform | OpenAPI JSON parses and route inventory includes `/feedback`. |
| Performance | Not applicable. |
| Logs/Audit | Audit action `PROGRESS_REPORT_FEEDBACK_RECORDED` verified by service test. |

## Fixtures

Existing `ContractExecutionServiceTest` builders for Business, Expert,
contract, milestone, and progress reports.

## Commands

```text
command:
.\mvnw.cmd -Dtest=ContractExecutionServiceTest test
result:
pass - 71 tests, 0 failures, 0 errors.
notes:
Focused proof for progress-report feedback, acknowledgement gate, audit, and notification behavior.

command:
Runtime boot on http://localhost:8082/v3/api-docs, then regenerate docs/openapi/openapi-v1.json
result:
pass - OpenAPI runtime exposed 166 operations and /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback.
notes:
Flyway validated 54 migrations and JPA initialized during runtime boot. The saved OpenAPI artifact was normalized back to server URL http://localhost:8080.

command:
.\mvnw.cmd test
result:
pass - 281 tests, 0 failures, 0 errors.
notes:
Full suite included Docker/PostgreSQL-backed integration tests and Flyway/JPA validation.

command:
.\scripts\bin\harness-cli.exe story verify US-052
result:
pass - Story US-052 verification: pass.
notes:
Runs the focused ContractExecutionServiceTest command configured for this story.

command:
git diff --check
result:
pass - no whitespace errors; Git printed LF-to-CRLF working-copy warnings only.
notes:
Whitespace issue in SPEC line 4 was fixed before this pass.
```

## Acceptance Evidence

- Entity maps existing feedback columns from `V51`: `businessFeedback`,
  `feedbackCategory`, `feedbackSeverity`, `feedbackDodItems`,
  `requiresAdjustment`, `feedbackByAccountId`, `feedbackAt`.
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback`
  is present in controller, OpenAPI JSON, Swagger guide, Postman guide, and
  product/spec docs.
- `feedbackProgressReport_shouldStoreFeedbackAcknowledgeAndNotify` verifies
  Business feedback storage, pending report acknowledgement, audit event, and
  Expert notification.
- `feedbackProgressReport_shouldRejectBlankFeedback` verifies blank feedback is
  rejected.
- `submitProgressReport_shouldAcceptAfterBusinessFeedbackAcknowledgesPreviousReport`
  verifies feedback opens the existing ack gate.

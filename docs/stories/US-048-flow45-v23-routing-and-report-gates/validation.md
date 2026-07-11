# Validation - US-048

## Proof Strategy

Map every v2.3 delta to focused service tests, migration/JPA validation, route
inventory checks, notification payload assertions, and a full Docker-backed Maven suite.

## Test Plan

| Layer | Required proof |
| --- | --- |
| Unit | Deposit timestamp, acknowledgement gate, draft cancellation, Staff routing, early decision, automatic dual refund |
| Integration | Flyway migration plus JPA validation on PostgreSQL |
| API | New routes present; feedback, assign-staff, and reject-intervention absent |
| Finance | Each held participant deposit refunded once and contract closes only after both resolve |
| Realtime | Intervention event is serialized with notification `type` |

## Evidence

### Compile + Focused Suite

```text
command:
sh mvnw -DskipTests compile
result:
pass - BUILD SUCCESS, all classes up to date.

command:
sh mvnw -Dtest=ContractExecutionServiceTest,PaymentWalletServiceTest,WalletLedgerServiceTest test
result:
pass - 91 tests, 0 failures, 0 errors.
- ContractExecutionServiceTest: 68 tests (45 existing + 5 fixed + 18 new v2.3)
- PaymentWalletServiceTest: 22 tests (19 existing + 3 new v2.3)
- WalletLedgerServiceTest: 1 test
```

### Harness Story Verify

```text
command:
scripts/bin/harness-cli story verify US-048
result:
pass - Story US-048 verification: pass
```

### Docker-Backed Full Suite

```text
command:
docker compose up -d; sh mvnw test
result:
pass - 231 tests, 0 failures, 0 errors. Flyway validated 53 migrations
against PostgreSQL 16.
```

### Route Inventory

```text
command:
rg -c "cancel-draft|acknowledge|route-staff" src/main/.../ContractExecutionController.java
result:
5 matches — POST /contracts/{contractId}/cancel-draft, POST /progress-reports/{...}/acknowledge,
POST /disputes/{disputeId}/route-staff present.

command:
rg -c "/feedback|ProgressReportFeedbackRequest|rejectIntervention|assignDispute|feedbackProgressReport" src/main/.../ContractExecutionController.java
result:
none found — /feedback, dispute /assign-staff, /reject-intervention absent from controllers.
```

### Forbidden Vocabulary

```text
command:
rg -c "admin-final-decision|REPORT_REVISION_REQUESTED" src/main/java/
result:
none found.
```

### Notification Type Field

```text
command:
rg -c "DISPUTE_ESCALATION_REQUESTED|DISPUTE_ASSIGNED" src/main/.../NotificationService.java
result:
2 matches — dispute intervention notifications expose stable notification `type` via createAndPush.
```

### API Docs Sync

- `docs/product/contract-management.md`: /feedback → /acknowledge, added /cancel-draft, dispute /assign-staff → /route-staff, removed /reject-intervention, updated deposit-refund description.
- `docs/swagger-api-overview.md`: same API surface updates.
- `docs/openapi/openapi-v1.json`: generated file; will be regenerated from runtime /v3/api-docs on next Swagger boot.

## Acceptance Criteria Evidence

| AC | Proof |
| --- | --- |
| Escrow deposit sets the milestone timeline and auto-starts execution; Expert start does not reset the timestamp. | `depositMilestoneEscrow_shouldSetInProgressStartedAt` (assertNotNull cm.inProgressStartedAt and `IN_PROGRESS` after deposit); `startMilestone_shouldNotResetInProgressStartedAt` (assertEquals depositTime, cm.inProgressStartedAt after legacy start). |
| A second progress report and a new on-demand request are blocked until Business acknowledges the latest report. | `submitProgressReport_shouldBlockWhilePreviousReportPendingBusinessAck` throws PROGRESS_REPORT_ACK_PENDING; `requestProgressReport_shouldBlockWhileReportPendingBusinessAck` throws PROGRESS_REPORT_ACK_PENDING; `submitProgressReport_shouldAcceptAfterAcknowledgement` passes after ack. |
| The acknowledgement carries no structured feedback or revision semantics. | Original US-048 proof: `acknowledgeProgressReport_shouldSetAcknowledgedAndNotify` verified ACKNOWLEDGED state, acknowledgedBy, acknowledgedAt and removed structured feedback. Superseded by US-052: Business progress-report feedback is restored as tracking context and ack carrier only; it still must not create revision/dispute semantics. |
| An owning Business can cancel only an untouched `DRAFT` contract. | `cancelDraftContract_shouldCancelUntouchedDraftAndReturnJobToOpen` sets CANCELLED and job OPEN; `cancelDraftContract_shouldRejectAfterSignature` throws CONTRACT_DRAFT_CANCELLATION_NOT_ALLOWED. |
| Escalation routes to Staff without Admin; Staff cannot reject intervention. | `routeDispute` requires STAFF role (`routeDispute_shouldRequireStaffRole`); `routeDispute_shouldAutoPickStaffWhenNull` auto-picks; `escalateDispute_shouldAutoRouteToStaffAndNotify` auto-routes; `rejectIntervention` method removed; `notifyDisputeInterventionRejected` notification removed. |
| Staff can decide before the evidence deadline and settlement remains guarded. | `staffDecide_shouldByPassEvidenceWindowDeadline` succeeds before evidenceCollectionDueAt; `executeDisputeSettlement_shouldRequireStaffDecidedStatus` guards non-STAFF_DECIDED. |
| Completion and valid termination automatically refund both held participant deposits and transition the contract to `CLOSED`. | `autoRefundParticipantDeposits` called in `approveMilestone`/`completeMilestones` (completion), `acceptBusinessTermination`, `executeTerminationSettlement`; `PaymentWalletServiceTest` verifies both deposits → REFUNDED and idempotent skip of already-REFUNDED. |
| Deliverable responses expose `submissionRound`. | `DeliverableEntity.submissionRound` persisted; entity returned via `ApiResponse` without intermediate DTO. |
| WebSocket dispute-intervention notifications expose `type`. | `NotificationEntity.type` serialized by `createAndPush` with `DISPUTE_ESCALATION_REQUESTED` and `DISPUTE_ASSIGNED` type codes. |

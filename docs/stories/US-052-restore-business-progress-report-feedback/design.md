# Design

## Domain Model

`MilestoneProgressReportEntity` uses existing columns:

- `business_feedback`
- `feedback_category`
- `feedback_severity`
- `feedback_dod_items`
- `requires_adjustment`
- `feedback_by_account_id`
- `feedback_at`

`acknowledgement_state`, `acknowledged_by_account_id`, and `acknowledged_at`
remain the report gate fields.

## Application Flow

Business posts feedback for a report it owns through its contract. The service
checks Business role, approved account, contract ownership, and report
membership in the contract milestone.

If the report is `PENDING_BUSINESS_ACK`, feedback changes it to `ACKNOWLEDGED`
and fills acknowledgement actor/time. If the report was already acknowledged,
feedback updates only feedback fields.

## Interface Contract

Route:

`POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback`

Request DTO:

```json
{
  "category": "SCOPE",
  "severity": "INFO",
  "dodItems": ["API_CONTRACT_STABLE"],
  "feedback": "Tien do tot, can bo sung demo link o lan sau.",
  "requiresAdjustment": false
}
```

Response:

`ApiResponse<MilestoneProgressReportEntity>`.

Errors:

- `PROGRESS_REPORT_FEEDBACK_NOT_ALLOWED` when feedback text is blank or cannot
  be serialized.
- `PROGRESS_REPORT_REQUEST_NOT_FOUND` when the report is not in the contract
  milestone.

## Data Model

No new migration is expected because `V51` already added the feedback columns.
JPA validation should pass after entity fields are restored.

## UI / Platform Impact

Frontend can show the feedback fields on progress-report detail/list responses
and can use the same action to unlock the next report.

## Observability

Record audit action `PROGRESS_REPORT_FEEDBACK_RECORDED` and notify the Expert
with type `PROGRESS_REPORT_FEEDBACK_RECORDED`.

## Alternatives Considered

1. Keep acknowledgement-only. Rejected because Business needs to respond to
   progress reports during milestone execution.
2. Revert US-048 wholesale. Rejected because the acknowledgement gate, routing,
   and refund changes remain valid.

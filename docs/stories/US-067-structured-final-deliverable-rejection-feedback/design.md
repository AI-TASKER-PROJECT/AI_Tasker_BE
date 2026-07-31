# Design

## Domain Model

`deliverables.rejection_feedback` remains the overall rejection reason.
`deliverables.rejected_criteria_feedback` stores JSON for criterion-level
feedback:

```json
[
  { "criteriaId": 12, "reason": "Missing OTP expiry validation." }
]
```

## Application Flow

1. Business calls reject milestone while the milestone is `UNDER_REVIEW`.
2. Backend reads `RejectMilestoneRequest` body or legacy `reason` query.
3. Backend requires a non-empty overall reason.
4. Backend validates every failed criterion belongs to the milestone.
5. Backend saves the current deliverable as `REJECTED` and persists the JSON.
6. Milestone returns to `IN_PROGRESS` for Expert correction/resubmission.

## Interface Contract

`POST /api/v1/milestones/{milestoneId}/reject`

Request body:

```json
{
  "reason": "Final deliverable is not ready for acceptance.",
  "failedCriteria": [
    {
      "criteriaId": 12,
      "reason": "Login still succeeds with expired OTP."
    }
  ]
}
```

The existing `?reason=...` query parameter remains as a compatibility fallback.

## Data Model

Add nullable JSONB column:

- `deliverables.rejected_criteria_feedback`

No new table is added.

## UI / Platform Impact

Frontend can render criteria as checkboxes and send per-criteria reasons.
Existing clients that only send `reason` continue to work.

## Observability

Existing `MILESTONE_REJECTED` audit action remains. No new audit action is
needed for this data-shape refinement.

## Alternatives Considered

1. Normalized rejection criteria table. Rejected by product constraint.
2. Single string feedback only. Rejected because it is not transparent enough.

# Design - US-048

## Domain Model

Add acknowledgement state and actor/timestamp fields to progress reports through
the next additive Flyway migration. Keep legacy feedback columns readable for
historical rows, but remove their public write API and stop new writes.

## Application Flow

- Set `in_progress_started_at` once during milestone escrow deposit; Expert
  start changes status only.
- Save each progress report as `PENDING_BUSINESS_ACK`; reject another report or
  request until Business acknowledges the latest report.
- Let the owning Business cancel only a `DRAFT` contract before any participant
  has signed contract or NDA.
- Route an escalated milestone dispute automatically to the best available
  Staff candidate. A Staff operator may explicitly route to an eligible Staff;
  Admin is excluded from candidate reads, routing, and cancellation.
- Treat the evidence deadline as guidance and execute settlement immediately
  after a valid Staff decision.
- Invoke an idempotent system refund for both held participant deposits when a
  contract reaches normal `COMPLETED` or valid `TERMINATED`, then close it.

## Interface Contract

- Add `POST /api/v1/contracts/{contractId}/cancel-draft`.
- Replace progress-report `/feedback` with `/acknowledge`.
- Replace dispute `/assign-staff` with `/route-staff`.
- Remove milestone-dispute `/reject-intervention`.
- Keep `submissionRound` exposed by deliverable responses.
- WebSocket intervention notifications use the notification DTO `type` field.

## Data Safety

The migration is additive. It backfills existing progress reports as
`ACKNOWLEDGED` so deployed history does not block future submissions. Legacy
feedback and `INTERVENTION_REJECTED` data remain readable; new code does not
create them.

## Hard Gates

- Authorization and ownership.
- Financial custody and idempotent refunds.
- Flyway/JPA alignment under `ddl-auto=validate`.
- No double escrow release or participant-deposit refund.

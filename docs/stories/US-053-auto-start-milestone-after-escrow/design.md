# Design

## Domain Model

`contract_milestones.status` and `milestones.status` are kept aligned for the
normal milestone execution path. New Business escrow deposits move from
`PENDING` directly to `IN_PROGRESS`. `DEPOSITED` is retained for legacy rows and
historical compatibility only.

## Application Flow

`depositMilestoneEscrow(contractId, milestoneId)`:

- Requires approved Business ownership and `ACTIVE` contract.
- Requires current contract milestone status `PENDING`.
- Holds Business available balance into escrow.
- Sets `in_progress_started_at` if missing.
- Sets both milestone records to `IN_PROGRESS`.
- Records `MILESTONE_ESCROW_DEPOSITED` and notifies Expert.

`startMilestone(milestoneId)`:

- Requires approved Expert ownership and `ACTIVE` contract.
- Returns success when the contract milestone is already `IN_PROGRESS`.
- Transitions legacy `DEPOSITED` rows to `IN_PROGRESS` without resetting
  `in_progress_started_at`.
- Rejects other states.

## Interface Contract

No route or request/response schema changes. The behavior behind existing
routes changes:

- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`
  returns a milestone whose status is `IN_PROGRESS` after success.
- `POST /api/v1/milestones/{milestoneId}/start` is compatibility/idempotent for
  auto-started milestones.

## Data Model

No migration is required. Existing status values already include
`IN_PROGRESS`, and `DEPOSITED` remains valid for historical rows.

## UI / Platform Impact

Frontend should no longer require Expert to click start after Business deposit
for newly deposited milestones.

## Observability

New deposits keep the existing `MILESTONE_ESCROW_DEPOSITED` audit event.
`MILESTONE_STARTED` remains only for legacy `DEPOSITED -> IN_PROGRESS`
transitions.

## Alternatives Considered

1. Remove `startMilestone`: rejected to avoid breaking older clients.
2. Keep `DEPOSITED` and require Expert action: rejected because it conflicts
   with the requested business flow.

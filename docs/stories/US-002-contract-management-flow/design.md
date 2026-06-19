# Design

## Domain Model

Contracts remain in `contracts` with statuses `Draft`, `Negotiating`, `Active`,
`Completed`, `Terminated`, and `Cancelled`.

Jobs gain lifecycle statuses `PROPOSAL_REVIEW` and `IN_PROGRESS` in addition to
the existing `DRAFT`, `OPEN`, `CLOSED`, and `CANCELLED`.

Milestones gain `Completed` in addition to the existing execution statuses.

`contract_milestones` continues to snapshot the accepted budget and milestone
ordering at draft creation.

## Application Flow

- `createDraftFromProposal` validates the accepted proposal, ownership, and job
  milestones, then creates a `Draft` contract and contract milestone snapshots.
- `requestChange` moves a draft or negotiating contract back to `Negotiating`
  and clears signatures, NDA signatures, and activation time.
- `signContract` and `signNda` update the participant timestamp and call the
  activation check.
- Activation applies contract milestone budgets, attaches the contract id to job
  milestones, and moves the job to `IN_PROGRESS`.
- `rejectContract` is expert-only and moves the contract to `Cancelled` and the
  job to `PROPOSAL_REVIEW`.
- `submitDeliverable` keeps the existing expert-only active-contract rule and
  moves the milestone to `Under Review`.
- `completeMilestone` is business-only, accepts only `Under Review`, and calls
  contract completion when every contract milestone is `Completed`.

## Interface Contract

New API routes:

- `POST /api/v1/contracts/{contractId}/reject`
- `POST /api/v1/milestones/{milestoneId}/complete`

Existing routes preserved:

- `POST /api/v1/contracts/from-proposals/{proposalId}`
- `POST /api/v1/contracts/change-requests`
- `POST /api/v1/contracts/{contractId}/sign`
- `POST /api/v1/contracts/{contractId}/nda-sign`
- `POST /api/v1/contracts/{contractId}/terminate`
- `POST /api/v1/deliverables`

## Data Model

Migration `V29__contract_flow_status_alignment.sql` updates status check
constraints only. It does not rewrite existing rows.

## UI / Platform Impact

No frontend code is in this repository. API clients should display
`PROPOSAL_REVIEW`, `IN_PROGRESS`, and milestone `Completed` if they consume
status enums.

## Observability

Audit events were added for contract activation, rejection, milestone
completion, and contract completion. Notifications are emitted to participants
where account ids are available.

## Alternatives Considered

1. Keep activation closing the job. Rejected because it contradicts
   `SPEC-CONSTRACT.md` and prevents an in-progress execution phase.
2. Treat `Released` as milestone completion. Rejected because the supplied spec
   explicitly names `Completed` and the contract close condition depends on it.

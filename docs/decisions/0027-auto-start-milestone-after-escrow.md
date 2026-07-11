# 0027 Auto-Start Milestone After Escrow

Date: 2026-07-11

## Status

Accepted

## Context

The previous milestone execution flow required Business to deposit milestone
escrow and then Expert to call a separate start endpoint before the milestone
became executable. The product direction now requires a successful Business
milestone escrow deposit to start execution immediately, removing a redundant
Expert action and avoiding stalled deposited milestones.

## Decision

`depositMilestoneEscrow` moves both `milestones.status` and
`contract_milestones.status` directly from `PENDING` to `IN_PROGRESS` after the
escrow hold succeeds. It also sets `contract_milestones.in_progress_started_at`
when missing, so the deposit timestamp remains the execution timeline anchor.

`startMilestone` remains available for compatibility. It transitions legacy
`DEPOSITED` rows to `IN_PROGRESS`, does not reset
`in_progress_started_at`, and returns success without a duplicate audit event
when the milestone is already `IN_PROGRESS`.

## Alternatives Considered

1. Remove `startMilestone`.
2. Keep `DEPOSITED` as an intermediate state and auto-call start internally.

## Consequences

Positive:

- The normal Business deposit flow no longer leaves a milestone waiting for a
  second Expert action before progress reports or deliverables can be submitted.
- Older clients can still call `startMilestone` without failing when the
  milestone has already auto-started.

Tradeoffs:

- `DEPOSITED` remains a valid compatibility/historical status even though new
  deposits should skip it.
- Audit evidence for new work remains `MILESTONE_ESCROW_DEPOSITED`; the
  separate `MILESTONE_STARTED` audit applies only to legacy `DEPOSITED` starts.

## Follow-Up

- Frontend should treat successful milestone escrow deposit as the start signal
  and hide any mandatory Expert start action for newly deposited milestones.

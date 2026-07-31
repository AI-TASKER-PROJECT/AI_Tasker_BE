# 0043 Backend-Owned Milestone Review SLA

Date: 2026-07-30

## Status

Accepted

## Context

The existing milestone review SLA is an Admin-triggered simulation that scans
all milestones and derives deadlines from deliverable creation time plus an
integer day setting. The requested product rule requires automatic acceptance
and escrow release after Business inactivity, with minute/hour/day configuration
and no Admin settlement action.

## Decision

The backend is the sole authority for SLA expiry and settlement. Each final
deliverable submission snapshots `review_started_at` and `review_due_at` on the
contract milestone using the active `milestone_review_sla_duration` setting.
Configuration is one atomic positive value/unit pair and applies only to future
review rounds.

A backend fixed-delay scheduler selects and locks due review milestones, then
rechecks review state, contract state, disputes, termination, and escrow release
before using the existing idempotent ledger operations. Business accept/reject
paths share the contract-milestone lock. The frontend only renders the deadline,
counts down for the user, and refreshes server state.

Remove the manual Admin SLA processing endpoint and workspace action. Admin can
only change the duration; automatic SLA cannot be disabled through settings.

## Alternatives Considered

1. Browser-owned timer and settlement request. Rejected because it is unreliable
   and unsafe for financial state changes.
2. Scheduler recomputes deadlines using the latest configuration. Rejected
   because it retroactively changes an already-promised review window.
3. Continue manual Admin processing. Rejected because it contradicts the target
   workflow and leaves overdue reviews dependent on operator action.

## Consequences

Positive:

- SLA continues when no browser is open.
- Every review round has an explainable server deadline.
- Multi-run processing and user-action races cannot release escrow twice.
- Admin configuration supports production durations and short test durations.

Tradeoffs:

- Scheduler cadence can delay visible completion slightly after the exact
  deadline.
- A database migration and scheduler deployment configuration are required.
- Existing active reviews need a deterministic deadline backfill.

## Follow-Up

- Add operational metrics if production monitoring later requires SLA lag and
  failure dashboards.

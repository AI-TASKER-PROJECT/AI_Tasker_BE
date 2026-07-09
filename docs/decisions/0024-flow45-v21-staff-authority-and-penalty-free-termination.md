# Flow 4–5 v2.1 Staff Authority And Penalty-Free Termination

Date: 2026-07-09

## Status

Accepted

## Context

Supplementary Flow 4–5 backend work introduced useful progress-report SLA,
overdue, feedback, Staff assignment, and termination behavior. The same work
also experimented with Admin final dispute review and a fixed 10% immediate
termination penalty. The project owner explicitly rejected those two behaviors
and required all documented public APIs to use version `v1`.

## Decision

- Assigned Staff remains the final professional dispute decision-maker.
- Admin may assign or operationally replace Staff before decision, but may not
  approve, revise, reject, or change Staff's payout percentage.
- A valid Staff decision triggers system settlement; retry reuses the stored
  percentage unchanged.
- Immediate termination has no fixed 10% penalty for Business or Expert.
- An expired on-demand progress-report request may unlock Business immediate
  termination, but does not move money or adjudicate work quality by itself.
- Contract security-deposit handling remains binary and is never partially
  deducted as a fixed termination penalty.
- Every new public Flow 4–5 route uses `/api/v1`.

## Alternatives Considered

1. Staff recommendation followed by Admin final approval and ±10-point
   adjustment. Rejected because it overrides Staff's professional authority.
2. Fixed 10% compensation paid by the party invoking immediate termination.
   Rejected because the project owner does not want this penalty.
3. Store the latest progress-report request state in both milestone tables.
   Rejected as the source of truth because it loses request history and risks
   divergence; use a request-history table.

## Consequences

Positive:

- Dispute authority is simple and auditable.
- Settlement cannot change after Staff decision.
- Immediate termination has no hidden or contradictory fixed charge.
- Progress-report SLA history can be audited.
- API versioning is consistent.

Tradeoffs:

- Existing experimental Admin-final-decision and penalty code must be removed or
  excluded during implementation.
- Product/API docs and tests will need a later implementation synchronization
  story.

## Follow-Up

- Implement SPEC v2.1 through high-risk backend stories and migrations.
- Update product contract, Swagger/OpenAPI inventory, and test guides with the
  implemented `/api/v1` surface.

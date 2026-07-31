# Block Overdue Milestone Deliverables

Date: 2026-07-15

## Status

Accepted

## Context

The execution contract previously allowed final deliverables and correction
rounds while a milestone was `OVERDUE`. Overdue detection is exposed through an
Admin command, so an expired milestone can also remain persisted as
`IN_PROGRESS` until that command runs.

## Decision

- The contract-milestone snapshot is authoritative for the execution deadline.
- The deadline is `in_progress_started_at + duration`, with `WEEK` treated as
  seven days and `MONTH` as thirty days.
- Final deliverable submission and source-code ZIP upload are rejected when the
  current time is after that deadline, even if the persisted status is still
  `IN_PROGRESS`.
- A persisted `OVERDUE` milestone is always rejected by those two commands.
- The rule applies to first submissions and correction/resubmission rounds.
- Missing start or duration data keeps the existing no-deadline behavior.
- Overdue rejection does not automatically refund, release, or split milestone
  escrow. Existing dispute and termination settlement flows remain responsible
  for money movement.

## Consequences

- The backend no longer depends on the Admin overdue command to enforce the
  deadline.
- A Business rejection does not extend the original execution deadline.
- Escrow remains locked after deadline expiry until an existing settlement flow
  resolves it.
- No schema migration is required.


# Milestone Dispute V2 State Machine

Date: 2026-07-03

## Status

Accepted

## Context

`SPEC-MILESTONE-DISPUTER.md` defines a per-milestone escrow and dispute model
that differs from the partial implementation in US-037 through US-039. The
existing code still used intermediate milestone `APPROVED`/`REJECTED` states,
allowed security-deposit refund to keep contracts `COMPLETED`, and did not
persist an escrow-release guard.

## Decision

Align the current backend with the v2 state machine while reusing the existing
service boundary and existing migrations V45 through V47:

- Milestone approval releases escrow once and moves the milestone to
  `COMPLETED`.
- Business rejection creates or updates a single active self-resolve dispute and
  moves the milestone to `DISPUTED`.
- Staff decision and settlement execution remain separate states.
- Settlement records `escrow_released_at` and source fields before terminal
  milestone completion.
- Termination requests use a dedicated persisted lifecycle for Staff review,
  settlement execution, deposit refund, withdrawal, and case attachments.
- Termination settlement cannot run while active disputes exist.
- Contract security-deposit refund moves eligible `COMPLETED` or `TERMINATED`
  contracts to `CLOSED`, and reviews are allowed only at `CLOSED`.

## Alternatives Considered

1. Keep the partial `APPROVED`/`REJECTED` states for compatibility. Rejected
   because the active spec requires `COMPLETED`/`DISPUTED` terminal behavior and
   DB constraints should not preserve unused milestone execution states.
2. Create new V49+ migrations. Rejected for this task because the project owner
   explicitly asked to update the existing unstable migrations instead of adding
   new ones.
3. Implement new standalone services for dispute and termination immediately.
   Deferred to avoid widening the change while the current controller/service
   already exposes the required MVP flow.

## Consequences

Positive:

- Escrow release now has an idempotency guard.
- Contract closure and review opening match the v2 spec.
- The existing migrations now carry the required v2 schema pieces without adding
  another migration number.

Tradeoffs:

- Full DB/Flyway validation is covered by Docker-backed tests.
- The compatibility contract termination endpoints remain available, while the
  richer termination request endpoints expose the v2 lifecycle.

## Follow-Up

- Keep frontend route naming aligned with the v2 endpoint aliases as screens are
  wired.

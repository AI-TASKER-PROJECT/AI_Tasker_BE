# 0010 Status Enum Alignment

Date: 2026-06-20

## Status

Accepted

## Context

Payment, contract, job, and milestone statuses had drifted across migrations,
service logic, API examples, and docs. The active wallet ledger only posts
final movements, while contract execution no longer needs a negotiation/change
request lifecycle.

## Decision

Use uppercase lifecycle values for the affected execution tables:

- `wallet_transactions.status`: `POSTED` only.
- `contracts.status`: `DRAFT`, `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
- `jobs.status`: `DRAFT`, `OPEN`, `IN_PROGRESS`, `CLOSED`.
- `milestones.status` and contract milestone snapshots: `PENDING`,
  `DEPOSITED`, `IN_PROGRESS`, `UNDER_REVIEW`, `DISPUTED`, `COMPLETED`.

Contract drafts remain `DRAFT` until all contract and NDA signatures exist,
then become `PENDING` while waiting for deposit. Deposit payment starts
execution by moving the contract to `ACTIVE` and the job to `IN_PROGRESS`.
Completion moves the contract to `COMPLETED` and the job to `CLOSED`.
Rejection, termination, and cancellation use `CANCELLED`.

## Alternatives Considered

1. Keep the previous negotiation, proposal-review, and closed final states.
   Rejected because the current requested lifecycle removes negotiation and
   collapses old final states into `COMPLETED` or `CANCELLED`.
2. Add pending/success states to wallet ledger rows. Rejected because the
   active ledger code only records posted balance movement.

## Consequences

Positive:

- Database constraints, service logic, tests, and docs use one vocabulary.
- Public job listing remains a simple `OPEN` query.
- Contract execution is gated by deposit without reintroducing an activation
  endpoint.

Tradeoffs:

- Existing clients must stop sending old mixed-case contract/milestone values.
- Old data must be normalized by a new Flyway migration before constraints are
  tightened.

## Follow-Up

- Regenerate exported OpenAPI JSON after running the application if the static
  checked-in snapshot is used by frontend tooling.

# 0008 Contract Lifecycle Statuses

Date: 2026-06-19

## Status

Accepted

## Context

`SPEC-CONSTRACT.md` defines a contract lifecycle where activation starts work
and completion closes the job. The existing backend activated contracts by
moving jobs directly to `CLOSED`, and the database job status constraint did not
allow `PROPOSAL_REVIEW` or `IN_PROGRESS`.

## Decision

Align the backend lifecycle to the spec:

- Activation moves jobs to `IN_PROGRESS`.
- Expert rejection moves contracts to `Cancelled` and jobs to
  `PROPOSAL_REVIEW`.
- Business milestone completion uses milestone status `Completed`.
- When all contract milestones are `Completed`, the contract moves to
  `Completed` and the job moves to `CLOSED`.
- Add a Flyway migration to extend status constraints without rewriting
  existing data.

## Alternatives Considered

1. Keep using `CLOSED` on activation. Rejected because it collapses execution
   and completion into one state.
2. Reuse `Released` as completion. Rejected because the spec names
   `Completed`, and release/payment semantics are not identical to acceptance
   completion.

## Consequences

Positive:

- Contract lifecycle now has separate review, execution, and completion states.
- API clients can distinguish accepted proposal review from active execution.
- Database constraints match service behavior.

Tradeoffs:

- Frontend/API clients that hard-code job status enums must accept
  `PROPOSAL_REVIEW` and `IN_PROGRESS`.
- Existing manual docs and Postman scripts need to be refreshed around the
  activation expectation.

## Follow-Up

- Expand integration coverage for the full accepted-proposal-to-completed
  contract path.

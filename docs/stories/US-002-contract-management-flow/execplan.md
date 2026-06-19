# Exec Plan

## Goal

Implement the contract management flow from `SPEC-CONSTRACT.md` as the current
backend contract.

## Scope

In scope:

- Contract activation sets job status to `IN_PROGRESS`.
- Expert rejection cancels the contract and returns job status to
  `PROPOSAL_REVIEW`.
- Business completion of reviewed milestones can complete the contract and close
  the job.
- Database status constraints accept the lifecycle statuses.
- Product docs, API docs, decision record, and tests are updated.

Out of scope:

- Payment provider settlement.
- Escrow ledger finalization.
- NDA document generation.

## Risk Classification

Risk flags:

- Authorization.
- Data model.
- Audit/security.
- Public contracts.
- Existing behavior.
- Multi-domain.

Hard gates:

- Authorization.
- Data migration.
- Public API shape.

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- Contract lifecycle status names need to match a frontend enum not present in
  this repository.
- A migration would delete or rewrite existing production data.
- Payment or dispute behavior must become production-grade in this same slice.

# 0038 Custom SoW Budget Reallocation

## Status

Accepted

## Context

US-066 returns one milestone allocation for the Business-entered amount and one
for the AI recommendation. A third confirmation option lets Business enter a
different final amount, but neither existing allocation necessarily totals that
amount. Frontend-only financial rounding would create inconsistent totals.

## Decision

Add a stateless endpoint:

`POST /api/jobs/reallocate-sow-budget`

The request contains a positive whole-VND `selectedBudget` and milestone
references identified by `milestoneIndex`. Frontend copies each generated
`milestones[].recommendedBudget` into `referenceBudget`.

Backend sorts by `milestoneIndex`, rejects duplicate indexes or non-positive
amounts, proportionally scales every reference, rounds down whole VND for all
but the final milestone, and assigns the exact remainder to the final
milestone. The returned `allocationTotal` must exactly equal `selectedBudget`.

The endpoint does not call OpenAI, mutate the AI range, create a Job, or write
to the database. Business remains responsible for confirming the selected
amount before the existing create-Job API is called.

## Consequences

- Frontend has one authoritative backend calculation for custom budgets.
- Request order cannot change the result because allocation order is based on
  `milestoneIndex`.
- Rounding cannot create a negative final allocation or a total mismatch.
- No table, migration, entity, repository, or audit record is added.

## Rejected Alternatives

1. Let every frontend calculate proportional values independently; rejected
   because decimal and rounding behavior could diverge.
2. Regenerate the SoW through OpenAI; rejected because changing the final
   Business price does not require another model call.
3. Persist the custom selection before Job creation; rejected because the
   existing Job creation flow is the persistence boundary.

# Design

## API Contract

Route: `POST /api/jobs/reallocate-sow-budget`.

Request:

- `selectedBudget`: positive whole-VND final amount entered by Business.
- `milestones`: 1 to 50 items.
- `milestoneIndex`: unique non-negative generated milestone index.
- `referenceBudget`: positive whole-VND weight copied from
  `milestones[].recommendedBudget`.

Response:

- `currency`: `VND`.
- `selectedBudget`: confirmed custom amount from the request.
- `allocationTotal`: exact sum of returned allocations.
- `allocations[]`: index, echoed reference, and calculated `fundsAllocated`.

## Allocation Rule

Items are sorted by `milestoneIndex`. Each item except the last receives:

`floor(referenceBudget / totalReferenceBudget * selectedBudget)`

The last item receives the remaining whole-VND amount. This makes the result
deterministic, non-negative, and exact.

## Data and Authority

The operation is pure calculation. It has no repository access and no database
transaction. AI estimates stay read-only; the Business-entered custom amount is
the final Job budget only after frontend sends it through the existing Job
creation flow.

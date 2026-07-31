# 0042 AI SoW VND Scale And HIGH Visibility

Date: 2026-07-23

## Status

Accepted

## Context

OpenAI can return internally coherent budget values such as `80`, `100`, and
`130` while intending millions of VND. Treating those values as literal VND
produces an unusable price display and can classify a normal Business budget as
`HIGH`. The existing Create Job UI also shows an advisory card even when the
Business amount already exceeds the complete normalized AI range.

## Decision

- Money output from OpenAI remains untrusted boundary data.
- The prompt requires full whole-VND integers.
- Text abbreviations such as `140 triệu` are parsed into full VND.
- A bare positive whole-number recommendation from `1` through `10000` is
  treated as millions of VND. Matching abbreviated range values are scaled by
  the same factor before status comparison.
- A repaired unit scale lowers assessment confidence to `LOW`.
- `HIGH` means `businessBudget > estimatedMax` after normalization.
- For `HIGH`, `requiresBusinessConfirmation=false`; frontend hides the complete
  advisory card and uses the existing Business budget and milestone allocation.
- For all other statuses, the advisory card remains visible and read-only.
- AI recommendation values never become the authoritative Job, proposal, or
  contract price.

## Alternatives Considered

1. Display the bare provider numbers and label them VND. Rejected because the
   resulting price and comparison status are misleading.
2. Multiply every value below one million by one million. Rejected because the
   broader threshold would transform more ambiguous provider output.
3. Call OpenAI a second time whenever the scale is abbreviated. Rejected because
   it adds latency, cost, and another provider failure path for a deterministic
   repair.
4. Keep showing the advisory card for `HIGH`. Rejected because Business already
   supplied an amount above the complete AI range and does not need a lower AI
   proposal.

## Consequences

Positive:

- `80/100/130` is displayed as `80,000,000/100,000,000/130,000,000 VND`.
- Status is calculated only after unit normalization.
- Higher Business budgets proceed without an unnecessary advisory confirmation.
- Business, Expert, and contract pricing authorities remain separate.

Tradeoffs:

- The bounded million-scaling rule is a provider-output heuristic and therefore
  lowers confidence.
- A future calibrated pricing service should replace this heuristic when
  historical market data becomes available.

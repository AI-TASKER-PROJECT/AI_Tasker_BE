# Design

## Domain Model

`BudgetAssessmentDto` separates the Business-entered amount from the advisory
AI range. `MilestoneDto.budget` stays aligned with the Business amount, while
`MilestoneDto.recommendedBudget` aligns with the advisory recommendation.

Statuses are `TOO_LOW`, `LOW`, `SUITABLE`, and `HIGH`. Currency is normalized to
`VND`. `requiresBusinessConfirmation` is false only for `HIGH`, where the
Business-entered amount remains authoritative and the advisory card is hidden.

## Application Flow

1. Business sends the existing positive `budget` with the SoW request.
2. Backend excludes that Business amount and all sample prices from the OpenAI
   prompt; OpenAI returns a complete draft, milestones, and an independent
   project-specific price range.
3. Backend parses formatted money, expands coherent bare whole-number shorthand
   from 1 through 10000 as millions of VND, repairs invalid/missing range
   boundaries, sanitizes factors/confidence, and compares the Business amount.
4. Backend scales `recommendedBudget` by the raw AI milestone proportions.
5. Backend keeps existing `budget` scaled to the Business-entered total.
6. Frontend shows the advisory card only when status is not `HIGH`. For `HIGH`,
   it keeps the Business amount without a second confirmation. Otherwise it
   asks the Business to keep or custom-edit the entered amount.

If OpenAI returns neither a valid top-level recommendation nor a positive
milestone total, backend rejects the response. It never substitutes the
Business amount as an AI estimate.

## Interface Contract

Route remains `POST /api/jobs/generate-sow`.

Response additions:

- `budgetAssessment`: structured advisory comparison.
- `milestones[].recommendedBudget`: recommended milestone allocation.

Existing request fields and existing response fields remain compatible.

## Data Model

No table, index, migration, or entity change. Only the Business-selected value
continues to be persisted through the existing job/milestone flow.

## UI / Platform Impact

Frontend displays the range/status/message for non-`HIGH` assessments and
presents keep/custom-edit actions. It hides the full advisory card for `HIGH`
and keeps `businessBudget`. `recommendedBudget` remains read-only and can be
used only as a proportional custom-allocation reference. Mapping details live
in `docs/product/ai-sow-budget-assessment.md`.

## Observability

No audit row is added because the estimate is stateless and advisory. Provider
errors continue through the existing SoW error handling.

## Alternatives Considered

1. Persist every estimate now; rejected to keep Release A schema-free.
2. Replace Business budget automatically; rejected because Business is the
   final authority.
3. Let the frontend derive all statuses; rejected to keep comparison semantics
   consistent across clients.
4. Trust bare provider numbers as literal VND; rejected because values such as
   `130` create a misleading `130 VND` estimate and incorrect status.

# Design

## Domain Model

`BudgetAssessmentDto` separates the Business-entered amount from the advisory
AI range. `MilestoneDto.budget` stays aligned with the Business amount, while
`MilestoneDto.recommendedBudget` aligns with the advisory recommendation.

Statuses are `TOO_LOW`, `LOW`, `SUITABLE`, and `HIGH`. Currency is normalized to
`VND`. `requiresBusinessConfirmation` is always true.

## Application Flow

1. Business sends the existing positive `budget` with the SoW request.
2. OpenAI returns a complete draft, milestones, and an independent price range.
3. Backend parses formatted money, repairs invalid/missing range boundaries,
   sanitizes factors/confidence, and compares the Business amount.
4. Backend scales `recommendedBudget` by the raw AI milestone proportions.
5. Backend keeps existing `budget` scaled to the Business-entered total.
6. Frontend asks the Business to keep the entered amount or use the recommended
   amount before calling `POST /api/v1/jobs`.

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

Frontend displays the range/status/message and presents two explicit actions:
keep `businessBudget`, or use `recommendedBudget`. Mapping details live in
`docs/product/ai-sow-budget-assessment.md`.

## Observability

No audit row is added because the estimate is stateless and advisory. Provider
errors continue through the existing SoW error handling.

## Alternatives Considered

1. Persist every estimate now; rejected to keep Release A schema-free.
2. Replace Business budget automatically; rejected because Business is the
   final authority.
3. Let the frontend derive all statuses; rejected to keep comparison semantics
   consistent across clients.

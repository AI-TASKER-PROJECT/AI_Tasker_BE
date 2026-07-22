# 0037 AI SoW Budget Assessment Authority

Date: 2026-07-22

## Status

Accepted

## Context

`POST /api/jobs/generate-sow` currently requires a Business-entered budget and
normalizes milestone budgets to that amount. The response cannot tell the
Business that the entered amount is too low for the generated full scope.
Allowing OpenAI to overwrite the entered budget would also blur the boundary
between an advisory estimate, the Business job budget, an Expert bid, and the
accepted contract total.

## Decision

Add an advisory `budgetAssessment` to the generated SoW response without
changing database schema or persistence behavior.

- OpenAI proposes an independent VND range and pricing factors for the full
  generated scope.
- The backend owns range normalization, confidence fallback, comparison status,
  gap calculation, source labeling, and the Vietnamese user message.
- The Business-entered amount remains unchanged in `businessBudget` and in the
  existing milestone `budget` allocation.
- Each milestone adds `recommendedBudget`, whose sum matches the advisory
  recommended total.
- The frontend must ask the Business whether to keep the entered amount or use
  the recommendation before creating the draft job.
- The accepted Expert proposal remains the authority for contract pricing.
- No estimate is persisted in Release A; estimate history and calibration are
  deferred.

## Alternatives Considered

1. Automatically replace the Business budget with the AI recommendation.
2. Persist estimate history and Business decisions in the same release.
3. Return only a warning string without structured amounts or milestone
   allocations.

## Consequences

Positive:

- Existing clients can continue using `milestones[].budget`.
- The frontend receives a structured comparison and a complete recommended
  alternative.
- AI output cannot silently become the job or contract price.
- No Flyway migration or JPA entity change is required.

Tradeoffs:

- AI pricing remains advisory and is not yet calibrated against accepted
  proposals or completed contracts.
- The assessment is lost if the frontend does not retain it before job
  creation.
- A future persisted estimate history requires a separate story and migration.

## Follow-Up

- Measure advisory estimate accuracy before adding historical calibration.
- Consider a versioned `job_budget_estimates` table only when audit or analytics
  becomes a product requirement.

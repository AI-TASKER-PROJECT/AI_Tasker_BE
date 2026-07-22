# US-070 Custom SoW Budget Reallocation

## Current Behavior

SoW generation returns allocations for the original Business amount and the AI
recommended amount. A Business-entered third amount has no backend-owned
milestone allocation.

## Target Behavior

`POST /api/jobs/reallocate-sow-budget` proportionally converts AI-recommended
milestone amounts into whole-VND allocations whose total exactly matches the
custom amount selected by Business.

## Affected Users

- Business confirming a generated SoW budget.
- Frontend developers implementing the custom-budget option.

## Non-Goals

- Editing the AI `estimatedMin`, `recommendedBudget`, or `estimatedMax` range.
- Calling OpenAI again.
- Creating or updating a Job.
- Database schema or persistence changes.

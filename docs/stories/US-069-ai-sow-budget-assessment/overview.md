# US-069 AI SoW Budget Assessment

## Current Behavior

The Business must enter a positive budget before generating a SoW. AI milestone
amounts are normalized back to that exact total, so the response cannot identify
an underfunded full scope or offer a separate recommended allocation.

## Target Behavior

`POST /api/jobs/generate-sow` returns a normalized advisory VND range, compares
the Business amount with that range, and returns both Business-budget and
recommended milestone allocations. The Business remains the final decision
maker and no estimate is persisted. The pricing prompt excludes the Business
amount and all sample prices so the provider derives a project-specific range
from scope, duration, roles, integrations, data, testing, infrastructure, and
risk. Abbreviated provider values such as
`80/100/130` are expanded to full VND before comparison. When the Business
amount exceeds the normalized maximum, the frontend hides the advisory card
and keeps the Business amount.

## Affected Users

- Business creating or regenerating an AI SoW.
- Frontend developers implementing the budget confirmation UI.

## Affected Product Docs

- `docs/product/ai-sow-budget-assessment.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Non-Goals

- Database schema changes or estimate history.
- Automatic replacement of `jobs.budget`.
- Historical-market calibration.
- Changes to Expert bid or contract total authority.

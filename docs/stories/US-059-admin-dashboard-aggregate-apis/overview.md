# Overview

## Current Behavior

Admin dashboard currently has a small `GET /api/v1/admin/analytics/overview`
endpoint and several list/detail APIs for contracts, wallet transactions,
accounts, disputes, withdrawals, and membership packages. Frontend can build
tables from those APIs, but chart data requires loading large lists and grouping
records client-side.

## Target Behavior

Add admin-only dashboard aggregate APIs that return chart-ready summary,
timeline, status breakdown, finance, membership, user, job/proposal, and dispute
data. Existing list/detail APIs remain available for drill-down screens.

## Affected Users

- Admin.
- Frontend developers building admin dashboard charts.

## Affected Product Docs

- `docs/product/admin-dashboard.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`
- `README.md`

## Non-Goals

- Do not remove or change existing list/detail dashboard support APIs.
- Do not add a database migration.
- Do not add frontend UI.

# Admin Dashboard

## Purpose

Admin dashboard APIs provide chart-ready operational and finance aggregates for
the admin UI. They complement existing list/detail APIs, which remain the source
for drill-down management tables.

## API Shape

All routes require `ADMIN` role:

- `GET /api/v1/admin/dashboard/summary`
- `GET /api/v1/admin/dashboard/revenue?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/contracts?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/users?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/jobs-proposals?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/disputes?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/membership?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/finance-breakdown?from=&to=`

`from` and `to` use ISO date format. Missing ranges default to the latest six
months. Invalid `groupBy` values fall back to `month`.

## Frontend Usage

- Use dashboard APIs for cards, charts, trend lines, status breakdowns, and
  finance summaries.
- Use existing list/detail APIs for tables and drill-down screens, such as
  contract lists, account management, dispute detail, wallet transaction detail,
  and withdrawal review.

## Revenue And Finance Notes

- `revenue` aggregates posted wallet transactions by `transactionType`.
- `membership` aggregates successful membership purchases.
- `finance-breakdown` exposes wallet balances, gross transaction volume,
  withdrawal totals, and transaction-type breakdowns so the frontend can label
  gross movement separately from net revenue.

# Design

## Domain Model

Dashboard aggregates read existing domain data:

- Accounts and roles for user counts/trends.
- Business/expert profiles for verification backlog.
- Jobs/proposals for marketplace funnel counts.
- Contracts and disputes for operational status.
- Wallet transactions, payment orders, membership purchases, withdrawals, and
  system wallet for finance/revenue data.

## Application Flow

`AdminDashboardController` accepts admin dashboard requests and delegates to
`AdminDashboardService`. The service enforces `ADMIN` role, normalizes date
ranges and grouping, and returns DTOs designed for direct chart rendering.

Existing list/detail APIs remain the drill-down source.

## Interface Contract

Routes:

- `GET /api/v1/admin/dashboard/summary`
- `GET /api/v1/admin/dashboard/revenue?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/contracts?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/users?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/jobs-proposals?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/disputes?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/membership?from=&to=&groupBy=day|week|month`
- `GET /api/v1/admin/dashboard/finance-breakdown?from=&to=`

`from` and `to` are ISO local date strings. Missing values default to the last
six months.

## Data Model

No schema changes. Aggregate reads use existing tables and repository queries.

## UI / Platform Impact

Frontend can render cards and charts from the new dashboard endpoints while
continuing to call existing list/detail APIs for drill-down tables.

## Observability

Dashboard reads do not write audit logs. Harness trace records validation.

## Alternatives Considered

1. Frontend groups list APIs client-side. Rejected because it is heavy and can
   drift from backend business definitions.
2. Replace existing list/detail APIs. Rejected because dashboard aggregates do
   not cover management table workflows.

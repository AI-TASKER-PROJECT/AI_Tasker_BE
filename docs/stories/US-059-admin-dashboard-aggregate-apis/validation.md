# Validation

## Proof Strategy

Focused unit tests cover dashboard aggregate behavior and authorization guard.
Compile/test validation proves the public API additions build. Docs scans prove
Swagger-facing files mention the new routes.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Summary totals, revenue series, contract breakdown/trend, user trend, dispute trend, membership package sales, finance breakdown, ADMIN role guard |
| Integration | Not required unless Docker/PostgreSQL is available for runtime OpenAPI |
| E2E | Not in scope |
| Platform | OpenAPI/docs parse/count checks |
| Performance | Aggregates use repository-level queries where practical |
| Logs/Audit | No audit writes for read-only dashboard endpoints |

## Fixtures

Mockito repository fixtures for accounts, contracts, jobs, proposals,
transactions, wallet transactions, memberships, withdrawals, disputes, and
profiles.

## Commands

```text
command: .\mvnw.cmd -Dtest=AdminDashboardServiceTest test
result: PASS
notes: 8 tests, 0 failures, 0 errors, 0 skipped.

command: python docs/openapi/docs dashboard route count check
result: PASS
notes: OpenAPI has 184 operations; all 8 /api/v1/admin/dashboard routes are present in OpenAPI, Swagger guide, Postman guide, and API overview.

command: git diff --check
result: PASS
notes: No whitespace errors; Git reported existing LF/CRLF normalization warnings only.
```

## Acceptance Evidence

- Added admin-only dashboard aggregate endpoints for summary, revenue, contracts,
  users, jobs/proposals, disputes, membership, and finance breakdown.
- Added focused unit coverage for aggregate totals, time-series behavior,
  breakdowns, and admin role enforcement.
- Updated OpenAPI JSON, Swagger API overview, Swagger API test guide, Postman
  API test guide, architecture, README, and product documentation.

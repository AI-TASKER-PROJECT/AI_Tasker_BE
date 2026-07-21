# Validation

## Proof Strategy

Proof must show route aliases delegate safely, edit guards preserve ownership
and contract snapshots, and contract change requests require counterparty
approval before applying.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Job `OPEN` edit, locked contract-owned job, proposal edit for pending/accepted before contract, locked proposal with contract, contract change create/accept/reject, counterparty guard. |
| Integration | Flyway migration and JPA schema validation in full suite. |
| E2E | Not required for this backend slice. |
| Platform | Runtime OpenAPI contains preferred routes and request schemas. |
| Performance | Small milestone/proposal lists use existing scoped repository lookups. |
| Logs/Audit | Audit log and notification calls are asserted in focused tests. |

## Fixtures

- Business-owned `DRAFT` and `OPEN` jobs.
- Expert-owned `Pending` and `Accepted` proposals.
- `DRAFT`/`ACTIVE` contracts with Business and Expert participants.
- Contract milestones in editable and non-editable statuses.

## Commands

```text
command: .\mvnw.cmd '-Dtest=MarketplaceServiceTest,ContractExecutionServiceTest' test
result: Pass
notes: 101 tests passed. Covered open-job edit + notification, proposal edit + notification, contract change request create/self-approval guard/accept apply, and existing contract execution regressions.
```

```text
command: Runtime boot on SERVER_PORT=8082 with RAG_INGEST_ON_STARTUP=false, then GET http://localhost:8082/v3/api-docs
result: Pass
notes: HTTP 200, Flyway validated 65 migrations and applied V65 locally, OpenAPI snapshot refreshed into docs/openapi/openapi-v1.json.
```

```text
command: OpenAPI JSON parse/path/schema check
result: Pass
notes: docs/openapi/openapi-v1.json has 166 paths and 199 operations; required job/proposal/change-request/contract-scoped milestone paths and ContractChangeRequest schemas are present.
```

```text
command: .\mvnw.cmd test
result: Pass
notes: Full backend suite passed: 382 tests, 0 failures, 0 errors, 0 skipped.
```

```text
command: git diff --check
result: Pass
notes: No whitespace errors; only Windows CRLF normalization warnings.
```

## Acceptance Evidence

- Preferred API aliases were added while compatibility routes remain.
- Business can update `DRAFT`/`OPEN` jobs before a contract exists, with audit
  and notifications to experts who submitted proposals.
- Expert can update own `Pending`/`Accepted` proposal before a contract exists,
  with audit and notification to the owning Business.
- Contract change requests are stored in the existing table with new nullable
  payload columns, require counterparty review, block requester self-approval,
  and apply contract budget/timeline/scope/milestone changes only on accept.
- README, product docs, Swagger overview, Swagger test guide, Postman guide,
  and runtime OpenAPI snapshot were updated.

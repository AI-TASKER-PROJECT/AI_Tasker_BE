# Validation — US-049

## Proof Strategy

Map each SPEC acceptance criterion to focused tests, then validate with:
1. Focused unit tests (Mockito) — functional + pagination + auth
2. Focused integration tests (MockMvc + PostgreSQL) — RBAC enforcement
3. Full Maven suite (all tests, Flyway validation)
4. Route inventory scan — AC9 mutation check
5. OpenAPI/JSON parse — AC10 contract

## Test Plan

| Layer | Required Proof | Command |
|-------|---------------|---------|
| Unit (functional) | AdminDisputeDashboardTest | `sh mvnw -Dtest=AdminDisputeDashboardTest test` |
| Unit (auth) | AdminDisputeDashboardAuthTest | `sh mvnw -Dtest=AdminDisputeDashboardAuthTest test` |
| Integration | AdminDisputeDashboardIntegrationTest | `sh mvnw -Dtest=AdminDisputeDashboardIntegrationTest test` |
| Full Suite | All tests pass, Flyway validates | `sh mvnw test` |
| Route Scan | No mutation routes on admin/disputes | `rg '@PostMapping|@PutMapping|@PatchMapping|@DeleteMapping' AdminController.java` |
| OpenAPI | json parse + path check | `jq . docs/openapi/openapi-v1.json`, verify paths present |

## Acceptance Criteria Evidence

| AC | Description | Verification |
|----|-------------|-------------|
| AC1 | Admin can paginate and filter all disputes through GET /api/v1/admin/disputes | AdminDisputeDashboardTest (listDisputes_shouldReturnPaginatedList, listDisputes_shouldFilterByStatus/Staff/Date/Q, listDisputes_shouldPaginateCorrectly) |
| AC2 | Non-Admin receives authorization failure for both new dashboard endpoints | AdminDisputeDashboardAuthTest (2 auth tests), AdminDisputeDashboardIntegrationTest (4 authorization tests — unauthenticated 401, BUSINESS/EXPERT 401) |
| AC3 | List ordering, pagination, status/Staff/date/ID filters are deterministic and correct | AdminDisputeDashboardTest (listDisputes_shouldFilterByStatus, listDisputes_shouldFilterByAssignedStaffId, listDisputes_shouldFilterByDateRange, listDisputes_shouldSearchByQ, listDisputes_shouldPaginateCorrectly) |
| AC4 | Settled response amounts and transaction ID match persisted dispute and ledger data | AdminDisputeDashboardTest (listDisputes_settledDisputeShouldHaveFinancials, getDisputeDetail_shouldReturnSettledDisputeWithLedgerAndAttachments) |
| AC5 | Unsettled disputes expose null settlement fields; no estimated payout is shown | AdminDisputeDashboardTest (listDisputes_unsettledDisputeShouldHaveNullFinancials, getDisputeDetail_shouldReturnUnsettledDisputeWithNullFinancials) |
| AC6 | Detail returns only evidence and ledger rows belonging to its dispute | getDisputeDetail_shouldReturnSettledDisputeWithLedgerAndAttachments (verifies ledger filtered by referenceType=DISPUTE + referenceId=disputeId, attachments by ownerType=DISPUTE + ownerId=disputeId) |
| AC7 | A successful Staff-triggered settlement sends exactly one Admin informational report after commit | Code proof: notifyAllAdmins -> notifyAdminDisputeSettlementReported called after settlement commit in executeDisputeSettlementInternal (ContractExecutionService.java:1810-1814) |
| AC8 | Retrying a previously settled case cannot duplicate escrow release or Admin report | Code proof: ensureEscrowNotReleased guard + DISPUTE_NOT_STAFF_DECIDED check prevents double execution in executeDisputeSettlementInternal (ContractExecutionService.java:1766-1776) |
| AC9 | Admin cannot approve, change payout percentage, route, or cancel a milestone dispute through this feature | Route scan: 0 mutation methods on /api/v1/admin/disputes in AdminController.java |
| AC10 | Swagger/OpenAPI and dashboard documentation describe the new read-only API and notification type | openapi-v1.json refreshed from runtime (141 paths, 2 admin/disputes paths present); swagger-api-overview.md updated; dispute-flow.md updated; README.md updated |

## Evidence

| Check | Command | Result |
|-------|---------|--------|
| Compile | `sh mvnw -DskipTests compile` | BUILD SUCCESS |
| Unit tests (functional) | `sh mvnw -Dtest=AdminDisputeDashboardTest test` | Tests run: 19, Failures: 0, Errors: 0 |
| Unit tests (auth) | `sh mvnw -Dtest=AdminDisputeDashboardAuthTest test` | Tests run: 2, Failures: 0, Errors: 0 |
| Integration tests | `sh mvnw -Dtest=AdminDisputeDashboardIntegrationTest test` | Tests run: 4, Failures: 0, Errors: 0 |
| Full test suite | `sh mvnw test` | Tests run: 252, Failures: 0, Errors: 0 |
| Story verify | `harness-cli story verify US-049` | pass (25 tests, 0 failures) |
| OpenAPI JSON parse | `python3 -c "import json; d=json.load(open('docs/openapi/openapi-v1.json')); print(len(d['paths']))"` | 141 paths, includes /api/v1/admin/disputes + /api/v1/admin/disputes/{disputeId} |
| Route mutation scan | `rg '@PostMapping|@PutMapping|@PatchMapping|@DeleteMapping' AdminController.java | rg 'disputes'` | 0 matches (exit 1) |
| DISPUTE_SETTLEMENT_REPORTED hook | `rg 'notifyAllAdmins.*notifyAdminDisputeSettlementReported' ContractExecutionService.java` | Present at line 1810 |

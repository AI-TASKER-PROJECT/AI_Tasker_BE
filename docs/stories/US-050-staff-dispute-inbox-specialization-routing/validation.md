# Validation

## Proof Strategy

Unit tests verify individual components (Admin staff CRUD, routing logic, inbox pagination, authorization gates, attachment access).
Integration tests verify full HTTP paths with JWT roles.
Migration tests verify Flyway V54 creates tables, indexes, and backfills demo data.
Regression tests ensure existing dispute settlement flow is unchanged.

## Test Plan

| Layer | Cases | Files |
| --- | --- | --- |
| Unit | 22 | AdminStaffSpecializationTest (5), StaffDisputeInboxRoutingTest (4), StaffDisputeInboxTest (6), StaffDisputeAuthTest (7) |
| Integration | 0 new | Existing AdminDisputeDashboardIntegrationTest (4), MarketplaceFlowIntegrationTest (1), AdminFlowIntegrationTest (1), AuthFlowIntegrationTest (3) |
| Migration | 1 | Flyway validates 54 migrations, schema at version 54 |
| Regression | 278 total | Full ./mvnw test passes |

## Acceptance Evidence

| AC | Criteria | Test(s) | Result |
|----|----------|---------|--------|
| AC1 | Admin create/update staff with structured domains/skills | AdminStaffSpecializationTest (5 tests) | Pass |
| AC2 | Flyway creates mapping constraints and backfills demo Staff | V54 migration validates on PostgreSQL 16.14 | Pass |
| AC3 | Auto-routing only considers domain-eligible Staff | StaffDisputeInboxRoutingTest.listStaffCandidates_excludesIneligibleStaff | Pass |
| AC4 | Domain count, skill count, workload, staffId produce deterministic ordering | StaffDisputeInboxRoutingTest.listStaffCandidates_deterministicOrdering | Pass |
| AC5 | Manual routing rejects staff with no matching domain | StaffDisputeInboxRoutingTest.routeDispute_manualDomainMismatch_throwsException | Pass |
| AC6 | No matching staff leaves dispute unchanged, no side effects | StaffDisputeInboxRoutingTest.routeDispute_noMatchingStaff_throwsException | Pass |
| AC7 | Candidate API returns only eligible staff with match/workload fields | StaffDisputeInboxRoutingTest.listStaffCandidates_excludesIneligibleStaff | Pass |
| AC8 | Staff inbox pagination, status filtering, JWT-derived identity | StaffDisputeInboxTest (6 tests) | Pass |
| AC9 | Staff A cannot read Staff B's disputes, attachments, or create attachments on other Staff's case | StaffDisputeAuthTest (7 tests: 3 dispute scope + 4 attachment isolation) | Pass |
| AC10 | Assigned staff still decides payout and triggers settlement once | ContractExecutionServiceTest (68 tests, all pass, regression) | Pass |
| AC11 | Swagger/OpenAPI and documentation describe new API and DTOs | openapi-v1.json (142 paths, +7 schemas), swagger overview (154 endpoints), swagger/postman test guides, dispute-flow.md, README | Pass |

## Commands

```
command: ./mvnw test -Dtest=AdminStaffSpecializationTest,StaffDisputeInboxRoutingTest,StaffDisputeInboxTest,StaffDisputeAuthTest -pl .
result: 22 tests, 0 failures, 0 errors
notes: Focused unit tests for US-050

command: ./mvnw test -pl .
result: 278 tests, 0 failures, 0 errors; Flyway validated 54 migrations
notes: Full regression suite including existing dispute tests

command: scripts/bin/harness-cli story verify US-050
result: pass
notes: Story verification with ./mvnw test -pl .
```

## Trace

Trace #88 (initial, minimal) replaced by detailed trace #89 with full fields.

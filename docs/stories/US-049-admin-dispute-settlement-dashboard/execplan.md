# Exec Plan — US-049

## Goal

Implement the Admin Dispute & Settlement Dashboard per SPEC.md: two read-only
Admin API endpoints, settlement report notification, and analytics bug fix.

## Work Phases

### Phase 1 — Harness Setup
- harness-cli intake (#64)
- harness-cli story add (US-049)
- docs/decisions/0021 + harness-cli decision add
- Create high-risk story folder

### Phase 2 — DTOs + Repositories
- 5 DTOs: AdminDisputeListItem, AdminDisputeDetail, AdminDisputeListResponse,
  AdminDisputeFilter, SettlementLedgerEntry
- DisputeRepository: add findByStatusIn+, findAllByOrderByCreatedAtDesc,
  findByAssignedStaffIdOrderByCreatedAtDesc, countByStatusIn
- WalletTransactionRepository: add findByReferenceTypeAndReferenceIdOrderByCreatedAtAsc

### Phase 3 — Service + Controller
- AdminDisputeDashboardService (listDisputes, getDisputeDetail)
- AdminController: add GET /disputes, GET /disputes/{disputeId}
- AdminService: fix analyticsOverview openDisputes filter
- OpenApiConfig: explicit branch for /api/v1/admin/disputes → ADMIN_FLOW

### Phase 4 — Notification Hook
- NotificationService.notifyAdminsOfDisputeSettlement
- ContractExecutionService.executeDisputeSettlementInternal: add post-commit hook

### Phase 5 — Tests
- AdminDisputeDashboardTest (unit, Mockito)
- AdminDisputeDashboardAuthTest (unit, RBAC rejection)
- AdminDisputeDashboardIntegrationTest (MockMvc + PostgreSQL)

### Phase 6 — Validation
- mvnw -Dtest=AdminDisputeDashboardTest,AdminDisputeDashboardAuthTest test
- mvnw -Dtest=AdminDisputeDashboardIntegrationTest test
- mvnw test (full suite)
- docker compose up -d (for PostgreSQL)

### Phase 7 — Documentation
- Swagger overview + openapi-v1.json refresh
- dispute-flow.md + README.md
- validation.md fill concrete evidence

### Phase 8 — Harness Completion
- harness-cli story verify US-049
- harness-cli story update proof flags
- harness-cli trace
- Gate check: query matrix --numeric, placeholder scan, API docs sync

## Stop Conditions

- Destructive migration (no migration planned; skip)
- Breaking existing dispute flow (settlement hook is additive, post-commit)
- Exposing unrelated wallet transactions (detail query is reference-constrained)
- Double escrow release (existing settlement guard unchanged)

# Decision 0021 — Admin Dispute Dashboard

## Status

Accepted

## Context

SPEC.md defines a read-only Admin Dispute & Settlement Dashboard. The dashboard
lets Admin paginate, filter, and inspect all milestone disputes, their Staff
decisions, and settlement outcomes without creating an Admin approval step.

SPEC-MILESTONE-DISPUTER.md v2.3 is the authoritative dispute workflow:
- Assigned Staff is the only role that decides the Expert payout percentage.
- A valid Staff decision triggers settlement automatically.
- Admin may observe, audit, and receive a financial report, but may not approve,
  reject, revise, route, cancel, or alter a milestone dispute or its payout.

## Decision

1. Two new Admin-only read-only GET endpoints under `/api/v1/admin/disputes`:
   - `GET /api/v1/admin/disputes` — paginated/filtered list
   - `GET /api/v1/admin/disputes/{disputeId}` — detail with ledger + attachments

2. A post-commit informational notification `DISPUTE_SETTLEMENT_REPORTED` sent
   to all Admin accounts after a successful Staff-triggered settlement.

3. No new database tables or columns. Reuse existing `disputes`,
   `wallet_transactions`, `case_attachments`, `contracts`, `milestones`,
   `contract_milestones`, and `staff` tables.

4. No modification to the automatic Staff decision → settlement flow.
   The notification is hooked into `ContractExecutionService.executeDisputeSettlementInternal`
   after the ledger commit, not before.

5. No Admin approval queue, override, appeal, or payout percentage adjustment.

6. Admin analytics overview bug in `AdminService.analyticsOverview()` (filtering
   `openDisputes` with legacy strings `"Open"`/`"UnderReview"` instead of
   `DisputeEntity` active status constants) is fixed in the same US-049.

## Consequences

- Admin gains auditable visibility into dispute outcomes without changing the
  settlement authority model.
- The notification introduces a new `type` constant `DISPUTE_SETTLEMENT_REPORTED`
  processed through the existing `NotificationService` / WebSocket pipeline.
- No schema migration is required (indexes are optional and evidence-driven).
- Swagger/OpenAPI, test guide, and README gain 2 new documented routes.

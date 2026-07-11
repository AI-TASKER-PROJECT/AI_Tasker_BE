# Overview — US-049

## Current Behavior

1. Admin can list disputes only one contract at a time through
   `GET /api/v1/contracts/{contractId}/disputes`.
2. Admin can inspect one dispute through `GET /api/v1/disputes/{disputeId}`.
3. Wallet and audit records exist, but there is no single Admin API that joins a
   dispute with its Staff decision and settlement amounts.
4. Settlement notifications go to Business and Expert. Admin is notified for a
   Staff-SLA escalation, not for a completed dispute settlement.
5. `AdminService.analyticsOverview()` openDisputes count uses legacy filter
   strings `"Open"`/`"UnderReview"` that do not match `DisputeEntity` active
   status constants (`PENDING_SELF_RESOLVE`, `ESCALATION_REQUESTED`,
   `STAFF_REVIEWING`, `STAFF_DECIDED`).

## Target Behavior

1. Admin can paginate and filter all disputes through
   `GET /api/v1/admin/disputes` with query parameters: page, size, status,
   assignedStaffId, from, to, q.
2. Admin can inspect dispute detail through
   `GET /api/v1/admin/disputes/{disputeId}` including case attachments,
   settlement ledger entries, Staff report, and audit timestamps.
3. Non-Admin receives authorization failure for both new dashboard endpoints.
4. A successful Staff-triggered settlement sends exactly one informational
   Admin report (`type=DISPUTE_SETTLEMENT_REPORTED`) after commit.
5. Unsettled disputes expose `null` settlement fields; no estimated payout is shown.
6. `AdminService.analyticsOverview()` openDisputes count uses correct
   `DisputeEntity` active status constants.

## Affected Users

- Admin (read-only dashboard, settlement report notification).
- No change for Business, Expert, or Staff roles.

## Affected Product Docs

- `SPEC.md` (this spec)
- `SPEC-MILESTONE-DISPUTER.md` v2.3 (authoritative, unchanged)
- `docs/swagger-api-overview.md`
- `docs/flows/dispute-flow.md`
- `docs/openapi/openapi-v1.json`
- `README.md` §7

## Non-Goals

- No Admin approval queue for Staff decisions or settlement.
- No Admin override, appeal, or payout percentage adjustment.
- No modification to the automatic Staff decision → settlement flow.
- No deletion/deprecation of the legacy Admin settlement endpoint.
- No frontend implementation; this contract defines the backend dashboard API.
- No new database tables or columns.

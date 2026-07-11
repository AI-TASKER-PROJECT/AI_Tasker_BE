# Design — US-049

## Domain Model

No new tables or columns. Reuse existing entities:

- `DisputeEntity` (disputes) — case state, initiator, Staff decision, payout/refund, timestamps
- `ContractEntity` / `MilestoneEntity` / `ContractMilestoneEntity` — identity, escrow, settlement guard
- `StaffEntity` — assigned Staff summary
- `CaseAttachmentEntity` — dispute evidence (owner_type=DISPUTE)
- `WalletTransactionEntity` — settlement ledger evidence (reference_type=DISPUTE)

No Flyway migration is required. Index creation is evidence-driven: only add if
query-plan analysis shows a need.

## Application Flow

### 1. Admin Dispute List (read-only)

```
GET /api/v1/admin/disputes?page=0&size=20&status=RESOLVED&assignedStaffId=12&q=42
  → AdminController.listDisputes(filter)
    → AdminDisputeDashboardService.listDisputes(filter)
      → accessService.requireRole("ADMIN")
      → disputeRepository.findAllByOrderByCreatedAtDesc()  (or filtered query)
      → stream → filter by status/staff/date/q → skip(offset) → limit(size)
      → map each DisputeEntity → AdminDisputeListItem.from(entity, contract, milestone, staff)
      → build AdminDisputeListResponse(content, page, size, totalElements, totalPages)
```

Filter rules:
- `status`: exact match on `DisputeEntity.status` string constant
- `assignedStaffId`: filter by `dispute.assignedStaffId`
- `from`/`to`: filter `dispute.createdAt` in range [from, to]
- `q`: search over `disputeId.toString()` and `contractId.toString()` (parameterized)
- Default ordering: `createdAt DESC, disputeId DESC`
- Validation errors for: invalid dates, invalid status, `from > to`, `size < 1 || size > 100`

### 2. Admin Dispute Detail (read-only)

```
GET /api/v1/admin/disputes/{disputeId}
  → AdminController.getDisputeDetail(disputeId)
    → AdminDisputeDashboardService.getDisputeDetail(disputeId)
      → accessService.requireRole("ADMIN")
      → disputeRepository.findById(disputeId) or 404
      → load contract, milestone, contractMilestone, staff
      → load caseAttachments by ownerType=DISPUTE, ownerId=disputeId
      → load walletTransactions by referenceType=DISPUTE, referenceId=disputeId.longValue()
      → map → AdminDisputeDetail (extends list fields)
```

For unsettled disputes (`staffDecisionPercentage == null`), settlement fields are `null`.
For settled disputes, settlement amounts come from the dispute row (computed during
settlement execution by `executeDisputeSettlementInternal`).

### 3. Settlement Report Notification (post-commit)

Hooked into `ContractExecutionService.executeDisputeSettlementInternal` after:
- Ledger rows are written
- Dispute status is set to `RESOLVED`
- `settlementExecutedAt` and `settlementWalletTransactionId` are set

```java
// After settlement commit but before method returns:
notifyAdminsOfDisputeSettlement(dispute, contract, milestone, expertPayout, businessRefund, settlementTxId, actorAccountId);
```

`NotificationService.notifyAdminsOfDisputeSettlement`:
- Iterates `accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")`
- For each admin, calls `createAndPush` with:
  - `type = "DISPUTE_SETTLEMENT_REPORTED"`
  - `targetUrl = "/admin/disputes/" + disputeId`
  - metadata: `{ disputeId, contractId, milestoneId, expertPayoutPercentage, expertPayoutAmount, businessRefundAmount, settlementWalletTransactionId }`
- Emitted at most once per settlement (guaranteed by settlement idempotency guard)

## Interface Contract

### New Routes

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | `/api/v1/admin/disputes` | ADMIN | Paginated/filtered dispute list |
| GET | `/api/v1/admin/disputes/{disputeId}` | ADMIN | Dispute detail with evidence/ledger |

### New Notification Type

| Type | Purpose |
|------|---------|
| `DISPUTE_SETTLEMENT_REPORTED` | Informational audit report after settlement commit |

### Unchanged Routes

All existing dispute and admin routes remain unchanged. Legacy admin settlement
endpoint is not deprecated or removed.

## Data Safety

- Dashboard is read-only; no mutation of disputes, wallets, or contracts.
- Notification reads committed dispute/ledger state; it does not fabricate amounts.
- Ledger queries are constrained by `reference_type = 'DISPUTE'` and `reference_id`.
- Detail response only includes attachments belonging to the requested dispute.
- No N+1: bulk-load entities in one pass, map in memory.
- No entity graph exposed directly in API response.
- Settlement amounts come from `DisputeEntity` fields set by settlement execution,
  not from client-supplied values.

## Hard Gates

| Gate | Rule |
|------|------|
| Authorization | `accessService.requireRole("ADMIN")` on every service method |
| Financial invariants | `expertPayoutAmount + businessRefundAmount = milestoneEscrowAmount` for settled disputes |
| Data isolation | Detail only returns attachments/ledger rows belonging to its dispute |
| Idempotency | Settlement report emitted exactly once; duplicate settlement blocked by existing guard |
| No approval | Dashboard never holds a valid settlement waiting for Admin action |

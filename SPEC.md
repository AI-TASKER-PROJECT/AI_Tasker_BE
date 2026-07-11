# Admin Dispute & Settlement Dashboard

## Status

planned

## Lane

high-risk (financial data, authorization, public API contract, existing dispute flow)

## Product Contract

Admin needs a read-only operational dashboard for milestone disputes and their
financial settlement. The dashboard makes the full case and settlement outcome
auditable without creating an Admin approval step.

`SPEC-MILESTONE-DISPUTER.md` v2.3 remains authoritative for the dispute
workflow:

- Assigned Staff is the only role that decides the Expert payout percentage.
- A valid Staff decision triggers settlement automatically.
- Admin may observe, audit, and receive a financial report, but may not approve,
  reject, revise, route, cancel, or alter a milestone dispute or its payout.
- The dashboard must never hold a valid settlement waiting for Admin action.

## User Stories

**As an Admin**, I want to see all milestone disputes in one dashboard, filter
them by operational state and Staff owner, and inspect the resulting payout and
refund so I can monitor risk and reconcile platform financial activity.

**As an Admin**, I want to receive an informational settlement report when a
dispute is resolved, so I can investigate unusual financial outcomes without
becoming a second decision-maker.

## Current Behavior

1. Admin can list disputes only one contract at a time through
   `GET /api/v1/contracts/{contractId}/disputes`.
2. Admin can inspect one dispute through `GET /api/v1/disputes/{disputeId}`.
3. Wallet and audit records exist, but there is no single Admin API that joins a
   dispute with its Staff decision and settlement amounts.
4. Settlement notifications go to Business and Expert. Admin is notified for a
   Staff-SLA escalation, not for a completed dispute settlement.
5. A legacy Admin settlement endpoint may still exist. It is not an approval
   workflow and is outside the scope of this dashboard change; removal or
   deprecation requires a separate compatibility decision.

## Target Behavior

### 1. Admin Dispute List

Add an Admin-only API:

```text
GET /api/v1/admin/disputes
```

Query parameters:

| Parameter | Type | Default | Meaning |
|---|---|---:|---|
| `page` | integer >= 0 | `0` | Zero-based page number |
| `size` | integer 1..100 | `20` | Page size |
| `status` | enum | — | Exact dispute status |
| `assignedStaffId` | integer | — | Assigned Staff identifier |
| `from` | ISO-8601 datetime | — | Created-at lower bound, inclusive |
| `to` | ISO-8601 datetime | — | Created-at upper bound, inclusive |
| `q` | string, max 100 | — | Safe exact/partial search over dispute ID and contract ID only |

Default ordering is newest dispute first by `createdAt DESC`, then `disputeId DESC`.
Invalid dates, invalid status, invalid pagination, or `from > to` return a clear
validation error. The API must not interpolate `q` into raw SQL.

Response is a paginated purpose-built DTO, never a raw JPA entity:

```json
{
  "content": [
    {
      "disputeId": 8,
      "contractId": 42,
      "milestoneId": 103,
      "status": "RESOLVED",
      "initiatedBy": "BUSINESS",
      "initiationType": "BUSINESS_REJECTED_DELIVERABLE",
      "createdAt": "2026-07-11T10:00:00",
      "assignedStaff": { "staffId": 12, "displayName": "Nguyen Van A" },
      "staffDecidedAt": "2026-07-12T09:30:00",
      "expertPayoutPercentage": 70,
      "expertPayoutAmount": 7000000,
      "businessRefundAmount": 3000000,
      "settlementExecutedAt": "2026-07-12T09:30:01",
      "settlementWalletTransactionId": 1234
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

For disputes not yet settled, the payout, refund, settlement timestamp, and
settlement transaction ID are `null`. The API must not fabricate financial
amounts before Staff has made a decision.

### 2. Admin Dispute Detail

Add an Admin-only API:

```text
GET /api/v1/admin/disputes/{disputeId}
```

The detail response must include the list summary fields plus:

- dispute reason, escalation reason, evidence summary, and case attachments;
- Staff report and decision note when present;
- milestone escrow amount and settlement source fields;
- immutable settlement ledger entries linked by `reference_type = DISPUTE` and
  `reference_id = disputeId`;
- audit-relevant timestamps: created, routed, Staff-decided, resolved, and
  settlement-executed.

The detail response is read-only. It must not expose sensitive data from other
contracts or unrelated wallet transactions.

### 3. Settlement Report for Admin

After a successful dispute settlement transaction commits, the system sends an
informational notification/event to every Admin:

```text
type = DISPUTE_SETTLEMENT_REPORTED
```

The notification payload contains `disputeId`, `contractId`, `milestoneId`,
`expertPayoutPercentage`, `expertPayoutAmount`, `businessRefundAmount`, and
`settlementWalletTransactionId`.

Rules:

- It is an audit/reporting event, not an approval request.
- It is emitted only after ledger rows, dispute state, and settlement timestamp
  are persisted successfully.
- It is emitted at most once for a settlement; retries after an already released
  escrow must not create duplicate reports.
- Business and Expert settlement notifications remain unchanged.

## Authorization Rules

| Action | Admin | Staff | Business | Expert |
|---|---|---|---|---|
| List Admin dashboard disputes | Yes | No | No | No |
| View Admin dashboard dispute detail | Yes | No | No | No |
| Receive settlement report event | Yes | No | No | No |
| Decide payout percentage | No | Assigned Staff only | No | No |
| Approve/revise settlement | No | No | No | No |
| Route or cancel milestone dispute | No | Staff workflow only | Initiator cancellation before route only | Initiator cancellation before route only |

Existing participant and assigned-Staff read APIs stay unchanged. The new Admin
endpoints are a dashboard projection and must use an explicit `ADMIN` role gate.

## Data Contract And Query Rules

The dashboard joins only records belonging to the same dispute:

- `disputes` for case state, initiator, Staff decision, payout/refund fields,
  and timestamps;
- `contracts`, `milestones`, and `contract_milestones` for identity, escrow,
  and settlement guard/source information;
- `staff` for the assigned Staff summary;
- `case_attachments` for dispute evidence in the detail response;
- `wallet_transactions` for settlement ledger evidence, constrained by
  `reference_type = DISPUTE` and the dispute ID.

Implement a projection/query service or repository query that avoids N+1
lookups. Do not return an entity graph directly. Reuse the existing wallet
metadata and ledger references; do not introduce duplicate financial truth.

## Financial Invariants

- `expertPayoutAmount + businessRefundAmount = milestoneEscrowAmount` for every
  settled dispute.
- No milestone escrow may be released twice.
- A dashboard query and an Admin notification never move wallet balances.
- Admin reporting cannot delay or alter Staff-triggered automatic settlement.
- Settlement report data must correspond to the committed dispute and ledger
  records, not client-supplied amounts.

## Acceptance Criteria

| # | Criteria | Verification |
|---|---|---|
| AC1 | Admin can paginate and filter all disputes through `GET /api/v1/admin/disputes`. | Controller/service integration test |
| AC2 | Non-Admin receives authorization failure for both new dashboard endpoints. | RBAC tests |
| AC3 | List ordering, pagination, status/Staff/date/ID filters are deterministic and correct. | Repository/service tests |
| AC4 | Settled response amounts and transaction ID match persisted dispute and ledger data. | Service/integration test |
| AC5 | Unsettled disputes expose `null` settlement fields; no estimated payout is shown. | Service test |
| AC6 | Detail returns only evidence and ledger rows belonging to its dispute. | Authorization/data-isolation test |
| AC7 | A successful Staff-triggered settlement sends exactly one Admin informational report after commit. | Settlement/notification test |
| AC8 | Retrying a previously settled case cannot duplicate escrow release or Admin report. | Idempotency test |
| AC9 | Admin cannot approve, change payout percentage, route, or cancel a milestone dispute through this feature. | Route/RBAC regression tests |
| AC10 | Swagger/OpenAPI and dashboard documentation describe the new read-only API and notification type. | Docs/OpenAPI validation |

## Execution Plan

| Step | Area | Action |
|---|---|---|
| 1 | Harness | Create a high-risk story and define financial/RBAC proof before implementation. |
| 2 | DTOs | Add paginated list and detail response DTOs plus filter request/query object. |
| 3 | Persistence | Add efficient projection queries/repositories with indexed, parameterized filters. Add an additive index only if query-plan evidence requires it. |
| 4 | Service | Implement explicit Admin gates, list/detail mapping, data isolation, and no-N+1 retrieval. |
| 5 | Controller | Add the two `/api/v1/admin/disputes` GET routes. |
| 6 | Notification | Add idempotent post-commit Admin settlement-report notification. |
| 7 | Tests | Cover AC1–AC9, including settlement financial invariant and retry behavior. |
| 8 | Documentation | Update Swagger/OpenAPI, API overview/test guide, dispute flow, story proof, and Harness trace. |

## Non-Goals

- No Admin approval queue for Staff decisions or settlement.
- No Admin override, appeal, or payout percentage adjustment.
- No modification to the automatic Staff decision → settlement flow.
- No deletion/deprecation of the legacy Admin settlement endpoint in this change;
  that requires a separately approved compatibility story.
- No frontend implementation; this contract defines the backend dashboard API.

## Risk Checklist

| Flag | Applies? | Reason |
|---|---|---|
| Financial | Yes | Reports committed escrow payout/refund and must preserve one-release invariants. |
| Authorization | Yes | New Admin-only aggregate access to dispute and financial records. |
| Public contract | Yes | Two new public GET APIs and a notification type. |
| Existing behavior | Yes | Extends the settlement completion path with post-commit reporting. |
| Data model | Maybe | Prefer existing fields; add only evidence-backed indexes or additive notification persistence if needed. |

**Classification:** high-risk. Execute focused and full tests, validate ledger
invariants against PostgreSQL, then record a high-risk Harness trace.

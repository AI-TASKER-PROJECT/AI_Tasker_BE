# 0037 Wallet History Scope Split

Date: 2026-07-21

## Status

Accepted

## Context

Admin wallet history previously used `GET /api/v1/admin/wallet/transactions`
to show a filtered platform-wide projection over all `wallet_transactions`.
That view is useful for reviewing user finance activity, but it is not the same
thing as the platform wallet's own ledger. It also hid internal
`PLATFORM_REVENUE_CREDIT` rows, so the platform wallet's own revenue-changing
ledger was not directly visible.

User wallet history already returned a transparent DTO, but several
reconciliation fields stored in the ledger or related rows were still not
projected to clients.

## Decision

Keep `wallet_transactions` as the durable ledger and split admin read models by
scope:

- `GET /api/v1/admin/wallet/platform-ledger` returns only the platform/Admin
  wallet ledger rows and includes platform-balance-changing events such as
  `PLATFORM_REVENUE_CREDIT`.
- `GET /api/v1/admin/wallet/user-activity-transactions` returns the
  platform-wide user activity history and filters duplicate internal transfer
  legs.
- `GET /api/v1/admin/wallet/transactions` remains as a compatibility alias for
  user activity history.

Expand `WalletTransactionHistoryResponse` with reconciliation fields such as
history scope, transaction category, wallet id/type, actor role, platform
balance flag, metadata, masked bank account number, provider details, milestone
context, and gross/fee/net amounts. Existing fields remain present.

Do not add a new table or rewrite historical ledger rows.

## Consequences

Positive:

- Admin can distinguish platform wallet ledger from user financial activity.
- Platform revenue credit rows are visible where they actually belong.
- User and admin histories expose enough context for finance support and demo
  review without frontend-specific joins.

Tradeoffs:

- The old admin route name remains broad for compatibility, so docs must make
  clear that it is the user-activity view.
- Legacy commission-only revenue in `transactions` still has no synthetic
  `wallet_transactions` row; synchronization keeps aggregate totals correct
  without inventing historical ledger snapshots.

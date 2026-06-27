# Platform Wallet History Uses Wallet Ledger Projection

Date: 2026-06-27

## Status

Accepted

## Context

Admin needs a separate platform-wallet history API that explains money movement
clearly in Vietnamese while preserving IDs for reconciliation. The system
already stores durable wallet events in `wallet_transactions`, and user wallet
history already enriches those rows into `WalletTransactionHistoryResponse`.

Some wallet operations write multiple ledger rows for one business event, such
as moving available balance into escrow or holding. Showing every leg in the
platform history would make admin history noisy and harder to reconcile with
business actions.

## Decision

Expose `GET /api/v1/admin/wallet/transactions` as an ADMIN-only read API.

Use `wallet_transactions` as the source of truth and return
`WalletTransactionHistoryResponse`. For platform display, filter transfer-style
ledger rows to one business event row:

- Deposit hold shows the `HOLD` row.
- Withdrawal hold shows the `HOLD` row.
- Deposit refund and withdrawal rejection show the `CREDIT` row.
- Membership, credit purchase, top-up, withdrawal approval, and deposit
  resolution show their natural posted row.

Do not add a new table or rewrite historical ledger descriptions.

## Alternatives Considered

1. Create a separate admin history table. Rejected because it would duplicate
   ledger truth and add reconciliation risk.
2. Return all raw ledger rows to admin. Rejected because transfer operations
   would appear as duplicate business events.
3. Let frontend render all text. Rejected because backend owns finance joins
   and should keep Vietnamese business wording consistent.

## Consequences

Positive:

- Admin gets a dedicated transparent finance history API.
- Existing ledger and reconciliation fields remain intact.
- Platform history is easier to scan because one displayed row maps to one
  business event.

Tradeoffs:

- The endpoint is currently unpaginated, matching current wallet history style.
- STAFF access remains out of scope because the existing system-wallet service
  is ADMIN-only.

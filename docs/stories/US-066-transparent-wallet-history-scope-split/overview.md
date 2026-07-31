# Transparent Wallet History Scope Split

## Current Behavior

User wallet history exposes transparent Vietnamese presentation fields, but the
DTO does not project every reconciliation field already available in the ledger
or related payment/withdrawal/milestone records.

Admin wallet history is exposed through `GET /api/v1/admin/wallet/transactions`
as a platform-wide user activity projection over all wallet transactions. That
view is useful, but it is named and positioned like the platform wallet's own
ledger. It also hides internal platform revenue credit legs, so the platform
wallet's balance-changing rows are not visible as their own history.

## Target Behavior

- User wallet history returns a richer transparent DTO with wallet, actor,
  payment provider, metadata, amount breakdown, milestone, and masked bank
  account context where available.
- Admin can separately read:
  - the platform wallet's own ledger;
  - the user activity transaction history across the platform.
- The existing admin wallet transaction route remains compatible by returning
  the user activity projection.

## Affected Users

- BUSINESS and EXPERT users reviewing wallet history.
- ADMIN users reviewing platform wallet balances and platform-wide user
  finance activity.
- Finance/support reviewers reconciling wallet events.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `README.md`

## Non-Goals

- No schema migration.
- No rewrite or backfill of historical ledger rows.
- No frontend implementation.
- No provider-backed refund or payout reconciliation redesign.

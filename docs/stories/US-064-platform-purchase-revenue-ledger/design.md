# Design

## Domain Model

Platform purchase revenue has two ledger legs:

1. `PURCHASER_AVAILABLE_DEBIT` on the Business or Expert wallet.
2. `PLATFORM_REVENUE_CREDIT` on the first Admin platform wallet.

Both rows use the same non-null `operation_key`. The existing partial unique
index on `(operation_key, operation_leg)` prevents duplicate legs.

Platform `totalRevenue` is the sum of:

- successful contract commission fees; and
- posted `AVAILABLE` debits whose type is `MEMBERSHIP_PURCHASE` or
  `CREDIT_PURCHASE`.

The source debit is used for historical aggregation so newly added platform
credit rows are not counted twice.

## Application Flow

For each successful package or credit purchase:

1. Resolve the platform Admin account before mutating balances.
2. Confirm purchaser available balance.
3. Debit the purchaser with a generated purchase operation key.
4. Credit Admin available balance and increment Admin `totalRevenue` using the
   same key with a different leg.
5. Persist membership/quota state and audit data.
6. Commit all changes in the existing service transaction. Any failure rolls
   back both wallet legs and the entitlement grant.

All purchase paths acquire the purchaser wallet before the platform wallet, so
the lock order is stable across this operation type.

## Interface Contract

No route or request/response shape changes.

- Membership purchase keeps returning `PaymentActionResponse<MembershipPurchaseEntity>`.
- Credit purchase keeps returning `PaymentActionResponse<UserQuotaEntity>`.
- Admin wallet endpoints return corrected balances through existing fields.

## Data Model

- Reuse `wallet_transactions.operation_key` and `operation_leg` from V56.
- Add a partial PostgreSQL index for posted purchase-debit revenue aggregation.
- Historical source rows remain unchanged. `syncWallet()` aggregates them and
  persists the corrected Admin wallet totals.

## UI / Platform Impact

The Admin platform wallet starts showing package and credit revenue. Platform
purchase history remains one user-facing event per purchase by hiding the
internal `PLATFORM_REVENUE_CREDIT` counterpart.

## Observability

- Existing audit actions for membership and credit purchase remain unchanged.
- Raw `wallet_transactions` contains both accounting legs for new purchases.
- `operation_key` links the purchaser and platform rows.

## Alternatives Considered

1. Only add package and credit amounts in `syncWallet()`. Rejected because live
   purchases would still lack a platform counter-entry.
2. Only credit Admin during purchase. Rejected because the next sync would
   overwrite the balance with commission-only revenue.
3. Insert synthetic historical platform-credit rows. Rejected because accurate
   historical balance snapshots cannot be reconstructed from the current
   legacy commission model; source-debit aggregation repairs totals without
   inventing ledger state.

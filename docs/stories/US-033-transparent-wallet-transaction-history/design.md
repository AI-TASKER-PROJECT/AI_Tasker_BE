# Design

## Domain Model

`wallet_transactions` remains the durable ledger. Related rows such as
`contract_deposits`, `withdrawal_requests`, `membership_purchases`,
`payment_order`, `contracts`, `jobs`, profiles, and accounts are used only to
enrich the read response.

## Application Flow

`PaymentWalletService.listCurrentWalletTransactions()` still reads the current
account ledger in newest-first order, but maps every row to
`WalletTransactionHistoryResponse`.

The mapper resolves known finance flows:

- PayOS wallet top-up.
- Membership purchase.
- Credit purchase.
- Contract security deposit hold/refund/admin resolution.
- Withdrawal request hold/admin approval/admin rejection.

## Interface Contract

`GET /api/wallet/transactions` now returns a list of
`WalletTransactionHistoryResponse`.

Raw fields remain:

- `transactionType`
- `direction`
- `balanceType`
- `status`
- `referenceType`
- `referenceId`
- `rawDescription`

Display fields are added:

- `title`
- `description`
- `actorName`
- `businessName`
- `expertName`
- `jobTitle`
- `contractTitle`
- withdrawal bank/admin context
- membership/payment order context

## Data Model

No migration is required. Repository lookup methods were added for existing
columns that already link wallet transactions to contract deposits,
withdrawal requests, and membership purchases.

## UI / Platform Impact

Frontend can render `title` and `description` directly for wallet history,
while keeping raw fields for filters or details.

## Observability

Audit log behavior remains durable. New credit purchase audit actions are
written as `Mua lượt sử dụng`; historical `Mua credit` rows map to that clean
label on read.

## Alternatives Considered

1. Keep returning raw ledger rows and let frontend join context. Rejected
   because the backend owns the finance relationships and already has access
   to the repositories needed to explain the event safely.
2. Rewrite ledger descriptions in the database. Rejected because ledger rows
   are durable evidence and should not be mutated for presentation.

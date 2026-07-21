# Design

## Domain Model

`wallet_transactions` remains the source of truth for actual balance movement.
The read DTO is expanded to project existing data:

- wallet identity: `systemWalletId`, `walletType`, `walletOwnerRole`;
- scope/category: `historyScope`, `transactionCategory`;
- actor/counterparty: account id, role, name;
- reconciliation: `operationKey`, `operationLeg`, `metadata`;
- money: gross, fee, net, currency, balance before/after;
- references: contract, job, milestone, withdrawal, package, payment provider.

## Application Flow

User flow:

1. `GET /api/wallet/transactions`.
2. Read current account ledger.
3. Deduplicate withdrawal and known operation-key transfer groups.
4. Return `historyScope=USER_WALLET`.

Admin user-activity flow:

1. `GET /api/v1/admin/wallet/user-activity-transactions`.
2. Read all wallet transactions.
3. Filter duplicate internal transfer legs and platform revenue counterpart
   legs.
4. Return `historyScope=USER_ACTIVITY`.

Admin platform-ledger flow:

1. `GET /api/v1/admin/wallet/platform-ledger`.
2. Resolve the first ADMIN account as the platform wallet owner.
3. Read only that account's wallet ledger rows.
4. Return `historyScope=PLATFORM_WALLET` and keep balance-changing platform
   revenue legs visible.

## Interface Contract

New routes:

- `GET /api/v1/admin/wallet/platform-ledger`
- `GET /api/v1/admin/wallet/user-activity-transactions`

Compatibility route:

- `GET /api/v1/admin/wallet/transactions`

All return `ApiResponse<List<WalletTransactionHistoryResponse>>`.

## Data Model

No migration. Existing columns and repositories are used.

## UI / Platform Impact

Frontend/admin UI can show two tabs:

- Platform wallet ledger.
- User activity transactions.

Existing clients using `/api/v1/admin/wallet/transactions` continue receiving
the platform-wide user activity list.

## Observability

Read endpoints do not create audit rows. Existing write-action audit rows and
ledger records remain the durable evidence. Harness trace records validation.

## Alternatives Considered

1. Rename the existing route and remove compatibility. Rejected because it
   risks breaking current clients.
2. Create a new read table. Rejected because it duplicates ledger truth.
3. Backfill synthetic platform ledger rows. Rejected because historical
   balance snapshots would be invented.

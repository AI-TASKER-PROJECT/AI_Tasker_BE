# Design

## Domain Model

`wallet_transactions` remains the durable ledger. The admin platform history is
a read projection over existing rows. For transfer-style operations that write
two ledger rows, the platform history returns the event row that best represents
the business action:

- `CONTRACT_SECURITY_DEPOSIT_HOLD`: `HOLD` on `ESCROW`.
- `WITHDRAW_HOLD`: `HOLD` on `HOLDING`.
- `CONTRACT_SECURITY_DEPOSIT_REFUND`: `CREDIT` to `AVAILABLE`.
- `WITHDRAW_REJECTED`: `CREDIT` to `AVAILABLE`.
- Other transaction types: the posted ledger row.

## Application Flow

`AdminController` exposes `GET /api/v1/admin/wallet/transactions`.
`PaymentWalletService.listPlatformWalletTransactions()` checks ADMIN role, reads
wallet transactions ordered newest first, filters duplicate ledger legs, and
maps each row through the existing enriched history mapper.

## Interface Contract

Route:

- `GET /api/v1/admin/wallet/transactions`

Auth:

- Bearer JWT with ADMIN role.

Response:

- `ApiResponse<List<WalletTransactionHistoryResponse>>`
- Each item includes raw ledger fields plus Vietnamese `title`, `description`,
  actor/counterparty names, related package/contract/job/withdrawal/bank/admin
  fields, and technical IDs for reconciliation.

## Data Model

No schema migration. A Spring Data repository method reads all wallet
transactions by `createdAt desc`.

## UI / Platform Impact

Swagger inventory and manual test guide list the new admin endpoint in the
admin wallet section, after the wallet snapshot endpoint.

## Observability

The endpoint is read-only. Existing ledger rows, audit logs from write actions,
and Harness trace provide evidence.

## Alternatives Considered

1. Create a new platform history table. Rejected because current ledger already
   stores the durable money movement and related references.
2. Return raw ledger rows only. Rejected because admin needs transparent
   Vietnamese explanations without frontend-specific joining.

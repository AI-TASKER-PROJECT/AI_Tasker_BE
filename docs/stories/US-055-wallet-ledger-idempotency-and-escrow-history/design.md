# Design

## Domain Model

`WalletTransactionEntity` gains `operationKey` and `operationLeg`. The pair
identifies one persisted accounting leg inside one logical financial
operation.

## Application Flow

`WalletLedgerService` becomes the only writer responsible for inserting
idempotent legs. Same-wallet hold/release flows verify the full expected leg
set before mutating balances. Cross-wallet settlements reuse the same
`operationKey` with distinct `operationLeg` values per affected account leg.

## Interface Contract

`GET /api/wallet/transactions` keeps the response shape backward compatible and
adds optional `operationKey` for troubleshooting. User history groups known
multi-leg escrow/deposit operations into one row. Admin history stays raw.

## Data Model

Flyway `V56__wallet_operation_identity.sql` adds nullable operation identity
columns plus a partial unique index on `(operation_key, operation_leg)`.

## UI / Platform Impact

Frontend wallet history receives fewer duplicate-looking entries for escrow and
deposit flows.

## Observability

Operation identity is visible in wallet history DTOs and raw admin ledger rows,
which improves audit/debugging without deleting ledger detail.

## Alternatives Considered

1. Unique index on `reference_type + reference_id + transaction_type` was
   rejected because valid multi-leg transfers intentionally share those fields.

# Transparent Wallet Transaction History And Vietnamese Realtime Text

## Current Behavior

`GET /api/wallet/transactions` returns raw `WalletTransactionEntity` rows. The
frontend can see ledger codes such as `CONTRACT_SECURITY_DEPOSIT_HOLD`,
`WITHDRAW_REJECTED`, `referenceType`, and `referenceId`, but it cannot reliably
show who deposited, which contract/job was affected, who withdrew money, which
bank account was used, or why a withdrawal failed.

Some realtime notification titles/messages were also written without
Vietnamese diacritics.

## Target Behavior

Wallet transaction history is returned as a read DTO with Vietnamese
presentation fields and transparent business context. Raw ledger codes remain
available for filtering/debugging, but the visible fields explain the event in
plain Vietnamese.

Realtime notification titles/messages for the active websocket flows use
Vietnamese with diacritics. Audit writes for wallet credit purchases use a
clean Vietnamese action, and legacy no-diacritic actions continue to translate
on read.

## Affected Users

- BUSINESS and EXPERT users viewing wallet transaction history.
- ADMIN users reviewing withdrawal/deposit and audit evidence.
- Users receiving realtime websocket notifications.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Non-Goals

- No wallet ledger schema change.
- No rewrite of historical notification/audit rows.
- No frontend layout change in this backend slice.

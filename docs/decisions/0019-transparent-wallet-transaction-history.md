# Return Transparent Vietnamese Wallet Transaction History

Date: 2026-06-26

## Status

Accepted

## Context

Wallet history previously returned raw `wallet_transactions` rows. That is
useful for ledger evidence, but weak for UI transparency: users cannot tell
which business deposited money, which contract was secured, who requested a
withdrawal, which bank account was used, or why admin rejected it without
extra frontend-specific joins.

Some websocket notification text also used no-diacritic Vietnamese, which made
active realtime messages look unfinished.

## Decision

Keep `wallet_transactions` as the durable ledger but return a read DTO from
`GET /api/wallet/transactions`. The DTO preserves raw ledger codes and adds
Vietnamese `title`, `description`, and related business context.

Normalize active websocket notification titles/messages to Vietnamese with
diacritics. Write new credit-purchase audit rows as `Mua lượt sử dụng` and
translate historical `Mua credit` rows on read.

## Alternatives Considered

1. Let frontend join and render all finance context. Rejected because backend
   owns finance relationships and can prevent inconsistent rendering across
   clients.
2. Rewrite old ledger descriptions. Rejected because ledger history should
   remain durable evidence.

## Consequences

Positive:

- Wallet history can be rendered directly with transparent finance context.
- Raw ledger fields remain available for filters/debugging.
- Websocket messages read consistently in Vietnamese.

Tradeoffs:

- API clients using `/api/wallet/transactions` must adapt from entity rows to
  `WalletTransactionHistoryResponse`.

## Follow-Up

- Frontend can move raw ledger fields into detail/expandable views if needed.

# 0009 Payment Wallet Lifecycle

Date: 2026-06-20

## Status

Accepted

## Context

`SPEC-PAYMENT.md` expands the backend from PayOS wallet top-up into a broader
payment MVP: internal wallet balance, membership and credit purchases, job and
proposal quota, contract security deposit, admin deposit resolution, and manual
withdrawal review.

The existing contract flow activated work as soon as both parties signed the
contract and NDA. The new payment spec requires a 20% security deposit before
work starts, and it explicitly keeps `transactions` separate from
`wallet_transactions`.

## Decision

Keep PayOS only for wallet top-up. All internal purchases, quota, deposit, and
withdrawal movements use `WalletLedgerService` and create
`wallet_transactions` rows.

Contracts that have both contract signatures and both NDA signatures move to
`PendingDeposit`. They become `Active`, and the job becomes `IN_PROGRESS`, only
after the Business pays the 20% security deposit from wallet available balance.
Admin deposit finalization closes the contract after refund/resolution.

## Alternatives Considered

1. Keep signature-only activation. Rejected because it bypasses the security
   deposit gate in the payment spec.
2. Process internal purchases through `payment_order`. Rejected because
   `payment_order` is provider-facing and should remain PayOS top-up only.
3. Merge `transactions` with wallet ledger rows. Rejected because the spec uses
   them for different questions: workflow event versus balance movement.

## Consequences

Positive:

- Real wallet balance changes have one service boundary and durable ledger
  evidence.
- Contract execution cannot begin before the security deposit is held.
- Existing PayOS sync remains isolated from internal wallet spending.

Tradeoffs:

- Existing clients that expect `Active` immediately after signatures must handle
  `PendingDeposit`.
- Additional schema and service tests are required to keep wallet state
  consistent.

## Follow-Up

- Add full API integration tests for package purchase, publish, proposal,
  deposit refund, and withdrawal flows once stable test fixtures exist.
- Revisit milestone escrow/payout ledger replacement in a later phase.

# Overview

## Current Behavior

PayOS wallet top-up already creates a `payment_order`, syncs provider status by
order code, and credits `wallet_transactions` once. Wallet balance exists on
`system_wallet`, but only top-up is ledger-backed. Membership, credit purchase,
quota consumption, contract security deposit, admin deposit refund, withdrawal,
and premium recommendation visibility from `SPEC-PAYMENT.md` are not yet
implemented as a cohesive wallet flow.

Contract execution currently moves from fully signed/NDA-signed directly to
`Active` and moves the job to `IN_PROGRESS`.

## Target Behavior

The payment MVP uses PayOS only for wallet top-up. Internal purchases and
security-deposit movements use wallet balance and ledger records. Business and
Expert users can buy role-scoped membership packages, buy credits, view quota,
consume job/proposal quota, and submit withdrawal requests. Business contracts
move to `PendingDeposit` after both contract and NDA signatures, then move to
`Active` and the job moves to `IN_PROGRESS` only after the 20% security deposit
is held. Admin can finalize deposit refund/resolution and withdrawal approval or
rejection.

## Affected Users

- Business accounts.
- Expert accounts.
- Admin operators.

## Affected Product Docs

- `SPEC-PAYMENT.md`
- `docs/ARCHITECTURE.md`
- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `docs/data-dictionary.md`

## Non-Goals

- Automatic bank transfer.
- Full milestone escrow/payout ledger replacement.
- Hot-job packages.
- Dispute fund lock and penalty flow.
- Platform commission on project budget.

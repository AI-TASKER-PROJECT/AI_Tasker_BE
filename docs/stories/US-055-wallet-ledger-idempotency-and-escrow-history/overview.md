# US-055 — Wallet Ledger Idempotency And Escrow History Consolidation

## Current Behavior

`wallet_transactions` stores valid accounting legs, but escrow and deposit flows
do not persist a durable logical operation identity. User wallet history also
exposes many escrow/deposit legs as separate rows, so one logical action can
look duplicated.

## Target Behavior

- Financial writers persist `operationKey` and `operationLeg` for durable
  idempotency.
- Same-wallet multi-leg transfers are atomic and retry-safe.
- User wallet history consolidates known escrow/deposit operations into one
  logical history item.
- Admin ledger history remains raw and audit-oriented.

## Affected Users

- `BUSINESS`
- `EXPERT`
- `ADMIN`

## Affected Product Docs

- `SPEC.md`
- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`

## Non-Goals

- Do not collapse internal append-only ledger legs.
- Do not change payout authority or financial percentages.

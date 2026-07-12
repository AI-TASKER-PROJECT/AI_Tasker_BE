# Exec Plan

## Goal

Implement durable wallet ledger operation identity and remove duplicate-looking
escrow/deposit history rows without weakening accounting fidelity.

## Scope

In scope:

- `wallet_transactions.operation_key` and `operation_leg`
- wallet ledger atomic idempotent writes
- milestone escrow, dispute/termination settlement, contract deposit, and
  withdrawal writers
- consolidated read model for user wallet history

Out of scope:

- automated destructive cleanup of ambiguous historical rows
- payout rule changes
- admin ledger aggregation

## Risk Classification

Risk flags:

- Data model
- Public contracts
- Existing behavior
- Weak proof
- Multi-domain

Hard gates:

- data migration

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- production-safe duplicate repair needs destructive cleanup;
- a valid financial flow cannot supply deterministic operation identity;
- the new unique index conflicts with legitimate rows.

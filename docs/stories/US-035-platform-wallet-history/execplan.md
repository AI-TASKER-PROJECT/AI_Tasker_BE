# Exec Plan

## Goal

Add a dedicated admin API for platform wallet transaction history with
transparent Vietnamese display text and reconciliation fields.

## Scope

In scope:

- Add `GET /api/v1/admin/wallet/transactions`.
- Reuse `WalletTransactionHistoryResponse` for platform history.
- Show one business event row for multi-leg ledger movements.
- Improve Vietnamese titles/descriptions for all requested transaction types.
- Update Swagger overview and test guide.
- Add focused unit tests.

Out of scope:

- Changing wallet ledger schema.
- Reprocessing historical rows.
- Adding pagination.

## Risk Classification

Risk flags:

- Authorization.
- Audit/security.
- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- Authorization boundary for admin finance history.
- Public API shape change.

## Work Phases

1. Discovery of existing wallet, withdrawal, membership, deposit, and docs.
2. Design API and history rendering behavior.
3. Implement repository/service/controller changes.
4. Add focused tests.
5. Update Swagger-facing docs.
6. Run validation and record Harness evidence.

## Stop Conditions

Pause for human confirmation if:

- The endpoint must be visible to STAFF as well as ADMIN.
- Platform history must aggregate balances differently from `wallet_transactions`.
- A migration becomes necessary.

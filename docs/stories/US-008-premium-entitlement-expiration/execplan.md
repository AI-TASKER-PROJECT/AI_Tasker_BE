# Exec Plan

## Goal

Fix Premium entitlement so it survives lower-tier purchases until the Premium
expiration timestamp passes.

## Scope

In scope:

- New Flyway migration only.
- Entity, DTO, service, and test updates.
- `/api/users/me/quota` response contract update.
- Harness story, decision, and validation evidence.

Out of scope:

- Frontend changes.
- Old migration edits.
- Wallet ledger, PayOS, contract deposit, or status enum changes.

## Risk Classification

Risk flags:

- Data model.
- Public contracts.
- Existing behavior.
- Authorization.
- Weak proof.

Hard gates:

- Data migration.

## Work Phases

1. Discovery.
2. Harness story and decision setup.
3. Migration and model update.
4. Service and DTO update.
5. Tests and docs.
6. Verification and trace.

## Stop Conditions

Pause for human confirmation if:

- Active package priority cannot be inferred from existing package codes.
- Migration backfill cannot preserve existing Premium users.
- Validation requirements need to be weakened.

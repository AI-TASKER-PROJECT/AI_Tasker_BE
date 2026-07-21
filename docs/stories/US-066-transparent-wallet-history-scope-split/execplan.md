# Exec Plan

## Goal

Make wallet histories transparent and unambiguous by enriching user/admin
history responses and splitting admin platform wallet history into platform
ledger versus platform-wide user activity.

## Scope

In scope:

- Expand wallet history DTO with reconciliation fields.
- Add admin platform ledger and user activity routes.
- Preserve compatibility route.
- Update wallet history service projection and tests.
- Update Swagger-facing docs and story evidence.

Out of scope:

- Schema migration.
- Historical ledger rewrite/backfill.
- Frontend tab implementation.
- Provider-backed payout/refund reconciliation.

## Risk Classification

Risk flags:

- Public contracts.
- Existing behavior.
- Weak proof around finance history.
- Multi-role finance behavior.
- Audit/security-adjacent financial transparency.

Hard gates:

- Financial read contract changes require high-risk story proof.

## Work Phases

1. Review current wallet ledger writers, projections, routes, and tests.
2. Design response fields and admin scope split.
3. Implement DTO, service, controller, and tests.
4. Update docs and OpenAPI-facing inventory.
5. Run focused and broader validation.
6. Record Harness decision, story status, and trace.

## Stop Conditions

Pause if:

- The change requires schema migration or destructive data cleanup.
- Existing clients must be broken.
- Validation cannot cover the changed finance history behavior.

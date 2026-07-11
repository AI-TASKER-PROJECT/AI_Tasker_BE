# Exec Plan

## Goal

Make Business milestone escrow deposit the automatic start point for milestone
execution.

## Scope

In scope:

- Change `depositMilestoneEscrow` to set milestone and contract milestone
  status to `IN_PROGRESS`.
- Preserve `in_progress_started_at` as the deposit-time timeline anchor.
- Keep `startMilestone` compatible for legacy `DEPOSITED` rows and idempotent
  for already `IN_PROGRESS` rows.
- Update tests, product docs, API-facing guides, and Harness records.

Out of scope:

- Schema migration.
- Removing public routes.
- Reworking wallet ledger or notification infrastructure.

## Risk Classification

Risk flags:

- Authorization.
- Audit/security.
- Public contracts.
- Existing behavior.
- Weak proof if not covered by focused and full Maven tests.

Hard gates:

- Authorization/public workflow behavior change.

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- Auto-start requires a different audit or notification event.
- A data migration becomes necessary.
- Existing tests reveal a wider state-machine conflict.

# Exec Plan

## Goal

Deliver backend-owned automatic milestone review, safe escrow release, and an
accurate frontend countdown without changing unrelated workflows.

## Scope

In scope:

- SLA duration setting with minute, hour, and day units.
- Durable per-review start/deadline timestamps and migration/backfill.
- Scheduled due-review processing with locking and idempotent ledger behavior.
- Removal of manual Admin SLA processing from API and frontend.
- Admin SLA configuration UI and workspace countdown/polling.
- Focused tests, build checks, OpenAPI/Swagger, and affected product docs.

Out of scope:

- Execution overdue checks, progress report SLA, Staff dispute SLA.
- New settlement percentages or changes to escrow funding.
- General refactors of contract execution or Admin settings.

## Risk Classification

Risk flags:

- Data model.
- Audit/security.
- Public contracts.
- Existing behavior.
- Cross-role frontend workflow.
- Finance/escrow side effects.

Hard gates:

- Additive database migration.
- Automatic money movement.

## Work Phases

1. Map current submission, review, settlement, settings, UI, and documentation.
2. Record the deadline/configuration/concurrency decision and validation plan.
3. Add migration, domain fields, setting policy, due query, and scheduler.
4. Harden Business approve/reject races and add backend tests.
5. Update Admin configuration and workspace countdown in the frontend.
6. Refresh OpenAPI/docs, run focused and full validation, then record evidence.

## Stop Conditions

Pause for human confirmation if:

- Automatic settlement cannot reuse the existing idempotent escrow ledger path.
- A change to dispute or termination semantics becomes necessary.
- Migration would delete or rewrite historical finance records.
- Required validation must be weakened.

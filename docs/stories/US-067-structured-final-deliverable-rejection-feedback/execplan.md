# Exec Plan

## Goal

Make final deliverable rejection feedback transparent without adding a new
database table.

## Scope

In scope:

- Add JSONB column on `deliverables`.
- Add request DTO for structured reject feedback.
- Validate failed criteria ownership.
- Persist and expose rejected criteria feedback.
- Update API docs and tests.

Out of scope:

- New normalized rejection table.
- Frontend implementation.
- Progress report feedback behavior changes.

## Risk Classification

Risk flags:

- Data model.
- Public contracts.
- Existing behavior.
- Authorization.

Hard gates:

- Database migration.

## Work Phases

1. Discovery of current reject/deliverable/criteria implementation.
2. Design JSONB contract and compatibility behavior.
3. Add migration/entity/DTO/service/controller changes.
4. Add regression tests.
5. Refresh OpenAPI/docs.
6. Run validation and record trace.

## Stop Conditions

Pause for human confirmation if:

- A new table becomes required.
- Existing reject/dispute semantics must change.
- Validation cannot prove migration and reject behavior.

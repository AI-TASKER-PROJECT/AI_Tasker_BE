# Exec Plan

## Goal

Align DB constraints, service behavior, API docs, tests, and Harness records for
payment and contract lifecycle statuses.

## Scope

In scope:

- New Flyway migration only.
- Service and test updates for wallet, contract, job, and milestone statuses.
- Product docs, Swagger overview, decision record, and story evidence.

Out of scope:

- Editing old migrations.
- Reintroducing contract activation.
- Removing the physical change-request table.

## Risk Classification

Risk flags:

- Data model.
- Public contracts.
- Existing behavior.
- Multi-domain.
- Weak proof.

Hard gates:

- Data migration.

## Work Phases

1. Discovery.
2. Design and Harness record updates.
3. Migration and service implementation.
4. Unit test and doc updates.
5. Local validation.
6. Trace and final report.

## Stop Conditions

Pause for human confirmation if:

- A requested old status cannot be mapped without data loss.
- The test suite requires weakening validation.

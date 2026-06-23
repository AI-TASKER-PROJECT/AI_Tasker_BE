# Exec Plan

## Goal

Remove malformed recommendation-demo account seed data and reinsert the demo
dataset with zero-padded identifiers that do not contain unexpected spaces.

## Scope

In scope:

- Identify the seed formatting issue in the recommendation demo migration.
- Add a new Flyway migration that deletes the affected deterministic demo rows.
- Re-seed the same demo dataset with `lpad(..., '0')` instead of `%0s` string formatting.
- Add migration-time checks for whitespace in account identifiers and profile identifiers.

Out of scope:

- Editing already existing migrations.
- Cleaning manually created production-like data outside the seed ID ranges.
- Changing recommendation ranking logic.

## Risk Classification

Risk flags:

- Data model.
- Existing behavior.
- Weak proof.

Hard gates:

- Data migration/deletion.

## Work Phases

1. Discovery: inspect the existing seed migration and story evidence.
2. Design: choose additive migration instead of editing V39.
3. Validation planning: verify Flyway/test behavior and SQL formatting checks.
4. Implementation: add V41 seed reset migration and story packet.
5. Verification: run migration-sensitive tests/compile where available.
6. Harness update: record durable story, proof, and trace.

## Stop Conditions

Pause for human confirmation if:

- The cleanup must affect non-demo account IDs.
- Existing manual contracts or payment records depend on the malformed demo seed rows.
- Validation requires a live database but Docker/Postgres is unavailable.

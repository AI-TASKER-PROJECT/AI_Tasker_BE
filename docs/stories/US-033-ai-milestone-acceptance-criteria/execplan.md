# Exec Plan

## Goal

Replace the fixed acceptance-criteria catalog with AI-generated, editable,
milestone-owned criteria while preserving existing milestone and contract data.

## Scope

In scope:

- AI response contract.
- Milestone persistence and response shape.
- Business criterion CRUD.
- PostgreSQL migration and legacy-data conversion.
- Contract snapshot lookup.
- Swagger/product documentation and tests.

Out of scope:

- Frontend code.
- Post-contract criterion mutation.
- Contract snapshot format changes.

## Risk Classification

Risk flags:

- Data model and migration.
- Public API contract.
- Existing behavior.
- Authorization.
- Multi-domain.

Hard gates:

- Data migration.
- Authorization.

## Work Phases

1. Map global catalog and join-table behavior.
2. Record the milestone-ownership architecture decision.
3. Add `V45` migration that clones linked legacy criteria per milestone, drops
   the join table, removes unowned catalog rows, and adds indexed ownership.
4. Extend AI milestone output with `acceptanceCriteria`.
5. Persist criteria during job create and draft replacement.
6. Add Business-owned criterion CRUD and update snapshot reads.
7. Remove catalog routes and update documentation.
8. Run focused tests, compile, migration/full tests when PostgreSQL is available.

## Stop Conditions

Pause if:

- Existing linked criteria cannot be migrated without losing descriptions.
- Contract snapshots would need rewriting.
- Criteria must remain editable after contract creation.

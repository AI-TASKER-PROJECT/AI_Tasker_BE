# 0019 Milestone-Owned Acceptance Criteria

Date: 2026-06-30

## Status

Accepted

## Context

Acceptance criteria are currently a fixed platform catalog seeded with 26 rows.
Milestones select catalog ids through a many-to-many join table. This makes
criteria generic, prevents AI from tailoring them to one milestone, and lets
only Admin add new values. The requested product behavior is for AI to generate
criteria per milestone and for the owning Business to edit or add criteria.

Existing jobs may already link catalog rows, and existing contracts hold
criteria text snapshots that must not change.

## Decision

Acceptance criteria will be milestone-owned one-to-many records. AI returns
criterion descriptions inside each milestone. Job create/update persists them,
and the owning Business receives criterion CRUD before contract creation.

Migration clones every legacy milestone-to-catalog link into a dedicated
milestone-owned row before removing the join table and unowned global catalog
rows. Existing contract snapshots are retained unchanged.

## Alternatives Considered

1. Keep the global catalog and allow custom values. Rejected because criteria
   could still be shared and the fixed catalog remains part of the workflow.
2. Store criteria as milestone JSON. Rejected because CRUD identity, ordering,
   indexing, audit, and cascade behavior are weaker.
3. Delete all existing criteria. Rejected because linked milestone history
   would be lost unnecessarily.

## Consequences

Positive:

- Criteria match each AI-generated milestone.
- Business users can refine criteria without Admin catalog changes.
- Ownership and cascade behavior become explicit.
- Contract snapshots remain stable.

Tradeoffs:

- Public catalog and Admin-create APIs are removed.
- Frontend must send/render milestone criteria instead of catalog ids.
- Migration rewrites legacy linked criteria into owned copies.

## Follow-Up

- Frontend must display the optional AI-generated list and call milestone CRUD.
- Add an E2E flow once the frontend is available.


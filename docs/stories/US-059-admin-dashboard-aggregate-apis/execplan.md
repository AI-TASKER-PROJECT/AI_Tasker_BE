# Exec Plan

## Goal

Provide backend aggregate APIs for an admin dashboard without forcing the
frontend to download and group large operational lists.

## Scope

In scope:

- Admin-only `/api/v1/admin/dashboard/*` endpoints.
- Chart-ready response DTOs with `from`, `to`, and `groupBy` query support.
- Aggregate service logic and repository queries.
- Focused unit tests for summary, revenue, contracts, users, disputes,
  membership, jobs/proposals, and finance breakdown.
- Swagger/OpenAPI-facing docs.

Out of scope:

- Replacing existing list/detail APIs.
- Database schema changes.
- Browser/UI implementation.

## Risk Classification

Risk flags:

- Authorization.
- Public contracts.
- Multi-domain.
- Existing behavior.
- Weak proof if dashboard aggregates are not test-covered.

Hard gates:

- Authorization.

## Work Phases

1. Discovery of existing admin analytics/list APIs.
2. Design dashboard DTOs and endpoint shape.
3. Implement service/repository aggregate logic.
4. Add focused tests.
5. Update Swagger/OpenAPI/docs.
6. Record validation and trace.

## Stop Conditions

Pause for human confirmation if:

- Existing dashboard/list APIs must be removed or renamed.
- A migration becomes necessary.
- Revenue definition is ambiguous enough to change money movement semantics.

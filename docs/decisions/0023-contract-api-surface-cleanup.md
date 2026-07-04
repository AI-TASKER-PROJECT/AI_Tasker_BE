# Contract API Surface Cleanup

Date: 2026-07-04

## Status

Accepted

## Context

Milestone/dispute/termination v2 behavior was implemented correctly, but the
public REST surface grew from 134 to 170 operations because duplicate aliases,
legacy routes, and support/system endpoints were exposed alongside the intended
v1 routes. The target cleanup is about 151 total operations and about 47
Contract Execution Flow operations without removing service behavior.

## Decision

Expose only the target v1 routes in `ContractExecutionController` and remove
the duplicate/legacy/support mappings from public Swagger. Keep the underlying
service methods, repositories, entities, and tests intact.

The kept routes include milestone-scoped deposit/deliverable/dispute actions,
non-admin dispute lifecycle actions, termination request lifecycle actions,
case attachments, progress reports, and closed-contract reviews. No `/api/v2`
namespace is introduced.

## Alternatives Considered

1. Keep compatibility mappings and hide them with `@Hidden`. Rejected because
   the requested outcome is a clean public API count and no backend-local
   frontend compatibility dependency was found.
2. Delete legacy/support service methods. Rejected because tests still cover
   those methods and the task only requires public route cleanup.
3. Keep admin dispute aliases as the staff/admin public contract. Rejected
   because product docs already prefer the non-admin v1 dispute routes and
   service-level role checks enforce the required permissions.

## Consequences

Positive:

- Runtime Swagger reports 151 total operations and 47 Contract Execution Flow
  operations.
- API consumers have one clear v1 route per main contract execution behavior.
- Milestone escrow, dispute state machine, termination lifecycle, settlement,
  and review behavior remain intact.

Tradeoffs:

- Clients still calling removed aliases must migrate to the documented v1
  target routes.
- Legacy/support service methods remain in code for test/internal
  compatibility even though they are no longer public endpoints.

## Follow-Up

- Frontend/API clients should update any remaining calls to the removed aliases
  before integrating the milestone/dispute/termination screens.

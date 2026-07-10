# Design

## Domain Model

No entity or migration changes. The cleanup changes HTTP exposure only.
Milestone escrow, dispute, termination request, case attachment, progress
report, wallet ledger, and review entities keep their current behavior.

## Application Flow

The kept public routes call the same service methods as the removed aliases:

- Milestone escrow deposit remains on
  `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`.
- Dispute initiation remains on
  `POST /api/v1/milestones/{milestoneId}/disputes?contractId=...`.
- Dispute escalation, assignment, intervention rejection, staff decision, and
  settlement execution remain on the non-admin v1 dispute routes.
- Termination uses the `termination-requests` lifecycle.
- Deliverables use the milestone-scoped route.

## Interface Contract

Removed public mappings:

- Duplicate aliases for milestone escrow deposit, dispute initiate/escalate,
  dispute staff assignment, admin dispute aliases, and admin settlement aliases.
- Legacy raw `deliverables`, `disputes`, `transactions`, dispute resolve, and
  contract terminate routes.
- Support/system routes for SLA auto approve, demo testing, and technical
  report.

Kept public Contract Execution Flow remains v1-only and has 47 operations.

## Data Model

No schema changes. Legacy service methods and repositories remain available for
tests or internal compatibility.

## UI / Platform Impact

API consumers should call only the target v1 routes listed in generated Swagger
and product docs. No `/api/v2` namespace is introduced.

## Observability

OpenAPI, markdown API inventories, Maven test output, and Harness trace are the
proof surfaces for this cleanup.

## Alternatives Considered

1. Keep compatibility routes with `@Hidden`; rejected because the requested
   target count is a clean public surface and no frontend compatibility need is
   present in this backend repo.
2. Delete service methods too; rejected because tests and internal flows still
   cover several legacy/support methods.

# Design

## Domain Model

The execution lifecycle is uppercase and constrained in the database through a
new Flyway migration. Old data is normalized before constraints are tightened.
The negotiation/change-request lifecycle no longer changes contract state.

## Application Flow

Contract creation starts at `DRAFT`. Contract and NDA signatures keep the draft
in `DRAFT` until all four acceptance timestamps exist, then move it to
`PENDING`. Deposit payment moves the contract to `ACTIVE` and the job to
`IN_PROGRESS`. Deliverable submission moves the milestone to `UNDER_REVIEW`.
Milestone completion moves the milestone to `COMPLETED`; when all milestones
are complete, the contract becomes `COMPLETED` and the job becomes `CLOSED`.

## Interface Contract

Affected endpoints:

- `PATCH /api/v1/jobs/{jobId}/status`
- `POST /api/v1/contracts/from-proposals/{proposalId}`
- `POST /api/v1/contracts/change-requests`
- `POST /api/v1/contracts/{contractId}/sign`
- `POST /api/v1/contracts/{contractId}/nda-sign`
- `POST /api/v1/contracts/{contractId}/reject`
- `POST /api/v1/contracts/{contractId}/terminate`
- `POST /api/v1/contracts/{contractId}/deposit/pay`
- `POST /api/v1/admin/contracts/{contractId}/deposit/refund`
- `POST /api/v1/deliverables`
- `POST /api/v1/milestones/{milestoneId}/complete`
- `POST /api/v1/milestones/sla-auto-approve`

## Data Model

Add `V31__status_enum_constraint_alignment.sql`. Do not edit old migrations.

## UI / Platform Impact

Clients must send and display the new uppercase values. Public job listing
continues to return only `OPEN` jobs.

## Observability

Existing audit events remain in place for contract, wallet, deposit, and admin
operations.

## Alternatives Considered

1. Keep mixed-case values and only document mappings. Rejected because the DB
   constraints and API responses would still drift.

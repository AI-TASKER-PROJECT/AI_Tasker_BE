# Design

## Domain Model

No entity or schema changes. The authorization boundary stays path-based in
Spring Security and state-based in the service layer.

## Application Flow

Spring Security permits unauthenticated `GET /api/v1/jobs/{jobId}/milestones`
requests. `ContractExecutionService.listMilestonesByJob` remains the business
gate: `OPEN` jobs are readable, while non-`OPEN` jobs still require owner or
participant access.

## Interface Contract

- Route: `GET /api/v1/jobs/{jobId}/milestones`
- Public behavior: anonymous callers can read milestones for `OPEN` jobs.
- Protected behavior: non-`OPEN` jobs still return authorization errors unless
  the caller is an allowed owner or participant.

## Data Model

No migration.

## UI / Platform Impact

Landing pages and public job detail views can reuse the milestone endpoint
without forcing login.

## Observability

No new audit event. Existing service-level authorization logic continues to
cover non-public job states.

## Alternatives Considered

1. Duplicating milestone data into `GET /api/v1/jobs`. Rejected because the
   endpoint already carries milestone details today, and the underlying public
   milestone route still needed its security rule aligned with service logic.

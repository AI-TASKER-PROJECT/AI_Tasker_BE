# Design — US-043

## Domain Model

Add `OVERDUE`, on-demand progress-report request SLA state, structured progress
feedback, Staff case SLA metadata, and penalty-free immediate termination.

## Application Flow

Business may request reports during active work. Staff assignment opens evidence
collection, after which assigned Staff makes the binding dispute decision and
the system settles escrow.

## Interface Contract

Every public route is rooted at `/api/v1`. No Admin final-decision route exists.

## Data Model

Prefer a request-history table for progress-report requests; do not duplicate
mutable SLA state across `milestones` and `contract_milestones`.

## UI / Platform Impact

Frontend is out of scope; response DTOs must expose enough SLA state for clients.

## Observability

Audit report requests, submissions, feedback, overdue detection, Staff
assignment/access/SLA, decisions, settlement, and immediate termination.

## Alternatives Considered

1. Admin final review: rejected by the product owner.
2. Fixed 10% termination penalty: rejected by the product owner.

# Design

## Domain Model

- The active setting is `milestone_review_sla_duration`, stored atomically as
  `<positive integer>:<MINUTE|HOUR|DAY>`.
- `contract_milestones.review_started_at` records the final-deliverable submission
  that opened the current review round.
- `contract_milestones.review_due_at` is the immutable deadline for that review
  round. Resubmission replaces both timestamps; Business rejection clears them.
- Automatic approval is eligible only while both live and contract milestone
  states are `UNDER_REVIEW`, escrow is unreleased, the contract is active, and no
  active dispute or termination request exists.

## Application Flow

1. Expert submits a valid final deliverable.
2. Backend reads the active SLA duration, snapshots start/deadline, and returns
   them from the contract milestone API.
3. A fixed-delay backend scheduler selects due review rows and locks them.
4. The service revalidates all state under the lock and uses existing idempotent
   wallet operation keys to debit milestone escrow and credit Expert balance.
5. It completes milestone/contract lifecycle, records an automatic audit event,
   and notifies the affected participant.
6. Business approval/rejection obtains the same milestone lock and revalidates
   the state, so it cannot race the scheduled settlement.

## Interface Contract

- Remove `POST /api/v1/contracts/{contractId}/milestones/sla-auto-approve`.
- Extend `GET /api/v1/contracts/{contractId}/milestones` items with nullable
  `reviewStartedAt` and `reviewDueAt`.
- Keep Admin system-setting CRUD but allow SLA updates only for the dedicated
  duration key; its active state is enforced and its value is validated.

## Data Model

- Add the two nullable review timestamp columns and a partial due-review index.
- Seed the new duration setting from the legacy day value and deactivate the
  legacy key.
- Backfill deadlines for currently reviewed milestones from their latest
  deliverable submission so deployment does not strand active reviews.

## UI / Platform Impact

- Admin settings render the SLA as a numeric value plus minute/hour/day unit.
- Contract workspace removes the Admin manual SLA button.
- `UNDER_REVIEW` milestones display a live countdown and poll the server around
  expiry to observe the backend transition.

## Observability

- Each completed automatic review records `MILESTONE_REVIEW_SLA_AUTO_APPROVED`.
- Scheduler failures are logged without stopping later fixed-delay executions.
- No audit row is written for empty scans.

## Alternatives Considered

1. Frontend timer calls the settlement endpoint. Rejected because closing a tab,
   clock skew, retries, and client tampering make it unsafe.
2. Recompute every deadline from the current setting. Rejected because changing
   configuration would retroactively shorten or extend active reviews.
3. Keep the Admin manual trigger. Rejected because the requested workflow must
   be automatic and Admin is configuration-only for review SLA.


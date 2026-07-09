# Design — US-045

## Domain Model

Contract deposits become participant-owned rows keyed by contract and owner
role. Deliverables carry submission/rejection history. Progress-report requests
are durable history rows. Disputes remain explicit and Staff decisions remain
binding. Standard and immediate termination are separate commands.

## Application Flow

- Signatures move the contract to `PENDING`.
- Business 20% and Expert 10% deposits are held idempotently; activation occurs
  only when both are held.
- Rejection records feedback and returns the milestone to `IN_PROGRESS`.
- Resubmission uses the existing deliverable endpoint and increments a durable
  submission round.
- Explicit dispute, Staff evidence-window decision, and atomic settlement use
  stored decision values and one-time escrow guards.
- Standard termination uses the response/Staff lifecycle. Immediate termination
  atomically applies the initiator's 10% held-deposit compensation.

## Interface Contract

Add the missing `/api/v1` routes from spec section 16, retain the existing
`ApiResponse<T>` envelope, and keep authorization in service methods.

## Data Model

Add migration V51 after the current V50 baseline. The migration is additive,
backfills existing contract deposits as Business-owned, does not fabricate
Expert payments for legacy contracts, and aligns JPA with `ddl-auto=validate`.

## UI / Platform Impact

Backend API consumers must display both deposit requirements, rejection history,
progress-report SLA state, and explicit immediate-termination penalty
confirmation.

## Observability

Audit deposit holds, dual-deposit activation, progress request/feedback/expiry,
overdue state, rejection/resubmission, explicit dispute decisions, termination
responses, penalty settlement, participant refunds, and closure.

## Alternatives Considered

1. Reuse the one-row Business deposit model. Rejected because it cannot secure
   the Expert penalty or prove dual-deposit activation.
2. Auto-create disputes on rejection. Rejected by v2.2.
3. Edit V45–V50. Rejected because applied Flyway migrations are immutable.

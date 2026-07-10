# Flow 4-5 v2.3 Routing, Report Gates, And Automatic Refunds

Date: 2026-07-11

## Status

Accepted

## Context

SPEC-MILESTONE-DISPUTER.md v2.3 added nine binding decisions that override v2.2
behavior: escrow deposit starts the milestone timeline, progress reports use a
lightweight Business acknowledgement gate instead of structured feedback,
Business may cancel its own untouched draft contract, milestone-dispute Staff
routing must not require Admin, Staff cannot reject intervention, the evidence
window is guidance only, participant deposits must be refunded automatically at
normal completion or valid termination, deliverable responses must expose
`submissionRound`, and WebSocket dispute-intervention notifications must expose
the notification `type` field.

## Decision

1. **Escrow deposit starts milestone timeline.** Set `contract_milestones.in_progress_started_at`
   during `depositMilestoneEscrow`. `startMilestone` must not reset it.
2. **Business acknowledgement gate replaces structured feedback.** Remove the
   `/feedback` endpoint, `ProgressReportFeedbackRequest` DTO, and all structured-
   feedback fields from `MilestoneProgressReportEntity`. Add `acknowledgement_state`
   (`PENDING_BUSINESS_ACK` / `ACKNOWLEDGED`), an `/acknowledge` endpoint, and a gate
   that blocks new reports and on-demand requests while the latest report is pending ack.
3. **Business may cancel its own untouched `DRAFT` contract.** Add `POST /contracts/{contractId}/cancel-draft`.
   Requires `STATUS_DRAFT` with no signatures and no NDA signs; sets contract to `CANCELLED`
   and returns the job to `OPEN`.
4. **Dispute routing without Admin.** Replace dispute `POST /assign-staff` (ADMIN) with
   `POST /route-staff` (STAFF). Escalation auto-routes to the best available Staff
   candidate. Admin is excluded from milestone-dispute candidate reads, routing, and cancellation.
5. **Staff cannot reject intervention.** Remove `POST /reject-intervention`, the
   `rejectIntervention` service method, and the `notifyDisputeInterventionRejected` notification.
   `INTERVENTION_REJECTED` remains readable for legacy data but new code must not create it.
6. **Staff may decide before the evidence deadline.** Remove the `EVIDENCE_WINDOW_STILL_OPEN`
   hard guard from `staffDecide`. The 48-hour evidence window remains informational guidance only.
   Staff decision triggers settlement automatically.
7. **Automatic participant-deposit refund on completion and valid termination.** Call
   `autoRefundParticipantDeposits` in completion, termination-accept, and termination-settlement
   paths. Refund both held deposits exactly once; set contract to `CLOSED` after both resolve.
8. **Deliverable responses expose `submissionRound`.** `DeliverableEntity.submissionRound` is
   already persisted and the entity is returned directly via `ApiResponse`.
9. **WebSocket dispute-intervention notifications expose `type`.** `NotificationEntity.type`
   is already serialized by `createAndPush` with `DISPUTE_ESCALATION_REQUESTED` and
   `DISPUTE_ASSIGNED` type codes.

## Alternatives Considered

1. Keep structured progress-report feedback. Rejected by spec — structured feedback
   creates ambiguity and conflates flow control with deliverable-quality judgment.
2. Keep Admin as dispute-Staff assigner. Rejected — Admin is an operational manager,
   not a dispute workflow participant. Staff-ops routing decouples management from
   escalation.
3. Keep hard evidence-window block. Rejected — the evidence window provides guidance
   for participants to submit materials; blocking Staff from issuing a decision before
   the window closes harms resolution speed without improving quality.
4. Keep participant-deposit refund as an Admin manual step. Rejected — deposits are
   held by the system and should be refunded automatically when completion or valid
   termination criteria are satisfied.

## Consequences

Positive:
- Milestone timeline starts at the moment Business commits escrow, eliminating
  ambiguity about when the clock begins.
- Progress-report flow control is clean: ack is a gate, not quality feedback.
- Business has a clean exit before any commitments are made.
- Staff workload is managed inside Staff operations, not Admin gates.
- Automatic refunds close the financial loop without manual Admin steps.
- Staff can resolve disputes efficiently when evidence is sufficient.

Tradeoffs:
- Legacy structured-feedback columns remain readable but are no longer mapped or
  writable by the Java entity.
- `INTERVENTION_REJECTED` rows in legacy data are not backfilled to a different
  status; they remain readable for historical reference.
- The escalation flow now auto-routes to Staff without Admin visibility; Admin
  oversight of dispute volume may require a separate monitoring endpoint (out of scope).

## Follow-Up

- Implement through additive V53 migration and service/controller refactors.
- Synchronize OpenAPI and product documentation after runtime implementation.
- Monitor dispute auto-routing correctness in production; add Staff-reassignment
  capability if workload/availability becomes a bottleneck.

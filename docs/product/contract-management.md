# Contract Management

## Source

This product contract is derived from the active backend implementation,
Flyway migrations, and tests.

## Actors

- Business creates draft contracts, signs contracts, signs NDA, pays deposits,
  terminates eligible contracts, and completes reviewed milestones.
- Expert reviews draft contracts, signs contracts, signs NDA, rejects eligible
  contracts, starts funded milestones, submits progress reports, and submits
  deliverables.
- Admin can terminate eligible contracts and resolve deposit handling.
- Staff participates through dispute handling and termination-request review.

## Lifecycle

```text
Accepted Proposal
  -> DRAFT
  -> PENDING
  -> ACTIVE
  -> COMPLETED
```

Alternate exits:

- `DRAFT` or `PENDING` -> `CANCELLED` when the expert rejects.
- Non-final contract -> `CANCELLED` when the owning business or admin
  terminates or cancels it.

## Status Values

- Contracts: `DRAFT`, `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
- Jobs: `DRAFT`, `OPEN`, `IN_PROGRESS`, `CLOSED`.
- Milestones: `PENDING`, `DEPOSITED`, `IN_PROGRESS`, `UNDER_REVIEW`,
  `DISPUTED`, `COMPLETED`, `CANCELLED`.
- Wallet transactions: `POSTED`.

## Rules

- A draft contract can be created only from an `Accepted` proposal owned by the
  current approved business.
- A proposal can have only one contract.
- The proposal job must have at least one milestone.
- Job duration uses `plannedDurationValue` and `plannedDurationUnit`; milestone
  duration uses `duration` and `durationUnit`. Valid units are `DAY`, `WEEK`,
  and `MONTH`. During `POST /api/v1/jobs`, units are normalized to uppercase,
  milestone durations require job duration, and the converted total milestone
  duration must not exceed the converted job duration (`DAY=1`, `WEEK=7`,
  `MONTH=30`).
- `DRAFT` creation snapshots job milestones into `contract_milestones`, using
  proposal milestone budgets when supplied.
- Contract and NDA signatures are limited to the business and expert attached
  to the contract.
- Signing is allowed only while the contract is `DRAFT`.
- Contract change requests are enabled for `DRAFT`, `PENDING`, and `ACTIVE`
  contracts. Either participant can propose budget, timeline, scope, or
  milestone snapshot changes; only the counterparty can accept or reject. The
  system applies proposed changes only after acceptance and records audit log
  plus notification for request/review.

- The contract moves to `PENDING` after business signature, expert signature,
  business NDA, and expert NDA are all present.
- The owning Business must hold 20% and the assigned Expert must separately
  hold 10% of total contract value before execution starts.
- Each deposit operation is idempotent. Only after both participant deposits
  are held does the system set the contract to `ACTIVE`, apply final milestone
  budgets, attach the contract id, and move the job to `IN_PROGRESS`.
- Expert rejection is allowed only from `DRAFT` or `PENDING`; it moves the
  contract to `CANCELLED` and the job back to `OPEN`.
- Deliverables can be submitted only by the contract Expert while the contract
  is `ACTIVE`, both NDA signatures exist, the milestone is `IN_PROGRESS`, and
  the contract snapshot deadline has not passed. First submission and
  correction/resubmission use the same endpoint, retain submission rounds, and
  move the milestone to `UNDER_REVIEW`; neither is accepted after the original
  execution deadline. Source-code ZIP upload follows the same deadline rule.
- Final deliverables must include at least one source-code handoff:
  `sourceCodeUrl` for a repository or `sourceCodeFileUrl` for an uploaded ZIP;
  both are allowed. `demoLink` remains a separate runnable-product URL. Source
  archives are uploaded first through the milestone-scoped authenticated route,
  accept ZIP only, and are capped at 50 MB.
- The owning business can deposit milestone escrow only from `PENDING`; a
  successful deposit automatically moves both milestone records to
  `IN_PROGRESS` and starts the execution timeline. The Expert start endpoint is
  retained only as a compatibility/idempotent path for legacy `DEPOSITED`
  records or older clients. A Business approval from
  `UNDER_REVIEW` releases the escrow once, marks the milestone `COMPLETED`, and
  resolves any self-resolve dispute with
  `BUSINESS_APPROVED_AFTER_SELF_RESOLVE`.
- While a milestone is `IN_PROGRESS`, the Expert can submit progress reports at
  `MIDPOINT` and `PRE_DEADLINE` checkpoints for Business review. These reports
  are progress tracking records only; they are not dispute evidence and are not
  stored as case attachments. If milestone duration or start timestamp is
  missing, the report is accepted with no checkpoint assignment.
- The owning Business can acknowledge a progress report without feedback, or
  submit progress-report feedback with category, severity, DoD context,
  feedback text, and an adjustment flag. Feedback acknowledges a pending report
  and unlocks the next Expert report, but it does not create a dispute,
  deliverable rejection, report revision state, or money movement.
- A Business rejection from `UNDER_REVIEW` marks the current deliverable
  `REJECTED`, stores overall feedback, optionally stores failed
  acceptance-criteria feedback with a reason per criterion, increments rejection
  history, and returns the milestone to `IN_PROGRESS`. It never creates a
  dispute. Either participant must explicitly invoke the dispute API for a
  genuine disagreement.
- Business on-demand progress-report requests use a durable request history:
  the first response SLA is 24 hours and later requests use 12 hours. Reports
  remain accepted in `OVERDUE`; final deliverables and source-code ZIP uploads
  do not. Missed deadlines do not move money automatically.
- Staff decision is separate from settlement execution: assigned Staff moves a
  dispute to `STAFF_DECIDED`; Admin settlement execution then splits escrow,
  marks the milestone `COMPLETED`, sets the dispute `RESOLVED`, and records the
  settlement guard. Participants can cancel a self-resolve/escalation dispute
  before Staff review; Admin can cancel invalid active disputes.
- When every contract milestone is `COMPLETED`, the system moves the contract
  to `COMPLETED` and the job to `CLOSED`.
- Final deliverable submission snapshots `reviewStartedAt` and `reviewDueAt`
  from the active `milestone_review_sla_duration` value. The backend scheduler
  automatically approves a due `UNDER_REVIEW` milestone only when the contract
  remains active and no dispute or termination blocks settlement. It releases
  escrow exactly once and applies the same finalization rule: if this is the
  last milestone, the contract becomes `COMPLETED` and the job becomes `CLOSED`.
  Admin config changes apply to later review rounds and cannot disable the rule.
- Termination requests move eligible active contracts to
  `TERMINATION_PENDING`. Admin assigns Staff review; assigned Staff approves or
  rejects. Approved requests either await milestone escrow settlement or move
  directly to deposit refund. Termination settlement is blocked while any
  dispute is still active, can split the current milestone escrow between
  Business and Expert, cancels unfinished milestones, and moves the contract to
  `TERMINATED`.
- Standard termination supports Business-request Expert accept/dispute/three-day
  timeout before optional Staff review. Immediate termination is a separate
  guarded command: the initiator pays exactly 10% of total contract value from
  its held deposit to the counterparty, without Staff review.
- Admin participant-deposit refund is an operational retry/audit tool. The system
  automatically refunds both held participant deposits at normal completion or
  valid termination, then transitions the contract to `CLOSED`. Reviews are
  available only after `CLOSED`.
- `COMPLETED`, `TERMINATED`, `CLOSED`, and `CANCELLED` contracts cannot start
  new milestone work.

- `GET /api/v1/contracts/{contractId}/milestones` returns contract milestones as
  `ContractMilestoneViewResponse` DTOs. Snapshot fields (`milestoneName`,
  `description`, `originalBudget`, `finalBudget`, `orderIndex`, `duration`,
  `durationUnit`, `criteriaSnapshot`, `deliverableExpectation`) come from
  `contract_milestones`. Displayed status comes from the live `milestones` table
  (via `job_milestone_id` lookup), falling back to `contract_milestones.status`
  when the linked milestone is missing. `criteriaSnapshot` is built from the
  milestone-owned acceptance criteria descriptions at contract creation time,
  joined by newlines. `deliverableExpectation` is copied from the milestone
  `description` at contract creation time. Both snapshot fields stay stable after
  contract creation even if the source milestone or its acceptance criteria are
  later edited.

## API

- `POST /api/v1/contracts/from-proposals/{proposalId}`
- `POST /api/v1/contracts/{contractId}/sign`
- `POST /api/v1/contracts/{contractId}/nda-sign`
- `POST /api/v1/contracts/{contractId}/deposit/pay`
- `POST /api/v1/contracts/{contractId}/expert-deposit/pay`
- `POST /api/v1/admin/contracts/{contractId}/deposits/refund`
- `POST /api/v1/contracts/{contractId}/reject`
- `POST /api/v1/contracts/{contractId}/cancel-draft`
- `GET /api/v1/contracts/{contractId}/milestones`
- `POST /api/v1/contracts/{contractId}/change-requests`
- `GET /api/v1/contracts/{contractId}/change-requests`
- `POST /api/v1/contracts/{contractId}/change-requests/{requestId}/accept`
- `POST /api/v1/contracts/{contractId}/change-requests/{requestId}/reject`
- `POST /api/v1/contracts/{contractId}/termination-requests`
- `POST /api/v1/contracts/{contractId}/immediate-termination`
- `POST /api/v1/termination-requests/{terminationRequestId}/accept`
- `POST /api/v1/termination-requests/{terminationRequestId}/dispute`
- `POST /api/v1/termination-requests/expire-awaiting-expert`
- `GET /api/v1/contracts/{contractId}/termination-requests`
- `GET /api/v1/termination-requests/{terminationRequestId}`
- `POST /api/v1/termination-requests/{terminationRequestId}/assign-staff`
- `POST /api/v1/termination-requests/{terminationRequestId}/approve`
- `POST /api/v1/termination-requests/{terminationRequestId}/reject`
- `POST /api/v1/termination-requests/{terminationRequestId}/partial-evidence`
- `POST /api/v1/termination-requests/{terminationRequestId}/execute-settlement`
- `POST /api/v1/termination-requests/{terminationRequestId}/refund-deposit`
- `POST /api/v1/termination-requests/{terminationRequestId}/withdraw`
- `POST /api/v1/milestones/{milestoneId}/deliverables`
- `GET /api/v1/milestones/{milestoneId}/deliverables`
- `POST /api/v1/milestones/{milestoneId}/source-code-file`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deliverables`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/source-code-file`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-report-request`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/acknowledge`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback`
- `GET /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- `POST /api/v1/contracts/{contractId}/milestones/check-overdue`
- `POST /api/v1/milestones/{milestoneId}/start`
- `POST /api/v1/milestones/{milestoneId}/approve`
- `POST /api/v1/milestones/{milestoneId}/reject`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/approve`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/reject`
- `POST /api/v1/milestones/{milestoneId}/complete` (compatibility alias for
  approval/release)
- `POST /api/v1/milestones/{milestoneId}/disputes?contractId=...`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/disputes`

Staff dispute routing keeps the mandatory job-domain gate. Automatic routing
locks the Staff pool, keeps only approved Staff below
`dispute_staff_max_active_cases`, builds a qualified pool from normalized
domain (60%) and skill (40%) coverage, then orders by active workload,
specialization score, oldest assignment time, and Staff id. If no Staff has
capacity, the dispute remains `ESCALATION_REQUESTED` for later routing.
Automatic routing records an audit event that identifies the assigned Staff and
displays the dispute as the Business/Expert participant pair instead of the
Staff actor.

- `POST /api/v1/disputes/{disputeId}/escalation-request`
- `POST /api/v1/disputes/{disputeId}/route-staff`
- `GET /api/v1/disputes/{disputeId}/staff-candidates`
- `POST /api/v1/disputes/{disputeId}/staff-decision`
- `POST /api/v1/disputes/{disputeId}/execute-settlement`
- `POST /api/v1/disputes/{disputeId}/cancel`
- `POST /api/v1/disputes/staff-sla-escalate`
- `POST /api/v1/case-attachments`
- `GET /api/v1/case-attachments?ownerType=...&ownerId=...`
- `POST /api/v1/contracts/{contractId}/reviews`
- `GET /api/v1/contracts/{contractId}/reviews`
- `GET /api/v1/milestones/{milestoneId}/criteria`
- `POST /api/v1/milestones/{milestoneId}/criteria`
- `PUT /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- `DELETE /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`

## Notifications And Audit

The backend records audit events for draft creation, signing, NDA signing,
deposit activation, milestone start/completion, dispute cancellation and
settlement, termination request review/settlement/refund, contract completion,
deliverable submission, and termination. PayOS wallet top-up sync records audit
only when a payment order first reaches a terminal outcome (`PAID`, `FAILED`,
`CANCELLED`, or `EXPIRED`), so repeated sync polling does not spam audit logs.
Dispute decision and settlement audit rows display the two contract participants
as the business object context.
Automatic milestone review settlement records the actor as `Hệ thống tự động`,
uses the fully Vietnamese action “Hệ thống tự động duyệt và giải ngân cột mốc
khi hết hạn nghiệm thu”, and sends the Expert a clear confirmation that both
approval and escrow release have completed.

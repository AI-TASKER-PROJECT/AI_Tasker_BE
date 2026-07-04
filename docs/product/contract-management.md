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
- The negotiation/change-request lifecycle is disabled and must not move
  contracts out of `DRAFT`. The endpoint has been removed.

- The contract moves to `PENDING` after business signature, expert signature,
  business NDA, and expert NDA are all present.
- The owning business must pay the 20% wallet security deposit before execution
  starts.
- Deposit payment sets the contract to `ACTIVE`, applies final contract
  milestone budgets back to job milestones, attaches the contract id to those
  milestones, and moves the job to `IN_PROGRESS`.
- Expert rejection is allowed only from `DRAFT` or `PENDING`; it moves the
  contract to `CANCELLED` and the job back to `OPEN`.
- Deliverables can be submitted only by the contract expert while the contract
  is `ACTIVE`, both NDA signatures exist, and the milestone is `IN_PROGRESS`.
  Resubmission while `DISPUTED` is allowed only when the active dispute is still
  `PENDING_SELF_RESOLVE`. Submission moves the milestone to `UNDER_REVIEW`.
- The owning business can deposit milestone escrow only from `PENDING`, then
  the expert starts execution from `DEPOSITED` to `IN_PROGRESS`. A Business approval from
  `UNDER_REVIEW` releases the escrow once, marks the milestone `COMPLETED`, and
  resolves any self-resolve dispute with
  `BUSINESS_APPROVED_AFTER_SELF_RESOLVE`.
- While a milestone is `IN_PROGRESS`, the Expert can submit progress reports at
  `MIDPOINT` and `PRE_DEADLINE` checkpoints for Business review. These reports
  are progress tracking records only; they are not dispute evidence and are not
  stored as case attachments. If milestone duration or start timestamp is
  missing, the report is accepted with no checkpoint assignment.
- A Business rejection from `UNDER_REVIEW` creates or updates a single active
  `PENDING_SELF_RESOLVE` dispute and moves the milestone to `DISPUTED`.
- Staff decision is separate from settlement execution: assigned Staff moves a
  dispute to `STAFF_DECIDED`; Admin settlement execution then splits escrow,
  marks the milestone `COMPLETED`, sets the dispute `RESOLVED`, and records the
  settlement guard. Participants can cancel a self-resolve/escalation dispute
  before Staff review; Admin can cancel invalid active disputes.
- When every contract milestone is `COMPLETED`, the system moves the contract
  to `COMPLETED` and the job to `CLOSED`.
- SLA auto-approval of an overdue reviewed milestone uses the same finalization
  rule: if the auto-approved milestone completes the last remaining contract
  milestone, the contract becomes `COMPLETED` and the job becomes `CLOSED`.
- Termination requests move eligible active contracts to
  `TERMINATION_PENDING`. Admin assigns Staff review; assigned Staff approves or
  rejects. Approved requests either await milestone escrow settlement or move
  directly to deposit refund. Termination settlement is blocked while any
  dispute is still active, can split the current milestone escrow between
  Business and Expert, cancels unfinished milestones, and moves the contract to
  `TERMINATED`.
- Admin security-deposit refund is the gate from `COMPLETED` or `TERMINATED`
  to `CLOSED`. Reviews are available only after `CLOSED`.
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
- `POST /api/v1/admin/contracts/{contractId}/deposit/refund`
- `POST /api/v1/contracts/{contractId}/reject`
- `GET /api/v1/contracts/{contractId}/milestones`
- `POST /api/v1/contracts/{contractId}/termination-requests`
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
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- `GET /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- `POST /api/v1/milestones/{milestoneId}/start`
- `POST /api/v1/milestones/{milestoneId}/approve`
- `POST /api/v1/milestones/{milestoneId}/reject?reason=...`
- `POST /api/v1/milestones/{milestoneId}/complete` (compatibility alias for
  approval/release)
- `POST /api/v1/milestones/{milestoneId}/disputes?contractId=...`
- `POST /api/v1/disputes/{disputeId}/escalation-request`
- `POST /api/v1/disputes/{disputeId}/assign-staff`
- `POST /api/v1/disputes/{disputeId}/reject-intervention`
- `POST /api/v1/disputes/{disputeId}/staff-decision`
- `POST /api/v1/disputes/{disputeId}/execute-settlement`
- `POST /api/v1/disputes/{disputeId}/cancel`
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
deliverable submission, and termination.

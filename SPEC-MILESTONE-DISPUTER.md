# SPEC-MILESTONE-DISPUTER.md — v2.1

**Project:** AITASKER-BE  
**Platform:** AITASKER — AI expert and business matching platform  
**Spec purpose:** Implementation guide for harness engineering agents  
**Version:** v2.1
**Prepared date:** 2026-07-01  
**Last updated:** 2026-07-09
**Primary target:** Backend agents, database agents, integration agents, tester agents, reviewer agents  
**Language:** English technical specification

---

## 0. Executive Summary

This specification defines the final v2 design for the remaining contract execution flows of AITASKER:

1. **Flow 4 — Milestone Execution, Escrow Payment, Contract Completion, and Contract Termination**
2. **Flow 5 — Milestone Dispute, Self-Resolve, Staff Intervention, Staff Settlement Decision, and Settlement Execution**

The v2 design replaces the earlier incomplete milestone payment model with a **per-milestone escrow model**:

- Business must deposit the current milestone budget before Expert starts the milestone.
- The milestone escrow is released only once.
- Normal approval releases 100% of milestone escrow to Expert.
- Dispute or termination settlement releases a Staff-decided percentage to Expert and refunds the remaining amount to Business.
- Contract security deposit of 20% is separate from milestone escrow and is refunded manually by Admin after contract completion or valid termination.

The spec also fixes the review findings from v1:

- Business can reject a re-submitted deliverable without creating a second dispute.
- Dispute and termination settlement cannot double-release the same milestone escrow.
- Milestones that are not completed due to contract termination must enter `CANCELLED` instead of remaining zombie `PENDING` rows.
- `STAFF_DECIDED` and settlement execution are separated.
- Contract status transitions are explicit.
- Dispute can be initiated by either Business or Expert in valid contexts.
- Cancel/withdraw rules are based on the dispute initiator.
- Database migration must not delete existing data by default.
- All public backend routes in this specification use the mandatory `/api/v1`
  prefix. Unversioned aliases are out of scope for new implementation.
- Milestones support an `OVERDUE` execution state without losing the ability to
  submit progress reports, final deliverables, disputes, or valid termination
  evidence.
- Business may request an on-demand progress report with a 24-hour first-request
  SLA and a 12-hour subsequent-request SLA.
- Progress reports support product links and structured Business feedback.
- Dispute Staff assignment supports specialization/workload candidates, a
  48-hour evidence window, temporary read/execute scope, and a three-day Staff
  decision SLA.
- Assigned Staff remains the final professional decision-maker. Admin may assign
  or replace Staff for operational reasons but may not approve, reject, revise,
  or change Staff's payout percentage.
- Immediate termination never charges a fixed 10% penalty to either party.

### 0.1 v2.1 Binding Decisions

The following decisions override conflicting experimental code or supplementary
notes:

1. There is no `admin-final-decision` dispute step.
2. There is no `REPORT_REVISION_REQUESTED` dispute status.
3. Staff's valid decision is final and triggers system settlement.
4. Admin cannot adjust Staff's payout percentage, including by a plus/minus
   tolerance.
5. There is no fixed 10% abrupt-termination penalty, compensation, confiscation,
   or partial security-deposit deduction.
6. An overdue on-demand progress-report request may unlock immediate termination
   for Business, but does not create a penalty or transfer money from one party
   to the other.
7. Contract security-deposit handling remains binary under sections 4.1, 9.7,
   and 11.8.

---

## 1. Source Context And Agent Reading Order

Before implementation, agents must inspect the actual project source code and current database migrations. Do not implement from this spec alone without validating existing code.

Required reading order:

1. `AGENTS.md` or equivalent harness instructions in the repository.
2. Current Flyway migrations, especially existing status constraints and wallet migrations.
3. Current JPA entities and enum classes for:
   - Contract
   - Milestone
   - ContractMilestone
   - Deliverable
   - Dispute
   - Wallet/SystemWallet
   - WalletTransaction
   - ContractDeposit
   - Review
4. Current service classes for contract, milestone, wallet ledger, dispute, notification, audit log.
5. `db.md` database inventory.
6. This spec.

Important current DB assumptions from inventory:

- The repository currently contains migrations beyond the original `V44`
  baseline. Agents must query the actual latest migration before implementation
  and add a new migration after it; never reuse a migration number.
- `ddl-auto=validate` is used, so migration and JPA entity definitions must stay aligned.
- `wallet_transactions` is the real wallet ledger.
- Legacy `transactions` must not be used for new v2 financial logic.
- `invoices` was removed and must not be reintroduced.
- `contract_change_requests` exists but the current change-request lifecycle is considered disabled/legacy; do not reuse it for termination requests.

---

## 2. Scope

### 2.1 In Scope

This spec covers:

- Milestone escrow deposit.
- Milestone start.
- Milestone overdue tracking.
- Scheduled checkpoint progress reports.
- Business on-demand progress-report requests and SLA tracking.
- Structured Business feedback on progress reports.
- Deliverable submission.
- Business milestone approval.
- Business milestone rejection.
- Expert re-submit during self-resolve.
- Business reject again during self-resolve.
- Business approval after self-resolve.
- Business or Expert dispute initiation.
- Dispute escalation request with file/evidence.
- Admin Staff assignment based on job domain and required skills.
- Staff candidate ranking, evidence window, temporary case access, and Staff SLA.
- Staff review.
- Staff intervention rejection.
- Staff mandatory decision.
- System settlement execution after Staff decision.
- Contract termination request by Business or Expert.
- Immediate termination without a fixed penalty when section 11A guards are
  satisfied.
- Termination Staff review.
- Partial work evidence for termination.
- Termination settlement.
- Admin manual refund of 20% contract deposit.
- Contract closure.
- Cross-review opening after contract closure.
- Database impact and migration plan.
- Required service guards and acceptance criteria.

### 2.2 Out Of Scope

This spec does not cover:

- PayOS provider reconciliation beyond wallet top-up.
- Automatic bank transfer withdrawal payout.
- Invoice reintroduction.
- Automatic AI judging of deliverable quality.
- Legal arbitration outside the platform.
- Complex appeal flow after Staff decision.
- Platform penalties, platform fees, or confiscation of the unpaid dispute percentage.
- Automatic AI judgment based on missed deadlines.
- Financial penalties caused only by a missed progress-report deadline.
- Multi-Staff voting or Staff committee review.
- Admin review or override of a valid Staff dispute decision.
- Appeal after a valid Staff dispute decision.

---

## 3. Roles And Responsibilities

### 3.1 Business

Business is the client that owns the job and contract budget.

Business can:

- Deposit milestone escrow.
- Review submitted deliverables.
- Approve a milestone.
- Reject a deliverable and initiate a dispute.
- Submit reasons/evidence for dispute.
- Request an on-demand progress report while the current milestone is
  `IN_PROGRESS` or `OVERDUE`.
- Submit structured feedback on a progress report.
- Request Staff intervention for an active dispute.
- Request contract termination.
- Request immediate termination when section 11A guards are satisfied.
- Submit termination reason and evidence.
- Receive refund of unused milestone escrow after dispute or termination settlement.
- Receive manual refund of the 20% contract security deposit after contract completion or valid termination.
- Review Expert after contract is closed.

Business cannot:

- Approve milestone if milestone is not `UNDER_REVIEW`.
- Start or submit Expert deliverables.
- Release escrow manually.
- Override Staff decision.
- Cancel a dispute initiated by Expert.
- Cancel a dispute after Staff review has started.
- Withdraw contract deposit directly without Admin refund.

### 3.2 Expert

Expert performs the milestone work.

Expert can:

- Start a deposited milestone.
- Submit deliverable.
- Re-submit deliverable during self-resolve.
- Explain why the deliverable meets acceptance criteria.
- Initiate dispute in valid contexts.
- Submit reasons/evidence for dispute.
- Request Staff intervention for an active dispute.
- Request contract termination.
- Submit partial work evidence during termination review.
- Receive milestone payout.
- Review Business after contract is closed.

Expert cannot:

- Start a milestone before Business deposits the milestone budget.
- Create dispute in `PENDING`, `DEPOSITED`, `COMPLETED`, or `CANCELLED` milestone states.
- Create a second active dispute on the same milestone.
- Cancel a dispute initiated by Business.
- Cancel a dispute after Staff review has started.
- Override Staff decision.

### 3.3 Admin

Admin is an operational platform manager.

Admin can:

- Assign a suitable Staff to dispute escalation or termination request.
- Choose Staff based on job domain, required skills, Staff specialization, workload, and conflict of interest if available.
- Replace an assignment before Staff decides when required for availability,
  conflict-of-interest, or SLA reasons.
- Cancel invalid or duplicate dispute records before settlement.
- Cancel invalid or duplicate termination requests before settlement.
- Execute or approve manual contract deposit refund, depending on existing wallet service design.
- Manage system categories, role access, and operational oversight.

Admin must not:

- Override Staff professional decision about deliverable quality or payout percentage.
- Approve, reject, request revision of, or alter a valid Staff dispute decision.
- Execute duplicate milestone escrow settlement.
- Use legacy `transactions` for new wallet settlement.

### 3.4 Staff

Staff is the professional reviewer assigned to a dispute or termination case.

Staff can:

- Review dispute files, deliverables, SoW, milestone criteria, timeline, and evidence.
- Reject intervention and return dispute to self-resolve with reason.
- Accept intervention and issue a mandatory decision.
- Decide Expert payout percentage from 0% to 100%.
- Issue the final professional dispute decision without Admin approval.
- Write Staff report and decision rationale.
- Review termination request and decide whether termination is valid.
- Assess partial work evidence during termination.

Staff cannot:

- Manually move wallet balances outside approved service methods.
- Override wallet guards.
- Refund the 20% contract deposit directly unless existing system explicitly grants that operation.
- Cancel a dispute instead of using `INTERVENTION_REJECTED`.

### 3.5 System

System is responsible for:

- Enforcing state transitions.
- Enforcing wallet invariants.
- Locking rows during settlement.
- Executing escrow release/split.
- Writing wallet ledger entries.
- Writing audit logs.
- Sending notifications.
- Preventing duplicate active disputes and termination requests.
- Preventing double settlement.

---

## 4. Core Financial Concepts

### 4.1 Contract Security Deposit

The contract security deposit is 20% of the total contract budget.

It is:

- Paid by Business after contract signing.
- Held by platform/admin/system wallet according to existing implementation.
- Separate from milestone escrow.
- Refunded manually by Admin after:
  - contract normal completion; or
  - valid contract termination after required settlement is complete.

It is not:

- A milestone payment.
- A milestone escrow.
- A platform penalty by default.
- Automatically confiscated.
- Partially refundable. Admin refund action is binary: refund the full held amount (100%), or withhold the full held amount (0%). There is no percentage-based split, deduction, or partial confiscation of the contract security deposit. This is different from milestone escrow, which Staff may split by percentage during dispute resolution (10.9); the contract deposit has no equivalent Staff-decision mechanism, so no partial-amount authority exists for it.

### 4.2 Milestone Escrow

Milestone escrow is the budget of the current milestone.

Business must deposit it before Expert starts work.

Normal flow:

```text
Business available balance -> Business escrow balance
```

After Business approval:

```text
Business escrow balance -> Expert available balance
```

After dispute/termination Staff settlement:

```text
Business escrow balance -> Expert available balance for Staff-decided payout
Business escrow balance -> Business available balance for remaining refund
```

### 4.3 Staff Settlement Percentage

Staff may enter any percentage from 0% to 100%.

Formula:

```text
expertPayoutAmount = milestoneEscrowAmount * staffExpertPercentage / 100
businessRefundAmount = milestoneEscrowAmount - expertPayoutAmount
```

The refund must be calculated by subtraction from escrow amount, not by independently multiplying the remaining percentage, to avoid rounding mismatch.

### 4.4 Platform Fee Or Penalty

There is no platform penalty or platform fee in v2 dispute settlement.

If Staff decides Expert receives 70%, the remaining 30% is refunded to Business.

There is also no fixed immediate-termination penalty:

- Business immediate termination does not transfer a fixed percentage to Expert.
- Expert immediate termination does not debit Expert's available wallet by a
  fixed percentage.
- The 20% contract security deposit must not be partially consumed as a fixed
  termination penalty.
- Money movement during termination is limited to refunding unreleased escrow,
  executing a Staff-approved partial-work split when applicable, and processing
  the binary contract security-deposit result.

---

## 5. Contract State Machine

### 5.1 Contract Status Values

Existing values must be preserved if code depends on them, but v2 requires additional values.

Required v2 values:

```text
DRAFT
PENDING
ACTIVE
TERMINATION_PENDING
COMPLETED
TERMINATED
CLOSED
CANCELLED
```

### 5.2 Status Meanings

| Status | Meaning |
|---|---|
| `DRAFT` | Contract created but not fully accepted/signed. Existing behavior. |
| `PENDING` | Contract pending required pre-activation steps. Existing behavior. |
| `ACTIVE` | Contract is active and milestones can be executed. |
| `TERMINATION_PENDING` | A termination request is being reviewed. Contract execution actions are blocked except allowed termination/dispute actions. |
| `COMPLETED` | All milestones are completed. Contract waits for Admin refund of 20% security deposit. |
| `TERMINATED` | Termination has been approved and required milestone settlement is complete. Contract waits for Admin refund of 20% security deposit. |
| `CLOSED` | Final state. Deposit refund is done. Cross-review is opened. |
| `CANCELLED` | Legacy or invalid cancellation state. Do not use for new valid v2 termination flow unless existing code requires compatibility. |

### 5.3 Normal Completion Transitions

```text
ACTIVE -> COMPLETED -> CLOSED
```

Rules:

- `ACTIVE -> COMPLETED` occurs after all contract milestones are `COMPLETED`.
- `COMPLETED -> CLOSED` occurs only after Admin refunds the 20% contract security deposit.
- Cross-review opens only at `CLOSED`.

### 5.4 Termination Transitions

```text
ACTIVE -> TERMINATION_PENDING -> TERMINATED -> CLOSED
ACTIVE -> TERMINATED -> CLOSED
```

Rules:

- `ACTIVE -> TERMINATION_PENDING` occurs when Business or Expert creates a valid termination request.
- `TERMINATION_PENDING -> ACTIVE` occurs if Staff rejects the termination request.
- `TERMINATION_PENDING -> TERMINATED` occurs after Staff approves termination and required current milestone settlement is executed.
- `ACTIVE -> TERMINATED` is allowed only through the guarded immediate
  termination flow in section 11A. It never applies a fixed percentage penalty.
- `TERMINATED -> CLOSED` occurs only after Admin refunds the 20% contract security deposit.

### 5.5 Contract Blocking Rules

When contract is `TERMINATION_PENDING`:

- No new milestone work may start.
- No new deliverable may be submitted except partial evidence required by termination review.
- Existing active dispute may continue to resolution if it already exists.
- Termination settlement cannot execute while current milestone has an active dispute.

When contract is `COMPLETED`, `TERMINATED`, or `CLOSED`:

- No milestone deposit/start/submit/reject/dispute initiation actions are allowed.
- Only deposit refund and review actions are allowed according to status.

---

## 6. Milestone State Machine

### 6.1 Milestone Status Values

Required v2 values:

```text
PENDING
DEPOSITED
IN_PROGRESS
OVERDUE
UNDER_REVIEW
DISPUTED
COMPLETED
CANCELLED
```

### 6.2 Status Meanings

| Status | Meaning |
|---|---|
| `PENDING` | Milestone exists but Business has not deposited milestone budget yet. |
| `DEPOSITED` | Business has deposited full milestone budget into escrow. Expert can start. |
| `IN_PROGRESS` | Expert is working on the milestone. Expert submits progress reports during this state per `9.2A`; reports do not change milestone status. |
| `OVERDUE` | The milestone execution deadline has passed while work remains active. Expert may still submit progress reports and a final deliverable; Business may request an on-demand progress report. |
| `UNDER_REVIEW` | Expert submitted deliverable; Business is reviewing. |
| `DISPUTED` | There is an active dispute on the milestone. Contract is blocked. |
| `COMPLETED` | Milestone is closed and escrow settlement is done. |
| `CANCELLED` | Milestone is closed because contract was terminated before it could be completed. |

### 6.3 Allowed Transitions

```text
PENDING -> DEPOSITED
DEPOSITED -> IN_PROGRESS
IN_PROGRESS -> OVERDUE when the milestone due time passes
IN_PROGRESS -> UNDER_REVIEW
OVERDUE -> UNDER_REVIEW
UNDER_REVIEW -> COMPLETED
UNDER_REVIEW -> DISPUTED
IN_PROGRESS -> DISPUTED
OVERDUE -> DISPUTED
DISPUTED -> UNDER_REVIEW
DISPUTED -> COMPLETED
DISPUTED -> CANCELLED only if dispute is cancelled and contract termination closes milestone without settlement, but prefer previous state rollback before Staff review
PENDING -> CANCELLED due to contract termination
DEPOSITED -> CANCELLED due to contract termination only if full escrow is refunded and no payout settlement is needed
IN_PROGRESS -> COMPLETED due to Staff termination settlement
OVERDUE -> COMPLETED due to Staff termination settlement
UNDER_REVIEW -> COMPLETED due to Staff termination settlement
DISPUTED -> COMPLETED due to Staff dispute settlement
```

### 6.4 Forbidden Transitions

```text
PENDING -> IN_PROGRESS
PENDING -> UNDER_REVIEW
PENDING -> COMPLETED without Staff termination rule
DEPOSITED -> UNDER_REVIEW
IN_PROGRESS -> COMPLETED without Business approval or Staff settlement
OVERDUE -> COMPLETED without Business approval, configured review-SLA
auto-approval, or Staff settlement
UNDER_REVIEW -> IN_PROGRESS without reject/self-resolve process
COMPLETED -> any non-terminal status
CANCELLED -> any non-terminal status
```

### 6.5 Contract Termination Cleanup

When contract is terminated:

- Completed milestones remain `COMPLETED`.
- Current milestone with settlement becomes `COMPLETED` after settlement.
- An `OVERDUE` milestone follows the same escrow cleanup rules as an
  `IN_PROGRESS` milestone.
- Current milestone with no escrow and no valid evidence becomes `CANCELLED`.
- Future milestones that are not completed become `CANCELLED`.

This prevents zombie milestones such as `PENDING` milestones under a `TERMINATED` or `CLOSED` contract.

---

## 7. Dispute State Machine

### 7.1 Dispute Status Values

Required v2 values:

```text
PENDING_SELF_RESOLVE
ESCALATION_REQUESTED
STAFF_REVIEWING
INTERVENTION_REJECTED
STAFF_DECIDED
RESOLVED
CANCELLED
```

### 7.2 Status Meanings

| Status | Meaning |
|---|---|
| `PENDING_SELF_RESOLVE` | Business and Expert are self-resolving the dispute. |
| `ESCALATION_REQUESTED` | A party submitted file/evidence requesting Staff intervention. |
| `STAFF_REVIEWING` | Admin assigned Staff and Staff is reviewing the case. |
| `INTERVENTION_REJECTED` | Staff rejected intervention. This should be logged/history state, then current status returns to `PENDING_SELF_RESOLVE`. |
| `STAFF_DECIDED` | Staff issued mandatory decision; settlement execution is pending or retrying. |
| `RESOLVED` | Dispute is resolved and settlement or approval has been executed. |
| `CANCELLED` | Dispute was withdrawn/cancelled before Staff review or cancelled by Admin as invalid/duplicate. |

### 7.3 Active Dispute Statuses

The following statuses count as active:

```text
PENDING_SELF_RESOLVE
ESCALATION_REQUESTED
STAFF_REVIEWING
STAFF_DECIDED
```

`INTERVENTION_REJECTED` should not remain active as a stable current state unless the codebase needs it. Preferred behavior:

```text
STAFF_REVIEWING -> INTERVENTION_REJECTED logged -> PENDING_SELF_RESOLVE
```

### 7.4 Allowed Transitions

```text
PENDING_SELF_RESOLVE -> ESCALATION_REQUESTED
PENDING_SELF_RESOLVE -> RESOLVED
PENDING_SELF_RESOLVE -> CANCELLED
ESCALATION_REQUESTED -> STAFF_REVIEWING
ESCALATION_REQUESTED -> CANCELLED
STAFF_REVIEWING -> INTERVENTION_REJECTED -> PENDING_SELF_RESOLVE
STAFF_REVIEWING -> STAFF_DECIDED
STAFF_DECIDED -> RESOLVED
```

### 7.5 Dispute Initiation Types

Required enum values:

```text
BUSINESS_REJECTED_DELIVERABLE
EXPERT_SCOPE_CONCERN
EXPERT_NO_REVIEW_RESPONSE
EXPERT_BAD_FAITH_REJECTION
OTHER
```

Meanings:

| Type | Meaning |
|---|---|
| `BUSINESS_REJECTED_DELIVERABLE` | Business rejected a submitted deliverable. |
| `EXPERT_SCOPE_CONCERN` | Expert claims Business is requesting out-of-scope work. |
| `EXPERT_NO_REVIEW_RESPONSE` | Expert claims Business is not reviewing or responding appropriately. |
| `EXPERT_BAD_FAITH_REJECTION` | Expert claims Business rejected deliverable unfairly or outside acceptance criteria. |
| `OTHER` | Other valid dispute reason requiring evidence. |

### 7.6 Resolution Types

Required enum values:

```text
BUSINESS_APPROVED_AFTER_SELF_RESOLVE
STAFF_DECISION_SETTLEMENT
CANCELLED_BY_INITIATOR
CANCELLED_BY_ADMIN
```

---

## 8. Termination Request State Machine

### 8.1 Termination Request Status Values

Required values:

```text
AWAITING_EXPERT_RESPONSE
REQUESTED
STAFF_REVIEWING
STAFF_APPROVED
STAFF_REJECTED
AWAITING_SETTLEMENT_EXECUTION
AWAITING_DEPOSIT_REFUND
COMPLETED
CANCELLED
```

### 8.2 Status Meanings

| Status | Meaning |
|---|---|
| `AWAITING_EXPERT_RESPONSE` | Business requested termination; Expert has three days to accept or escalate to Staff review. |
| `REQUESTED` | Business or Expert submitted termination request. |
| `STAFF_REVIEWING` | Admin assigned Staff and Staff is reviewing termination request. |
| `STAFF_APPROVED` | Staff approved termination and defined required settlement if any. |
| `STAFF_REJECTED` | Staff rejected termination request. Contract returns to `ACTIVE`. |
| `AWAITING_SETTLEMENT_EXECUTION` | Required milestone settlement is pending or retrying. |
| `AWAITING_DEPOSIT_REFUND` | Milestone settlement is done; waiting for Admin refund of 20% security deposit. |
| `COMPLETED` | Termination request is fully completed. Contract should be `CLOSED`. |
| `CANCELLED` | Requester withdrew request before Staff decision or Admin cancelled invalid/duplicate request. |

### 8.3 Allowed Transitions

```text
AWAITING_EXPERT_RESPONSE -> AWAITING_DEPOSIT_REFUND when Expert accepts or the three-day response SLA expires
AWAITING_EXPERT_RESPONSE -> REQUESTED when Expert disputes and requests Staff review
AWAITING_EXPERT_RESPONSE -> CANCELLED when Business withdraws before resolution
REQUESTED -> STAFF_REVIEWING
REQUESTED -> CANCELLED
STAFF_REVIEWING -> STAFF_APPROVED
STAFF_REVIEWING -> STAFF_REJECTED
STAFF_REVIEWING -> CANCELLED only if requester withdraws before Staff decision and Staff has not issued decision
STAFF_APPROVED -> AWAITING_SETTLEMENT_EXECUTION if current milestone settlement is required
STAFF_APPROVED -> AWAITING_DEPOSIT_REFUND if no milestone settlement is required
AWAITING_SETTLEMENT_EXECUTION -> AWAITING_DEPOSIT_REFUND
AWAITING_DEPOSIT_REFUND -> COMPLETED
STAFF_REJECTED -> terminal for request; contract returns ACTIVE
```

### 8.4 Termination Request Rules

- Business and Expert can request contract termination when contract is `ACTIVE`.
- Request must include reason and may include files/evidence.
- Contract becomes `TERMINATION_PENDING` when request is accepted by system.
- A Business-created request begins at `AWAITING_EXPERT_RESPONSE`.
- An Expert-created request begins at `REQUESTED` and notifies Admin.
- Expert has three calendar days to accept or dispute a Business-created request.
- Expert acceptance terminates the contract, refunds unreleased unfinished
  milestone escrow to Business without a fixed penalty, and moves the request to
  `AWAITING_DEPOSIT_REFUND`.
- Expert dispute moves the same request to `REQUESTED` for Admin Staff
  assignment; it must not create a second request.
- If Expert does not respond within three calendar days, the system idempotently
  treats the Business request as accepted, terminates the contract, refunds
  unreleased unfinished milestone escrow to Business without a fixed penalty,
  and moves the request to `AWAITING_DEPOSIT_REFUND`.
- Only one active termination request is allowed per contract.
- If current milestone has active dispute, termination request may exist, but termination settlement cannot execute until active dispute is `RESOLVED` or `CANCELLED`.
- Requester can withdraw termination request before Staff decision.
- After Staff decision, requester cannot withdraw.

---

## 9. Flow 4 — Milestone Execution And Escrow Payment

### 9.1 Deposit Current Milestone Escrow

Actor: Business

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `PENDING`.
- Milestone belongs to contract.
- Milestone is the current executable milestone by `order_index`.
- All previous milestones are `COMPLETED`.
- Business wallet available balance >= milestone budget.
- No active dispute on milestone.
- No active termination request on contract.

System behavior:

1. Lock Business wallet row.
2. Lock milestone row.
3. Debit Business available balance by milestone budget.
4. Credit Business escrow balance by milestone budget.
5. Write `wallet_transactions` entries.
6. Set milestone status to `DEPOSITED`.
7. Mirror `contract_milestones.status` if required by existing design.
8. Write audit log.
9. Notify Expert.

Ledger reference:

```text
transaction_type = MILESTONE_ESCROW_DEPOSIT
reference_type = MILESTONE
reference_id = milestoneId
contract_id = contractId
milestone_id = milestoneId
```

### 9.2 Start Milestone

Actor: Expert

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `DEPOSITED`.
- Expert is assigned to contract.
- No active dispute.
- No active termination request.

System behavior:

1. Set milestone status to `IN_PROGRESS`.
2. Write audit log.
3. Notify Business.

### 9.2A Submit Milestone Progress Report

Actor: Expert

Purpose: while a milestone is active, Expert reports current status so Business
can monitor project health. A report may satisfy a scheduled checkpoint, answer
an on-demand Business request, or be voluntary.

Checkpoints (mandatory, non-blocking):

- `MIDPOINT` — due at 50% of the milestone's declared timeline, measured from `contract_milestones.in_progress_started_at`.
- `PRE_DEADLINE` — due at 80% of the milestone's declared timeline (i.e. some time before the deadline).
- Timeline length in days is derived from `contract_milestones.duration` + `duration_unit` (`DAY` = 1, `WEEK` = 7, `MONTH` = 30), matching the existing conversion used for job/milestone duration elsewhere in the spec.
- Missing a scheduled checkpoint alone does not create a dispute, financial
  penalty, or termination right. The separate on-demand request in section
  9.2B has an explicit response SLA.
- Each report submission is tagged with the earliest checkpoint not yet fulfilled (`MIDPOINT` first, then `PRE_DEADLINE`). Once both checkpoints have at least one report, further voluntary submissions are recorded with no checkpoint tag.
- If `duration`/`duration_unit` is not set on the milestone, checkpoint due dates cannot be computed; reports are still accepted and stored with `checkpoint_type = NULL`, `is_late = false`.

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `IN_PROGRESS` or `OVERDUE`.
- Expert is assigned to contract.

System behavior:

1. Compute the next unfulfilled checkpoint type for this milestone (`MIDPOINT`, then `PRE_DEADLINE`, then `NULL`).
2. Locate the latest open on-demand request for the same contract milestone.
3. Compute `is_late` against the on-demand request deadline when such a request
   exists; otherwise compute it against the scheduled checkpoint deadline.
4. Insert `milestone_progress_reports` with:
   - required `content`;
   - optional `percent_complete`, `attachment_url`, `source_code_url`,
     `demo_link`, and `submission_notes`;
   - optional `checkpoint_type`;
   - computed `is_late`.
5. If an open on-demand request exists, set its status to `SUBMITTED`, record
   `submitted_at`, and set its `progress_report_id`. A late submission closes the
   request but preserves `is_late = true`.
6. Write audit log.
7. Notify Business.

Business view: `GET` list of all progress reports for a milestone, ordered
oldest first. Business, Expert, assigned Staff, and authorized Admin may read
the reports for contracts they are permitted to inspect.

### 9.2B Business Requests An On-Demand Progress Report

Actor: Business

Preconditions:

- Contract status is `ACTIVE`.
- Business owns the contract.
- Milestone belongs to the contract.
- Milestone status is `IN_PROGRESS` or `OVERDUE`.
- There is no request for the milestone with status `PENDING` and
  `due_at > now()`.

SLA:

- Request number 1 is due 24 hours after creation.
- Request number 2 and every later request are due 12 hours after creation.
- Request counts are monotonic and must not be reset when an earlier request is
  submitted or expires.

System behavior:

1. Lock the contract milestone or request counter.
2. Re-check that no still-valid pending request exists.
3. Allocate the next request number.
4. Create a progress-report request with status `PENDING`, `requested_at = now`,
   and the applicable `due_at`.
5. Notify Expert with type `PROGRESS_REPORT_REQUESTED`.
6. Write `PROGRESS_REPORT_REQUESTED` audit event.

Derived response flags:

```text
progressReportRequestPending =
  latestRequest.status = PENDING AND latestRequest.dueAt >= now

progressReportRequestOverdue =
  latestRequest.status = PENDING AND latestRequest.dueAt < now
```

Expiry is time-derived and may also be materialized as `EXPIRED` by a scheduled
job. The result must be equivalent and idempotent.

An expired request:

- does not automatically judge work quality;
- does not automatically create a dispute;
- does not move money;
- does not apply a penalty;
- unlocks Business immediate termination under section 11A.

### 9.2C Business Feedback On A Progress Report

Actor: Business

Preconditions:

- Business owns the contract.
- Report belongs to the specified contract and milestone.
- Milestone is not terminal.

Feedback fields:

- `category`: `CORE_LOGIC`, `UI_UX`, `SECURITY`, `PERFORMANCE`, or `OTHER`;
- `severity`: `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL`;
- `dod_items`: zero or more acceptance-criteria identifiers with pass/fail and
  optional comment;
- required free-text `feedback`;
- `requires_adjustment`;
- `feedback_by_account_id` and `feedback_at`.

Feedback is a work-cycle record. It does not itself create a dispute or change
milestone status. Business may update feedback only through an audited revision
operation; previous feedback content must remain recoverable through audit or
version history.

### 9.2D Milestone Overdue Detection

Actor: System

When `now()` passes the computed milestone deadline and the milestone is still
`IN_PROGRESS`, the system atomically changes both live and contract execution
status to `OVERDUE`, writes an audit event, and notifies both participants.
Repeated detection is idempotent. `OVERDUE` does not release escrow and does not
prevent Expert from submitting a report or final deliverable.

### 9.2E Business Review SLA Auto-Approval

If the product enables review-SLA auto-approval through an active system
setting, an `UNDER_REVIEW` milestone may be approved automatically only after
the configured review deadline expires.

The same locking, escrow-release, ledger, notification, dispute-resolution, and
contract-completion rules from section 9.4 apply. Auto-approval must be
idempotent and must be disabled when an active dispute or termination request
exists. The SLA value must come from configuration, not a hard-coded controller
parameter.

### 9.3 Submit Deliverable

Actor: Expert

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `IN_PROGRESS` or `OVERDUE`, or `DISPUTED` if re-submit
  during self-resolve.
- Expert is assigned to contract.
- Milestone escrow is deposited.
- Milestone escrow is not released.
- Re-submit count does not exceed configured maximum if current dispute was caused by deliverable rejection.

System behavior for normal submit:

1. Create new `deliverables` row.
2. Set submission round.
3. Set milestone status to `UNDER_REVIEW`.
4. Write audit log.
5. Notify Business.

System behavior for re-submit during self-resolve:

1. Use the active dispute.
2. Create new `deliverables` row with `submitted_during_dispute_id` if field exists.
3. Increment or derive resubmit count.
4. Set milestone status to `UNDER_REVIEW`.
5. Keep dispute status `PENDING_SELF_RESOLVE`.
6. Write audit log.
7. Notify Business.

### 9.4 Business Approves Milestone

Actor: Business

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `UNDER_REVIEW`.
- Business owns contract.
- Milestone escrow exists.
- `milestones.escrow_released_at IS NULL`.
- No active termination request.

System behavior:

1. Lock milestone row.
2. Verify `escrow_released_at IS NULL`.
3. Lock relevant wallet rows.
4. Release 100% milestone escrow to Expert available balance.
5. Set `milestones.escrow_released_at = now()`.
6. Set `settlement_source_type = BUSINESS_APPROVAL`.
7. Set `settlement_source_id = NULL` unless approval record exists.
8. Set milestone status `COMPLETED`.
9. If there is active self-resolve dispute, set dispute `RESOLVED` and `resolution_type = BUSINESS_APPROVED_AFTER_SELF_RESOLVE`.
10. Write wallet ledger.
11. Write audit log.
12. Notify Expert.
13. If all milestones are completed, set contract `COMPLETED` and notify Admin to refund contract deposit.

Important:

- Business approval after self-resolve must resolve the active dispute, not cancel it.
- Approval must not create Staff settlement.

### 9.5 Business Rejects Deliverable

Actor: Business

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `UNDER_REVIEW`.
- Business owns contract.
- Milestone escrow exists and is not released.
- No active termination request.

There are two valid branches.

#### 9.5.1 First Rejection

Additional precondition:

- No active dispute exists for milestone.

System behavior:

1. Create dispute.
2. Set dispute status `PENDING_SELF_RESOLVE`.
3. Set initiation type `BUSINESS_REJECTED_DELIVERABLE`.
4. Set initiated by Business account.
5. Store rejection reason/evidence.
6. Store `previous_milestone_status = UNDER_REVIEW`.
7. Set milestone status `DISPUTED`.
8. Write audit log.
9. Notify Expert.

#### 9.5.2 Reject Re-submitted Deliverable

Additional precondition:

- Active dispute exists for milestone.
- Active dispute status is `PENDING_SELF_RESOLVE`.
- Milestone was moved back to `UNDER_REVIEW` by Expert re-submit.

System behavior:

1. Do not create a new dispute.
2. Append rejection reason/evidence to the existing dispute or attachment/log table.
3. Set milestone status back to `DISPUTED`.
4. Keep dispute status `PENDING_SELF_RESOLVE`.
5. Write audit log.
6. Notify Expert.

This fixes the v1 bug where reject re-submission was blocked by the `no active dispute` precondition.

### 9.6 All Milestones Completed

Actor: System

Preconditions:

- Contract status is `ACTIVE`.
- Every milestone under contract is `COMPLETED`.
- No active dispute.
- No active termination request.

System behavior:

1. Set contract status `COMPLETED`.
2. Notify Admin that 20% contract security deposit can be refunded.
3. Do not open cross-review yet.
4. Cross-review opens only after contract becomes `CLOSED`.

### 9.7 Admin Refunds Contract Security Deposit After Completion

Actor: Admin

Preconditions:

- Contract status is `COMPLETED`.
- Contract deposit status is `HELD` or compatible refundable status.
- All milestones completed.
- No active dispute.
- No active termination request.

System behavior:

1. Lock contract deposit row.
2. Lock relevant wallets.
3. Refund 20% contract deposit to Business available balance. Refund amount must equal exactly `contract_deposits.held_amount` (100%). Partial refund percentages are not supported (see 4.1); Admin may only choose refund or withhold, never a fraction.
4. Write `wallet_transactions` with `transaction_type = CONTRACT_DEPOSIT_REFUND`.
5. Update `contract_deposits.status = REFUNDED`.
6. Set `contract_deposits.refunded_at = now()`.
7. Set contract status `CLOSED`.
8. Open review capability for both parties.
9. Write audit log.
10. Notify Business and Expert.

---

## 10. Flow 5 — Dispute Resolution

### 10.1 Dispute Initiation Overview

A dispute is a formal disagreement about the current milestone, deliverable, scope, acceptance criteria, review behavior, or work evidence.

Both Business and Expert may initiate dispute in valid contexts.

Business usually initiates dispute by rejecting a deliverable.

Expert may initiate dispute when there is a valid issue such as:

- Business requests out-of-scope work.
- Business rejects deliverable unfairly or outside acceptance criteria.
- Business does not review or respond appropriately.
- Business changes requirements after milestone started.

### 10.2 Expert-Initiated Dispute

Actor: Expert

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `IN_PROGRESS`, `OVERDUE`, or `UNDER_REVIEW`.
- Milestone must not be `PENDING`, `DEPOSITED`, `COMPLETED`, or `CANCELLED`.
- Expert belongs to contract.
- No active dispute exists for milestone.
- No active termination request exists for contract.
- Expert provides reason and supporting evidence/file.

System behavior:

1. Create dispute.
2. Set `initiated_by_account_id = expertAccountId`.
3. Set `initiated_by = EXPERT`.
4. Set `initiation_type` to one of `EXPERT_SCOPE_CONCERN`, `EXPERT_NO_REVIEW_RESPONSE`, `EXPERT_BAD_FAITH_REJECTION`, or `OTHER` (see 7.5).
5. Store reason/evidence.
6. Store `previous_milestone_status` from milestone current status.
7. Set dispute status `PENDING_SELF_RESOLVE`.
8. Set milestone status `DISPUTED`.
9. Write audit log.
10. Notify Business.

### 10.3 One Active Dispute Per Milestone

A milestone must not have more than one active dispute.

If an active dispute exists:

- Do not create another dispute.
- Append comments/evidence to existing dispute.
- Allow either party to request Staff intervention on the existing dispute.

Database must enforce this using a partial unique index if PostgreSQL is available.

### 10.4 Self-Resolve Phase

Self-resolve means Business and Expert attempt to resolve the disagreement before Staff intervention.

Self-resolve may be:

#### 10.4.1 Correction-Based

Expert agrees that deliverable needs change.

Flow:

```text
DISPUTED -> Expert updates deliverable -> UNDER_REVIEW -> Business approves or rejects again
```

#### 10.4.2 Explanation-Based

Expert believes the deliverable already satisfies requirements.

Expert may submit:

- Explanation mapping deliverable to acceptance criteria.
- Evidence showing completed features.
- Screenshots, demo link, source code, document, or report.
- Reason why Business request is out of scope.
- Reason why Business rejection is not aligned with SoW.

Business may then:

- Approve milestone; or
- Maintain rejection; or
- Request Staff intervention.

### 10.5 Re-submit Limit

Maximum re-submit count is 3 for Business-rejection deliverable disputes.

Important rule:

```text
The 3 re-submit limit is a maximum self-resolve limit, not a prerequisite for Staff escalation.
```

Either Business or Expert may request Staff intervention at any time during active dispute if the issue is serious.

### 10.6 Request Staff Intervention

Actor: Business or Expert

Preconditions:

- Active dispute exists.
- Dispute status is `PENDING_SELF_RESOLVE`.
- Requester is Business or Expert of the contract.
- Requester submits written reason and supporting file/evidence.

System behavior:

1. Set dispute status `ESCALATION_REQUESTED`.
2. Store `escalation_requested_by_account_id`.
3. Store `escalation_requested_at`.
4. Store reason and file/attachment.
5. Notify Admin.
6. Write audit log.

### 10.7 Admin Assigns Staff

Actor: Admin

Preconditions:

- Dispute status is `ESCALATION_REQUESTED`.
- Dispute has required escalation reason/evidence.
- Admin selects suitable Staff.

Staff selection criteria:

- Job domain.
- Required skills.
- Required technologies if relevant.
- Staff specialization.
- Staff workload if available.
- Conflict of interest if available.

Candidate response must expose, without leaking unrelated private information:

- Staff identifier and display name.
- Specialization/domain match.
- Required technology/skill match summary.
- Availability: `IDLE` or `BUSY`.
- Active dispute workload count.
- Conflict-of-interest eligibility when known.

The system should rank suitable candidates and may propose or automatically
assign the best eligible Staff when `auto_assign_staff_enabled` is active.
Admin remains able to select or replace an eligible Staff before a final
decision. Automatic routing is operational assignment only; it does not grant
Admin any professional decision authority.

System behavior:

1. Set `assigned_staff_id`.
2. Set dispute status `STAFF_REVIEWING`.
3. Set `staff_review_started_at = now()`.
4. Set `evidence_collection_due_at = now() + 48 hours`.
5. Grant temporary `READ_EXECUTE` case access to assigned Staff.
6. Set `staff_access_expires_at = evidence_collection_due_at + 3 days`.
7. Set `staff_sla_due_at = evidence_collection_due_at + 3 days`.
8. Notify Staff, Business, and Expert of the evidence deadline and assigned
   reviewer.
9. Write audit log.

During the 48-hour evidence window:

- Business and Expert may add case attachments.
- Assigned Staff may inspect case materials.
- Staff must not issue the official decision before the window closes.

After the evidence window:

- Assigned Staff has until `staff_sla_due_at` to issue the final decision.
- Missing the SLA sets `staff_sla_escalated_at` once and notifies Admin.
- SLA escalation may cause reassignment before a decision, but does not permit
  Admin to decide, revise, or change payout percentage.
- Temporary access expires when the case resolves, Staff is replaced, or
  `staff_access_expires_at` is reached.

Admin does not decide the professional outcome.

### 10.8 Staff Rejects Intervention

Actor: Staff

Preconditions:

- Dispute status is `STAFF_REVIEWING`.
- Staff is assigned to dispute.

Valid reasons:

- Evidence is insufficient.
- Rejection is not specific to acceptance criteria.
- Parties should continue self-resolve.
- Request appears to be premature.
- Request appears to be abusive or delaying.

System behavior:

1. Store `intervention_rejection_reason`.
2. Set `intervention_rejected_at`.
3. Write status history/log entry `INTERVENTION_REJECTED`.
4. Return current dispute status to `PENDING_SELF_RESOLVE`.
5. Keep milestone status `DISPUTED`.
6. Notify Business and Expert.
7. Write audit log.

### 10.9 Staff Issues Mandatory Decision

Actor: Staff

Preconditions:

- Dispute status is `STAFF_REVIEWING`.
- Staff is assigned to dispute.
- Milestone status is `DISPUTED`.
- Milestone escrow is not released.

Staff decision must include:

- Expert payout percentage from 0 to 100.
- Staff report.
- Decision reason.
- Evidence summary.
- Optional recommended actions.
- Evidence-based explanation mapped to available case evidence and acceptance
  criteria.

System behavior:

1. Validate payout percentage.
2. Calculate expert payout amount.
3. Calculate business refund amount by subtraction.
4. Store Staff report and decision fields.
5. Set dispute status `STAFF_DECIDED`.
6. Set `staff_decided_at = now()`.
7. Write audit log.
8. Trigger system settlement execution automatically.

Staff decision is mandatory. Business and Expert cannot reject it.
Admin cannot approve, adjust, reject, return, or request revision of it.

### 10.10 System Executes Dispute Settlement

Actor: System

Preconditions:

- Dispute status is `STAFF_DECIDED`.
- Milestone status is `DISPUTED`.
- `milestones.escrow_released_at IS NULL`.
- Milestone escrow amount is available.
- No active termination settlement is executing for same milestone.

System behavior:

1. Start transaction.
2. Lock milestone row.
3. Re-check `escrow_released_at IS NULL`.
4. Lock Business wallet and Expert wallet rows.
5. Debit Business escrow by full milestone escrow amount.
6. Credit Expert available by expert payout amount.
7. Credit Business available by business refund amount.
8. Write wallet ledger entries.
9. Set milestone `escrow_released_at = now()`.
10. Set milestone `settlement_source_type = DISPUTE`.
11. Set milestone `settlement_source_id = disputeId`.
12. Set milestone `resolved_by_dispute_id = disputeId`.
13. Set milestone status `COMPLETED`.
14. Set dispute `settlement_executed_at = now()`.
15. Set dispute status `RESOLVED`.
16. Set dispute `resolution_type = STAFF_DECISION_SETTLEMENT`.
17. Commit transaction.
18. Notify Business and Expert.
19. If all milestones completed, set contract `COMPLETED`.

If settlement execution fails after Staff decision:

- Keep dispute status `STAFF_DECIDED`.
- Do not mark milestone completed.
- Do not set `escrow_released_at` unless wallet movement is committed.
- Log error for retry.

Retry may be initiated by an internal system job or an authorized operational
endpoint, but retry must use the exact Staff percentage already stored. Retry
does not create a new decision step.

### 10.11 Cancel Or Withdraw Dispute

Actor: Dispute initiator or Admin

Rules:

- The dispute initiator may withdraw the dispute before Staff starts reviewing.
- Admin may cancel invalid or duplicate disputes.
- The non-initiating party cannot unilaterally cancel the dispute.
- No party can cancel after `STAFF_REVIEWING`, `STAFF_DECIDED`, or `RESOLVED`.

Preconditions for initiator withdrawal:

- Current user is `disputes.initiated_by_account_id`.
- Dispute status is `PENDING_SELF_RESOLVE` or `ESCALATION_REQUESTED`.
- Staff review has not started.

System behavior:

1. Set dispute status `CANCELLED`.
2. Set `cancelled_by_account_id`.
3. Set `cancelled_at`.
4. Set `resolution_type = CANCELLED_BY_INITIATOR` or `CANCELLED_BY_ADMIN`.
5. Restore milestone status to `previous_milestone_status`.
6. Write audit log.
7. Notify both parties.

If Business wants to accept the deliverable after self-resolve, Business should use approve milestone, not cancel dispute.

---

## 11. Flow 4b — Contract Termination

### 11.1 Request Termination

Actor: Business or Expert

Preconditions:

- Contract status is `ACTIVE`.
- Requester belongs to contract.
- No active termination request exists for contract.
- Request includes reason and optional evidence file.

System behavior:

1. Create `termination_requests` row.
2. Set status:
   - `AWAITING_EXPERT_RESPONSE` when Business requests termination.
   - `REQUESTED` when Expert requests termination.
3. Set requested by account and role.
4. Set current milestone if determinable.
5. Set contract status `TERMINATION_PENDING`.
6. For a Business request, notify Expert and set the three-day response
   deadline. For an Expert request, notify Admin.
7. Write audit log.

For a Business request:

- Expert acceptance ends the waiting state and performs penalty-free termination
  cleanup.
- Expert disagreement moves the same request to `REQUESTED` and notifies Admin
  for Staff assignment.
- No Expert response within three days is treated as acceptance by an
  idempotent scheduled system operation.

### 11.2 Admin Assigns Staff For Termination

Actor: Admin

Preconditions:

- Termination request status is `REQUESTED`.
- Contract status is `TERMINATION_PENDING`.

System behavior:

1. Select Staff based on job domain and skill requirements.
2. Set `assigned_staff_id`.
3. Set status `STAFF_REVIEWING`.
4. Notify Staff.
5. Notify Business and Expert.
6. Write audit log.

### 11.3 Staff Rejects Termination

Actor: Staff

Preconditions:

- Termination request status is `STAFF_REVIEWING`.
- Staff is assigned.

System behavior:

1. Store Staff decision reason.
2. Set request status `STAFF_REJECTED`.
3. Set contract status back to `ACTIVE`.
4. Notify Business and Expert.
5. Write audit log.

### 11.4 Staff Approves Termination

Actor: Staff

Preconditions:

- Termination request status is `STAFF_REVIEWING`.
- Staff is assigned.
- If current milestone has active dispute, termination approval may be recorded, but settlement execution must wait until active dispute is resolved.

System behavior:

1. Evaluate current milestone state.
2. Determine whether milestone settlement is required.
3. Determine Expert payout percentage if settlement is required.
4. Store Staff report and decision reason.
5. Set status `STAFF_APPROVED`.
6. If settlement is required, set status `AWAITING_SETTLEMENT_EXECUTION`.
7. If settlement is not required, set status `AWAITING_DEPOSIT_REFUND` and set contract `TERMINATED`.
8. Write audit log.

### 11.5 Current Milestone Termination Cases

#### Case A — Current Milestone `PENDING`

- Business has not deposited milestone escrow.
- Expert should not have started official work.
- No payout required unless there is separately accepted evidence and Staff decides otherwise.
- Current milestone becomes `CANCELLED`.
- Future milestones become `CANCELLED`.

#### Case B — Current Milestone `DEPOSITED`

- Business deposited escrow.
- Expert has not officially started.
- Expert may submit partial evidence only if Staff asks or termination reason requires review.
- If no valid evidence, Expert payout = 0%, Business refund = 100% escrow.
- Milestone may become `CANCELLED` after full escrow refund if no payout.
- If Staff grants payout, settlement executes and milestone becomes `COMPLETED`.

#### Case C — Current Milestone `IN_PROGRESS` Or `OVERDUE`

- Expert may submit partial evidence.
- Staff evaluates partial work.
- Staff decides payout percentage.
- System splits escrow.
- Milestone becomes `COMPLETED` after settlement.

#### Case D — Current Milestone `UNDER_REVIEW`

- Expert submitted deliverable.
- Staff evaluates deliverable against acceptance criteria.
- Staff decides payout percentage.
- System splits escrow.
- Milestone becomes `COMPLETED` after settlement.

#### Case E — Current Milestone `DISPUTED`

- Active dispute already exists.
- Termination settlement must not run in parallel.
- Resolve dispute first.
- After dispute resolution, continue termination flow if still applicable.

### 11.6 Partial Evidence In Termination

If milestone is `DEPOSITED`, `IN_PROGRESS`, or `OVERDUE`, Expert may provide
partial evidence.

Accepted evidence examples:

- Source code repository.
- Document or design file.
- Demo link/video.
- Screenshots.
- Work log.
- Technical explanation.

If Expert provides no evidence:

```text
Expert payout = 0%
Business refund = 100% current milestone escrow
```

### 11.7 Execute Termination Settlement

Actor: System

Preconditions:

- Termination request status is `AWAITING_SETTLEMENT_EXECUTION`.
- Contract status is `TERMINATION_PENDING`.
- No active dispute exists on current milestone.
- Milestone escrow is not released.
- Staff decision exists.

System behavior:

1. Start transaction.
2. Lock termination request row.
3. Lock current milestone row.
4. Verify `milestones.escrow_released_at IS NULL`.
5. Lock Business and Expert wallets.
6. Debit Business escrow by full milestone escrow amount.
7. Credit Expert available by expert payout amount.
8. Credit Business available by business refund amount.
9. Write wallet ledger entries.
10. Set milestone `escrow_released_at = now()`.
11. Set `settlement_source_type = TERMINATION`.
12. Set `settlement_source_id = terminationRequestId`.
13. Set `resolved_by_termination_request_id = terminationRequestId`.
14. Set current milestone status `COMPLETED` if Expert payout > 0 or Staff settlement closes milestone.
15. Set future non-completed milestones to `CANCELLED`.
16. Set termination request `settlement_executed_at = now()`.
17. Set termination request status `AWAITING_DEPOSIT_REFUND`.
18. Set contract status `TERMINATED`.
19. Commit transaction.
20. Notify Admin to refund contract deposit.

If expert payout = 0 and full escrow is refunded to Business, current milestone may be `CANCELLED` instead of `COMPLETED` if Staff reports no accepted work.

The Staff report must state whether current milestone is closed as `COMPLETED` or `CANCELLED`.

### 11.8 Admin Refunds Contract Security Deposit After Termination

Actor: Admin

Preconditions:

- Contract status is `TERMINATED`.
- Termination request status is `AWAITING_DEPOSIT_REFUND`.
- Required milestone settlement is done or not required.
- Contract deposit is refundable.

System behavior:

1. Lock contract deposit row.
2. Lock Business wallet.
3. Refund 20% contract security deposit to Business available balance. Refund amount must equal exactly `contract_deposits.held_amount` (100%). Partial refund percentages are not supported (see 4.1); Admin may only choose refund or withhold, never a fraction.
4. Write wallet ledger entry.
5. Update contract deposit status `REFUNDED`.
6. Set `contract_deposits.refunded_at = now()`.
7. Set termination request status `COMPLETED`.
8. Set contract status `CLOSED`.
9. Open review capability.
10. Write audit log.
11. Notify Business and Expert.

### 11.9 Withdraw Termination Request

Actor: Requester

Preconditions:

- Current user is termination request requester.
- Termination request status is `REQUESTED` or `STAFF_REVIEWING`.
- A Business requester may also withdraw while status is
  `AWAITING_EXPERT_RESPONSE`.
- Staff has not issued decision.

System behavior:

1. Set termination request status `CANCELLED`.
2. Set cancellation fields.
3. Set contract status back to `ACTIVE`.
4. Write audit log.
5. Notify both parties and assigned Staff if any.

---

## 11A. Immediate Termination Without Fixed Penalty

Immediate termination is a guarded alternative to Staff-reviewed standard
termination. It is not a punishment mechanism and does not decide disputed work.

### 11A.1 Common Preconditions

- Caller is Business or Expert attached to the contract.
- Contract status is `ACTIVE`.
- No active termination request exists.
- No milestone is `UNDER_REVIEW` or `DISPUTED`.
- All unreleased milestone escrow can be identified and refunded atomically.
- Caller explicitly confirms immediate termination and provides a reason.

### 11A.2 Business Eligibility

Business may terminate immediately when either condition is true:

1. The contract has no final-deliverable rejection history; or
2. The latest on-demand progress-report request is overdue and still has no
   linked Expert submission.

The overdue-request exception allows Business to terminate even if earlier final
deliverables were rejected. Scheduled checkpoint lateness alone does not qualify.

### 11A.3 Expert Eligibility

Expert may terminate immediately when the common preconditions are satisfied.
This operation does not debit Expert's available wallet and does not pay a fixed
compensation percentage to Business.

### 11A.4 Financial And State Behavior

1. Start one transaction and lock contract, affected milestones, escrow balances,
   and contract deposit.
2. Refund every unreleased unfinished milestone escrow amount to Business.
3. Preserve completed milestones.
4. Mark all other unfinished milestones `CANCELLED`.
5. Set contract status `TERMINATED`.
6. Do not transfer a fixed percentage between Business and Expert.
7. Do not partially deduct the 20% contract security deposit.
8. Leave contract security-deposit disposition to the existing binary
   Admin-controlled refund/withhold process.
9. Write ledger, audit, and participant notifications.

If work quality, rejection, payout, or evidence is contested, immediate
termination must reject and the parties must use standard termination or dispute.

---

## 12. Cross-Review Flow

### 12.1 When Reviews Open

Reviews open only when:

```text
contract.status = CLOSED
```

This means:

- All milestone or termination settlement is complete.
- 20% contract security deposit is refunded.
- Contract has reached final state.

### 12.2 Review Rules

- Business may review Expert.
- Expert may review Business.
- Review is optional in MVP.
- No deadline required in MVP.
- Each reviewer can review each reviewee once per contract.

Database should enforce or service should enforce:

```text
unique(contract_id, reviewer_id, reviewee_id)
```

---

## 13. Database Impact And Required Migration Plan

### 13.1 Migration Policy

Agents must follow these rules:

1. Do not edit old Flyway migrations.
2. Query the repository's latest applied migration and create the next available
   version. At the time of v2.1 planning this is expected to be after the
   existing Flow 4–5 migrations; do not assume or reuse `V45`.
3. Do not delete or truncate existing data by default.
4. Prefer additive schema changes and compatibility backfills.
5. Normalize old data to the new schema where safe.
6. Insert new seed/config/demo data only if needed.
7. Destructive reset is not allowed unless explicitly approved for dev/test.
8. Keep JPA entities aligned with schema because `ddl-auto=validate` is used.
9. Do not use legacy `transactions` for new wallet movement.
10. Do not reintroduce `invoices`.

### 13.2 Data Normalization Policy

After schema changes:

- Map old dispute statuses to new statuses.
- Leave new audit fields `NULL` if historical data cannot be safely inferred.
- Do not fabricate settlement source for legacy rows.
- Existing wallet ledger rows may keep `contract_id`, `milestone_id`, and `metadata` as `NULL` if not safely inferable.
- New v2 wallet rows must always populate `contract_id`, `milestone_id` when applicable, `reference_type`, `reference_id`, and `metadata`.
- Existing completed milestones must not be re-settled.

### 13.3 Required Existing Table Changes

#### 13.3.1 `contracts`

Add status values:

```text
TERMINATION_PENDING
TERMINATED
CLOSED
```

Do not remove `CANCELLED` unless codebase confirms it is unused.

#### 13.3.2 `milestones`

Add statuses:

```text
OVERDUE
CANCELLED
```

`escrow_released_at`, `settlement_source_type`, and `settlement_source_id` already exist on `MilestoneEntity`/`milestones`. Add the two still-missing fields (required by 10.10 step 12 and 11.7 step 13, which set them):

```sql
resolved_by_dispute_id BIGINT NULL,
resolved_by_termination_request_id BIGINT NULL
```

Recommended indexes:

```sql
CREATE INDEX idx_milestones_contract_status ON milestones(contract_id, status);
CREATE INDEX idx_milestones_settlement_source ON milestones(settlement_source_type, settlement_source_id);
```

Recommended FKs after `termination_requests` exists:

```sql
ALTER TABLE milestones
ADD CONSTRAINT fk_milestones_resolved_by_dispute
FOREIGN KEY (resolved_by_dispute_id) REFERENCES disputes(dispute_id);

ALTER TABLE milestones
ADD CONSTRAINT fk_milestones_resolved_by_termination
FOREIGN KEY (resolved_by_termination_request_id) REFERENCES termination_requests(termination_request_id);
```

#### 13.3.3 `contract_milestones`

Add status:

```text
CANCELLED
```

Do not add settlement audit fields here for MVP. Settlement source of truth is `milestones`.

Add field (checkpoint anchor for `9.2A` progress reports):

```sql
in_progress_started_at TIMESTAMP NULL
```

Set when milestone transitions `DEPOSITED -> IN_PROGRESS` (`9.2`). Backfill existing `IN_PROGRESS` rows from `updated_at` since no earlier signal exists.

The contract execution snapshot must support `OVERDUE`. The source-of-truth
deadline is derived from `in_progress_started_at`, `duration`, and
`duration_unit`; do not store a second independently editable deadline unless
the existing schema already requires it.

#### 13.3.4 `disputes`

Replace or expand status values to:

```text
PENDING_SELF_RESOLVE
ESCALATION_REQUESTED
STAFF_REVIEWING
INTERVENTION_REJECTED
STAFF_DECIDED
RESOLVED
CANCELLED
```

The following already exist on `DisputeEntity`/`disputes` and cover the equivalent proposed field below (names differ, no action needed):

```text
initiated_by            (existing) covers initiated_by_role
escalation_reason       (existing)
escalation_evidence_file (existing) covers escalation_file_url
staff_decision_percentage (existing) covers staff_proposed_expert_percentage
staff_decision_note     (existing) covers staff_decision_reason
previous_milestone_status (existing)
resolution_type         (existing)
resolved_at             (existing)
cancelled_at            (existing)
```

Correction: an earlier pass of this section mistakenly claimed `initiation_type` had no defined enum and dropped it. That was wrong — 7.5 "Dispute Initiation Types" defines a required 5-value enum (`BUSINESS_REJECTED_DELIVERABLE`, `EXPERT_SCOPE_CONCERN`, `EXPERT_NO_REVIEW_RESPONSE`, `EXPERT_BAD_FAITH_REJECTION`, `OTHER`), distinct from `initiated_by` (which only says Business vs Expert, not the reason category). 9.5.1 step 3 and 10.2 step 4 both set it. It must be added.

Add the remaining fields — each is set by a concrete system-behavior step (9.5, 10.2, 10.6, 10.7, 10.8, 10.9, 10.10, 11.x) that current code cannot fully execute without them:

```sql
initiated_by_account_id BIGINT NULL,
initiation_type VARCHAR(80) NULL,

escalation_requested_by_account_id BIGINT NULL,
escalation_requested_at TIMESTAMP NULL,

staff_review_started_at TIMESTAMP NULL,
staff_decided_at TIMESTAMP NULL,
evidence_collection_due_at TIMESTAMP NULL,
staff_access_scope VARCHAR(50) NULL,
staff_access_expires_at TIMESTAMP NULL,
staff_sla_due_at TIMESTAMP NULL,
staff_sla_escalated_at TIMESTAMP NULL,
intervention_rejected_at TIMESTAMP NULL,
intervention_rejection_reason TEXT NULL,

staff_report TEXT NULL,
staff_proposed_expert_amount DECIMAL(19,2) NULL,
business_refund_amount DECIMAL(19,2) NULL,

settlement_executed_at TIMESTAMP NULL,
settlement_wallet_transaction_id BIGINT NULL,

cancelled_by_account_id BIGINT NULL,
cancellation_reason TEXT NULL
```

Do not add or use:

```text
admin_final_expert_percentage
admin_final_note
admin_revision_note
admin_revision_requested_at
REPORT_REVISION_REQUESTED
```

Those fields/status belong to the rejected Admin-override model.

Recommended indexes:

```sql
CREATE INDEX idx_disputes_contract_status ON disputes(contract_id, status);
CREATE INDEX idx_disputes_milestone_status ON disputes(milestone_id, status);
CREATE INDEX idx_disputes_assigned_staff_status ON disputes(assigned_staff_id, status);
```

Required one-active-dispute guard:

```sql
CREATE UNIQUE INDEX uq_disputes_one_active_per_milestone
ON disputes(milestone_id)
WHERE status IN (
  'PENDING_SELF_RESOLVE',
  'ESCALATION_REQUESTED',
  'STAFF_REVIEWING',
  'STAFF_DECIDED'
);
```

Status backfill recommendation:

```text
Open        -> PENDING_SELF_RESOLVE
UnderReview -> STAFF_REVIEWING
Escalated   -> ESCALATION_REQUESTED
Resolved    -> RESOLVED
Rejected    -> CANCELLED or INTERVENTION_REJECTED depending on context; if unknown, prefer CANCELLED with migration comment
```

#### 13.3.5 `wallet_transactions`

Add fields:

```sql
contract_id BIGINT NULL REFERENCES contracts(contract_id),
milestone_id BIGINT NULL REFERENCES milestones(milestone_id),
metadata JSONB NULL
```

Recommended indexes:

```sql
CREATE INDEX idx_wallet_tx_contract ON wallet_transactions(contract_id);
CREATE INDEX idx_wallet_tx_milestone ON wallet_transactions(milestone_id);
CREATE INDEX idx_wallet_tx_reference ON wallet_transactions(reference_type, reference_id);
```

Metadata standard:

```json
{
  "settlementSourceType": "DISPUTE",
  "disputeId": 8,
  "terminationRequestId": null,
  "staffUserId": 2,
  "businessUserId": 5,
  "expertUserId": 9,
  "expertPayoutPercentage": 70,
  "expertPayoutAmount": 7000000,
  "businessRefundAmount": 3000000
}
```

#### 13.3.6 `reviews`

Uniqueness (one review per reviewer per contract, see 12.2) is enforced at service level for MVP, not by a DB constraint. `AdminService.createReview` already guards this via `existsByContractIdAndReviewerId(contractId, reviewerId)`. Since a contract has exactly one Business and one Expert, checking `(contract_id, reviewer_id)` is equivalent to checking `(contract_id, reviewer_id, reviewee_id)` — no separate `reviewee_id` check is needed.

A DB unique index is optional / nice-to-have, not required for MVP:

```sql
-- Optional. Only add after confirming no duplicate rows exist.
CREATE UNIQUE INDEX uq_reviews_one_per_pair_per_contract
ON reviews(contract_id, reviewer_id, reviewee_id);
```

If added later, the migration must not fail silently on duplicates. Either clean duplicates in dev/test first or defer the index with a clear note.

#### 13.3.7 `deliverables` Recommended

Optional but recommended for v2 re-submit tracking:

```sql
submission_round INT NOT NULL DEFAULT 1,
submitted_during_dispute_id BIGINT NULL REFERENCES disputes(dispute_id),
status VARCHAR(50) NULL
```

Recommended status values:

```text
SUBMITTED
APPROVED
REJECTED
SUPERSEDED
```

If not implemented, service must derive latest deliverable by `created_at` and count re-submits through dispute logs/attachments.

#### 13.3.8 Required New Table: `milestone_progress_reports`

Create table for `9.2A`:

```sql
CREATE TABLE milestone_progress_reports (
    progress_report_id BIGSERIAL PRIMARY KEY,
    contract_id INT NOT NULL REFERENCES contracts(contract_id),
    milestone_id INT NOT NULL REFERENCES milestones(milestone_id),
    submitted_by_account_id INT NOT NULL REFERENCES account(account_id),
    checkpoint_type VARCHAR(20) NULL,
    content TEXT NOT NULL,
    percent_complete INT NULL,
    attachment_url TEXT NULL,
    source_code_url TEXT NULL,
    demo_link TEXT NULL,
    submission_notes TEXT NULL,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    business_feedback TEXT NULL,
    feedback_category VARCHAR(30) NULL,
    feedback_severity VARCHAR(20) NULL,
    feedback_dod_items JSONB NULL,
    requires_adjustment BOOLEAN NOT NULL DEFAULT FALSE,
    feedback_by_account_id INT NULL REFERENCES account(account_id),
    feedback_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_progress_report_checkpoint_type CHECK (checkpoint_type IN ('MIDPOINT', 'PRE_DEADLINE') OR checkpoint_type IS NULL),
    CONSTRAINT chk_progress_report_percent CHECK (percent_complete IS NULL OR (percent_complete BETWEEN 0 AND 100))
);

CREATE INDEX idx_milestone_progress_reports_milestone ON milestone_progress_reports(milestone_id, created_at);
```

Not linked to `disputes` or `case_attachments` — kept as an independent
progress-work-cycle record per `9.2A`–`9.2C`.

The report remains independent of disputes and case attachments, but may link to
the on-demand request it satisfies. Structured feedback belongs to the report
work cycle and must not be interpreted as a dispute decision.

#### 13.3.9 Required New Table: `milestone_progress_report_requests`

Use a history table rather than copying mutable request state into both
`milestones` and `contract_milestones`:

```sql
CREATE TABLE milestone_progress_report_requests (
    progress_report_request_id BIGSERIAL PRIMARY KEY,
    contract_id INT NOT NULL REFERENCES contracts(contract_id),
    milestone_id INT NOT NULL REFERENCES milestones(milestone_id),
    requested_by_account_id INT NOT NULL REFERENCES account(account_id),
    request_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP NULL,
    progress_report_id BIGINT NULL REFERENCES milestone_progress_reports(progress_report_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_progress_report_request_status
      CHECK (status IN ('PENDING', 'SUBMITTED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT uq_progress_report_request_number
      UNIQUE (contract_id, milestone_id, request_number)
);

CREATE INDEX idx_progress_report_requests_milestone_due
ON milestone_progress_report_requests(contract_id, milestone_id, status, due_at);

CREATE UNIQUE INDEX uq_progress_report_request_pending
ON milestone_progress_report_requests(contract_id, milestone_id)
WHERE status = 'PENDING';
```

If the implementation chooses to materialize `EXPIRED`, it must atomically
transition the prior pending row before allowing a new request. The service must
still reject two simultaneously pending requests.

`ContractMilestoneViewResponse` must derive and expose:

```text
progressReportRequestCount
progressReportRequestedAt
progressReportDueAt
progressReportSubmittedAt
progressReportRequestPending
progressReportRequestOverdue
```

Do not duplicate these mutable values as independently writable columns on both
`milestones` and `contract_milestones`. If compatibility columns already exist,
they are read-model caches only and must be updated from the request row in the
same transaction.

### 13.4 Required New Table: `termination_requests`

Create table:

```sql
CREATE TABLE termination_requests (
    termination_request_id BIGSERIAL PRIMARY KEY,

    contract_id BIGINT NOT NULL REFERENCES contracts(contract_id),
    current_milestone_id BIGINT NULL REFERENCES milestones(milestone_id),

    requested_by_account_id BIGINT NOT NULL REFERENCES account(account_id),
    requested_by_role VARCHAR(20) NOT NULL,
    request_reason TEXT NOT NULL,
    request_file_url TEXT NULL,

    assigned_staff_id BIGINT NULL REFERENCES staffs(staff_id),

    status VARCHAR(50) NOT NULL,

    staff_review_started_at TIMESTAMP NULL,
    staff_decided_at TIMESTAMP NULL,

    staff_decision_reason TEXT NULL,
    staff_report TEXT NULL,

    expert_payout_percentage DECIMAL(5,2) NULL,
    expert_payout_amount DECIMAL(19,2) NULL,
    business_refund_amount DECIMAL(19,2) NULL,

    partial_evidence_required BOOLEAN NOT NULL DEFAULT FALSE,
    partial_evidence_submitted_at TIMESTAMP NULL,
    partial_evidence_url TEXT NULL,
    partial_evidence_note TEXT NULL,

    settlement_executed_at TIMESTAMP NULL,
    settlement_wallet_transaction_id BIGINT NULL,

    deposit_refund_required BOOLEAN NOT NULL DEFAULT TRUE,
    deposit_refunded_at TIMESTAMP NULL,
    deposit_refund_transaction_id BIGINT NULL,

    cancelled_at TIMESTAMP NULL,
    cancelled_by_account_id BIGINT NULL REFERENCES account(account_id),
    cancellation_reason TEXT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Unique active request guard:

```sql
CREATE UNIQUE INDEX uq_termination_one_active_per_contract
ON termination_requests(contract_id)
WHERE status IN (
  'AWAITING_EXPERT_RESPONSE',
  'REQUESTED',
  'STAFF_REVIEWING',
  'STAFF_APPROVED',
  'AWAITING_SETTLEMENT_EXECUTION',
  'AWAITING_DEPOSIT_REFUND'
);
```

Indexes:

```sql
CREATE INDEX idx_termination_contract_status
ON termination_requests(contract_id, status);

CREATE INDEX idx_termination_staff_status
ON termination_requests(assigned_staff_id, status);
```

Business-created termination requests also require a response deadline:

```sql
ALTER TABLE termination_requests
ADD COLUMN expert_response_due_at TIMESTAMP NULL,
ADD COLUMN expert_responded_at TIMESTAMP NULL;
```

### 13.5 Required New Table: `case_attachments`

Create table:

```sql
CREATE TABLE case_attachments (
    attachment_id BIGSERIAL PRIMARY KEY,
    owner_type VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL,
    uploaded_by_account_id BIGINT NOT NULL REFERENCES account(account_id),
    file_url TEXT NOT NULL,
    file_name TEXT NULL,
    file_type VARCHAR(100) NULL,
    note TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Owner types:

```text
DISPUTE
TERMINATION_REQUEST
DELIVERABLE_REJECTION
PARTIAL_EVIDENCE
STAFF_REPORT
```

Recommended index:

```sql
CREATE INDEX idx_case_attachments_owner ON case_attachments(owner_type, owner_id);
```

### 13.6 Tables Not To Reuse Incorrectly

Do not use these for v2 core logic:

| Table | Reason |
|---|---|
| `transactions` | Legacy milestone transaction table. New flow must use `wallet_transactions`. |
| `payment_order` | PayOS wallet top-up provider flow, not internal milestone settlement. |
| `contract_change_requests` | Legacy/disabled change request lifecycle, semantic mismatch with termination. |
| `invoices` | Removed; do not reintroduce. |

---

## 14. Wallet Ledger Requirements

### 14.1 Wallet Balance Types

Existing balance types include:

```text
AVAILABLE
ESCROW
HOLDING
DISPUTE
```

For v2 MVP:

- Use `AVAILABLE` and `ESCROW` for milestone escrow.
- Do not move milestone escrow to `DISPUTE` balance by default.
- Dispute blocks contract logically, not by moving funds to disputed balance.

### 14.2 Required Ledger Values

Recommended transaction types:

```text
MILESTONE_ESCROW_DEPOSIT
MILESTONE_ESCROW_RELEASE
MILESTONE_ESCROW_REFUND
DISPUTE_SETTLEMENT_PAYOUT
DISPUTE_SETTLEMENT_REFUND
TERMINATION_SETTLEMENT_PAYOUT
TERMINATION_SETTLEMENT_REFUND
CONTRACT_DEPOSIT_REFUND
```

Recommended reference types:

```text
CONTRACT
MILESTONE
DISPUTE
TERMINATION_REQUEST
CONTRACT_DEPOSIT
```

### 14.3 Ledger Metadata Policy

`wallet_transactions` must use mixed model:

Columns:

```text
contract_id
milestone_id
reference_type
reference_id
```

Metadata JSONB:

```json
{
  "settlementSourceType": "DISPUTE",
  "disputeId": 8,
  "terminationRequestId": null,
  "staffUserId": 2,
  "businessUserId": 5,
  "expertUserId": 9,
  "expertPayoutPercentage": 70,
  "expertPayoutAmount": 7000000,
  "businessRefundAmount": 3000000
}
```

New v2 wallet movements must populate metadata consistently.

### 14.4 Atomic Settlement Rule

Any operation that releases milestone escrow must be atomic.

Required guard:

```text
if milestone.escrowReleasedAt != null:
    reject settlement
```

Settlement transaction must:

1. Lock milestone row.
2. Verify `escrow_released_at IS NULL`.
3. Lock wallet rows.
4. Move balances.
5. Write wallet ledger.
6. Set milestone settlement source fields.
7. Set `escrow_released_at`.
8. Commit.

If any step fails before commit, no balance or milestone state must be partially updated.

---

## 15. Service Layer Requirements

### 15.1 MilestoneService

Required methods or equivalent:

```text
depositMilestoneEscrow(contractId, milestoneId, businessAccountId)
startMilestone(milestoneId, expertAccountId)
markMilestoneOverdue(contractId, milestoneId)
requestProgressReport(contractId, milestoneId, businessAccountId)
submitProgressReport(contractId, milestoneId, expertAccountId, request)
feedbackProgressReport(contractId, milestoneId, progressReportId, businessAccountId, request)
autoApproveExpiredReviewSla(contractId, milestoneId)
submitDeliverable(milestoneId, expertAccountId, request)
approveMilestone(milestoneId, businessAccountId)
rejectMilestone(milestoneId, businessAccountId, request)
```

`rejectMilestone` must handle both first rejection and rejection after re-submit.

### 15.2 DisputeService

Required methods or equivalent:

```text
createDisputeFromBusinessRejection(milestoneId, businessAccountId, request)
createExpertInitiatedDispute(milestoneId, expertAccountId, request)
requestStaffIntervention(disputeId, accountId, request)
assignStaff(disputeId, adminAccountId, staffId)
listStaffAssignmentCandidates(disputeId, adminAccountId)
rejectIntervention(disputeId, staffAccountId, request)
issueStaffDecision(disputeId, staffAccountId, request)
executeDisputeSettlement(disputeId)
cancelDispute(disputeId, accountId, request)
resolveByBusinessApproval(disputeId, milestoneId)
```

`issueStaffDecision` stores the binding percentage and triggers
`executeDisputeSettlement`. No Admin final-decision method is permitted.

### 15.3 TerminationRequestService

Required methods or equivalent:

```text
requestTermination(contractId, requesterAccountId, request)
acceptBusinessTermination(terminationRequestId, expertAccountId)
disputeBusinessTermination(terminationRequestId, expertAccountId, request)
expireBusinessTerminationResponse(terminationRequestId)
assignStaff(terminationRequestId, adminAccountId, staffId)
rejectTermination(terminationRequestId, staffAccountId, request)
approveTermination(terminationRequestId, staffAccountId, request)
submitPartialEvidence(terminationRequestId, expertAccountId, request)
executeTerminationSettlement(terminationRequestId)
withdrawTerminationRequest(terminationRequestId, requesterAccountId, request)
refundDepositAfterTermination(terminationRequestId, adminAccountId, request)
immediateTerminate(contractId, participantAccountId, request)
```

### 15.4 WalletLedgerService

Required operations:

```text
moveAvailableToEscrow(walletId, amount, reference)
releaseEscrowToExpert(businessWalletId, expertWalletId, amount, reference)
splitEscrow(businessWalletId, expertWalletId, escrowAmount, expertPayoutAmount, businessRefundAmount, reference)
refundContractDeposit(contractDepositId, adminAccountId, reference)
```

`splitEscrow` must be shared by dispute settlement and termination settlement.

### 15.5 AuditLogService

Must log:

```text
MILESTONE_ESCROW_DEPOSITED
MILESTONE_STARTED
PROGRESS_REPORT_SUBMITTED
PROGRESS_REPORT_REQUESTED
PROGRESS_REPORT_FEEDBACK_RECORDED
PROGRESS_REPORT_REQUEST_EXPIRED
MILESTONE_MARKED_OVERDUE
MILESTONE_REVIEW_SLA_AUTO_APPROVED
DELIVERABLE_SUBMITTED
MILESTONE_APPROVED
MILESTONE_REJECTED
DISPUTE_CREATED
DISPUTE_ESCALATION_REQUESTED
DISPUTE_STAFF_ASSIGNED
DISPUTE_STAFF_SLA_ESCALATED
DISPUTE_INTERVENTION_REJECTED
DISPUTE_STAFF_DECIDED
DISPUTE_SETTLEMENT_EXECUTED
DISPUTE_CANCELLED
TERMINATION_REQUESTED
TERMINATION_ACCEPTED_BY_EXPERT
TERMINATION_DISPUTED_BY_EXPERT
TERMINATION_RESPONSE_EXPIRED
CONTRACT_IMMEDIATE_TERMINATED
TERMINATION_STAFF_ASSIGNED
TERMINATION_STAFF_REJECTED
TERMINATION_STAFF_APPROVED
TERMINATION_SETTLEMENT_EXECUTED
CONTRACT_DEPOSIT_REFUNDED
CONTRACT_CLOSED
REVIEW_CREATED
```

### 15.6 NotificationService

Must notify relevant parties for each major state change.

Metadata should include:

```json
{
  "contractId": 1,
  "milestoneId": 2,
  "disputeId": 3,
  "terminationRequestId": 4
}
```

---

## 16. Mandatory API Surface

All public routes in this specification must use `/api/v1`. Implementations must
not introduce unversioned aliases for these operations. Existing legacy routes
are outside this specification and should be migrated or deprecated separately.

### 16.0 Common API Contract

- Authentication: JWT bearer token for every route except separately documented
  public reads.
- Authorization: service-layer ownership and role checks are mandatory;
  controller path matching alone is insufficient.
- Content type: `application/json` unless uploading through the existing file
  service.
- Success envelope: existing project `ApiResponse<T>`.
- Validation failures return the existing project error envelope with stable
  machine-readable error code and human-readable message.
- Mutating financial/state endpoints must be idempotent against duplicate
  delivery. A repeated call may return the current result but must not move
  money or increment counters twice.
- Path `contractId` and `milestoneId` must describe the same persisted contract
  milestone. A mismatch returns not-found/forbidden according to the existing
  anti-enumeration policy.
- Timestamps are ISO-8601 and represent the backend clock.

New request DTO minimums:

```json
{
  "ProgressReportRequest": {},
  "ProgressReportSubmission": {
    "content": "Implemented authentication and validation",
    "percentComplete": 65,
    "attachmentUrl": "https://...",
    "sourceCodeUrl": "https://...",
    "demoLink": "https://...",
    "submissionNotes": "Known issue documented"
  },
  "ProgressReportFeedback": {
    "category": "CORE_LOGIC",
    "severity": "HIGH",
    "dodItems": [
      {"criteriaId": 12, "passed": false, "comment": "Missing refresh flow"}
    ],
    "feedback": "Please fix the refresh-token path.",
    "requiresAdjustment": true
  },
  "ImmediateTerminationRequest": {
    "reason": "Required progress report was not submitted by the deadline.",
    "confirmed": true
  }
}
```

New response DTO minimums:

```text
ContractMilestoneViewResponse:
  status, dueAt, overdue,
  progressReportRequestCount,
  progressReportRequestedAt,
  progressReportDueAt,
  progressReportSubmittedAt,
  progressReportRequestPending,
  progressReportRequestOverdue,
  rejectCount,
  lastRejectionFeedback

StaffAssignmentCandidateResponse:
  staffId, displayName, specializationMatch,
  technologyMatchSummary, availability,
  activeDisputeWorkloadCount, conflictEligible
```

Required stable conflict errors include:

```text
MILESTONE_NOT_EXECUTABLE
PROGRESS_REPORT_REQUEST_ALREADY_PENDING
PROGRESS_REPORT_REQUEST_NOT_FOUND
PROGRESS_REPORT_FEEDBACK_NOT_ALLOWED
EVIDENCE_WINDOW_STILL_OPEN
DISPUTE_ALREADY_ACTIVE
DISPUTE_NOT_STAFF_DECIDED
ESCROW_ALREADY_RELEASED
IMMEDIATE_TERMINATION_NOT_ALLOWED
TERMINATION_REQUEST_ALREADY_ACTIVE
```

### 16.1 Milestone APIs

```http
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit
POST /api/v1/milestones/{milestoneId}/start
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-report-request
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports
GET  /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback
POST /api/v1/milestones/{milestoneId}/deliverables
POST /api/v1/milestones/{milestoneId}/approve
POST /api/v1/milestones/{milestoneId}/reject
POST /api/v1/milestones/{milestoneId}/disputes
POST /api/v1/contracts/{contractId}/milestones/check-overdue
POST /api/v1/contracts/{contractId}/milestones/sla-auto-approve
```

The two scheduler-oriented routes are operational triggers. Production may run
the same service methods from scheduled jobs; if exposed, they require Admin or
internal-system authorization and remain idempotent.

### 16.2 Dispute APIs

```http
POST /api/v1/disputes/{disputeId}/escalation-request
GET  /api/v1/disputes/{disputeId}/staff-candidates
POST /api/v1/disputes/{disputeId}/assign-staff
POST /api/v1/disputes/{disputeId}/reject-intervention
POST /api/v1/disputes/{disputeId}/staff-decision
POST /api/v1/disputes/{disputeId}/execute-settlement
POST /api/v1/disputes/{disputeId}/cancel
GET  /api/v1/contracts/{contractId}/disputes
GET  /api/v1/disputes/{disputeId}
```

`staff-decision` stores the binding decision and triggers settlement.
`execute-settlement` is an internal/Admin-authorized retry endpoint only. It must
reuse the stored Staff percentage exactly. There is no
`/api/v1/disputes/{disputeId}/admin-final-decision` endpoint.

### 16.3 Termination APIs

```http
POST /api/v1/contracts/{contractId}/termination-requests
POST /api/v1/contracts/{contractId}/immediate-termination
POST /api/v1/termination-requests/{terminationRequestId}/accept
POST /api/v1/termination-requests/{terminationRequestId}/dispute
POST /api/v1/termination-requests/{terminationRequestId}/assign-staff
POST /api/v1/termination-requests/{terminationRequestId}/reject
POST /api/v1/termination-requests/{terminationRequestId}/approve
POST /api/v1/termination-requests/{terminationRequestId}/partial-evidence
POST /api/v1/termination-requests/{terminationRequestId}/execute-settlement
POST /api/v1/termination-requests/{terminationRequestId}/withdraw
POST /api/v1/termination-requests/{terminationRequestId}/refund-deposit
POST /api/v1/termination-requests/expire-awaiting-expert
GET  /api/v1/contracts/{contractId}/termination-requests
GET  /api/v1/termination-requests/{terminationRequestId}
```

`expire-awaiting-expert` is an Admin/internal idempotent trigger; a scheduled job
may invoke the same service without HTTP.

### 16.4 Case Attachment APIs

```http
POST /api/v1/case-attachments
GET  /api/v1/case-attachments?ownerType=...&ownerId=...
```

Upload authorization follows owner type and case participation. Assigned Staff
has access only while temporary case access is valid.

### 16.5 Review APIs

```http
POST /api/v1/contracts/{contractId}/reviews
GET  /api/v1/contracts/{contractId}/reviews
```

---

## 17. Authorization Matrix

| Action | Business | Expert | Admin | Staff | System |
|---|---:|---:|---:|---:|---:|
| Deposit milestone escrow | Yes | No | No | No | No |
| Start milestone | No | Yes | No | No | No |
| Request on-demand progress report | Contract Business | No | No | No | No |
| Submit progress report | No | Contract Expert | No | No | No |
| Feedback on progress report | Contract Business | No | No | No | No |
| Mark overdue / review-SLA auto-approve | No | No | Operational trigger | No | Yes |
| Submit deliverable | No | Yes | No | No | No |
| Approve milestone | Yes | No | No | No | No |
| Reject milestone | Yes | No | No | No | No |
| Expert initiate dispute | No | Yes | No | No | No |
| Request Staff intervention | Yes | Yes | No | No | No |
| List Staff candidates | No | No | Yes | No | No |
| Assign Staff to dispute | No | No | Yes | No | No |
| Reject intervention | No | No | No | Assigned Staff | No |
| Issue Staff dispute decision | No | No | No | Assigned Staff | No |
| Execute dispute settlement | No | No | Retry stored decision only | No | Yes |
| Cancel own dispute before Staff review | Initiator only | Initiator only | Yes for invalid/duplicate | No | No |
| Request termination | Yes | Yes | No | No | No |
| Accept/dispute Business termination request | No | Contract Expert | No | No | Timeout only |
| Immediate termination | Eligible Contract Business | Eligible Contract Expert | No | No | No |
| Assign Staff to termination | No | No | Yes | No | No |
| Approve/reject termination | No | No | No | Assigned Staff | No |
| Execute termination settlement | No | No | Retry only | No | Yes |
| Withdraw own termination request before Staff decision | Requester only | Requester only | Yes for invalid/duplicate | No | No |
| Refund contract deposit | No | No | Yes | No | System helper allowed |
| Create review after CLOSED | Yes | Yes | No | No | No |

---

## 18. Critical Invariants

1. A milestone cannot start before Business deposits milestone escrow.
2. A milestone escrow can be released only once.
3. `milestones.escrow_released_at` is the primary settlement idempotency guard.
4. Dispute settlement and termination settlement cannot execute for the same milestone escrow.
5. Each milestone can have at most one active dispute.
6. Each contract can have at most one active termination request.
7. Staff decision is mandatory after Staff accepts intervention and issues decision.
8. Admin assigns Staff but does not override Staff professional decision.
9. Business and Expert can request Staff intervention at any time during active dispute; three re-submits are not required first.
10. Three re-submits are a maximum self-resolve limit, not an escalation prerequisite.
11. Contract deposit refund must happen before contract becomes `CLOSED`.
12. Reviews open only when contract is `CLOSED`.
13. No destructive data deletion is allowed by default in migrations.
14. New v2 wallet movements must use `wallet_transactions`, not legacy `transactions`.
15. Contract deposit refund is binary — 100% of `held_amount` or 0% — never a partial percentage (see 4.1, 9.7, 11.8).
16. Every public route defined by this specification starts with `/api/v1`.
17. Admin never approves, revises, or changes an assigned Staff dispute
    decision or payout percentage.
18. Immediate termination never charges either party a fixed 10% penalty.
19. At most one on-demand progress-report request may be pending per contract
    milestone.
20. Missing a scheduled progress checkpoint alone never unlocks immediate
    termination; only an expired on-demand request does.
21. An expired report request never moves money or adjudicates work quality.
22. Staff cannot issue the official dispute decision before the 48-hour evidence
    window ends.
23. Settlement retry must use the stored Staff percentage unchanged.

---

## 19. Test Scenarios

### 19.1 Happy Path Milestone Completion

Given contract is `ACTIVE` and milestone is `PENDING`  
When Business deposits milestone escrow  
Then milestone becomes `DEPOSITED`

When Expert starts milestone  
Then milestone becomes `IN_PROGRESS`

When Expert submits deliverable  
Then milestone becomes `UNDER_REVIEW`

When Business approves  
Then escrow is released 100% to Expert  
And milestone becomes `COMPLETED`  
And `escrow_released_at` is set

### 19.2 Business Rejects, Expert Re-submits, Business Approves

Given milestone is `UNDER_REVIEW`  
When Business rejects deliverable  
Then dispute is created with `PENDING_SELF_RESOLVE`  
And milestone becomes `DISPUTED`

When Expert re-submits  
Then milestone becomes `UNDER_REVIEW`  
And dispute remains active

When Business approves  
Then dispute becomes `RESOLVED`  
And `resolution_type = BUSINESS_APPROVED_AFTER_SELF_RESOLVE`  
And escrow releases 100% to Expert

### 19.3 Business Rejects Re-submission

Given active dispute exists with `PENDING_SELF_RESOLVE`  
And Expert re-submitted deliverable  
And milestone is `UNDER_REVIEW`  
When Business rejects again  
Then no new dispute is created  
And rejection reason is appended to existing dispute  
And milestone returns to `DISPUTED`

### 19.4 Staff Dispute Decision 70/30

Given milestone escrow is 10,000,000  
And dispute is `STAFF_REVIEWING`  
When Staff decides Expert payout 70%  
Then dispute becomes `STAFF_DECIDED`

When system executes settlement  
Then Expert receives 7,000,000  
And Business receives 3,000,000 refund  
And milestone becomes `COMPLETED`  
And dispute becomes `RESOLVED`  
And `escrow_released_at` is set

### 19.5 Staff Rejects Intervention

Given dispute status is `STAFF_REVIEWING`  
When Staff rejects intervention with reason  
Then intervention rejection is logged  
And current dispute status returns to `PENDING_SELF_RESOLVE`  
And milestone remains `DISPUTED`

### 19.6 Expert-Initiated Dispute

Given milestone is `IN_PROGRESS`  
When Expert initiates dispute with evidence that Business requested out-of-scope work  
Then dispute is created  
And milestone becomes `DISPUTED`  
And previous milestone status is stored as `IN_PROGRESS`

When Expert withdraws dispute before Staff review  
Then dispute becomes `CANCELLED`  
And milestone returns to `IN_PROGRESS`

### 19.7 Termination While Dispute Active

Given current milestone is `DISPUTED`  
And active dispute exists  
When Expert or Business requests termination  
Then termination request can be created  
But termination settlement cannot execute until dispute is resolved

### 19.8 Termination With IN_PROGRESS Milestone

Given current milestone is `IN_PROGRESS`  
And Business has deposited escrow  
When Business requests termination  
And Staff approves with Expert payout 40%  
Then system splits escrow 40% to Expert and 60% to Business  
And current milestone becomes `COMPLETED` or `CANCELLED` based on Staff report  
And future milestones become `CANCELLED`  
And contract becomes `TERMINATED`  
And waits for deposit refund

### 19.9 Contract Closure After Refund

Given contract is `COMPLETED` or `TERMINATED`  
And contract deposit is held  
When Admin refunds deposit  
Then contract becomes `CLOSED`  
And reviews are opened

### 19.10 Double Settlement Guard

Given milestone `escrow_released_at` is not null  
When any service tries to release escrow again  
Then operation fails  
And no wallet balance changes

### 19.11 On-Demand Progress Report SLA

Given an `IN_PROGRESS` milestone with no pending request
When Business creates the first request
Then request number is 1 and deadline is 24 hours

When Business tries again before that deadline and before Expert submission
Then the request is rejected

When Expert submits after the deadline
Then the request becomes submitted
And the report has `is_late = true`

When Business creates the next request
Then request number is 2 and deadline is 12 hours

### 19.12 Overdue Request Unlocks Penalty-Free Immediate Termination

Given an on-demand report request is expired without submission
And no milestone is `UNDER_REVIEW` or `DISPUTED`
When Business terminates immediately
Then unreleased unfinished milestone escrow is refunded to Business
And unfinished milestones become `CANCELLED`
And no fixed percentage is transferred to Expert
And the contract security deposit is not partially deducted

### 19.13 Staff Decision Cannot Be Overridden

Given assigned Staff issues a valid 70% Expert decision after evidence window
When settlement executes or retries
Then Expert receives 70% and Business receives the remainder
And Admin cannot change the percentage or request decision revision

### 19.14 Evidence Window And Staff SLA

Given Admin assigns Staff
Then evidence collection closes after 48 hours
And temporary Staff access and Staff decision SLA end three days later

When Staff attempts a final decision before evidence collection closes
Then the operation is rejected

When Staff misses the decision SLA
Then escalation metadata is set once and Admin is notified
And Admin still cannot decide the payout

### 19.15 Business Termination Response

Given Business creates a standard termination request
Then it enters `AWAITING_EXPERT_RESPONSE`

When Expert disputes within three days
Then the same request enters `REQUESTED` for Staff assignment

When Expert accepts or does not respond for three days
Then unfinished escrow is refunded and the contract terminates without a fixed
penalty

---

## 20. Acceptance Criteria

### 20.1 Functional Acceptance

- Business can deposit current milestone escrow.
- Expert cannot start milestone before escrow deposit.
- Business can request progress reports with 24-hour first and 12-hour later
  request SLAs without creating duplicate pending requests.
- Expert submissions close pending report requests and preserve late status.
- Business can record structured report feedback without changing milestone
  state.
- `OVERDUE` milestones continue to accept reports and deliverables.
- Business approval releases 100% escrow to Expert.
- Business rejection creates or updates active dispute correctly.
- Expert can initiate dispute in allowed states.
- Only one active dispute exists per milestone.
- Either party can request Staff intervention at any time during active dispute.
- Staff can reject intervention and return dispute to self-resolve.
- Staff can issue mandatory decision.
- Staff cannot decide before the evidence window closes.
- Admin cannot override or revise Staff's decision.
- System executes dispute settlement and splits escrow correctly.
- Business and Expert can request termination.
- Business termination requests support Expert accept/dispute/three-day timeout.
- Eligible participants can terminate immediately without a fixed 10% penalty.
- Termination settlement cannot double-settle active dispute milestone.
- Admin can refund contract deposit after completion/termination.
- Contract becomes `CLOSED` only after deposit refund.
- Reviews are available only after `CLOSED`.

### 20.2 Database Acceptance

- New Flyway migration applies cleanly on current schema.
- No existing data is deleted by default.
- JPA validation passes.
- New status values are supported by DB constraints and Java enums.
- `termination_requests` table exists.
- `case_attachments` table exists.
- `wallet_transactions` has `contract_id`, `milestone_id`, and `metadata JSONB`.
- `milestones` has settlement guard fields.
- Partial unique index prevents multiple active disputes per milestone.
- Partial unique index prevents multiple active termination requests per contract.
- Progress-report request history exists and prevents two pending requests for
  the same contract milestone.
- Rejected Admin-final-decision fields and statuses are absent.

### 20.3 Financial Acceptance

- `expertPayoutAmount + businessRefundAmount = milestoneEscrowAmount` always.
- No milestone escrow can be released twice.
- Ledger entries are posted for every wallet balance movement.
- New v2 ledger rows include contract and milestone references.
- Contract deposit refund is recorded in wallet ledger and contract deposit row.
- Immediate termination creates no fixed-percentage compensation transaction.
- Settlement retry uses the stored Staff percentage unchanged.

### 20.4 Harness Agent Acceptance

- Agents inspect current migrations/entities before changing code.
- Agents do not edit old migrations.
- Agents do not delete data by default.
- Agents implement guards before UI/controller polish.
- Agents add tests for financial invariants.
- Agents do not use legacy `transactions` for v2 settlement.
- Agents do not reintroduce `invoices`.

---

## 21. Implementation Order For Harness Agents

Recommended order:

1. Database Agent:
   - Add migration.
   - Add/adjust enums.
   - Create new tables.
   - Add indexes/constraints.
   - Add backfill script.

2. Backend Entity Agent:
   - Update JPA entities.
   - Add enums.
   - Add repositories.

3. Wallet Agent:
   - Implement milestone escrow deposit.
   - Implement split escrow.
   - Implement idempotency guard.
   - Implement ledger metadata.

4. Milestone Agent:
   - Implement milestone state transitions.
   - Implement `OVERDUE` detection and review-SLA auto-approval guards.
   - Implement progress-report request history, 24h/12h SLA, submissions, and
     structured feedback.
   - Implement submit/re-submit.
   - Implement approve/reject logic.

5. Dispute Agent:
   - Implement dispute initiation, escalation, candidate ranking, evidence
     window, Staff access/SLA, final Staff decision, settlement execution, and
     cancel rules.

6. Termination Agent:
   - Implement termination request response lifecycle, settlement, and guarded
     immediate termination without fixed penalty.

7. Contract Closure Agent:
   - Implement deposit refund closure and review opening.

8. Integration Agent:
   - Ensure contract/milestone/dispute/termination/wallet status changes are consistent.
   - Ensure every public route uses `/api/v1`.

9. Tester Agent:
   - Implement unit tests and integration tests for all scenarios.

10. Reviewer Agent:
   - Verify invariants, authorization, and no double-settlement.

---

## 22. Final Notes For Agents

- Do not simplify Staff decision into Business/Expert acceptance. Staff decision is mandatory.
- Do not add Admin final review, payout adjustment, report revision, or approval
  after Staff decision.
- Do not add a fixed 10% immediate-termination penalty for Business or Expert.
- Do not use partial contract security-deposit deduction as a termination
  penalty.
- Do not expose a new public Flow 4–5 route without the `/api/v1` prefix.
- Do not allow Staff to decide before the 48-hour evidence window closes.
- Do not allow multiple pending on-demand report requests for one contract
  milestone.
- Do not treat scheduled checkpoint lateness as the on-demand SLA violation that
  unlocks immediate termination.
- Do not require 3 re-submits before Staff escalation. Three re-submits are only the self-resolve maximum.
- Do not allow Expert to start work before milestone escrow deposit.
- Do not allow milestone escrow release without checking `escrow_released_at`.
- Do not allow termination settlement to run while active dispute settlement is unresolved.
- Do not leave future milestones as `PENDING` after contract termination.
- Do not open reviews before contract `CLOSED`.
- Do not delete existing data unless project owner explicitly approves a dev/test reset.


# SPEC-MILESTONE-DISPUTER.md - v2.4

**Project:** AITASKER-BE  
**Platform:** AITASKER - AI expert and business matching platform
**Spec purpose:** Implementation guide for harness engineering agents  
**Version:** v2.4
**Prepared date:** 2026-07-01  
**Last updated:** 2026-07-11
**Primary target:** Backend agents, database agents, integration agents, tester agents, reviewer agents  
**Language:** English technical specification

---

## 0. Executive Summary

This specification defines the final v2 design for the remaining contract execution flows of AITASKER:

1. **Flow 4 â€” Milestone Execution, Escrow Payment, Contract Completion, and Contract Termination**
2. **Flow 5 â€” Milestone Dispute, Self-Resolve, Staff Intervention, Staff Settlement Decision, and Settlement Execution**

The v2 design replaces the earlier incomplete milestone payment model with a **per-milestone escrow model**:

- Business milestone escrow deposit starts the milestone timeline immediately;
  there is no separate wait for Expert to start the clock.
- The milestone escrow is released only once.
- Normal approval releases 100% of milestone escrow to Expert.
- Dispute or termination settlement releases a Staff-decided percentage to Expert and refunds the remaining amount to Business.
- Contract security deposits are separate from milestone escrow and are refunded
  automatically by the system after contract completion or valid termination,
  with Admin retry/audit only.

The spec also fixes the review findings from v1:

- Business rejection does not automatically create a dispute. It records
  rejection feedback and returns the milestone to `IN_PROGRESS`.
- Expert revises and re-submits through the normal deliverable flow.
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
- Progress reports support product links and Business feedback during milestone
  execution; feedback also acknowledges the report when it is waiting for
  acknowledgement, but it does not create dispute or revision semantics.
- Dispute Staff assignment is automatic/manual-by-Staff-pool and does not require
  Admin assignment. Admin is not part of milestone-dispute routing and cannot
  cancel milestone disputes.
- Assigned Staff remains the final professional decision-maker and cannot reject
  intervention after a case has been escalated.
- Business holds a 20% contract security deposit and Expert holds a separate 10%
  contract performance deposit before activation.
- Immediate termination does not require Staff and charges the initiating party
  10% of total contract value for the other party.

### 0.1 v2.4 Binding Decisions

The following decisions override conflicting experimental code or supplementary
notes:

1. There is no `admin-final-decision` dispute step.
2. There is no `REPORT_REVISION_REQUESTED` dispute status.
3. Staff's valid decision is final and triggers system settlement.
4. Admin cannot adjust Staff's payout percentage, including by a plus/minus
   tolerance.
5. Rejecting a final deliverable never creates a dispute automatically.
6. Rejection stores feedback, marks the rejected deliverable, and returns the
   milestone from `UNDER_REVIEW` to `IN_PROGRESS`.
7. Re-submission is the normal Expert submit flow; it does not require a dispute
   or a special re-submit request.
8. Dispute begins only through an explicit dispute action by Business or Expert.
9. Business must hold 20% of total contract value and Expert must hold 10%
   before the contract becomes `ACTIVE`.
10. Immediate termination requires no Staff review and charges 10% of total
    contract value to the initiating party for the counterparty.
11. A Business immediate-termination penalty is funded from Business's held 20%
    deposit; the remaining held Business deposit is refundable.
12. An Expert immediate-termination penalty consumes Expert's held 10% deposit.
13. Standard termination/disputed termination remains evidence-based and
    Staff-reviewed; the fixed immediate-termination penalty does not apply.
14. An overdue on-demand progress-report request may be used as Business's
    recorded reason, but immediate termination remains an explicit paid option;
    the timeout itself does not move money.
15. Business may cancel its own draft contract before it is signed or activated.
16. Business milestone escrow deposit starts the milestone execution timeline.
17. A new progress report cannot be submitted while the previous progress report
    is waiting for Business acknowledgement.
18. Structured progress-report feedback APIs are restored for milestone
    progress tracking. Feedback may carry category, severity, DoD context,
    feedback text, and an adjustment flag, and it must acknowledge a pending
    report without creating report revision or dispute semantics.
19. Dispute intervention requests auto-assign or directly route to Staff; Admin
    assignment and Admin dispute cancellation are removed.
20. Staff cannot reject intervention; an escalated dispute must proceed to Staff
    decision.
21. Staff decision must not be blocked by a hard 48-hour evidence-window error.
22. WebSocket notifications must expose notification `type`, not a workflow
    `status`, for dispute intervention request events.

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
- Business progress-report acknowledgement gate.
- Deliverable submission.
- Business milestone approval.
- Business final-deliverable rejection with structured feedback.
- Normal Expert correction and re-submission after rejection.
- Explicit dispute initiation when either party contests scope, rejection,
  evidence, review conduct, or contract termination.
- Business or Expert dispute initiation.
- Dispute escalation request with file/evidence.
- Automatic/manual Staff-pool routing based on job domain and required skills.
- Staff candidate ranking, evidence window, temporary case access, and Staff SLA.
- Staff review.
- Staff mandatory decision.
- System settlement execution after Staff decision.
- Contract termination request by Business or Expert.
- Immediate termination with a fixed 10% contract-value penalty when section 11A
  guards are satisfied.
- Separate Business and Expert contract deposits.
- Termination Staff review.
- Partial work evidence for termination.
- Termination settlement.
- System resolution/refund of Business and Expert contract deposits.
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
- Automatic financial movement caused only by a missed progress-report deadline;
  the deadline only unlocks an explicit immediate-termination action.
- Multi-Staff voting or Staff committee review.
- Admin review or override of a valid Staff dispute decision.
- Appeal after a valid Staff dispute decision.

---

## 3. Roles And Responsibilities

### 3.1 Business

Business is the client that owns the job and contract budget.

Business can:

- Fund the required 20% Business contract deposit.
- Deposit milestone escrow.
- Review submitted deliverables.
- Approve a milestone.
- Reject a deliverable without automatically initiating a dispute.
- Cancel its own draft contract before either party signs or the contract enters
  `PENDING` or `ACTIVE`.
- Explicitly initiate dispute as a separate action when there is a genuine
  disagreement.
- Submit reasons/evidence for dispute.
- Request an on-demand progress report while the current milestone is
  `IN_PROGRESS` or `OVERDUE`.
- Acknowledge a submitted progress report so the Expert may submit the next
  progress report when another request is opened.
- Request Staff intervention for an active dispute.
- Request contract termination.
- Request immediate termination when section 11A guards are satisfied.
- Submit termination reason and evidence.
- Receive refund of unused milestone escrow after dispute or termination settlement.
- Receive refund of the remaining Business contract deposit after completion or
  valid termination.
- Review Expert after contract is closed.

Business cannot:

- Approve milestone if milestone is not `UNDER_REVIEW`.
- Start or submit Expert deliverables.
- Release escrow manually.
- Override Staff decision.
- Cancel a dispute initiated by Expert.
- Cancel a dispute after it has been routed to Staff.
- Withdraw contract deposit directly outside the system refund flow.

### 3.2 Expert

Expert performs the milestone work.

Expert can:

- Pay and hold the required 10% Expert contract performance deposit.
- Start a deposited milestone.
- Submit deliverable.
- Re-submit deliverable during self-resolve.
- Submit progress reports only when there is no previous progress report waiting
  for Business acknowledgement.
- Explain why the deliverable meets acceptance criteria.
- Initiate dispute in valid contexts.
- Submit reasons/evidence for dispute.
- Request Staff intervention for an active dispute.
- Request contract termination.
- Submit partial work evidence during termination review.
- Receive milestone payout.
- Receive refund of the Expert 10% deposit after completion or standard
  termination.
- Receive Business's 10% immediate-termination penalty when Business cancels
  unilaterally.
- Review Business after contract is closed.

Expert cannot:

- Start a milestone before Business deposits the milestone budget.
- Create dispute in `PENDING`, `DEPOSITED`, `COMPLETED`, or `CANCELLED` milestone states.
- Create a second active dispute on the same milestone.
- Cancel a dispute initiated by Business.
- Cancel a dispute after it has been routed to Staff.
- Override Staff decision.
- Start contract execution before the Expert 10% deposit is held.

### 3.3 Admin

Admin is an operational platform manager.

Admin can:

- Assign a suitable Staff to termination request.
- Choose termination Staff based on job domain, required skills, Staff
  specialization, workload, and conflict of interest if available.
- Replace a termination assignment before Staff decides when required for
  availability, conflict-of-interest, or SLA reasons.
- Cancel invalid or duplicate termination requests before settlement.
- Execute or approve participant deposit resolution, depending on existing wallet service design.
- Manage system categories, role access, and operational oversight.

Admin must not:

- Override Staff professional decision about deliverable quality or payout percentage.
- Approve, reject, request revision of, or alter a valid Staff dispute decision.
- Assign, reassign, or cancel milestone disputes. Milestone dispute routing is
  automatic/manual inside the Staff workflow, not Admin-operated.
- Execute duplicate milestone escrow settlement.
- Use legacy `transactions` for new wallet settlement.

### 3.4 Staff

Staff is the professional reviewer assigned to a dispute or termination case.

Staff can:

- Review dispute files, deliverables, SoW, milestone criteria, timeline, and evidence.
- Receive dispute intervention requests directly from the system assignment
  workflow.
- Issue a mandatory decision for an escalated dispute.
- Decide Expert payout percentage from 0% to 100%.
- Issue the final professional dispute decision without Admin approval.
- Write Staff report and decision rationale.
- Review termination request and decide whether termination is valid.
- Assess partial work evidence during termination.

Staff cannot:

- Manually move wallet balances outside approved service methods.
- Override wallet guards.
- Refund participant deposits directly unless existing system explicitly grants
  that operation.
- Reject intervention or return an escalated milestone dispute to self-resolve.
- Cancel a dispute.

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
- Returning initial participant deposits to both sides after normal completion or
  valid standard termination.

---

## 4. Core Financial Concepts

### 4.1 Participant Contract Deposits

Two deposits are required before contract activation:

| Owner | Required held amount | Purpose |
|---|---:|---|
| Business | 20% of total contract value | Contract security and funding commitment |
| Expert | 10% of total contract value | Expert performance commitment |

Both deposits:

- are paid after required contract/NDA signatures and before `ACTIVE`;
- are held in ledger-backed escrow/holding balance;
- are separate from per-milestone escrow;
- must have a persisted owner role and account;
- must be idempotently funded once;
- are normally refunded in full after valid completion or standard termination
  by an idempotent system operation;
- may be resolved differently only by an explicit rule in this specification.

The fixed immediate-termination penalty is the explicit exception:

- Business immediate termination transfers 10% of total contract value from the
  held Business deposit to Expert. The remaining Business deposit is refundable.
- Expert immediate termination transfers the full held Expert deposit, equal to
  10% of total contract value, to Business.
- The counterparty's own deposit remains unaffected and refundable.
- The platform does not keep the 10% penalty.

No service may charge the penalty from a participant's available wallet when the
required held deposit exists. Contract activation must prevent the insufficient-
deposit case.

### 4.1A Deposit Funding And Activation

Contract status remains `PENDING` until all of these are true:

1. Business and Expert signed the contract.
2. Business and Expert signed the NDA when required.
3. Business's 20% deposit is `HELD`.
4. Expert's 10% deposit is `HELD`.

Only then may the system atomically activate the contract and move the job to
`IN_PROGRESS`.

### 4.1B Deposit Resolution

Normal completion or standard termination:

```text
Business held deposit -> Business available: 100% of remaining held amount
Expert held deposit -> Expert available: 100% of remaining held amount
```

Business immediate termination:

```text
Business held deposit -> Expert available: 10% of total contract value
Business held deposit -> Business available: remaining held amount
Expert held deposit -> Expert available: full held amount
```

Expert immediate termination:

```text
Expert held deposit -> Business available: 10% of total contract value
Business held deposit -> Business available: full held amount
```

All deposit resolution must use wallet ledger entries and one-time resolution
guards. The sum of penalty plus refunds must equal the deposits' held amounts.

### 4.2 Milestone Escrow

Milestone escrow is the budget of the current milestone.

Business must deposit it before Expert work is considered started. A successful
milestone escrow deposit automatically starts the milestone; Expert no longer
needs to click start for the normal path.

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

### 4.4 Platform Fee And Immediate-Termination Penalty

There is no platform penalty or platform fee in v2 dispute settlement.

If Staff decides Expert receives 70%, the remaining 30% is refunded to Business.

There is no platform fee in dispute or standard termination settlement.

Immediate termination is different: it charges exactly 10% of total contract
value to the initiating party for the counterparty. This is compensation, not a
platform fee and not a Staff-decided milestone payout.

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
| `PENDING` | Contract pending signatures and/or required Business 20% and Expert 10% deposits. |
| `ACTIVE` | Contract is active and milestones can be executed. |
| `TERMINATION_PENDING` | A termination request is being reviewed. Contract execution actions are blocked except allowed termination/dispute actions. |
| `COMPLETED` | All milestones are completed. Contract waits for resolution/refund of both participant deposits. |
| `TERMINATED` | Termination and required milestone settlement are complete. Contract waits for unresolved participant deposits, unless immediate termination resolved them atomically. |
| `CLOSED` | Final state. Deposit refund is done. Cross-review is opened. |
| `CANCELLED` | Legacy or invalid cancellation state. Do not use for new valid v2 termination flow unless existing code requires compatibility. |

### 5.2A Draft Contract Cancellation

Business may cancel its own contract while it is still `DRAFT` and before either
side has signed or funded any contract deposit. This cancellation:

- sets the contract status to `CANCELLED`;
- returns the related job to the proposal-review/open state required by the
  existing marketplace flow;
- writes an audit event and notifies the Expert when a draft was already visible;
- does not require Expert approval.

Expert rejection of an eligible `DRAFT` or `PENDING` contract remains valid, but
Business draft cancellation is a separate Business-owned action.

### 5.3 Normal Completion Transitions

```text
ACTIVE -> COMPLETED -> CLOSED
```

Rules:

- `PENDING -> ACTIVE` occurs only after all signatures and both participant
  deposits are held.
- `ACTIVE -> COMPLETED` occurs after all contract milestones are `COMPLETED`.
- `COMPLETED -> CLOSED` occurs only after the system resolves/refunds both
  participant deposits.
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
  termination flow in section 11A and applies the fixed 10% contract-value
  penalty.
- `TERMINATED -> CLOSED` occurs only after the system resolves both participant
  deposits, including any immediate-termination penalty.

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
| `DEPOSITED` | Legacy compatibility state for milestones deposited before auto-start or for historical data. New successful deposits should move directly to `IN_PROGRESS`. |
| `IN_PROGRESS` | Expert is working on the milestone. Expert submits progress reports during this state per `9.2A`; reports do not change milestone status. |
| `OVERDUE` | The milestone execution deadline has passed while work remains active. Expert may still submit progress reports and a final deliverable; Business may request an on-demand progress report. |
| `UNDER_REVIEW` | Expert submitted deliverable; Business is reviewing. |
| `DISPUTED` | There is an active dispute on the milestone. Contract is blocked. |
| `COMPLETED` | Milestone is closed and escrow settlement is done. |
| `CANCELLED` | Milestone is closed because contract was terminated before it could be completed. |

### 6.3 Allowed Transitions

```text
PENDING -> IN_PROGRESS after Business deposits milestone escrow
DEPOSITED -> IN_PROGRESS for legacy compatibility via startMilestone
IN_PROGRESS -> OVERDUE when the milestone due time passes
IN_PROGRESS -> UNDER_REVIEW
OVERDUE -> UNDER_REVIEW
UNDER_REVIEW -> COMPLETED
UNDER_REVIEW -> IN_PROGRESS after Business rejection
UNDER_REVIEW -> DISPUTED
IN_PROGRESS -> DISPUTED
OVERDUE -> DISPUTED
DISPUTED -> IN_PROGRESS when Staff returns work for correction
DISPUTED -> UNDER_REVIEW only when a deliverable submitted before dispute
remains under formal review
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
PENDING -> IN_PROGRESS without a successful Business milestone escrow deposit
PENDING -> UNDER_REVIEW
PENDING -> COMPLETED without Staff termination rule
DEPOSITED -> UNDER_REVIEW
IN_PROGRESS -> COMPLETED without Business approval or Staff settlement
OVERDUE -> COMPLETED without Business approval, configured review-SLA
auto-approval, or Staff settlement
UNDER_REVIEW -> IN_PROGRESS without a persisted Business rejection and feedback
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
STAFF_DECIDED
RESOLVED
CANCELLED
```

### 7.2 Status Meanings

| Status | Meaning |
|---|---|
| `PENDING_SELF_RESOLVE` | Business and Expert are self-resolving the dispute. |
| `ESCALATION_REQUESTED` | A party submitted file/evidence requesting Staff intervention; the system must route the case to Staff without Admin assignment. |
| `STAFF_REVIEWING` | Staff is assigned by the system/manual Staff workflow and is reviewing the case. |
| `STAFF_DECIDED` | Staff issued mandatory decision; settlement execution is pending or retrying. |
| `RESOLVED` | Dispute is resolved and settlement or approval has been executed. |
| `CANCELLED` | Dispute was withdrawn by the initiator before Staff routing. Admin cancellation is out of scope for milestone disputes. |

### 7.3 Active Dispute Statuses

The following statuses count as active:

```text
PENDING_SELF_RESOLVE
ESCALATION_REQUESTED
STAFF_REVIEWING
STAFF_DECIDED
```

`INTERVENTION_REJECTED` is not a valid v2.3 current status. Existing data may be
read for compatibility, but new code must not create it.

### 7.4 Allowed Transitions

```text
PENDING_SELF_RESOLVE -> ESCALATION_REQUESTED
PENDING_SELF_RESOLVE -> RESOLVED
PENDING_SELF_RESOLVE -> CANCELLED
ESCALATION_REQUESTED -> STAFF_REVIEWING by automatic/manual Staff routing
ESCALATION_REQUESTED -> CANCELLED
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
| `BUSINESS_REJECTED_DELIVERABLE` | A participant explicitly opened dispute about a persisted Business rejection; rejection alone does not create it. |
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
| `AWAITING_DEPOSIT_REFUND` | Milestone settlement is done; waiting for system resolution/refund of Business and Expert deposits. |
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

## 9. Flow 4 â€” Milestone Execution And Escrow Payment

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
6. Set milestone status to `IN_PROGRESS`.
7. Mirror `contract_milestones.status` to `IN_PROGRESS`.
8. Set `contract_milestones.in_progress_started_at = now()` if it is not already
   set. This timestamp is the source of truth for milestone timeline, overdue
   detection, and progress-report checkpoint calculation.
9. Write audit log.
10. Notify Expert.

Ledger reference:

```text
transaction_type = MILESTONE_ESCROW_DEPOSIT
reference_type = MILESTONE
reference_id = milestoneId
contract_id = contractId
milestone_id = milestoneId
```

### 9.2 Start Milestone Compatibility

Actor: Expert

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `DEPOSITED` for legacy data, or already `IN_PROGRESS`
  after the automatic deposit flow.
- Expert is assigned to contract.
- No active dispute.
- No active termination request.

System behavior:

1. If milestone is already `IN_PROGRESS`, return success without changing the
   timeline or writing a duplicate start audit event.
2. If milestone is legacy `DEPOSITED`, set milestone status to `IN_PROGRESS`.
3. Do not reset `contract_milestones.in_progress_started_at`; the timeline
   starts when Business deposits milestone escrow.
4. Write audit log only for the legacy `DEPOSITED -> IN_PROGRESS` transition.

### 9.2A Submit Milestone Progress Report

Actor: Expert

Purpose: while a milestone is active, Expert reports current status so Business
can monitor project health. A report may satisfy a scheduled checkpoint, answer
an on-demand Business request, or be voluntary.

Checkpoints (mandatory, non-blocking):

- `MIDPOINT` â€” due at 50% of the milestone's declared timeline, measured from `contract_milestones.in_progress_started_at`.
- `PRE_DEADLINE` â€” due at 80% of the milestone's declared timeline (i.e. some time before the deadline).
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
- No previous progress report for the same contract milestone is waiting for
  Business acknowledgement.
- If Business has an active on-demand request, the report satisfies that request.
  If no active request exists, voluntary checkpoint reports are allowed only when
  the previous report has already been acknowledged.

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
6. Set the report acknowledgement state to `PENDING_BUSINESS_ACK`.
7. Write audit log.
8. Notify Business.

Submit gate:

- The Expert cannot submit another progress report for the same contract
  milestone while the latest report has acknowledgement state
  `PENDING_BUSINESS_ACK`.
- Business acknowledgement is required before another report can be accepted,
  regardless of whether the previous report came from a scheduled checkpoint,
  on-demand request, or voluntary update.
- Business may acknowledge with no comment, or may submit progress-report
  feedback. Feedback can carry category, severity, DoD item context, feedback
  text, and an adjustment flag; if the report is pending acknowledgement, the
  feedback action records acknowledgement at the same time.
- Progress-report feedback is a tracking response only. It must not create a
  dispute, deliverable rejection, report revision state, or money movement.

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
5. Reject the request if the latest progress report is still waiting for Business
   acknowledgement; Business must acknowledge that report first.
6. Notify Expert with type `PROGRESS_REPORT_REQUESTED`.
7. Write `PROGRESS_REPORT_REQUESTED` audit event.

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
- may be recorded as the reason for Business immediate termination under
  section 11A, but is not required for that paid option.

### 9.2C Business Acknowledges A Progress Report

Actor: Business

Preconditions:

- Business owns the contract.
- Report belongs to the specified contract and milestone.
- Milestone is not terminal.
- Report acknowledgement state is `PENDING_BUSINESS_ACK`.

System behavior:

1. Set report acknowledgement state to `ACKNOWLEDGED`.
2. Set `acknowledged_by_account_id` and `acknowledged_at`.
3. Write audit log.
4. Notify Expert that the next progress report can be submitted when a new
   request or checkpoint is applicable.

This acknowledgement is only a flow-control action. It does not:

- require structured feedback;
- request report revision;
- change milestone status;
- create a dispute;
- move money.

### 9.2C.1 Business Gives Progress-Report Feedback

Actor: Business

Endpoint:

```http
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback
```

Request body:

```json
{
  "category": "SCOPE",
  "severity": "INFO",
  "dodItems": ["API_CONTRACT_STABLE"],
  "feedback": "Progress is acceptable. Please include the demo link in the next report.",
  "requiresAdjustment": false
}
```

Preconditions:

- Business owns the contract.
- Report belongs to the specified contract and milestone.
- Milestone is not terminal.
- `feedback` is not blank.

System behavior:

1. Store `business_feedback`, `feedback_category`, `feedback_severity`,
   `feedback_dod_items`, `requires_adjustment`, `feedback_by_account_id`, and
   `feedback_at`.
2. If the report acknowledgement state is `PENDING_BUSINESS_ACK`, also set it
   to `ACKNOWLEDGED` and store `acknowledged_by_account_id` and
   `acknowledged_at`.
3. Write `PROGRESS_REPORT_FEEDBACK_RECORDED` audit log.
4. Notify Expert with `PROGRESS_REPORT_FEEDBACK_RECORDED`.

Feedback is a Business response to progress tracking only. It does not create a
dispute, deliverable rejection, report revision state, or automatic
termination/settlement action.

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
- Milestone status is `IN_PROGRESS` or `OVERDUE`.
- Expert is assigned to contract.
- Milestone escrow is deposited.
- Milestone escrow is not released.

System behavior:

1. Create new `deliverables` row.
2. Set `submission_round = previous maximum + 1`.
3. Mark the prior rejected deliverable `SUPERSEDED` when applicable.
4. Preserve prior rejection feedback and history.
5. Set the new deliverable status `SUBMITTED`.
6. Set milestone status to `UNDER_REVIEW`.
7. Write audit log.
8. Notify Business.

This same flow handles first submission and every correction/resubmission.
The backend response must expose the current `submission_round` so the FE can
display how many times the Expert has submitted or resubmitted the product.
Expert does not need:

- an active dispute;
- a special re-submit request;
- Business permission to resubmit;
- a dispute-specific endpoint.

The normal submission endpoint is the only final-product submission command.

### 9.3A Explicit Dispute Instead Of Resubmission

If Expert believes Business's rejection is invalid, out of scope, or in bad
faith, Expert may explicitly initiate a dispute from `IN_PROGRESS` after the
rejection. The rejected deliverable and rejection feedback become evidence.

Business may also explicitly initiate a dispute if correction cycles reveal a
scope/evidence disagreement that normal rejection feedback cannot resolve.

Neither action happens automatically from reject or resubmit.

### 9.4 Business Approves Milestone

Actor: Business

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is:
  - `UNDER_REVIEW`; or
  - `DISPUTED` with an active `PENDING_SELF_RESOLVE` dispute and a submitted
    deliverable that Business now accepts.
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
13. If all milestones are completed, set contract `COMPLETED` and trigger the
    system participant-deposit refund flow.

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
- No active dispute exists for the milestone. If a dispute is already active,
  parties must use the dispute flow rather than normal rejection.

System behavior:

1. Require non-empty rejection feedback.
2. Mark the current deliverable `REJECTED`.
3. Store `rejection_feedback` and `rejected_at` on that deliverable.
4. Increment milestone `reject_count`.
5. Store milestone `last_rejection_feedback`.
6. Set milestone status from `UNDER_REVIEW` to `IN_PROGRESS`.
7. Do not create a dispute.
8. Do not set milestone status to `DISPUTED`.
9. Write audit log.
10. Notify Expert to correct and submit through section 9.3.

The same behavior applies to the first rejection and every later rejection.
Historical deliverables and feedback must remain readable.

### 9.6 All Milestones Completed

Actor: System

Preconditions:

- Contract status is `ACTIVE`.
- Every milestone under contract is `COMPLETED`.
- No active dispute.
- No active termination request.

System behavior:

1. Set contract status `COMPLETED`.
2. Trigger the system participant-deposit refund flow.
3. Do not open cross-review yet.
4. Cross-review opens only after contract becomes `CLOSED`.

### 9.7 System Refunds Participant Deposits After Completion

Actor: System with Admin-visible audit

Preconditions:

- Contract status is `COMPLETED`.
- Business 20% and Expert 10% deposits are `HELD` or compatible refundable
  status.
- All milestones completed.
- No active dispute.
- No active termination request.

System behavior:

1. Lock both participant deposit rows.
2. Lock Business and Expert wallets.
3. Refund the full remaining Business deposit to Business.
4. Refund the full remaining Expert deposit to Expert.
5. Write one ledger refund entry per deposit.
6. Update both deposit statuses and refund timestamps.
7. Set contract status `CLOSED`.
8. Open review capability for both parties.
9. Write audit log.
10. Notify Business and Expert.

The refund must be executed automatically by the backend when the completion
preconditions are satisfied, or by an idempotent system job immediately after
completion. It must not remain a manual business step that leaves the initial
20%/10% participant deposits held indefinitely.

---

## 10. Flow 5 â€” Dispute Resolution

### 10.1 Dispute Initiation Overview

A dispute is a formal disagreement about the current milestone, deliverable, scope, acceptance criteria, review behavior, or work evidence.

Both Business and Expert may initiate dispute in valid contexts.

Rejecting a deliverable never initiates dispute automatically. Business or
Expert must call the explicit dispute API and provide a dispute reason/evidence.

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

1. Create dispute only because Expert explicitly requested it.
2. Set `initiated_by_account_id = expertAccountId`.
3. Set `initiated_by = EXPERT`.
4. Set `initiation_type` to one of `EXPERT_SCOPE_CONCERN`, `EXPERT_NO_REVIEW_RESPONSE`, `EXPERT_BAD_FAITH_REJECTION`, or `OTHER` (see 7.5).
5. Store reason/evidence.
6. Store `previous_milestone_status` from milestone current status.
7. Set dispute status `PENDING_SELF_RESOLVE`.
8. Set milestone status `DISPUTED`.
9. Write audit log.
10. Notify Business.

### 10.2A Business-Initiated Dispute

Actor: Business

Business rejection under section 9.5 is not this action.

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `IN_PROGRESS`, `OVERDUE`, or `UNDER_REVIEW`.
- Business owns the contract.
- No active dispute exists for the milestone.
- No active termination request exists.
- Business explicitly provides dispute reason and evidence.

System behavior:

1. Create one dispute because Business explicitly requested it.
2. Store `initiated_by_account_id`, role, initiation type, reason, and evidence.
3. Store the current milestone status as `previous_milestone_status`.
4. Set dispute status `PENDING_SELF_RESOLVE`.
5. Set milestone status `DISPUTED`.
6. Write audit and notify Expert.

When the dispute is about a prior rejection, the system links or references the
rejected deliverable and feedback; it does not rewrite rejection history.

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
DISPUTED -> parties agree to return to correction
-> dispute RESOLVED/CANCELLED with recorded agreement
-> milestone IN_PROGRESS
-> Expert submits through normal flow
-> milestone UNDER_REVIEW
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

### 10.5 Correction And Escalation Rules

Normal rejection/resubmission has no dispute-specific three-resubmit gate.
Every rejection remains auditable through deliverable history and
`reject_count`.

Either Business or Expert may explicitly initiate or escalate a dispute when the
issue is a genuine disagreement rather than an ordinary correction cycle. The
backend must not:

- auto-create dispute at rejection count 1, 3, or any other count;
- require three re-submissions before escalation;
- block a valid normal resubmission merely because a reject counter reached a
  configured threshold.

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
5. Notify Staff operations/system routing channel.
6. Write audit log.

### 10.7 Route Staff

Actor: System or Staff operations

Preconditions:

- Dispute status is `ESCALATION_REQUESTED`.
- Dispute has required escalation reason/evidence.
- Staff routing selects a suitable Staff member without Admin participation.

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

The system must rank suitable candidates and route the dispute directly to an
eligible Staff member without Admin assignment. Routing may be fully automatic or
manual inside a Staff operations queue, but the public milestone-dispute flow must
not require Admin to assign Staff.

Admin cannot select, replace, or cancel Staff assignment for milestone disputes.
Staff replacement, if needed for availability, is an internal Staff-operations
action and must not change the dispute decision authority.

System behavior:

1. Set `assigned_staff_id` from the automatic/manual Staff routing workflow.
2. Set dispute status `STAFF_REVIEWING`.
3. Set `staff_review_started_at = now()`.
4. Set `evidence_collection_due_at = now() + 48 hours` for display and evidence
   guidance only.
5. Grant temporary `READ_EXECUTE` case access to assigned Staff.
6. Set `staff_access_expires_at = evidence_collection_due_at + 3 days`.
7. Set `staff_sla_due_at = evidence_collection_due_at + 3 days`.
8. Notify Staff, Business, and Expert of the evidence deadline and assigned
   reviewer.
9. Write audit log.

During the 48-hour evidence window:

- Business and Expert may add case attachments.
- Assigned Staff may inspect case materials.
- Staff may issue the official decision earlier when the available evidence is
  sufficient. The system must not throw a hard `EVIDENCE_WINDOW_STILL_OPEN`
  error.

After the evidence window:

- Assigned Staff has until `staff_sla_due_at` to issue the final decision.
- Missing the SLA sets `staff_sla_escalated_at` once and notifies Admin.
- SLA escalation may cause reassignment before a decision, but does not permit
  Admin to decide, revise, or change payout percentage.
- Temporary access expires when the case resolves, Staff is replaced, or
  `staff_access_expires_at` is reached.

Admin does not decide the professional outcome.

### 10.8 Staff Intervention Cannot Be Rejected

Once a dispute has been escalated and routed to Staff, Staff must resolve it with
a mandatory decision. There is no Staff rejection/intervention-refusal step in
v2.3.

Rules:

- Do not expose `reject-intervention` as a public milestone-dispute API.
- Do not create `INTERVENTION_REJECTED` for new disputes.
- Do not return an escalated dispute to `PENDING_SELF_RESOLVE` through a Staff
  action.
- If evidence is weak, Staff still records that assessment in the Staff report
  and chooses the appropriate payout percentage.

### 10.9 Staff Issues Mandatory Decision

Actor: Staff

Preconditions:

- Dispute status is `STAFF_REVIEWING`.
- Staff is assigned to dispute.
- Milestone status is `DISPUTED`.
- Milestone escrow is not released.
- Staff may decide before `evidence_collection_due_at` when the case evidence is
  sufficient; the timestamp is informational/SLA guidance, not a hard blocker.

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

### 10.11 Withdraw Dispute Before Staff Routing

Actor: Dispute initiator

Rules:

- The dispute initiator may withdraw the dispute before it is routed to Staff.
- The non-initiating party cannot unilaterally cancel the dispute.
- Admin cannot cancel milestone disputes.
- No party can cancel after `STAFF_REVIEWING`, `STAFF_DECIDED`, or `RESOLVED`.

Preconditions for initiator withdrawal:

- Current user is `disputes.initiated_by_account_id`.
- Dispute status is `PENDING_SELF_RESOLVE` or `ESCALATION_REQUESTED`.
- Staff has not been assigned and review has not started.

System behavior:

1. Set dispute status `CANCELLED`.
2. Set `cancelled_by_account_id`.
3. Set `cancelled_at`.
4. Set `resolution_type = CANCELLED_BY_INITIATOR`.
5. Restore milestone status to `previous_milestone_status`.
6. Write audit log.
7. Notify both parties.

If Business wants to accept the deliverable after self-resolve, Business should use approve milestone, not cancel dispute.

---

## 11. Flow 4b â€” Contract Termination

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

This is the evidence/reason-based termination path. If the counterparty
disagrees, the disagreement becomes a Staff-reviewed termination case. It is
separate from section 11A immediate termination and does not charge the fixed
10% penalty.

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

#### Case A â€” Current Milestone `PENDING`

- Business has not deposited milestone escrow.
- Expert should not have started official work.
- No payout required unless there is separately accepted evidence and Staff decides otherwise.
- Current milestone becomes `CANCELLED`.
- Future milestones become `CANCELLED`.

#### Case B â€” Current Milestone `DEPOSITED`

- Business deposited escrow.
- Expert has not officially started.
- Expert may submit partial evidence only if Staff asks or termination reason requires review.
- If no valid evidence, Expert payout = 0%, Business refund = 100% escrow.
- Milestone may become `CANCELLED` after full escrow refund if no payout.
- If Staff grants payout, settlement executes and milestone becomes `COMPLETED`.

#### Case C â€” Current Milestone `IN_PROGRESS` Or `OVERDUE`

- Expert may submit partial evidence.
- Staff evaluates partial work.
- Staff decides payout percentage.
- System splits escrow.
- Milestone becomes `COMPLETED` after settlement.

#### Case D â€” Current Milestone `UNDER_REVIEW`

- Expert submitted deliverable.
- Staff evaluates deliverable against acceptance criteria.
- Staff decides payout percentage.
- System splits escrow.
- Milestone becomes `COMPLETED` after settlement.

#### Case E â€” Current Milestone `DISPUTED`

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
20. Trigger the system participant-deposit refund flow.

If expert payout = 0 and full escrow is refunded to Business, current milestone may be `CANCELLED` instead of `COMPLETED` if Staff reports no accepted work.

The Staff report must state whether current milestone is closed as `COMPLETED` or `CANCELLED`.

### 11.8 System Resolves Participant Deposits After Standard Termination

Actor: System with Admin-visible audit/retry

Preconditions:

- Contract status is `TERMINATED`.
- Termination request status is `AWAITING_DEPOSIT_REFUND`.
- Required milestone settlement is done or not required.
- Business and Expert deposits are still held and unresolved.

System behavior:

1. Lock both participant deposit rows.
2. Lock Business and Expert wallets.
3. Refund the full remaining Business held deposit to Business.
4. Refund the full remaining Expert held deposit to Expert.
5. Write wallet ledger entries for both refunds.
6. Set both deposit statuses `REFUNDED` and resolution timestamps.
7. Set termination request status `COMPLETED`.
8. Set contract status `CLOSED`.
9. Open review capability.
10. Write audit log.
11. Notify Business and Expert.

The fixed 10% penalty applies only to immediate termination under section 11A,
not standard Staff-reviewed termination.

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

## 11A. Immediate Termination With Fixed 10% Penalty

Immediate termination is a guarded alternative to Staff-reviewed standard
termination. It does not decide disputed work and does not require Staff.

### 11A.1 Common Preconditions

- Caller is Business or Expert attached to the contract.
- Contract status is `ACTIVE`.
- No active termination request exists.
- No milestone is `UNDER_REVIEW` or `DISPUTED`.
- All unreleased milestone escrow can be identified and refunded atomically.
- Caller explicitly confirms immediate termination and provides a reason.
- Both participant contract deposits are held and unresolved.

### 11A.2 Business Eligibility

Business may terminate immediately whenever the common preconditions are
satisfied and Business confirms the 10% penalty. Final-deliverable rejection
history does not block this paid exit. An overdue on-demand progress-report
request may be stored as the reason, but is not a prerequisite.

### 11A.3 Expert Eligibility

Expert may terminate immediately when the common preconditions are satisfied.
The Expert's held 10% contract deposit funds the penalty to Business. The
service must not charge Expert's available wallet as a substitute for missing
deposit; missing deposit means the contract should never have become `ACTIVE`.

### 11A.4 Financial And State Behavior

1. Start one transaction and lock contract, affected milestones, escrow balances,
   and both participant deposits.
2. Refund every unreleased unfinished milestone escrow amount to Business.
3. Preserve completed milestones.
4. Mark all other unfinished milestones `CANCELLED`.
5. Calculate `penaltyAmount = contract.totalBudget * 10%`.
6. If Business initiated:
   - debit `penaltyAmount` from Business's held 20% deposit;
   - credit Expert available balance by `penaltyAmount`;
   - refund the remaining held Business deposit to Business;
   - refund Expert's full held deposit to Expert.
7. If Expert initiated:
   - debit the full Expert held deposit, which must equal `penaltyAmount`;
   - credit Business available balance by `penaltyAmount`;
   - refund Business's full held deposit to Business.
8. Mark both deposits resolved with penalty/refund transaction references.
9. Set contract status `TERMINATED`, then `CLOSED` when every required deposit
   and escrow movement is committed. If project policy requires an operational
   closure checkpoint, remain `TERMINATED`/`AWAITING_DEPOSIT_REFUND` but keep the
   deposit resolution system-executable and idempotent.
10. Write ledger, audit, and participant notifications.

If work quality, rejection, payout, or evidence is contested, immediate
termination must reject and the parties must use standard termination or dispute.

The 10% is based on total contract value, not current milestone value, remaining
contract value, or the 20% Business-deposit percentage.

---

## 12. Cross-Review Flow

### 12.1 When Reviews Open

Reviews open only when:

```text
contract.status = CLOSED
```

This means:

- All milestone or termination settlement is complete.
- Remaining Business and Expert contract deposits are resolved/refunded.
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
   version. At the time of v2.2 planning this is expected to be after the
   existing Flow 4â€“5 migrations; do not assume or reuse `V45`.
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

Contract activation must additionally validate both required participant
deposits.

#### 13.3.1A `contract_deposits`

The deposit model must support one deposit per participant role:

```sql
ALTER TABLE contract_deposits
ADD COLUMN owner_account_id BIGINT NULL REFERENCES account(account_id),
ADD COLUMN owner_role VARCHAR(20) NULL,
ADD COLUMN required_percentage DECIMAL(5,2) NULL,
ADD COLUMN required_amount DECIMAL(19,2) NULL,
ADD COLUMN penalty_amount DECIMAL(19,2) NULL DEFAULT 0,
ADD COLUMN penalty_beneficiary_account_id BIGINT NULL REFERENCES account(account_id),
ADD COLUMN penalty_transaction_id BIGINT NULL REFERENCES wallet_transactions(id),
ADD COLUMN resolution_type VARCHAR(50) NULL,
ADD COLUMN resolved_at TIMESTAMP NULL;
```

Required role values:

```text
BUSINESS
EXPERT
```

Required percentages:

```text
BUSINESS = 20.00
EXPERT = 10.00
```

Required uniqueness:

```sql
CREATE UNIQUE INDEX uq_contract_deposits_contract_role
ON contract_deposits(contract_id, owner_role);
```

Backfill existing deposit rows as `BUSINESS`. A new Expert deposit row must be
created and funded before an existing non-active contract can activate. Active
legacy-contract handling requires an explicit compatibility migration; do not
fabricate an Expert payment.

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

Set when Business deposits milestone escrow (`9.1`). `startMilestone` is a
compatibility endpoint and must not reset it. Backfill existing `IN_PROGRESS`
rows from `updated_at` since no earlier signal exists.

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

`initiation_type` is distinct from `initiated_by`: role identifies who opened
the dispute, while type identifies the explicit disagreement. A persisted
Business rejection may support `BUSINESS_REJECTED_DELIVERABLE`, but section 9.5
never sets this field because rejection does not create a dispute.

Add the remaining fields â€” each is set by a concrete system-behavior step (9.5,
10.2, 10.6, 10.7, 10.9, 10.10, 11.x) that current code cannot fully execute
without them:

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
Open                  -> PENDING_SELF_RESOLVE
UnderReview           -> STAFF_REVIEWING
Escalated             -> ESCALATION_REQUESTED
Resolved              -> RESOLVED
Rejected              -> CANCELLED with migration comment
INTERVENTION_REJECTED -> CANCELLED with migration comment for legacy rows
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

Uniqueness (one review per reviewer per contract, see 12.2) is enforced at service level for MVP, not by a DB constraint. `AdminService.createReview` already guards this via `existsByContractIdAndReviewerId(contractId, reviewerId)`. Since a contract has exactly one Business and one Expert, checking `(contract_id, reviewer_id)` is equivalent to checking `(contract_id, reviewer_id, reviewee_id)` â€” no separate `reviewee_id` check is needed.

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
status VARCHAR(50) NULL,
rejection_feedback TEXT NULL,
rejected_at TIMESTAMP NULL
```

Recommended status values:

```text
SUBMITTED
APPROVED
REJECTED
SUPERSEDED
```

If not implemented, service must derive latest deliverable and submission round
by `created_at`. Rejection history must not depend on dispute rows.

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
    acknowledgement_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_BUSINESS_ACK',
    acknowledged_by_account_id INT NULL REFERENCES account(account_id),
    acknowledged_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_progress_report_ack_status
      CHECK (acknowledgement_status IN ('PENDING_BUSINESS_ACK', 'ACKNOWLEDGED')),
    CONSTRAINT chk_progress_report_checkpoint_type CHECK (checkpoint_type IN ('MIDPOINT', 'PRE_DEADLINE') OR checkpoint_type IS NULL),
    CONSTRAINT chk_progress_report_percent CHECK (percent_complete IS NULL OR (percent_complete BETWEEN 0 AND 100))
);

CREATE INDEX idx_milestone_progress_reports_milestone ON milestone_progress_reports(milestone_id, created_at);
```

Not linked to `disputes` or `case_attachments` â€” kept as an independent
progress-work-cycle record per `9.2A`â€“`9.2C`.

The report remains independent of disputes and case attachments, but may link to
the on-demand request it satisfies. Business acknowledgement is a flow-control
gate. Business feedback can be stored on the report and can acknowledge a
pending report, but it is not rejection, revision, dispute evidence, or money
movement by itself.

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

`deposit_refund_required` means participant-deposit resolution is still
required. Implementations may rename it to
`participant_deposit_resolution_required` in a new additive migration if this
does not break compatibility.

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
EXPERT_CONTRACT_DEPOSIT_HOLD
EXPERT_CONTRACT_DEPOSIT_REFUND
IMMEDIATE_TERMINATION_PENALTY
IMMEDIATE_TERMINATION_COMPENSATION
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

Immediate-termination metadata must include:

```json
{
  "settlementSourceType": "IMMEDIATE_TERMINATION",
  "initiatingRole": "BUSINESS",
  "penaltyPercentage": 10,
  "contractTotalBudget": 100000000,
  "penaltyAmount": 10000000,
  "penaltyBeneficiaryAccountId": 9
}
```

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
fundBusinessContractDeposit(contractId, businessAccountId)
fundExpertContractDeposit(contractId, expertAccountId)
activateContractWhenBothDepositsHeld(contractId)
startMilestone(milestoneId, expertAccountId) // compatibility for legacy DEPOSITED rows or idempotent IN_PROGRESS retry
markMilestoneOverdue(contractId, milestoneId)
requestProgressReport(contractId, milestoneId, businessAccountId)
submitProgressReport(contractId, milestoneId, expertAccountId, request)
acknowledgeProgressReport(contractId, milestoneId, progressReportId, businessAccountId)
autoApproveExpiredReviewSla(contractId, milestoneId)
submitDeliverable(milestoneId, expertAccountId, request)
approveMilestone(milestoneId, businessAccountId)
rejectMilestone(milestoneId, businessAccountId, request)
cancelDraftContract(contractId, businessAccountId, request)
```

`rejectMilestone` must handle both first rejection and rejection after re-submit.
It must update deliverable/milestone rejection history and return the milestone
to `IN_PROGRESS`; it must not create a dispute.

### 15.2 DisputeService

Required methods or equivalent:

```text
createBusinessInitiatedDispute(milestoneId, businessAccountId, request)
createExpertInitiatedDispute(milestoneId, expertAccountId, request)
requestStaffIntervention(disputeId, accountId, request)
routeStaff(disputeId)
listStaffAssignmentCandidates(disputeId)
issueStaffDecision(disputeId, staffAccountId, request)
executeDisputeSettlement(disputeId)
cancelDispute(disputeId, accountId, request)
resolveByBusinessApproval(disputeId, milestoneId)
```

`routeStaff` assigns Staff without Admin participation. `issueStaffDecision`
stores the binding percentage and triggers `executeDisputeSettlement`. No Admin
final-decision or Staff intervention-rejection method is permitted.

Business-rejection history may be attached as evidence, but no dispute service
method is called automatically from `rejectMilestone`.

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

`immediateTerminate` must consume the initiator's held deposit, credit the
counterparty, refund remaining held deposits as defined in 11A, and never ask
Staff to approve the fixed 10% result.

### 15.4 WalletLedgerService

Required operations:

```text
moveAvailableToEscrow(walletId, amount, reference)
releaseEscrowToExpert(businessWalletId, expertWalletId, amount, reference)
splitEscrow(businessWalletId, expertWalletId, escrowAmount, expertPayoutAmount, businessRefundAmount, reference)
refundParticipantDeposits(contractId, adminAccountId, reference)
autoRefundParticipantDeposits(contractId, reference)
holdExpertContractDeposit(contractId, expertAccountId, amount)
settleImmediateTerminationPenalty(contractId, initiatingRole, penaltyAmount)
```

`splitEscrow` must be shared by dispute settlement and termination settlement.

### 15.5 AuditLogService

Must log:

```text
MILESTONE_ESCROW_DEPOSITED
BUSINESS_CONTRACT_DEPOSIT_HELD
EXPERT_CONTRACT_DEPOSIT_HELD
CONTRACT_ACTIVATED_AFTER_DUAL_DEPOSIT
MILESTONE_STARTED
PROGRESS_REPORT_SUBMITTED
PROGRESS_REPORT_REQUESTED
PROGRESS_REPORT_ACKNOWLEDGED
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
DISPUTE_STAFF_DECIDED
DISPUTE_SETTLEMENT_EXECUTED
DISPUTE_CANCELLED
TERMINATION_REQUESTED
TERMINATION_ACCEPTED_BY_EXPERT
TERMINATION_DISPUTED_BY_EXPERT
TERMINATION_RESPONSE_EXPIRED
CONTRACT_IMMEDIATE_TERMINATED
IMMEDIATE_TERMINATION_PENALTY_SETTLED
TERMINATION_STAFF_ASSIGNED
TERMINATION_STAFF_REJECTED
TERMINATION_STAFF_APPROVED
TERMINATION_SETTLEMENT_EXECUTED
CONTRACT_DEPOSIT_REFUNDED
PARTICIPANT_DEPOSITS_REFUNDED
CONTRACT_DRAFT_CANCELLED_BY_BUSINESS
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
  "ProgressReportAcknowledgement": {},
  "ImmediateTerminationRequest": {
    "reason": "Required progress report was not submitted by the deadline.",
    "confirmedPenalty": true
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
PROGRESS_REPORT_ACK_PENDING
PROGRESS_REPORT_REQUEST_NOT_FOUND
PROGRESS_REPORT_ACK_NOT_ALLOWED
DISPUTE_ALREADY_ACTIVE
DISPUTE_NOT_STAFF_DECIDED
ESCROW_ALREADY_RELEASED
IMMEDIATE_TERMINATION_NOT_ALLOWED
TERMINATION_REQUEST_ALREADY_ACTIVE
BUSINESS_CONTRACT_DEPOSIT_NOT_HELD
EXPERT_CONTRACT_DEPOSIT_NOT_HELD
CONTRACT_DEPOSIT_ALREADY_RESOLVED
```

### 16.1 Contract Deposit APIs

```http
POST /api/v1/contracts/{contractId}/deposit/pay
POST /api/v1/contracts/{contractId}/expert-deposit/pay
POST /api/v1/contracts/{contractId}/cancel-draft
POST /api/v1/admin/contracts/{contractId}/deposits/refund
```

- Business endpoint funds 20% of total contract value.
- Expert endpoint funds 10% of total contract value.
- Business may cancel its own draft contract before signatures/funding activate
  the contract.
- Each endpoint is idempotent for an already-held correct amount.
- Contract activation occurs only after signatures and both deposits are held.
- Participant deposit refund is a system operation after normal completion or
  standard termination; the Admin endpoint is only an idempotent operational
  retry/audit tool and must not be the required happy path. Immediate
  termination normally resolves deposits inside its own atomic transaction.

### 16.2 Milestone APIs

```http
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit
POST /api/v1/milestones/{milestoneId}/start
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-report-request
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports
GET  /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports
POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/acknowledge
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

### 16.3 Dispute APIs

```http
POST /api/v1/disputes/{disputeId}/escalation-request
GET  /api/v1/disputes/{disputeId}/staff-candidates
POST /api/v1/disputes/{disputeId}/route-staff
POST /api/v1/disputes/{disputeId}/staff-decision
POST /api/v1/disputes/{disputeId}/execute-settlement
POST /api/v1/disputes/{disputeId}/cancel
GET  /api/v1/contracts/{contractId}/disputes
GET  /api/v1/disputes/{disputeId}
```

`route-staff` is a system/Staff-ops trigger and must not require Admin.
`staff-decision` stores the binding decision and triggers settlement.
`execute-settlement` is an internal/Admin-authorized retry endpoint only. It must
reuse the stored Staff percentage exactly. There is no
`/api/v1/disputes/{disputeId}/admin-final-decision`,
`/api/v1/disputes/{disputeId}/assign-staff`, or
`/api/v1/disputes/{disputeId}/reject-intervention` endpoint.

### 16.4 Termination APIs

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

### 16.5 Case Attachment APIs

```http
POST /api/v1/case-attachments
GET  /api/v1/case-attachments?ownerType=...&ownerId=...
```

Upload authorization follows owner type and case participation. Assigned Staff
has access only while temporary case access is valid.

### 16.6 Review APIs

```http
POST /api/v1/contracts/{contractId}/reviews
GET  /api/v1/contracts/{contractId}/reviews
```

---

## 17. Authorization Matrix

| Action | Business | Expert | Admin | Staff | System |
|---|---:|---:|---:|---:|---:|
| Fund Business 20% contract deposit | Contract Business | No | No | No | No |
| Fund Expert 10% contract deposit | No | Contract Expert | No | No | No |
| Cancel own draft contract | Contract Business while DRAFT | No | No | No | No |
| Activate contract after both deposits | No | No | No | No | Yes |
| Deposit milestone escrow | Yes | No | No | No | No |
| Start milestone | No | Yes | No | No | No |
| Request on-demand progress report | Contract Business | No | No | No | No |
| Submit progress report | No | Contract Expert | No | No | No |
| Acknowledge progress report | Contract Business | No | No | No | No |
| Mark overdue / review-SLA auto-approve | No | No | Operational trigger | No | Yes |
| Submit deliverable | No | Yes | No | No | No |
| Approve milestone | Yes | No | No | No | No |
| Reject milestone | Yes | No | No | No | No |
| Expert initiate dispute | No | Yes | No | No | No |
| Request Staff intervention | Yes | Yes | No | No | No |
| List Staff candidates | No | No | No | Staff ops | System helper |
| Route Staff to dispute | No | No | No | Staff ops | Yes |
| Issue Staff dispute decision | No | No | No | Assigned Staff | No |
| Execute dispute settlement | No | No | Retry stored decision only | No | Yes |
| Withdraw own dispute before Staff routing | Initiator only | Initiator only | No | No | No |
| Request termination | Yes | Yes | No | No | No |
| Accept/dispute Business termination request | No | Contract Expert | No | No | Timeout only |
| Immediate termination | Eligible Contract Business | Eligible Contract Expert | No | No | No |
| Apply immediate-termination 10% deposit penalty | Initiator through immediate-termination command | Initiator through immediate-termination command | No override | No | Atomic helper |
| Assign Staff to termination | No | No | Yes | No | No |
| Approve/reject termination | No | No | No | Assigned Staff | No |
| Execute termination settlement | No | No | Retry only | No | Yes |
| Withdraw own termination request before Staff decision | Requester only | Requester only | Yes for invalid/duplicate | No | No |
| Refund participant deposits | No | No | Retry/audit only | No | Yes |
| Create review after CLOSED | Yes | Yes | No | No | No |

---

## 18. Critical Invariants

1. A milestone cannot start before Business deposits milestone escrow.
2. A milestone escrow can be released only once.
3. `milestones.escrow_released_at` is the primary settlement idempotency guard.
4. Dispute settlement and termination settlement cannot execute for the same milestone escrow.
5. Each milestone can have at most one active dispute.
6. Each contract can have at most one active termination request.
7. Staff decision is mandatory after dispute intervention is routed to Staff.
8. Staff routing for disputes never requires Admin assignment or Admin cancellation.
9. Rejecting a deliverable never creates a dispute automatically.
10. Rejection returns the milestone to `IN_PROGRESS`; normal resubmission does
    not require dispute state or a special request.
11. Both participant deposits must be resolved before contract becomes `CLOSED`.
12. Reviews open only when contract is `CLOSED`.
13. No destructive data deletion is allowed by default in migrations.
14. New v2 wallet movements must use `wallet_transactions`, not legacy `transactions`.
15. Business must hold 20% and Expert must hold 10% of total contract value
    before contract activation.
16. Every public route defined by this specification starts with `/api/v1`.
17. Admin never approves, revises, or changes an assigned Staff dispute
    decision or payout percentage.
18. Immediate termination charges exactly 10% of total contract value to the
    initiator for the counterparty.
19. At most one on-demand progress-report request may be pending per contract
    milestone.
20. Missing a scheduled or on-demand report never moves money automatically;
    immediate termination always requires an explicit confirmed command.
21. An expired report request never moves money or adjudicates work quality.
22. Staff decision cannot be blocked solely because the 48-hour evidence window
    has not ended; the evidence timestamp is guidance/SLA metadata.
23. Settlement retry must use the stored Staff percentage unchanged.
24. Standard termination does not apply the fixed immediate-termination penalty.
25. Immediate termination penalty is funded from the initiator's held contract
    deposit, not milestone escrow.
26. The platform never keeps the immediate-termination penalty.
27. A deliverable rejection and every later resubmission remain historically
    auditable.
28. A progress report blocks the next progress report until Business
    acknowledgement is recorded.
29. Progress-report feedback may acknowledge the report and store Business
    comments, but must not create report revision or dispute semantics.
30. WebSocket notifications for dispute intervention expose notification
    `type`; clients must not depend on workflow `status` as notification type.
31. Participant deposits are automatically refunded by the system when closure
    preconditions are met, with Admin retry/audit only.
32. Business can cancel its own draft contract before activation.

---

## 19. Test Scenarios

### 19.1 Happy Path Milestone Completion

Given contract is `ACTIVE` and milestone is `PENDING`  
When Business deposits milestone escrow  
Then milestone becomes `IN_PROGRESS`
And milestone execution timeline starts immediately

When Expert retries start milestone for compatibility
Then milestone remains `IN_PROGRESS`

When Expert submits deliverable  
Then milestone becomes `UNDER_REVIEW`

When Business approves  
Then escrow is released 100% to Expert  
And milestone becomes `COMPLETED`  
And `escrow_released_at` is set

### 19.2 Business Rejects, Expert Re-submits, Business Approves

Given milestone is `UNDER_REVIEW`
When Business rejects deliverable
Then no dispute is created
And current deliverable becomes `REJECTED`
And rejection feedback is stored
And milestone becomes `IN_PROGRESS`

When Expert re-submits
Then milestone becomes `UNDER_REVIEW`
And submission round increases

When Business approves
Then escrow releases 100% to Expert
And milestone becomes `COMPLETED`

### 19.3 Business Rejects Re-submission

Given Expert re-submitted after an earlier rejection
And milestone is `UNDER_REVIEW`
When Business rejects again
Then no new dispute is created
And the new deliverable rejection is stored independently
And milestone returns to `IN_PROGRESS`
And earlier rejection history remains readable

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

### 19.5 Staff Intervention Cannot Be Rejected

Given dispute status is `STAFF_REVIEWING`  
When assigned Staff reviews weak or incomplete evidence
Then Staff must still issue a decision or request evidence through the active
case flow
And no `INTERVENTION_REJECTED` status is created
And the dispute does not return to self-resolve

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

Given contract is `COMPLETED` or standard `TERMINATED`
And Business and Expert deposits are held
When closure preconditions are satisfied
Then the system refunds both deposits idempotently
And Admin retry/audit may observe the same resolved result
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

### 19.12 Overdue Request As Business Immediate-Termination Reason

Given an on-demand report request is expired without submission
And no milestone is `UNDER_REVIEW` or `DISPUTED`
When Business terminates immediately
Then unreleased unfinished milestone escrow is refunded to Business
And unfinished milestones become `CANCELLED`
And 10% of total contract value moves from Business's held deposit to Expert
And the remaining Business deposit is refunded
And Expert's held deposit is refunded

### 19.13 Staff Decision Cannot Be Overridden

Given assigned Staff issues a valid 70% Expert decision after evidence window
When settlement executes or retries
Then Expert receives 70% and Business receives the remainder
And Admin cannot change the percentage or request decision revision

### 19.14 Evidence Window And Staff SLA

Given dispute intervention is routed to Staff
Then evidence collection closes after 48 hours
And temporary Staff access and Staff decision SLA end three days later

When Staff attempts a final decision before evidence collection closes
Then the operation succeeds if Staff has sufficient evidence
And the system does not return `EVIDENCE_WINDOW_STILL_OPEN`

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

### 19.16 Contract Activation Requires Dual Deposits

Given all signatures are complete
And Business has funded 20% of total contract value
But Expert has not funded 10%
Then contract remains `PENDING`

When Expert funds the required 10%
Then the system may activate the contract exactly once

### 19.17 Expert Immediate Termination Penalty

Given contract total value is 100,000,000
And Expert's held deposit is 10,000,000
When Expert terminates immediately
Then 10,000,000 is credited to Business
And Business's held deposit is refunded to Business
And no Staff review is required

### 19.18 Business Immediate Termination Penalty

Given contract total value is 100,000,000
And Business's held deposit is 20,000,000
When Business terminates immediately
Then 10,000,000 is credited to Expert
And the remaining 10,000,000 Business deposit is refunded to Business
And Expert's held deposit is refunded to Expert
And no Staff review is required

### 19.19 Progress Report Acknowledgement Gate

Given Expert submitted a progress report
And Business has not acknowledged it
When Expert submits another progress report
Then the request is rejected with `PROGRESS_REPORT_ACK_PENDING`

When Business acknowledges the latest report
Then Expert can submit the next progress report when otherwise allowed

When Business submits feedback for the latest report
Then the feedback is stored
And the report is acknowledged if it was pending acknowledgement
And Expert can submit the next progress report when otherwise allowed
And no dispute, deliverable rejection, or report revision state is created

### 19.20 Business Cancels Draft Contract

Given a contract is `DRAFT`
And Business owns the contract
When Business cancels the draft
Then the contract becomes `CANCELLED`
And Expert approval is not required
And no funded escrow or participant deposit is moved

### 19.21 Dispute Notification Type

Given a participant requests Staff intervention
When the WebSocket notification is delivered
Then the notification payload contains notification `type`
And clients must not interpret dispute workflow `status` as notification type

---

## 20. Acceptance Criteria

### 20.1 Functional Acceptance

- Business can deposit current milestone escrow.
- Business milestone escrow deposit starts the milestone execution timeline
  immediately.
- Expert cannot start milestone before escrow deposit.
- Business can request progress reports with 24-hour first and 12-hour later
  request SLAs without creating duplicate pending requests.
- Expert submissions close pending report requests and preserve late status.
- Expert cannot submit another progress report until Business acknowledges the
  latest progress report.
- Business can acknowledge progress reports without feedback, or submit
  progress-report feedback that also acknowledges the report without creating
  revision or dispute semantics.
- `OVERDUE` milestones continue to accept reports and deliverables.
- Business approval releases 100% escrow to Expert.
- Business rejection stores feedback, marks the deliverable rejected, and
  returns milestone to `IN_PROGRESS` without creating dispute.
- Expert resubmits through the normal deliverable API without a dispute or
  special request.
- Dispute is created only by an explicit participant action.
- Expert can initiate dispute in allowed states.
- Only one active dispute exists per milestone.
- Either party can request Staff intervention at any time during active dispute.
- Dispute intervention is routed to Staff without Admin assignment.
- Staff cannot reject intervention or return an escalated dispute to
  self-resolve.
- Staff can issue mandatory decision.
- Staff decision is not blocked solely by the 48-hour evidence window.
- Admin cannot override or revise Staff's decision.
- Admin cannot cancel milestone disputes after Staff routing.
- System executes dispute settlement and splits escrow correctly.
- WebSocket dispute intervention notifications expose notification `type`, not
  workflow `status`, as the notification classification.
- Staff decision/settlement responses expose deliverable `submission_round` so
  FE can display resubmission count.
- Business and Expert can request termination.
- Business termination requests support Expert accept/dispute/three-day timeout.
- Eligible participants can terminate immediately without Staff and pay exactly
  10% of total contract value to the counterparty.
- Contract cannot activate until Business 20% and Expert 10% deposits are held.
- Termination settlement cannot double-settle active dispute milestone.
- System automatically refunds both participant deposits after
  completion/standard termination; Admin retry is audit/ops only.
- Contract becomes `CLOSED` only after both deposits are resolved.
- Business can cancel its own draft contract before activation.
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
- `contract_deposits` supports unique `BUSINESS` and `EXPERT` owner roles.
- Deliverables persist rejection feedback, rejection time, status, and
  submission round without requiring dispute rows.

### 20.3 Financial Acceptance

- `expertPayoutAmount + businessRefundAmount = milestoneEscrowAmount` always.
- No milestone escrow can be released twice.
- Ledger entries are posted for every wallet balance movement.
- New v2 ledger rows include contract and milestone references.
- Both participant deposit funding and refunds are recorded in wallet ledger and
  deposit rows.
- Immediate termination penalty equals exactly 10% of total contract value.
- Business immediate termination penalty comes from Business's held 20% deposit.
- Expert immediate termination penalty comes from Expert's held 10% deposit.
- The penalty beneficiary is the counterparty, never the platform.
- Penalty plus refunds equal the resolved held deposit amounts.
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
   - Implement Business 20% and Expert 10% contract deposit holds.
   - Gate activation on both deposits.
   - Implement milestone escrow deposit.
   - Implement split escrow.
   - Implement immediate-termination penalty and counterparty compensation.
   - Implement idempotency guard.
   - Implement ledger metadata.

4. Milestone Agent:
   - Implement milestone state transitions.
   - Implement `OVERDUE` detection and review-SLA auto-approval guards.
   - Implement progress-report request history, 24h/12h SLA, submissions, and
     Business acknowledgement gate.
   - Implement submit/re-submit.
   - Implement approve/reject logic.

5. Dispute Agent:
   - Implement dispute initiation, escalation, automatic/manual Staff routing
     without Admin, candidate ranking, evidence window metadata, Staff
     access/SLA, final Staff decision, settlement execution, and withdraw rules.

6. Termination Agent:
   - Implement termination request response lifecycle, settlement, and guarded
     immediate termination with fixed 10% counterparty compensation.

7. Contract Closure Agent:
   - Implement automatic deposit refund closure and review opening.

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
- Do not omit or change the fixed 10% immediate-termination penalty.
- Do not calculate the penalty from milestone value or remaining project value;
  use total contract value.
- Do not charge available balance when the initiator's required held deposit
  exists.
- Do not pay the penalty to the platform; credit the counterparty.
- Do not expose a new public Flow 4â€“5 route without the `/api/v1` prefix.
- Do not block Staff decision solely because the 48-hour evidence window is
  still open.
- Do expose Business progress-report feedback for tracking responses; keep
  acknowledgement as the gate and do not turn feedback into dispute/revision
  semantics.
- Do not allow Expert to submit a new progress report while the latest report is
  waiting for Business acknowledgement.
- Do not require Admin assignment for milestone dispute Staff routing.
- Do not allow Staff to reject dispute intervention.
- Do not allow Admin to cancel milestone disputes.
- Do not leave participant deposit refunds as a manual-only Admin operation.
- Do not return dispute workflow `status` as notification type over WebSocket.
- Do expose deliverable submission round for FE resubmission counts.
- Do allow Business to cancel its own draft contract before activation.
- Do not allow multiple pending on-demand report requests for one contract
  milestone.
- Do not move money or terminate automatically from any missed report deadline;
  require an explicit immediate-termination command and penalty confirmation.
- Do not auto-create dispute or block normal resubmission based on rejection
  count. Dispute requires an explicit participant action.
- Do not allow Expert to start work before milestone escrow deposit.
- Do not activate a contract before Business 20% and Expert 10% deposits are
  both held.
- Do not allow milestone escrow release without checking `escrow_released_at`.
- Do not allow termination settlement to run while active dispute settlement is unresolved.
- Do not leave future milestones as `PENDING` after contract termination.
- Do not open reviews before contract `CLOSED`.
- Do not delete existing data unless project owner explicitly approves a dev/test reset.

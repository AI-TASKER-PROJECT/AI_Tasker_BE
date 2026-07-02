# SPEC-MILESTONE-DISPUTER.md — v2

**Project:** AITASKER-BE  
**Platform:** AITASKER — AI expert and business matching platform  
**Spec purpose:** Implementation guide for harness engineering agents  
**Version:** v2  
**Prepared date:** 2026-07-01  
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

---

## 1. Source Context And Agent Reading Order

Before implementation, agents must inspect the actual project source code and current database migrations. Do not implement from this spec alone without validating existing code.

Required reading order:

1. `AGENT.md` or equivalent harness instructions in the repository.
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

- The current schema is managed by Flyway migrations `V1` through `V44`.
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
- Deliverable submission.
- Business milestone approval.
- Business milestone rejection.
- Expert re-submit during self-resolve.
- Business reject again during self-resolve.
- Business approval after self-resolve.
- Business or Expert dispute initiation.
- Dispute escalation request with file/evidence.
- Admin Staff assignment based on job domain and required skills.
- Staff review.
- Staff intervention rejection.
- Staff mandatory decision.
- System settlement execution after Staff decision.
- Contract termination request by Business or Expert.
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
- Automatic deadline escalation based on timeouts unless already implemented in the codebase.
- Multi-Staff voting or Staff committee review.

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
- Request Staff intervention for an active dispute.
- Request contract termination.
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
- Cancel invalid or duplicate dispute records before settlement.
- Cancel invalid or duplicate termination requests before settlement.
- Execute or approve manual contract deposit refund, depending on existing wallet service design.
- Manage system categories, role access, and operational oversight.

Admin must not:

- Override Staff professional decision about deliverable quality or payout percentage.
- Execute duplicate milestone escrow settlement.
- Use legacy `transactions` for new wallet settlement.

### 3.4 Staff

Staff is the professional reviewer assigned to a dispute or termination case.

Staff can:

- Review dispute files, deliverables, SoW, milestone criteria, timeline, and evidence.
- Reject intervention and return dispute to self-resolve with reason.
- Accept intervention and issue a mandatory decision.
- Decide Expert payout percentage from 0% to 100%.
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
```

Rules:

- `ACTIVE -> TERMINATION_PENDING` occurs when Business or Expert creates a valid termination request.
- `TERMINATION_PENDING -> ACTIVE` occurs if Staff rejects the termination request.
- `TERMINATION_PENDING -> TERMINATED` occurs after Staff approves termination and required current milestone settlement is executed.
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
| `IN_PROGRESS` | Expert is working on the milestone. |
| `UNDER_REVIEW` | Expert submitted deliverable; Business is reviewing. |
| `DISPUTED` | There is an active dispute on the milestone. Contract is blocked. |
| `COMPLETED` | Milestone is closed and escrow settlement is done. |
| `CANCELLED` | Milestone is closed because contract was terminated before it could be completed. |

### 6.3 Allowed Transitions

```text
PENDING -> DEPOSITED
DEPOSITED -> IN_PROGRESS
IN_PROGRESS -> UNDER_REVIEW
UNDER_REVIEW -> COMPLETED
UNDER_REVIEW -> DISPUTED
IN_PROGRESS -> DISPUTED
DISPUTED -> UNDER_REVIEW
DISPUTED -> COMPLETED
DISPUTED -> CANCELLED only if dispute is cancelled and contract termination closes milestone without settlement, but prefer previous state rollback before Staff review
PENDING -> CANCELLED due to contract termination
DEPOSITED -> CANCELLED due to contract termination only if full escrow is refunded and no payout settlement is needed
IN_PROGRESS -> COMPLETED due to Staff termination settlement
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
UNDER_REVIEW -> IN_PROGRESS without reject/self-resolve process
COMPLETED -> any non-terminal status
CANCELLED -> any non-terminal status
```

### 6.5 Contract Termination Cleanup

When contract is terminated:

- Completed milestones remain `COMPLETED`.
- Current milestone with settlement becomes `COMPLETED` after settlement.
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

### 9.3 Submit Deliverable

Actor: Expert

Preconditions:

- Contract status is `ACTIVE`.
- Milestone status is `IN_PROGRESS`, or `DISPUTED` if re-submit during self-resolve.
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
3. Refund 20% contract deposit to Business available balance.
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
- Milestone status is `IN_PROGRESS` or `UNDER_REVIEW`.
- Milestone must not be `PENDING`, `DEPOSITED`, `COMPLETED`, or `CANCELLED`.
- Expert belongs to contract.
- No active dispute exists for milestone.
- No active termination request exists for contract.
- Expert provides reason and supporting evidence/file.

System behavior:

1. Create dispute.
2. Set `initiated_by_account_id = expertAccountId`.
3. Set `initiated_by_role = EXPERT`.
4. Set valid `initiation_type`.
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

System behavior:

1. Set `assigned_staff_id`.
2. Set dispute status `STAFF_REVIEWING`.
3. Set `staff_review_started_at` if Staff immediately starts or when Staff opens review.
4. Notify Staff.
5. Notify Business and Expert.
6. Write audit log.

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
2. Set status `REQUESTED`.
3. Set requested by account and role.
4. Set current milestone if determinable.
5. Set contract status `TERMINATION_PENDING`.
6. Notify Admin.
7. Write audit log.

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

#### Case C — Current Milestone `IN_PROGRESS`

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

If milestone is `DEPOSITED` or `IN_PROGRESS`, Expert may provide partial evidence.

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
3. Refund 20% contract security deposit to Business available balance.
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
- Staff has not issued decision.

System behavior:

1. Set termination request status `CANCELLED`.
2. Set cancellation fields.
3. Set contract status back to `ACTIVE`.
4. Write audit log.
5. Notify both parties and assigned Staff if any.

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
2. Create a new migration, for example `V45__milestone_dispute_termination_v2.sql` or the next available version.
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

Add status:

```text
CANCELLED
```

Add fields:

```sql
escrow_released_at TIMESTAMP NULL,
settlement_source_type VARCHAR(50) NULL,
settlement_source_id BIGINT NULL,
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

Add fields:

```sql
initiated_by_account_id BIGINT NULL,
initiated_by_role VARCHAR(20) NULL,
initiation_type VARCHAR(80) NULL,
previous_milestone_status VARCHAR(50) NULL,

escalation_requested_by_account_id BIGINT NULL,
escalation_requested_at TIMESTAMP NULL,
escalation_reason TEXT NULL,
escalation_file_url TEXT NULL,

staff_review_started_at TIMESTAMP NULL,
staff_decided_at TIMESTAMP NULL,
intervention_rejected_at TIMESTAMP NULL,
intervention_rejection_reason TEXT NULL,

staff_report TEXT NULL,
staff_decision_reason TEXT NULL,
staff_proposed_expert_percentage DECIMAL(5,2) NULL,
staff_proposed_expert_amount DECIMAL(19,2) NULL,
business_refund_amount DECIMAL(19,2) NULL,

settlement_executed_at TIMESTAMP NULL,
settlement_wallet_transaction_id BIGINT NULL,

resolution_type VARCHAR(80) NULL,
resolved_at TIMESTAMP NULL,
cancelled_at TIMESTAMP NULL,
cancelled_by_account_id BIGINT NULL,
cancellation_reason TEXT NULL
```

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

Add unique index:

```sql
CREATE UNIQUE INDEX uq_reviews_one_per_pair_per_contract
ON reviews(contract_id, reviewer_id, reviewee_id);
```

If duplicates exist, migration must not fail silently. Either clean duplicates in dev/test or defer unique index with clear note.

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
rejectIntervention(disputeId, staffAccountId, request)
issueStaffDecision(disputeId, staffAccountId, request)
executeDisputeSettlement(disputeId)
cancelDispute(disputeId, accountId, request)
resolveByBusinessApproval(disputeId, milestoneId)
```

### 15.3 TerminationRequestService

Required methods or equivalent:

```text
requestTermination(contractId, requesterAccountId, request)
assignStaff(terminationRequestId, adminAccountId, staffId)
rejectTermination(terminationRequestId, staffAccountId, request)
approveTermination(terminationRequestId, staffAccountId, request)
submitPartialEvidence(terminationRequestId, expertAccountId, request)
executeTerminationSettlement(terminationRequestId)
withdrawTerminationRequest(terminationRequestId, requesterAccountId, request)
refundDepositAfterTermination(terminationRequestId, adminAccountId, request)
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
DELIVERABLE_SUBMITTED
MILESTONE_APPROVED
MILESTONE_REJECTED
DISPUTE_CREATED
DISPUTE_ESCALATION_REQUESTED
DISPUTE_STAFF_ASSIGNED
DISPUTE_INTERVENTION_REJECTED
DISPUTE_STAFF_DECIDED
DISPUTE_SETTLEMENT_EXECUTED
DISPUTE_CANCELLED
TERMINATION_REQUESTED
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

## 16. API Surface Suggestions

Agents must adapt route style to existing controller conventions.

### 16.1 Milestone APIs

```http
POST /api/contracts/{contractId}/milestones/{milestoneId}/deposit
POST /api/milestones/{milestoneId}/start
POST /api/milestones/{milestoneId}/deliverables
POST /api/milestones/{milestoneId}/approve
POST /api/milestones/{milestoneId}/reject
POST /api/milestones/{milestoneId}/disputes
```

### 16.2 Dispute APIs

```http
POST /api/disputes/{disputeId}/escalation-request
POST /api/disputes/{disputeId}/assign-staff
POST /api/disputes/{disputeId}/reject-intervention
POST /api/disputes/{disputeId}/staff-decision
POST /api/disputes/{disputeId}/execute-settlement
POST /api/disputes/{disputeId}/cancel
GET  /api/contracts/{contractId}/disputes
GET  /api/disputes/{disputeId}
```

`execute-settlement` may be internal/system-only if settlement is auto-triggered after Staff decision.

### 16.3 Termination APIs

```http
POST /api/contracts/{contractId}/termination-requests
POST /api/termination-requests/{terminationRequestId}/assign-staff
POST /api/termination-requests/{terminationRequestId}/reject
POST /api/termination-requests/{terminationRequestId}/approve
POST /api/termination-requests/{terminationRequestId}/partial-evidence
POST /api/termination-requests/{terminationRequestId}/execute-settlement
POST /api/termination-requests/{terminationRequestId}/withdraw
POST /api/termination-requests/{terminationRequestId}/refund-deposit
GET  /api/contracts/{contractId}/termination-requests
GET  /api/termination-requests/{terminationRequestId}
```

### 16.4 Review APIs

```http
POST /api/contracts/{contractId}/reviews
GET  /api/contracts/{contractId}/reviews
```

---

## 17. Authorization Matrix

| Action | Business | Expert | Admin | Staff | System |
|---|---:|---:|---:|---:|---:|
| Deposit milestone escrow | Yes | No | No | No | No |
| Start milestone | No | Yes | No | No | No |
| Submit deliverable | No | Yes | No | No | No |
| Approve milestone | Yes | No | No | No | No |
| Reject milestone | Yes | No | No | No | No |
| Expert initiate dispute | No | Yes | No | No | No |
| Request Staff intervention | Yes | Yes | No | No | No |
| Assign Staff to dispute | No | No | Yes | No | No |
| Reject intervention | No | No | No | Assigned Staff | No |
| Issue Staff dispute decision | No | No | No | Assigned Staff | No |
| Execute dispute settlement | No | No | No | No | Yes |
| Cancel own dispute before Staff review | Initiator only | Initiator only | Yes for invalid/duplicate | No | No |
| Request termination | Yes | Yes | No | No | No |
| Assign Staff to termination | No | No | Yes | No | No |
| Approve/reject termination | No | No | No | Assigned Staff | No |
| Execute termination settlement | No | No | No | No | Yes |
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

---

## 20. Acceptance Criteria

### 20.1 Functional Acceptance

- Business can deposit current milestone escrow.
- Expert cannot start milestone before escrow deposit.
- Business approval releases 100% escrow to Expert.
- Business rejection creates or updates active dispute correctly.
- Expert can initiate dispute in allowed states.
- Only one active dispute exists per milestone.
- Either party can request Staff intervention at any time during active dispute.
- Staff can reject intervention and return dispute to self-resolve.
- Staff can issue mandatory decision.
- System executes dispute settlement and splits escrow correctly.
- Business and Expert can request termination.
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

### 20.3 Financial Acceptance

- `expertPayoutAmount + businessRefundAmount = milestoneEscrowAmount` always.
- No milestone escrow can be released twice.
- Ledger entries are posted for every wallet balance movement.
- New v2 ledger rows include contract and milestone references.
- Contract deposit refund is recorded in wallet ledger and contract deposit row.

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
   - Implement submit/re-submit.
   - Implement approve/reject logic.

5. Dispute Agent:
   - Implement dispute initiation, escalation, Staff decision, settlement execution, cancel rules.

6. Termination Agent:
   - Implement termination request lifecycle and settlement.

7. Contract Closure Agent:
   - Implement deposit refund closure and review opening.

8. Integration Agent:
   - Ensure contract/milestone/dispute/termination/wallet status changes are consistent.

9. Tester Agent:
   - Implement unit tests and integration tests for all scenarios.

10. Reviewer Agent:
   - Verify invariants, authorization, and no double-settlement.

---

## 22. Final Notes For Agents

- Do not simplify Staff decision into Business/Expert acceptance. Staff decision is mandatory.
- Do not require 3 re-submits before Staff escalation. Three re-submits are only the self-resolve maximum.
- Do not allow Expert to start work before milestone escrow deposit.
- Do not allow milestone escrow release without checking `escrow_released_at`.
- Do not allow termination settlement to run while active dispute settlement is unresolved.
- Do not leave future milestones as `PENDING` after contract termination.
- Do not open reviews before contract `CLOSED`.
- Do not delete existing data unless project owner explicitly approves a dev/test reset.


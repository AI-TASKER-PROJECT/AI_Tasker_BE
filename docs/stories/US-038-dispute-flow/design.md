# Design — US-038 Milestone Execution & Dispute Flow (v2)

## 1. DB Changes (V46)
### 1.1 contracts_milestones
- Add `resubmit_count` INT DEFAULT 0 (0..3 limit)
### 1.2 disputes
- Add `initiated_by` VARCHAR(20) — 'BUSINESS' | 'EXPERT'
- Add `escalation_reason` TEXT
- Add `escalation_evidence_file` VARCHAR(255)
- Add `staff_decision_percentage` INT (0-100) — percentage of escrow to Expert
- Add `staff_decision_note` TEXT
- Existing statuses: Open, UnderReview, Resolved, Escalated must be preserved in data
- Add new statuses as CHECK constraint: PENDING_SELF_RESOLVE, ESCALATION_REQUESTED, STAFF_REVIEWING, STAFF_DECIDED, INTERVENTION_REJECTED

## 2. Service: ContractExecutionService
### 2.1 depositMilestoneEscrow(contractId, milestoneId)
- Guard: contract ACTIVE, milestone PENDING
- `walletLedgerService.holdEscrowFromAvailable(businessAccountId, amount, "MILESTONE_ESCROW_DEPOSIT", "CONTRACT_MILESTONE", milestoneId, ...)`
- Set milestone status → DEPOSITED

### 2.2 submitDeliverable(milestoneId, fileUrl, note)
- Guard: milestone is DEPOSITED or REJECTED (re-submit)
- Create DeliverableEntity with milestoneId
- Set milestone status → UNDER_REVIEW

### 2.3 approveMilestone(milestoneId)
- Guard: milestone is UNDER_REVIEW, caller is Business
- `walletLedgerService.debitEscrow(businessAccountId, finalBudget, "MILESTONE_ESCROW_RELEASE", "CONTRACT_MILESTONE", milestoneId, ...)`
- `walletLedgerService.creditAvailable(expertAccountId, finalBudget, "MILESTONE_ESCROW_RELEASE", "CONTRACT_MILESTONE", milestoneId, ...)`
- Set milestone → APPROVED

### 2.4 rejectMilestone(milestoneId, reason)
- Guard: milestone is UNDER_REVIEW, caller is Business
- Increment resubmit_count
- If resubmit_count >= 3 → auto-lock (Expert cannot resubmit further). Milestone stays REJECTED.
- Set milestone → REJECTED

### 2.5 initiateDispute(contractId, milestoneId, initiatedBy)
- Guard: milestone is REJECTED (or other valid state per spec)
- Only participate: Business or Expert of this contract
- Set milestone → DISPUTED
- Create DisputeEntity with status PENDING_SELF_RESOLVE, initiated_by

### 2.6 escalateDispute(disputeId, reason, evidenceFile)
- Guard: dispute in PENDING_SELF_RESOLVE
- Set dispute → ESCALATION_REQUESTED, fill escalation_reason/evidence

### 2.7 rejectIntervention(disputeId)
- Guard: Staff role, assigned to dispute, dispute in STAFF_REVIEWING
- Set dispute → INTERVENTION_REJECTED (back to self-resolve)
- Milestone remains DISPUTED

### 2.8 assignStaff(disputeId, staffId)
- Guard: Admin role
- Set dispute.assignedStaffId = staffId, dispute.status = STAFF_REVIEWING

### 2.9 staffDecide(disputeId, expertPercent, note)
- Guard: Staff role, assigned to dispute, dispute in STAFF_REVIEWING
- Validate expertPercent 0..100
- Set staff_decision_percentage, staff_decision_note
- Set dispute.status = STAFF_DECIDED
- Call `executeSettlement(disputeId)` internally

### 2.10 executeSettlement(disputeId)
- Load dispute + milestone + contract
- `escrowAmount = milestone.finalBudget`
- `expertPayout = escrowAmount * staffDecisionPercentage / 100`
- `businessRefund = escrowAmount - expertPayout`
- `walletLedgerService.debitEscrow(businessAccountId, escrowAmount, "MILESTONE_ESCROW_SETTLEMENT_PAYOUT", "DISPUTE", disputeId, ...)`
- `walletLedgerService.creditAvailable(expertAccountId, expertPayout, "MILESTONE_ESCROW_SETTLEMENT_PAYOUT", "DISPUTE", disputeId, ...)`
- `walletLedgerService.creditAvailable(businessAccountId, businessRefund, "MILESTONE_ESCROW_SETTLEMENT_REFUND", "DISPUTE", disputeId, ...)`
- Set milestone → COMPLETED (or CANCELLED if 100% refund)
- Record audit log entries for each tx + settlement

## 3. Resubmit limit check
- In `submitDeliverable`: if milestone resubmit_count >= 3 → throw "DA DAT TOI DA LAN NOP LAI"

## 4. Testing
- Unit test coverage for each guard/validation scenario
- Settlement math rounding test

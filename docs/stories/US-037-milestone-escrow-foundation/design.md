# Design — US-037 Milestone Escrow Foundation

## Current state observed
- `ContractEntity.status` is a string. DB status constraint from V31 currently allows `DRAFT`, `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
- `ContractMilestoneEntity.status` is a string. It currently snapshots job milestones with initial `PENDING`.
- `MilestoneEntity` and `ContractMilestoneEntity` are both present; execution flow uses job milestone id and contract milestone snapshots.
- `WalletTransactionEntity` supports generic ledger vocabulary: `transaction_type`, `direction`, `balance_type`, `reference_type`, `reference_id`.
- `ContractDepositEntity` already models the separate 20% security deposit and must not be conflated with milestone escrow.
- `DisputeEntity` exists but uses old statuses such as `Open`, `UnderReview`, `Resolved` in service code.

## v2 foundation vocabulary
Phase 1 introduces DB/JPA vocabulary only where safe:

### Contract statuses
Keep current statuses and add future-safe statuses:
- `TERMINATION_PENDING`
- `TERMINATED`

### Milestone execution statuses
Support v2 milestone execution without full dispute settlement:
- `PENDING`
- `DEPOSITED`
- `IN_PROGRESS` (if current code/tests require existing execution state)
- `UNDER_REVIEW`
- `APPROVED`
- `REJECTED`
- `DISPUTED`
- `COMPLETED`
- `CANCELLED`

### Dispute statuses (vocabulary only)
- `PENDING_SELF_RESOLVE`
- `ESCALATION_REQUESTED`
- `STAFF_REVIEWING`
- `STAFF_DECIDED`
- `INTERVENTION_REJECTED`
- keep legacy-compatible `Open`, `UnderReview`, `Resolved` only if existing tests/data need migration compatibility; prefer data migration to uppercase v2 values if safe.

### Wallet transaction/reference vocabulary
Add/allow transaction types and reference types for later flow:
- `MILESTONE_ESCROW_DEPOSIT`
- `MILESTONE_ESCROW_RELEASE`
- `MILESTONE_ESCROW_REFUND`
- `MILESTONE_ESCROW_SETTLEMENT_PAYOUT`
- `MILESTONE_ESCROW_SETTLEMENT_REFUND`
- reference type: `CONTRACT_MILESTONE`, `MILESTONE`, `DISPUTE`, `CONTRACT_DEPOSIT`

## Safety invariants
- Do not release escrow twice.
- Do not mix 20% contract security deposit with milestone escrow.
- Do not mark a high-risk story implemented unless validation proof is written.
- Do not edit old migrations; add V45 if schema changes are needed.

## Implementation shape
- Prefer additive migration `V45__milestone_escrow_foundation.sql`.
- Add helper constants/methods close to existing service boundaries instead of broad enum refactor.
- Tests should cover at least:
  - Status constraints/mapping for new statuses where unit-testable.
  - Contract remains PENDING after both parties sign until escrow/deposit flow triggers activation if that is existing behavior.
  - Milestone initial state remains `PENDING`, and Phase 1 deposit helper (if implemented) moves to `DEPOSITED` only once.

# Validation - US-038 Milestone Execution & Dispute Flow

## Proof Commands

- `.\mvnw.cmd '-Dtest=ContractExecutionServiceTest,PaymentWalletServiceTest,AdminServiceTest' test` passed: 65 tests, 0 failures, 0 errors.
- `.\mvnw.cmd -DskipTests compile` passed.
- `.\mvnw.cmd test` passed: 195 tests, 0 failures, 0 errors.
- Flyway validated 48 migrations against Docker Postgres and reported schema up to date.

## Evidence

- Milestone approval releases escrow once, marks live and contract milestones `COMPLETED`, and resolves self-resolve disputes.
- Business rejection creates or updates a single active dispute and moves the milestone to `DISPUTED`.
- Staff decision no longer executes settlement immediately; settlement execution requires `STAFF_DECIDED`.
- Settlement sets `escrow_released_at`, resolves the dispute, and uses v2 wallet transaction types.
- Expert can start a deposited milestone (`DEPOSITED` -> `IN_PROGRESS`).
- Dispute cancellation restores the previous milestone status before Staff review.
- Spec route aliases are exposed for escalation request, assign staff, reject intervention, staff decision, execute settlement, and cancel.

## Remaining Limits

- None known for the implemented backend slice.

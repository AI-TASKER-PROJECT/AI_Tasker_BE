# Validation - US-039 Contract Termination & Closure

## Proof Commands

- `.\mvnw.cmd '-Dtest=ContractExecutionServiceTest,PaymentWalletServiceTest,AdminServiceTest' test` passed: 65 tests, 0 failures, 0 errors.
- `.\mvnw.cmd -DskipTests compile` passed.
- `.\mvnw.cmd test` passed: 195 tests, 0 failures, 0 errors.
- Flyway validated 48 migrations against Docker Postgres and reported schema up to date.

## Evidence

- Termination request is limited to active participant contracts.
- Termination request lifecycle now persists `termination_requests`, supports Staff assignment, Staff approve/reject, Expert partial evidence, Admin settlement execution, withdrawal, and deposit refund completion.
- Termination execution is blocked while active disputes exist.
- Approved termination settlement can split current milestone escrow between Expert payout and Business refund, records settlement transaction metadata, cancels remaining milestones, and moves the contract to `TERMINATED`.
- Termination refunds unreleased escrow for deposited/in-progress/under-review milestones, cancels unfinished milestones, and leaves completed milestones intact.
- Contract security-deposit refund now closes `COMPLETED` or `TERMINATED` contracts by moving them to `CLOSED`.
- Review creation is gated on `CLOSED`.
- Case attachments are available for disputes, termination requests, partial evidence, and staff reports.

## Remaining Limits

- None known for the implemented backend slice.

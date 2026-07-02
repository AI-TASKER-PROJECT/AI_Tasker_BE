# Overview — US-038 Milestone Execution & Dispute Flow

## Goal
Implement the end-to-end milestone execution flow including escrow deposit, deliverable submission, business review, rejection limits (max 3 re-submit), dispute initiation, staff assignment, and staff decision settlement.

## Risk Classification
High-Risk (involves financial ledger escrow allocation, refund splits, and state transitions).

## Affected Areas
- `ContractExecutionService` (new entrypoints for escrow deposit, milestone submit, approve, reject, dispute, escalate, assign staff, staff decide).
- `WalletLedgerService` or `PaymentWalletService` (transfers from Business available to escrow, and escrow split to Expert / Business).
- DB migration V46 (new tables if any, e.g. for dispute evidence or assignment rules, or simple columns).

# Overview — US-039 Contract Termination & Closure

## Goal
Implement Flow 4 (Termination). If an escrow balance exists at the time of contract termination, it must be refunded to the Business's available wallet balance. Support states: `TERMINATION_PENDING` and `TERMINATED`.

## Risk Classification
High-Risk (financial refund).

## Affected Areas
- `ContractExecutionService.requestTermination()`
- `ContractExecutionService.executeTermination()`
- `WalletLedgerService` calls (release escrow refund).

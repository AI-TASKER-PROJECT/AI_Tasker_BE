# Design

## Deadline Source

Use `contract_milestones.in_progress_started_at`, `duration`, and
`duration_unit`. Do not use editable job milestone duration and do not require
the Admin overdue command to have run.

## Application Rule

Add one shared guard in `ContractExecutionService` and call it before any final
deliverable persistence or Firebase upload. The guard returns without enforcing
a date only when the start or positive duration is unavailable.

`OVERDUE` and an `IN_PROGRESS` snapshot whose computed deadline is before now
both produce `MILESTONE_DA_QUA_HAN_NOP_SAN_PHAM`. Other invalid states retain
their existing command-specific errors.

## Payment Boundary

Deadline rejection performs no wallet mutation. Business milestone escrow stays
held and can move only through the existing approval, dispute, SLA, or
termination settlement commands.

## Data Model

No migration. Existing contract snapshot and wallet-ledger fields are sufficient.


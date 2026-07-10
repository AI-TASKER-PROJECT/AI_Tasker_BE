# US-037 — Flow 4+5 Phase 1: Milestone Escrow & State Machine Foundation (v2)

## Intake
- Intake: #44
- Lane: high-risk
- Source spec: `SPEC-MILESTONE-DISPUTER.md`
- Scope slice: Phase 1 foundation only for Flow 4/Flow 5.

## Goal
Introduce the smallest safe backend foundation for the v2 milestone model:

- Per-milestone escrow status/state support.
- Contract and milestone statuses required by later Flow 4/5 phases.
- Wallet transaction/reference vocabulary required for escrow deposit/release/refund/settlement.
- Database constraints aligned with Java/JPA model.

## Non-goals
- Do not implement full staff dispute review in this story.
- Do not implement final settlement execution in this story.
- Do not implement full termination/security-deposit refund in this story.
- Do not remove the existing 20% contract deposit model unless a later decision/story explicitly replaces it.

## Affected Areas
- `contracts`
- `contract_milestones`
- `milestones`
- `wallet_transactions`
- `disputes` (status vocabulary only if needed for compile/constraints)
- `ContractExecutionService`
- wallet/system-wallet services and tests

## Risk Classification
High-risk because this changes financial state vocabulary, DB constraints, contract workflow states, and future money movement invariants across multiple roles.
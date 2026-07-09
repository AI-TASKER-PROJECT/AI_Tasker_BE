# Exec Plan — US-045

## Goal

Bring the backend implementation and executable proof into conformance with
`SPEC-MILESTONE-DISPUTER.md` v2.2.

## Scope

In scope:

- Additive migration, JPA entities, repositories, DTOs, services, routes.
- Dual participant deposits and activation gate.
- Reject/resubmit history and explicit-only disputes.
- Progress request SLA, feedback, overdue behavior.
- Staff evidence-window and settlement guards.
- Standard/immediate termination and deposit resolution.
- Unit/integration-facing tests and contract documentation.

Out of scope:

- External payout providers.
- Appeal flow.

## Risk Classification

Risk flags:

- Authorization
- Data model
- Audit/security
- Public contracts
- Existing behavior
- Multi-domain

Hard gates:

- Authorization
- Financial custody and additive migration

## Work Phases

1. Inventory source and migration baseline.
2. Add schema and entity/repository contracts.
3. Implement wallet/activation and milestone behavior.
4. Implement dispute and termination behavior.
5. Synchronize API/product docs.
6. Run focused and full verification, then bug-fix.
7. Record Harness proof and trace.

## Stop Conditions

Pause for human confirmation if the v2.2 binding decisions conflict with a
newer user instruction, destructive migration becomes necessary, or required
validation must be weakened.

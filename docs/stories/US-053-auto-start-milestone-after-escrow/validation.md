# Validation

## Proof Strategy

Focused service tests must prove the changed state transition and compatibility
behavior. The full Maven suite must pass before this story can be marked
implemented.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Deposit moves `PENDING -> IN_PROGRESS`; deposit sets timeline; legacy start moves `DEPOSITED -> IN_PROGRESS`; idempotent start returns already `IN_PROGRESS`. |
| Integration | Full Maven suite exercises Spring/JPA/Flyway-backed behavior. |
| E2E | Not run for this backend-only change. |
| Platform | OpenAPI shape unchanged; API-facing docs updated manually. |
| Performance | Not applicable. |
| Logs/Audit | Unit tests cover `MILESTONE_STARTED` only for legacy start; deposit audit remains existing behavior. |

## Fixtures

Tests use mocked Business/Expert accounts, active contracts, and contract
milestone rows inside `ContractExecutionServiceTest`.

## Commands

```text
command: .\mvnw.cmd -Dtest=ContractExecutionServiceTest test
result: pass, 72 tests, 0 failures, 0 errors
notes: Focused service proof for milestone deposit auto-start, legacy start, idempotent start, and adjacent progress-report behavior.

command: .\scripts\bin\harness-cli.exe story verify US-053
result: pass, ran .\mvnw.cmd -Dtest=ContractExecutionServiceTest test, 72 tests, 0 failures, 0 errors
notes: Durable Harness verification recorded.

command: .\mvnw.cmd test
result: pass, 282 tests, 0 failures, 0 errors
notes: Full backend suite on final source state.

command: Runtime OpenAPI refresh on http://localhost:8082/v3/api-docs
result: pass, 166 operations
notes: Refreshed docs/openapi/openapi-v1.json; deposit summary is "Deposit milestone escrow and auto-start milestone"; start summary is "Start milestone compatibility endpoint"; start description contains idempotent.
```

## Acceptance Evidence

- `depositMilestoneEscrow_shouldHoldEscrowAndAutoStartMilestone` proves
  successful Business escrow deposit returns and stores `IN_PROGRESS`.
- `depositMilestoneEscrow_shouldSetInProgressStartedAt` proves deposit sets the
  timeline anchor and status is `IN_PROGRESS`.
- `startMilestone_shouldMoveDepositedToInProgress` preserves legacy
  `DEPOSITED -> IN_PROGRESS` support and audit.
- `startMilestone_shouldReturnAlreadyInProgressMilestone` proves the start
  endpoint is idempotent for auto-started milestones and does not write a
  duplicate start audit event.
- Product docs, API-facing guides, runtime OpenAPI, decision 0027, and this
  story packet were updated.
- Final completed Harness trace: #93.

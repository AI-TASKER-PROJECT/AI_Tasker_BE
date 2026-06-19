# Validation

## Proof Strategy

Use focused service unit tests for lifecycle transitions plus the full Maven
test suite when feasible.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Activation sets contract `Active`, job `IN_PROGRESS`, milestone budgets, and contract id. |
| Unit | Expert rejection sets contract `Cancelled` and job `PROPOSAL_REVIEW`. |
| Unit | Completing the last reviewed milestone sets milestone `Completed`, contract `Completed`, and job `CLOSED`. |
| Integration | Existing Spring context and marketplace integration tests should still pass. |
| E2E | Manual Postman/Swagger flow can exercise the new routes. |
| Platform | Not applicable. |
| Performance | Not applicable. |
| Logs/Audit | Unit tests cover behavior; audit records are called through service paths. |

## Fixtures

- Approved business account with business profile.
- Approved expert account with expert profile.
- Accepted proposal attached to a business-owned job.
- Job milestone rows for contract snapshot and completion.

## Commands

```text
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd -Dtest=ContractExecutionServiceTest test` passed: 11 tests, 0
  failures, 0 errors.
- `scripts/bin/harness-cli story verify US-002` passed using the same focused
  service-unit verification command.
- Full `.\mvnw.cmd test` compiled sources and tests but did not complete
  because Docker Desktop/PostgreSQL was unavailable:
  `Connection to 127.0.0.1:5433 refused`.

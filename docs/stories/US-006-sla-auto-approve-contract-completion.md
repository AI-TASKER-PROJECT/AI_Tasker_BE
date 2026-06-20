# US-006 SLA Auto-Approve Contract Completion

## Status

implemented

## Lane

normal

## Product Contract

When SLA auto-approval completes the final milestone for an active contract,
the backend must complete the contract and close the linked job, matching the
normal business milestone completion path.

## Relevant Product Docs

- `docs/product/contract-management.md`

## Acceptance Criteria

- SLA auto-approval still marks overdue reviewed milestones as `COMPLETED`.
- If the auto-approved milestone is the last incomplete contract milestone, the
  contract moves to `COMPLETED`.
- The job linked to the completed contract moves to `CLOSED`.
- Completion logic does not finalize contracts that are not `ACTIVE`.

## Design Notes

- Commands: `POST /api/v1/milestones/sla-auto-approve`
- Service: `ContractExecutionService.runSlaAutoApprove`
- Domain rules: reuse the same contract/job finalization helper used by normal
  business milestone completion.

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` covers SLA final milestone completion. |
| Integration | Full Maven suite. |
| E2E | Not run. |
| Platform | Not applicable. |
| Release | Not applicable. |

## Harness Delta

No Harness policy changes.

## Evidence

- `.\mvnw.cmd -Dtest=ContractExecutionServiceTest test` passed on
  2026-06-20: 12 tests, 0 failures, 0 errors.
- `.\mvnw.cmd test` passed on 2026-06-20: 56 tests, 0 failures, 0 errors.
- `docker compose up -d` was attempted first but Docker Desktop was not
  running; Maven tests used the already available PostgreSQL at
  `127.0.0.1:5433`.

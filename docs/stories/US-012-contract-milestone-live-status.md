# US-012 Contract Milestone Live Status View

## Status

implemented

## Lane

normal

## Product Contract

`GET /api/v1/contracts/{contractId}/milestones` returns a new DTO named
`ContractMilestoneViewResponse` instead of returning `ContractMilestoneEntity`
directly. The displayed status comes from the live `milestones.status` when the
linked milestone exists, and falls back to `contract_milestones.status` when the
linked milestone is missing — without crashing.

Snapshot fields (`milestoneName`, `description`, `originalBudget`, `finalBudget`,
`orderIndex`) are always read from `contract_milestones`.

## Relevant Product Docs

- `docs/product/contract-management.md`

## Acceptance Criteria

1. `GET /api/v1/contracts/{contractId}/milestones` returns
   `List<ContractMilestoneViewResponse>` with live status from `milestones`
   table.
2. When the linked milestone exists, `milestones.status` is used as the
   displayed status.
3. When the linked milestone is missing, `contract_milestones.status` is used as
   the fallback status (no crash).
4. Snapshot fields (`milestoneName`, `description`, `originalBudget`,
   `finalBudget`, `orderIndex`) always come from `contract_milestones`, not from
   the live `milestones` table.
5. The view correctly reflects live status after deliverable submission
   (milestone becomes `UNDER_REVIEW`).
6. The view correctly reflects live status after milestone completion (milestone
   becomes `COMPLETED`).
7. The view correctly reflects live status after SLA auto-approve (milestone
   becomes `COMPLETED`).
8. No database schema or migration changes are required for this story.

## Design Notes

- Commands: none (no new write endpoints).
- Queries: `GET /api/v1/contracts/{contractId}/milestones` now returns
  `ContractMilestoneViewResponse` DTO.
- API: response type changed from `ApiResponse<Object>` to
  `ApiResponse<List<ContractMilestoneViewResponse>>`.
- Tables: reads `contract_milestones` (snapshot source) and `milestones` (live
  status source via `job_milestone_id` lookup).
- Domain rules: authorization unchanged (`requireContractParticipantOrOperator`).
- UI surfaces: contract milestone list widget that now shows live milestone
  status.
- DTO: `ContractMilestoneViewResponse` in `dto/core/` with fields
  `contractMilestoneId`, `contractId`, `jobMilestoneId`, `milestoneName`,
  `description`, `originalBudget`, `finalBudget`, `orderIndex`, `status`,
  `createdAt`, `updatedAt`, plus `getDifference()` helper.

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` covers 6 new test cases |
| Integration | `mvn test -pl . -Dtest=ContractExecutionServiceTest` passes; full `.\mvnw.cmd test` passes |
| E2E | — |
| Platform | — |
| Release | — |

## Harness Delta

- Recorded intake #16, story US-012, proof matrix (unit=yes, integration=yes), and trace #18.
- Created story file `docs/stories/US-012-contract-milestone-live-status.md`.
- Updated `docs/openapi/openapi-v1.json` with `ContractMilestoneViewResponse` schema.
- Updated `docs/swagger-api-overview.md`, `docs/swagger-api-test-guide.md`, `docs/postman-api-test-guide.md` to reference `ContractMilestoneViewResponse`.

## Evidence

```text
$ mvn test -pl . -Dtest=ContractExecutionServiceTest -DfailIfNoTests=false
[INFO] Tests run: 17, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS

$ .\scripts\bin\harness-cli.exe story verify US-012
Story US-012 verification: pass
```

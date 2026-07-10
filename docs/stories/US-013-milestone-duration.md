# US-013 Add Duration for Milestone and Contract Milestone Snapshot

## Status

implemented

## Lane

normal

## Product Contract

Milestones and contract milestone snapshots carry `duration` and `durationUnit`
fields so the frontend can display milestone timelines before and after contract
creation. Contract milestone snapshots preserve the agreed duration at contract
creation time and do not change when the source milestone is later edited.

## Relevant Product Docs

- `docs/product/contract-management.md`

## Acceptance Criteria

1. Business can create a milestone with `duration` and `durationUnit`.
2. Business can update milestone duration via `PATCH /api/v1/milestones/{milestoneId}` before the contract is created.
3. Milestone API responses return `duration` and `durationUnit`.
4. When a contract is created, `contract_milestones` copies `duration` and `durationUnit` from the source milestone.
5. After contract creation, editing the source milestone does not change the contract milestone snapshot.
6. Migration does not break existing data (columns are nullable).
7. Existing tests still pass.
8. Duration validation: both or neither of `duration`/`durationUnit` must be provided; `duration` must be > 0; `durationUnit` must be one of `DAY`, `WEEK`, `MONTH`.
9. Follow-up US-020 validates duration in `POST /api/v1/jobs`: job duration is normalized, nested milestone durations are normalized, milestone duration requires job duration, and total converted milestone duration must not exceed job duration.

## Design Notes

- Migration: `V33__add_milestone_duration.sql` adds nullable `duration INT` and `duration_unit VARCHAR(20)` to both `milestones` and `contract_milestones` with check constraints.
- Enum: `DurationUnit` in `com.aitasker.be.entity` with `DAY`, `WEEK`, `MONTH`.
- Entities: `MilestoneEntity` and `ContractMilestoneEntity` gain `duration` (Integer) and `durationUnit` (String, stored as `@Column(name = "duration_unit")`).
- DTO: `ContractMilestoneViewResponse` gains `duration` and `durationUnit` fields mapped from `contract_milestones`.
- Validation: `validateDuration()` in `ContractExecutionService` enforces pair consistency, positive duration, and valid unit enum.
- Snapshot copy: `createContractMilestones()` copies `duration` and `durationUnit` from `MilestoneEntity` into `ContractMilestoneEntity`.
- Update endpoint: `PATCH /api/v1/milestones/{milestoneId}` allows BUSINESS to edit milestone fields; blocked when the milestone already belongs to a contract.

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` covers 5 new test cases |
| Integration | Requires Docker/Postgres at 127.0.0.1:5433 |
| E2E | — |
| Platform | — |
| Release | — |

## Evidence

```text
$ mvn test -pl . -Dtest=ContractExecutionServiceTest -DfailIfNoTests=false
[INFO] Tests run: 22, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

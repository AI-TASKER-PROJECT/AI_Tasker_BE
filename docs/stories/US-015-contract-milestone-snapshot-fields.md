# US-015 Strengthen Contract Milestone Snapshot Fields

## Status

implemented

## Lane

normal

## Product Contract

`contract_milestones` is the snapshot of milestone content agreed at contract
creation time. It must carry `criteria_snapshot` (acceptance criteria text at
the moment the contract is created) and `deliverable_expectation` (deliverable
expectation text copied from the milestone). After the contract is created,
edits to the source milestone or to its acceptance criteria must not change the
contract milestone snapshot.

## Relevant Product Docs

- `docs/product/contract-management.md`

## Acceptance Criteria

1. `contract_milestones` has nullable `criteria_snapshot TEXT` and
   `deliverable_expectation TEXT` columns added by a new Flyway migration.
2. `ContractMilestoneEntity` exposes `criteriaSnapshot` and
   `deliverableExpectation` fields.
3. `ContractMilestoneViewResponse` exposes `criteriaSnapshot` and
   `deliverableExpectation` fields returned by
   `GET /api/v1/contracts/{contractId}/milestones`.
4. When a contract is created via `createDraftFromProposal`, the snapshot copy
   logic builds `criteriaSnapshot` from the milestone's active acceptance
   criteria descriptions (joined by `\n`) and copies `deliverableExpectation`
   from the milestone `description`.
5. After contract creation, editing the source milestone description or its
   acceptance criteria does not change the stored contract milestone snapshot.
6. Migration does not break existing data (columns are nullable).
7. Existing tests still pass.

## Design Notes

- Migration: `V35__add_contract_milestone_snapshot_fields.sql` adds nullable
  `criteria_snapshot TEXT` and `deliverable_expectation TEXT` to
  `contract_milestones`.
- Entities: `ContractMilestoneEntity` gains `criteriaSnapshot` (String) and
  `deliverableExpectation` (String), mapped to the new columns.
- DTO: `ContractMilestoneViewResponse` gains `criteriaSnapshot` and
  `deliverableExpectation` fields mapped from `contract_milestones`.
- Snapshot copy: `createContractMilestones()` loads acceptance criteria for each
  milestone via `milestoneCriteriaRepository.findByIdMilestoneId(...)` joined
  with `criteriaRepository`, builds the snapshot text, and copies the milestone
  `description` into `deliverableExpectation`.
- Response mapping: `listMilestonesByContract()` maps `criteriaSnapshot` and
  `deliverableExpectation` from the contract milestone entity into the response.
- Source field mapping is locked to current schema: `milestones.description` is
  the only text field on the milestone today; acceptance criteria text lives in
  `acceptance_criteria.description` joined through
  `milestone_acceptance_criteria`. If future migrations add a dedicated
  `deliverable_expectation` column on `milestones`, the copy source can be
  swapped without changing the snapshot columns.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id US-015 --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` covers snapshot creation and snapshot stability |
| Integration | Requires Docker/Postgres at 127.0.0.1:5433 |
| E2E | — |
| Platform | — |
| Release | — |

## Harness Delta

- Records intake #19 (change_request, normal) for US-015.
- Adds durable story row US-015.
- No new backlog item expected unless friction is discovered.

## Evidence

```text
$ .\mvnw.cmd -Dtest=ContractExecutionServiceTest test
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

New test cases added:

- `listMilestonesByContract_shouldReturnSnapshotFieldsFromContractMilestones`
  (extended to assert `criteriaSnapshot` and `deliverableExpectation`).
- `listMilestonesByContract_shouldKeepSnapshotStableWhenMilestoneDescriptionChanges`.
- `createDraftFromProposal_shouldCopySnapshotFieldsFromMilestoneAndAcceptanceCriteria`.

Full Maven suite requires Docker/Postgres at 127.0.0.1:5433 (same constraint as
US-008, US-013, US-014). Integration tests fail with `PSQL Connection refused`
when Docker is not running; not caused by this change.

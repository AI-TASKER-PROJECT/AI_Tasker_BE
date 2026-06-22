# US-016 Add Regression Tests for Contract Milestone and Notification Flow

## Status

implemented

## Lane

normal

## Product Contract

Regression tests lock the behavior fixed by US-012, US-013, US-014, and US-015
so future changes do not reintroduce status drift, missing snapshot data, or
broken FE routing. Tests cover: submit deliverable sets milestone to
`UNDER_REVIEW`; complete milestone sets status to `COMPLETED`; SLA auto approve
sets status to `COMPLETED`; deliverable notification `targetUrl` follows
`/contracts/{contractId}/workspace?milestoneId={milestoneId}`; notification
metadata carries `contractId`, `milestoneId`, `deliverableId`; duration snapshot
stays stable after source milestone edit; criteria/deliverable expectation
snapshot stays stable after source milestone edit.

## Relevant Product Docs

- `docs/product/contract-management.md`

## Acceptance Criteria

1. Test verifies `submitDeliverable` sets the linked milestone status to
   `UNDER_REVIEW`.
2. Test verifies `completeMilestone` sets the milestone status to `COMPLETED`
   and `/contracts/{contractId}/milestones` returns `COMPLETED` (already covered
   by US-012 regression tests).
3. Test verifies SLA auto approve sets the milestone status to `COMPLETED` and
   `/contracts/{contractId}/milestones` returns `COMPLETED` (already covered by
   US-006/US-012 regression tests).
4. Test verifies the deliverable notification `targetUrl` string is
   `/contracts/{contractId}/workspace?milestoneId={milestoneId}`.
5. Test verifies the deliverable notification metadata contains `contractId`,
   `milestoneId`, and `deliverableId`.
6. Test verifies `contract_milestones.duration` and `duration_unit` snapshot
   stays stable when the source milestone duration is edited after contract
   creation, and `/contracts/{contractId}/milestones` returns the snapshot
   values.
7. Test verifies `contract_milestones.criteria_snapshot` and
   `deliverable_expectation` stay stable when the source milestone description
   is edited after contract creation (covered by US-015 regression test).
8. Test suite passes.
9. Tests do not depend on fixed local data.

## Design Notes

- Tests live in `ContractExecutionServiceTest` (service-layer Mockito style
  matching the existing project convention).
- A new `NotificationServiceTest` is added to verify the
  `notifyDeliverableSubmitted` targetUrl and metadata through the
  `NotificationRepository` save capture, since `NotificationService` builds the
  targetUrl and metadata map internally.
- Existing regression coverage from US-012/US-013/US-014/US-015 is not
  duplicated; only the gaps are filled.
- Duration snapshot stability test: contract milestone snapshot has
  `duration=10, durationUnit=WEEK`; live milestone is edited to
  `duration=99, durationUnit=MONTH`; API returns snapshot `10/WEEK`.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id US-016 --unit 1 --integration 0 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` + `NotificationServiceTest` cover all 6 required test cases |
| Integration | Requires Docker/Postgres at 127.0.0.1:5433 |
| E2E | — |
| Platform | — |
| Release | — |

## Harness Delta

- Records intake #20 (change_request, normal) for US-016.
- Adds durable story row US-016.

## Evidence

```text
$ .\mvnw.cmd "-Dtest=ContractExecutionServiceTest,NotificationServiceTest" test
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0 -- ContractExecutionServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- NotificationServiceTest
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

New regression tests added:

- `submitDeliverable_shouldSetMilestoneStatusToUnderReview` (Test 1 gap).
- `listMilestonesByContract_shouldKeepDurationSnapshotStableWhenMilestoneDurationEdited` (Test 5 gap).
- `NotificationServiceTest.notifyDeliverableSubmitted_shouldBuildTargetUrlWithContractAndMilestone` (Test 4 targetUrl).
- `NotificationServiceTest.notifyDeliverableSubmitted_shouldSerializeMetadataWithContractMilestoneAndDeliverable` (Test 4 metadata).
- `NotificationServiceTest.notifyDeliverableSubmitted_shouldIncludeMilestoneNameInMessage` (message sanity).

Already covered by prior stories (not duplicated):

- Test 2: `completeMilestone_shouldCompleteContractAndCloseJobWhenAllMilestonesCompleted` + `listMilestonesByContract_shouldUseLiveMilestoneStatusAfterMilestoneCompletion` (US-012).
- Test 3: `runSlaAutoApprove_shouldCompleteContractAndCloseJobWhenFinalMilestoneApproved` + `listMilestonesByContract_shouldUseLiveMilestoneStatusAfterSlaAutoApprove` (US-006/US-012).
- Test 6: `listMilestonesByContract_shouldKeepSnapshotStableWhenMilestoneDescriptionChanges` (US-015).
- Test 4 method-call args: `submitDeliverable_shouldNotifyBusinessWithCorrectTargetUrl` (US-014).

Full Maven suite requires Docker/Postgres at 127.0.0.1:5433 (same constraint as US-008/US-013/US-014/US-015).

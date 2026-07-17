# US-063 Block Overdue Milestone Deliverable Submission

## Status

implemented

## Lane

high-risk

## Product Contract

An Expert cannot submit a final deliverable or upload its source-code archive
after the contract milestone execution deadline. Deadline enforcement must not
depend on whether an Admin has already marked the milestone `OVERDUE`.

## Acceptance Criteria

- Deadline is calculated from the contract snapshot start and duration.
- Submission before or exactly at the deadline remains allowed.
- Submission after the deadline is rejected while status is `IN_PROGRESS`.
- A persisted `OVERDUE` milestone is rejected.
- Correction/resubmission after the original deadline is rejected.
- Source-code ZIP upload follows the same deadline rule.
- A milestone without a computable deadline preserves existing behavior.
- Rejection creates no deliverable, upload, notification, status transition, or
  wallet movement.
- Existing dispute and termination settlement behavior is unchanged.

## Files

- `src/main/java/com/aitasker/be/service/core/ContractExecutionService.java`
- `src/main/java/com/aitasker/be/controller/core/ContractExecutionController.java`
- `src/test/java/com/aitasker/be/service/core/ContractExecutionServiceTest.java`
- `docs/product/contract-management.md`
- `docs/decisions/0034-block-overdue-milestone-deliverables.md`

## Validation

See `validation.md`.

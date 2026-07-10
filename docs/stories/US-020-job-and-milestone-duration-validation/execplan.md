# Exec Plan

## Goal

Make job and nested milestone duration validation consistent in the create-job
flow and prevent milestone duration totals from exceeding job duration.

## Scope

In scope:

- Validate and normalize `plannedDurationValue/plannedDurationUnit` in
  `MarketplaceService.createJob`.
- Validate and normalize nested milestone `duration/durationUnit` values.
- Reject nested milestone durations when job duration is missing.
- Reject total milestone duration greater than job duration.
- Add focused unit tests.
- Backfill Harness story records for the recommendation selection feature that
  previously used a conflicting story id.

Out of scope:

- Adding deadline/date fields.
- Changing AI SoW generation.
- Changing contract milestone snapshot schema.

## Risk Classification

Risk flags:

- Data model.
- Public contracts.
- Existing behavior.
- Weak proof.

Hard gates:

- Data model.

## Work Phases

1. Inspect current Harness matrix and traces.
2. Backfill missing recommendation-selection story and decision records.
3. Implement create-job duration validation.
4. Add unit tests for valid and invalid duration inputs.
5. Update product/story documentation.
6. Run focused tests and compile/full tests as available.
7. Record story status and trace.

## Stop Conditions

Pause for human confirmation if:

- Deadline/date behavior is requested in this same change.
- Existing nullable duration fields must become mandatory.
- Duration conversion rules must differ from DAY=1, WEEK=7, MONTH=30.

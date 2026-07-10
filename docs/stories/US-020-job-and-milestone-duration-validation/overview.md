# Overview

## Current Behavior

Jobs can carry `plannedDurationValue` and `plannedDurationUnit`, while
milestones can carry `duration` and `durationUnit`. The database has nullable
duration columns and check constraints, but `POST /api/v1/jobs` did not validate
or normalize job duration or nested milestone durations before saving a draft
job.

## Target Behavior

When a Business creates a job, the backend validates job duration and all nested
milestone durations in the same request. Duration unit values are normalized to
uppercase. If any milestone has a duration, the job must also have a planned
duration, and the sum of all milestone durations must not exceed the job
planned duration after unit conversion.

## Affected Users

- BUSINESS creating draft jobs with generated or manually edited milestones.
- EXPERT reading job milestones before proposal submission.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/stories/US-013-milestone-duration.md`

## Non-Goals

- No new deadline/date column.
- No change to contract milestone snapshot schema.
- No frontend implementation.

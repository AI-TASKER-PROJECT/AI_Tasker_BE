# Overview

## Current Behavior

`GET /api/v1/jobs/{jobId}/milestones` is blocked for unauthenticated callers by
Spring Security, even though the service allows milestone reads for `OPEN` jobs.

## Target Behavior

Unauthenticated callers can read milestones for `OPEN` jobs through
`GET /api/v1/jobs/{jobId}/milestones`. Non-`OPEN` jobs continue to require job
ownership or participation checks in the service layer.

## Affected Users

- Anonymous landing-page and marketplace visitors.
- Business and expert users who already rely on the job detail flow.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`

## Non-Goals

- Changing milestone payload shape.
- Making non-`OPEN` job milestones public.

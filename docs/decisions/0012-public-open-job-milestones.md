# 0012 Public Open Job Milestones

Date: 2026-06-21

## Status

Accepted

## Context

The backend service already allows milestone reads for `OPEN` jobs, but Spring
Security still required authentication for `GET /api/v1/jobs/{jobId}/milestones`
because the existing public matcher covered only one path segment after
`/api/v1/jobs/`. This mismatch blocked anonymous landing-page and public job
detail flows from reading milestone data.

## Decision

Permit unauthenticated `GET /api/v1/jobs/{jobId}/milestones` requests in Spring
Security while keeping the existing service-layer rule: only `OPEN` jobs are
public, and non-`OPEN` jobs still require owner or participant access.

## Alternatives Considered

1. Keep milestones authenticated and force public pages to rely only on
   embedded milestone data from `GET /api/v1/jobs`. Rejected because the route
   contract would remain inconsistent and fragile for direct job-detail reads.
2. Make all job milestone routes public regardless of job status. Rejected
   because it would leak draft or in-progress execution details.

## Consequences

Positive:

- Public job pages can read milestone details without login.
- Security configuration now matches the existing business rule in the service
  layer.

Tradeoffs:

- The public surface area of the API increases by one route and must stay
  aligned with service guards for non-`OPEN` jobs.

## Follow-Up

- Add a dedicated security/integration test for anonymous milestone reads when
  test coverage for security matchers is introduced or expanded.

# Design

## Domain Model

No entity or schema changes. The authorization boundary moves at the route
level only; the service-layer `OPEN`-job rule is preserved.

## Application Flow

Spring Security permits unauthenticated
`GET /api/v1/profiles/business/by-job/{jobId}` requests.
`ProfileService.businessProfileByJob(Integer jobId)` keeps its existing logic:

- Look up the job; `404` when not found.
- If job status is not `OPEN`, call
  `accessService.requireRole("STAFF","ADMIN","BUSINESS")` (anonymous callers
  fail here for non-`OPEN` jobs because they have no JWT/role).
- Return the Business profile for the job's `businessId`; `404` when missing.

Result: anonymous callers succeed only for `OPEN` jobs; non-`OPEN` jobs still
require an authenticated STAFF/ADMIN/BUSINESS caller.

## Interface Contract

- Route: `GET /api/v1/profiles/business/by-job/{jobId}`
- Public behavior: anonymous callers receive the Business profile when the job
  is `OPEN`; non-`OPEN` jobs still require protected access.
- Protected sibling behavior: `/me`, `/business`, and `/business/{businessId}`
  behavior is unchanged by this story (`/business/{businessId}` is public via
  US-017; `/me` and `/business` remain authenticated).

### Route Matcher Choice (critical)

The profile controller exposes siblings under `/api/v1/profiles/business/`:

```text
GET /api/v1/profiles/business                -> listBusiness
GET /api/v1/profiles/business/{id}           -> getBusinessById   (US-017 public)
GET /api/v1/profiles/business/me             -> myBusiness        (private)
GET /api/v1/profiles/business/by-job/{jobId} -> businessByJob     (to open)
```

An ant matcher like `/api/v1/profiles/business/**` would match every sibling
including `/me`, which is the leak the SPEC warns against.

Decision: use a **regex matcher** that matches only the `/by-job/{numericId}`
path:

```text
/api/v1/profiles/business/by-job/\d+
```

This never matches `/me`, `/business`, or `/business/{businessId}`. It is
independent of the US-017 matcher (`/api/v1/profiles/business/\d+`), which does
not match `/by-job/...` because `by-job` is not digits.

## Data Model

No migration.

## Public Data Boundary

`businessProfileByJob` returns `BusinessProfileEntity` without
`attachBusinessAccountInfo`, so `fullName` is not attached today. The SPEC does
not require `fullName` for US-018, so the existing surface is preserved. The
entity does not carry `email` or `phone`. The same
`rejectionReason`/`businessLicenseUrl` tradeoff noted in decision 0015 applies
and remains a follow-up public-DTO candidate, not a US-018 blocker.

## UI / Platform Impact

Public job detail pages can show the hiring Business profile without forcing
login.

## Observability

No new audit event. Anonymous permitAll reads follow the same filter-chain path
as other public GET routes.

## Alternatives Considered

1. Ant matcher `GET /api/v1/profiles/business/by-job/*`. Rejected: while it
   would not match `/me` (different segment count), a regex matcher on the
   numeric `jobId` is more explicit and consistent with US-017's approach.
2. Broaden the US-017 matcher to cover by-job. Rejected: the US-017 matcher is
   `/api/v1/profiles/business/\d+` and cannot match `/by-job/...`; a separate
   matcher is cleaner and keeps each story's boundary explicit.
3. Change the service to add `fullName` enrichment. Rejected as a non-goal; the
   SPEC says preserve the existing service logic.

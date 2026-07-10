# 0016 Public Business-by-Job Profile for Guest Access on OPEN Jobs

Date: 2026-06-22

## Status

Accepted

## Context

`GET /api/v1/profiles/business/by-job/{jobId}` exposes the Business profile of
the company that posted a job. The service layer
`ProfileService.businessProfileByJob(Integer jobId)` already enforces the
intended rule: `OPEN` jobs are readable, while non-`OPEN` jobs require
`requireRole("STAFF","ADMIN","BUSINESS")`.

Spring Security still blocks anonymous callers at the route level because the
path is not in the `permitAll` matcher set, so public job detail pages cannot
show the hiring company without forcing login.

US-017 already opened `/api/v1/profiles/business/{businessId}` with a regex
matcher (`/api/v1/profiles/business/\d+`). That matcher does not match
`/by-job/...` because `by-job` is not digits, so a separate matcher is required
for the by-job route.

The profile controller exposes single-segment siblings under
`/api/v1/profiles/business/`, including `/me` (BUSINESS-only). A broad ant
matcher like `/api/v1/profiles/business/**` would leak `/me` and `/business`,
which is the risk the SPEC explicitly calls out.

## Decision

1. Permit anonymous `GET /api/v1/profiles/business/by-job/{jobId}` in Spring
   Security using a **regex matcher** that matches only the
   `/by-job/{numericId}` path (`/api/v1/profiles/business/by-job/\d+`), so it
   can never match `/me`, `/business`, or `/business/{businessId}`.
2. Do not change `ProfileService.businessProfileByJob`. The existing `OPEN`-job
   rule remains the service-layer gate: anonymous callers succeed only for
   `OPEN` jobs; non-`OPEN` jobs still require an authenticated
   STAFF/ADMIN/BUSINESS caller.
3. Keep the US-017 matcher and sibling route guards unchanged.

## Alternatives Considered

1. Ant matcher `GET /api/v1/profiles/business/by-job/*`. Rejected: a regex
   matcher on the numeric `jobId` is more explicit and consistent with US-017.
2. Broaden the US-017 matcher to cover by-job. Rejected: the US-017 matcher
   cannot match `/by-job/...`; a separate matcher keeps each story's boundary
   explicit.
3. Open the route for all job statuses and rely on the service to enforce OPEN.
   Rejected: the SPEC requires preserving the existing service rule, and the
   route opening is for `OPEN`-job public access only.

## Consequences

Positive:

- Public job detail pages can show the hiring Business profile without login.
- The regex matcher makes the security boundary explicit and prevents `/me`
  leakage by construction.
- The service-layer `OPEN`-job rule remains the authoritative business gate.

Tradeoffs:

- Public surface area of the API increases by one route.
- `rejectionReason` and `businessLicenseUrl` remain on the preserved entity
  surface (same tradeoff as decision 0015; follow-up public-DTO candidate).
- Decision 0021 now also exposes account `email` and `phone` on this public
  Business profile read for `OPEN` jobs.

## Follow-Up

- Introduce a dedicated public Business profile DTO (shared with US-017) if
  `rejectionReason` and `businessLicenseUrl` should be hidden from Guests.
- Add a dedicated DB-free security test harness (backlog #2) so full HTTP
  security-chain proofs can run without Postgres.

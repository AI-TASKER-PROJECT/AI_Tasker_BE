# 0015 Business Public Profile Guest Access

Date: 2026-06-22

## Status

Accepted

## Context

`GET /api/v1/profiles/business/{businessId}` was introduced in US-009 as an
authenticated route for expert/business/staff/admin users to inspect a business
profile. The product contract now requires this surface to be Guest-public so
visitors can inspect a company before signing up.

Two gates currently block anonymous access:

- Route-level security in `SecurityConfig` (profile paths are not in the
  `permitAll` set).
- Service-level role check in `ProfileService.businessProfileById(...)` via
  `accessService.requireRole("EXPERT","BUSINESS","STAFF","ADMIN")`.

The profile controller exposes single-segment siblings under
`/api/v1/profiles/business/`, including `/me` (BUSINESS-only) and
`/by-job/{jobId}`. An ant matcher like `/api/v1/profiles/business/*` would also
match `/me`, which is the leak the SPEC explicitly warns against. The ant
matcher cannot distinguish a numeric `businessId` from the literal `me`.

The response entity `BusinessProfileEntity` is enriched only with `fullName`
via `attachBusinessAccountInfo`; it does not copy `email`, `phone`, or other
private account fields. The entity does carry `rejectionReason` and
`businessLicenseUrl`, which are reputation-/document-sensitive business fields.
The SPEC excludes switching to a new DTO for US-017 and permits preserving the
current surface.

## Decision

1. Permit anonymous `GET /api/v1/profiles/business/{businessId}` in Spring
   Security using a **regex matcher** that matches only a numeric path segment
   (`/api/v1/profiles/business/\d+`), so it can never match `/me`,
   `/by-job/{jobId}`, or the bare `/business` route.
2. Remove the `accessService.requireRole(...)` gate from
   `ProfileService.businessProfileById(Integer businessId)`. Keep the existing
   `404` lookup and `fullName` enrichment.
3. Keep the service-layer guards on the sibling routes unchanged:
   `currentBusinessProfile()` keeps `requireRole("BUSINESS")`;
   `businessProfileByJob(jobId)` keeps its `OPEN`-job rule; `allBusinessProfiles()`
   keeps `requireRole("STAFF")`.
4. Preserve the current entity response surface (no new DTO). Record the
   `rejectionReason` and `businessLicenseUrl` exposure as a tradeoff and
   recommend a follow-up public-DTO story if those fields should be hidden from
   Guests.

## Alternatives Considered

1. Ant matcher `GET /api/v1/profiles/business/*` with `/me` ordered before it as
   `.authenticated()`. Rejected: the SPEC explicitly warns against the broad
   matcher, and any future single-segment sibling would silently leak.
2. Relax only Spring Security and keep the service role gate. Rejected: Guests
   have no JWT, so `requireRole(...)` would still reject them at the service
   layer.
3. Introduce a public Business profile DTO that omits `rejectionReason` and
   `businessLicenseUrl`. Rejected as a non-goal of US-017; recorded as a
   follow-up.

## Consequences

Positive:

- Guests can inspect a Business public profile without signing up.
- The regex matcher makes the security boundary explicit and prevents `/me`
  leakage by construction.
- Sibling private routes keep their service-layer guards.

Tradeoffs:

- `rejectionReason` and `businessLicenseUrl` become reachable by anonymous
  callers because the entity surface is preserved. This is accepted for US-017
  and flagged for a follow-up public-DTO story.
- Public surface area of the API increases by one route.

## Follow-Up

- Introduce a dedicated public Business profile DTO (separate story) if
  `rejectionReason` and `businessLicenseUrl` should be hidden from Guests.
- Add a dedicated security/integration test for anonymous profile reads when
  lightweight security test coverage is introduced or expanded.

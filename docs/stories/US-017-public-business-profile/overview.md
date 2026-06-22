# Overview

## Current Behavior

`GET /api/v1/profiles/business/{businessId}` is blocked for unauthenticated
callers by two gates:

- Route-level security in `SecurityConfig` (the profile path is not in the
  `permitAll` matcher set).
- Service-level role check in
  `ProfileService.businessProfileById(Integer businessId)` via
  `accessService.requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN")`.

The endpoint already returns the business profile enriched with `fullName` from
the related account, and returns `404` when the profile is not found.

## Target Behavior

Guest (anonymous) users can call `GET /api/v1/profiles/business/{businessId}`
without a JWT. The endpoint still returns the business profile with `fullName`
enrichment and keeps the existing `404` behavior.

The following sibling routes must remain protected:

- `GET /api/v1/profiles/business/me` stays BUSINESS-only.
- `GET /api/v1/profiles/business/by-job/{jobId}` keeps its current business rule.
- `GET /api/v1/profiles/business` keeps its existing authorization rule.

Expert profile routes are not opened by this story.

## Affected Users

- Anonymous/Guest visitors inspecting a business before signing up.
- Business users who own the public profile surface.
- Expert users who already use the authenticated flow.

## Affected Product Docs

- `docs/ARCHITECTURE.md` (authorization rules section)
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Non-Goals

- Opening public expert profiles.
- Switching to a new DTO for Business profile.
- Filtering Business profile by `kybStatus`.
- Opening the public list of Business profiles.
- Opening public Business-by-job.
- Narrowing the existing entity response surface (see `design.md` for the
  recorded data-boundary analysis).

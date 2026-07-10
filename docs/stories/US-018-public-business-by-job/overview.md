# Overview

## Current Behavior

`GET /api/v1/profiles/business/by-job/{jobId}` is blocked for unauthenticated
callers by Spring Security, because the route is not in the `permitAll` matcher
set. The service layer already contains the correct business rule in
`ProfileService.businessProfileByJob(Integer jobId)`:

- `OPEN` job -> Business profile readable.
- non-`OPEN` job -> `accessService.requireRole("STAFF","ADMIN","BUSINESS")`.

Anonymous callers receive `401`/`403` before reaching the controller/service.

## Target Behavior

Guest (anonymous) users can call `GET /api/v1/profiles/business/by-job/{jobId}`
without a JWT. The route remains public only for jobs whose status is `OPEN`;
non-`OPEN` jobs continue to follow the existing protected behavior in the
service layer. No service business-rule change is required.

Sibling routes stay as defined by US-017 and existing rules:

- `GET /api/v1/profiles/business/{businessId}` remains public (US-017).
- `GET /api/v1/profiles/business/me` remains private (BUSINESS).
- `GET /api/v1/profiles/business` remains private (STAFF).

## Affected Users

- Anonymous/Guest visitors on the public job detail flow.
- Business users whose company is shown on public job pages.
- Expert users who already use the authenticated flow.

## Affected Product Docs

- `docs/ARCHITECTURE.md` (authorization rules section)
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Non-Goals

- Changing the `businessProfileByJob` service business rule.
- Opening `GET /api/v1/profiles/business/me` or `/business`.
- Opening Business-by-job for non-`OPEN` jobs.
- Adding `fullName` enrichment to `businessProfileByJob` (not in the SPEC; the
  service returns the entity as-is today).
- Opening expert profile routes.

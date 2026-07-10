# Exec Plan

## Goal

Open `GET /api/v1/profiles/business/by-job/{jobId}` to anonymous Guest access for
`OPEN` jobs, without weakening sibling business profile routes and without
changing the existing service-layer `OPEN`-job rule.

## Scope

In scope:

- Update `SecurityConfig` to permit anonymous access only for the
  Business-by-job route, using a regex matcher for the numeric `jobId` segment.
- Add a route-matcher unit test for the new boundary.
- Add service unit tests for `businessProfileByJob` (`OPEN` vs non-`OPEN`) since
  none exist today.
- Sync OpenAPI and Swagger/Postman guides.

Out of scope:

- Changing `ProfileService.businessProfileByJob` business logic.
- Opening `/me`, `/business`, or expert profile routes.
- Opening Business-by-job for non-`OPEN` jobs.
- Adding `fullName` enrichment to the by-job response.

## Risk Classification

Risk flags:

- Auth
- Authorization
- Public contracts
- Existing behavior

Hard gates:

- Auth
- Authorization

## Work Phases

1. Discovery (read SecurityConfig, ProfileService, ProfileController,
   ProfileServiceTest, SPEC US-018).
2. Design (choose a route matcher that cannot leak `/me` or `/business`).
3. Validation planning (matcher unit test + service unit test).
4. Implementation (SecurityConfig only; service unchanged).
5. Verification (unit tests + compile).
6. Harness update (story proof, decision, trace, docs sync).

## Stop Conditions

Pause for human confirmation if:

- The service `OPEN`-job rule is found to be missing or incorrect (it is not;
  it already exists).
- A route-level security test cannot be produced and the route opening cannot be
  bounded safely by matcher alone.
- The product contract needs to change beyond opening the route for `OPEN` jobs.

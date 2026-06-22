# Exec Plan

## Goal

Open `GET /api/v1/profiles/business/{businessId}` to anonymous Guest access
without leaking the sibling routes `/me`, `/by-job/{jobId}`, or `/business`, and
without exposing additional private account data.

## Scope

In scope:

- Update `SecurityConfig` to permit anonymous access only for the
  Business-by-ID route, using a matcher that cannot match `/me`.
- Remove the mandatory role gate from
  `ProfileService.businessProfileById(Integer businessId)`.
- Update `ProfileServiceTest` to reflect the new contract.
- Sync OpenAPI and Swagger/Postman guides.

Out of scope:

- Changing the Business profile response DTO or entity shape.
- Opening expert profile routes.
- Opening Business-by-job or the Business list route.
- Filtering by `kybStatus`.

## Risk Classification

Risk flags:

- Auth
- Authorization
- Public contracts
- Existing behavior
- Weak proof (no lightweight security/controller test exists today)

Hard gates:

- Authorization
- Public contracts

## Work Phases

1. Discovery (read SecurityConfig, ProfileService, ProfileController,
   ProfileServiceTest, BusinessProfileEntity).
2. Design (choose a route matcher that cannot leak `/me`).
3. Validation planning (unit + route-level proof).
4. Implementation (SecurityConfig + ProfileService).
5. Verification (unit tests + compile + route proof if feasible).
6. Harness update (story proof, decision, trace, docs sync).

## Stop Conditions

Pause for human confirmation if:

- The Business profile entity is found to leak email/phone or other private
  account fields (it does not today; only `fullName` is attached).
- A route-level security test cannot be produced and the route opening cannot be
  bounded safely by matcher alone.
- Validation requirements need to be weakened.
- Architecture direction changes (e.g. a new public DTO is required).

# Contract Milestone Sprint Spec

## Overall Scope

This sprint includes the completed stories and the next story to implement:

* US-013: Add duration for milestone and contract milestone snapshot
* US-014: Improve deliverable notification `targetUrl` and metadata
* US-015: Strengthen contract milestone snapshot fields
* US-016: Add regression tests for contract milestone and notification flow
* US-017: Open public business profile for guest access

## Current Completed Baseline

The next model must treat US-013 to US-016 as the completed baseline already present in the repository:

* `duration` and `durationUnit` already exist for milestone and contract milestone.
* Deliverable notification already has improved `targetUrl` and metadata.
* Contract milestone snapshot already has `criteriaSnapshot` and `deliverableExpectation`.
* Regression tests for contract milestone and notification flow have been added.

Do not break the behavior of US-013 to US-016 while implementing US-017.

---

# US-017 - Open Public Business Profile for Guest Access

## User Story

As a Guest user,
I want to view a Business public profile without logging in,
so that I can inspect the company before signing up or interacting further.

As the backend team,
we want to expose only the intended public route,
so that private business routes remain protected.

## Problem

Currently, `GET /api/v1/profiles/business/{businessId}` is still blocked by:

* Route-level security in `SecurityConfig`
* Service-level role check in `ProfileService.businessProfileById(...)`

However, the business requirement is that the Business public profile should be a surface that Guests can view.

The main risk of this story is opening the route too broadly, which could leak other private routes such as:

* `GET /api/v1/profiles/business/me`
* `GET /api/v1/profiles/business/by-job/{jobId}`
* `GET /api/v1/profiles/business`

## Target Product Contract

After completing US-017:

* Guest users can call `GET /api/v1/profiles/business/{businessId}` without a JWT.
* The endpoint still returns the business profile with `fullName` enriched from the related account.
* `GET /api/v1/profiles/business/me` remains a private route for BUSINESS.
* `GET /api/v1/profiles/business/by-job/{jobId}` keeps its current business rule.
* `GET /api/v1/profiles/business` remains a private route according to the existing authorization rules.
* This story does not open Guest access for expert profiles.

## Public Data Boundary

The Business profile is considered public, but this story must not expose additional private data.

Only the current surface of the route may be preserved:

* Existing business profile fields
* `fullName`

Do not expose additional private data such as:

* `email`
* `phone`
* Other private account fields

If implementation reveals that the entity response is currently leaking sensitive fields, the next model must narrow the response surface or stop and report the issue. It must not open the route blindly.

---

# Required Backend Changes

## 1. SecurityConfig

Allow anonymous access only for this route:

```http
GET /api/v1/profiles/business/{businessId}
```

Notes:

* Do not use an overly broad matcher such as `/api/v1/profiles/business/*` if it could accidentally permit `/me`.
* Prefer an exact matcher for the `businessId` pattern.
* A regex matcher may be needed if the ant matcher is not safe enough.

## 2. ProfileService

Update `businessProfileById(Integer businessId)`:

* Remove the mandatory logged-in role gate.
* Keep the existing `404` logic when the profile is not found.
* Keep the existing `fullName` enrichment logic.

## 3. Contract / Docs Sync

Synchronize the following documents:

* `docs/openapi/openapi-v1.json`
* `docs/swagger-api-overview.md`
* `docs/swagger-api-test-guide.md`
* `docs/postman-api-test-guide.md`
* New story packet for US-017
* Architecture / product wording if it currently says this route requires authentication

## 4. Harness Work Items

Because this story changes the public contract and authorization boundary, the next model must follow the high-risk flow:

* Record intake.
* Create a high-risk story packet.
* Create a new decision record.
* Update durable story proof.
* Record implementation / validation trace.

---

# Validation Expectations

## Unit Proof

Update `ProfileServiceTest` to confirm that:

* `businessProfileById` still returns the profile with `fullName`.
* It returns `404` when the profile does not exist.
* It no longer verifies `requireRole(...)` for this route.

## Route / Security Proof

There must be at least one proof for the security boundary:

* Anonymous `GET /api/v1/profiles/business/{businessId}` is not blocked with `401` by the security chain.
* Anonymous `GET /api/v1/profiles/business/me` is still not public.

If the repository does not have lightweight security/controller tests that can be written quickly, the next model must clearly record this limitation in the evidence / trace. It must not claim proof beyond what was actually run.

## Suggested Commands

For focused unit tests:

```powershell
.\mvnw.cmd -Dtest=ProfileServiceTest test
```

If a security/controller test is added, also run the appropriate command for that test.

---

# Acceptance Criteria

* Guest can successfully call `GET /api/v1/profiles/business/{businessId}` without a JWT.
* The response still includes `fullName`.
* `GET /api/v1/profiles/business/me` is not accidentally opened to the public.
* `GET /api/v1/profiles/business/by-job/{jobId}` is not accidentally opened to the public.
* `404` still works correctly when `businessId` does not exist.
* Docs and OpenAPI are updated according to the new contract.
* Story / decision / proof / trace in the Harness are updated.

---

# Suggested Implementation Stages

## Stage 1 - Story and Decision Scaffolding

* Create a high-risk story for US-017.
* Create a new decision record for Business public profile Guest access.
* Link the affected docs.

## Stage 2 - Narrow Route Opening

* Update `SecurityConfig`.
* Ensure that only the Business-by-ID route is permitted anonymously.
* Re-check `/me` and `/by-job`.

## Stage 3 - Service Adjustment

* Remove the role gate from `ProfileService.businessProfileById(...)`.
* Keep the existing `404` behavior.
* Keep the existing `fullName` enrichment.

## Stage 4 - Tests

* Update `ProfileServiceTest`.
* Add route-level proof if feasible.

## Stage 5 - Contract Sync

* Update OpenAPI.
* Update Swagger / Postman guides.
* Update story / architecture wording if needed.

## Stage 6 - Harness Closeout

* Update story proof flags according to the validation that was actually run.
* Record a clear trace.
* If there is any limitation in security testing, record it clearly in the evidence.

---

# Non-goals

US-017 does not include:

* Opening public expert profiles.
* Switching to a new DTO for Business profile.
* Filtering Business profile by `kybStatus`.
* Opening the public list of Business profiles.
* Opening public Business-by-job.

---

# Handoff Notes for the Next Model

This is an auth / public-contract change, not a harmless small patch.

The biggest risk is using an overly broad matcher that leaks `/me`.

Do not claim “public profile” proof unless there is at least minimal route-level validation.

US-009 is the historical version where this route was authenticated. US-017 is the new story that changes the contract to Guest-public.

---

# US-018 - Open Business-by-Job Profile for Guest Access on Public Jobs

## User Story

As a Guest user,
I want to view the Business profile associated with a public job without logging in,
so that I can inspect the company directly from the public job detail flow.

As the backend team,
we want to open only the intended route and preserve the existing `OPEN`-job rule,
so that unpublished jobs and private business routes remain protected.

## Problem

The current backend already contains the correct service-layer business rule in
`ProfileService.businessProfileByJob(Integer jobId)`:

* if the job is `OPEN`, the Business profile is readable
* if the job is not `OPEN`, access still requires a protected role

However, Spring Security blocks Guests before the controller is reached, because
`GET /api/v1/profiles/business/by-job/{jobId}` does not match the existing
public matcher set. As a result, anonymous callers receive `401` or `403`
before the request can reach `ProfileController` or `ProfileService`.

## Target Product Contract

After completing US-018:

* Guest users can call `GET /api/v1/profiles/business/by-job/{jobId}` without a JWT.
* The route remains public only for jobs whose status is `OPEN`.
* Non-`OPEN` jobs must continue to follow the existing protected behavior in the service layer.
* `GET /api/v1/profiles/business/{businessId}` remains public as defined by US-017.
* `GET /api/v1/profiles/business/me` remains private.
* `GET /api/v1/profiles/business` remains private.

## Required Backend Changes

## 1. SecurityConfig

Add an anonymous `permitAll` matcher only for:

```http
GET /api/v1/profiles/business/by-job/{jobId}
```

Recommended implementation approach:

* use an exact GET matcher for the `/by-job/{numericId}` pattern
* prefer a regex matcher if needed
* do not broaden the existing `/api/v1/profiles/business/...` security surface

Do not use overly broad patterns such as:

```text
/api/v1/profiles/business/**
/api/v1/profiles/business/*
```

because they can accidentally weaken unrelated routes.

## 2. ProfileService

No business-rule change is required if the product requirement is:

* Guest may read Business-by-job only when the job is `OPEN`

The existing service logic in `businessProfileByJob(Integer jobId)` should be preserved:

* `OPEN` job -> public read allowed
* non-`OPEN` job -> protected role required

Only change the service if the product contract itself changes beyond that.

## 3. Docs / Contract Sync

Update:

* `docs/openapi/openapi-v1.json`
* `docs/swagger-api-overview.md`
* `docs/swagger-api-test-guide.md`
* `docs/postman-api-test-guide.md`
* Story packet for US-018
* Architecture wording if it still implies the route is authenticated-only

## Validation Expectations

### Route / Security

At minimum, prove:

* anonymous `GET /api/v1/profiles/business/by-job/{jobId}` is not blocked by Spring Security
* unrelated sibling routes are still protected

### Service Behavior

At minimum, prove:

* `OPEN` job still returns the Business profile
* non-`OPEN` job still requires protected access and is not made public accidentally

## Suggested Commands

If the project only has focused tests available quickly, run the smallest safe
test scope and record any limitation honestly.

Example:

```powershell
.\mvnw.cmd -Dtest=ProfileServiceTest test
```

If a dedicated route matcher or security-boundary test is added, run that test too.

## Acceptance Criteria

* Guest can call `GET /api/v1/profiles/business/by-job/{jobId}` without JWT.
* The route works for jobs in `OPEN` status.
* The route is not unintentionally opened for non-`OPEN` job access beyond the existing service rule.
* `/me` and `/business` stay protected.
* Docs and OpenAPI are synchronized with the final contract.

## Handoff Notes for the Next Model

1. This is primarily a Spring Security route-opening task, not a service rewrite.
2. The existing `businessProfileByJob(...)` logic already contains the intended `OPEN`-job public rule.
3. The main risk is opening the route too broadly and weakening sibling business profile routes.
4. Do not claim full public-route proof unless at least one security-boundary check has actually been run.

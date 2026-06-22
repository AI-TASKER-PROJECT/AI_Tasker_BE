# Contract Milestone Sprint Spec

## Overall Scope



This sprint includes the completed stories and the next story to implement:

* US-013: Add duration for milestone and contract milestone snapshot
* US-014: Improve deliverable notification `targetUrl` and metadata
* US-015: Strengthen contract milestone snapshot fields
* US-016: Add regression tests for contract milestone and notification flow
* US-017: Open public business profile for guest access
* US-018: Open business-by-job profile for guest access on public jobs
* US-022: Preserve and update SoW when editing draft job milestones

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

---

# US-022 - Preserve and Update SoW When Editing Draft Job Milestones

## User Story

As a Business user,
I want to edit milestone content on a draft job without losing the job's SoW persistence state,
so that I can continue refining the draft and still publish it successfully later.

As the backend team,
we want draft-job editing to keep SoW and milestone data consistent in the database,
so that `publish` does not fail with `JOB_MUST_HAVE_AI_SOW` after ordinary draft edits.

## Problem

Current behavior suggests the following failure mode:

1. the client generates or prepares SoW and milestone data
2. the user edits milestone content before publishing
3. the draft job remains editable
4. later, `POST /api/v1/jobs/{jobId}/publish` fails with:

```text
JOB_MUST_HAVE_AI_SOW
```

The backend currently checks publish eligibility by querying persisted SoW rows:

* `sowRepository.findByJobId(jobId).isEmpty()` -> reject publish

This means client-side SoW state is not enough. A draft job must have a real row
in table `sow` for publish to succeed.

The likely product gap is that the repository currently has:

* job creation with SoW persistence
* job publish
* status update
* domain / skill / technology updates

but no clear full-draft update flow that re-persists both:

* edited milestone data
* edited or previously generated SoW data

## Target Product Contract

After completing US-022:

1. Editing a draft job's milestones must not silently break the persisted SoW requirement for publish.
2. A draft job that already had a persisted SoW must still be publishable after milestone edits, unless the user explicitly removes or invalidates SoW data.
3. If the product supports editing a draft job after initial creation, the backend must provide one consistent persistence path for:
   * job core fields
   * SoW
   * milestones
4. `JOB_MUST_HAVE_AI_SOW` should only occur when the draft job genuinely has no persisted SoW row.

## Required Investigation

The next model must first confirm which of these is true in the current product flow:

### Case A

The frontend is editing milestone state locally and later calling a backend flow
that never persists SoW to table `sow`.

### Case B

The backend has an update path for the draft job, but that path updates
milestones without preserving or upserting SoW.

### Case C

The frontend is re-creating or replacing draft job state incorrectly and loses
the original persisted SoW relation.

Do not implement blindly before determining which case matches the real code path.

## Expected Backend Direction

If the product intends draft jobs to remain editable after creation, the likely
backend shape should be:

* a dedicated draft job update endpoint, or
* a draft save/upsert flow

that persists together:

* `jobs`
* `sow`
* `milestones`

The update flow should:

1. verify ownership and `BUSINESS` role
2. allow editing only while the job is still in editable draft state
3. upsert the `sow` row by `jobId` instead of requiring only create-time insert
4. replace or update milestones safely for the same draft job

## Data Rules

For a draft job update flow:

* `sow` remains a single row per `jobId`
* milestone edits before contract creation are allowed
* once milestones belong to a contract, milestone editing remains blocked
* publish still requires a persisted SoW row

## Suggested Backend Changes

### 1. Marketplace API surface

Investigate whether the repo needs a new endpoint such as:

```http
PUT /api/v1/jobs/{jobId}
```

or another existing draft-save route that should be extended.

The contract should make it clear that the draft save operation persists:

* job fields
* SoW
* milestones

### 2. SoW persistence behavior

Current create flow saves SoW only through create-time logic.

The next model should likely introduce an upsert-style behavior for SoW on draft edits:

* if `sow` exists for `jobId`, update it
* otherwise create it

Do not break the unique-per-job rule of the `sow` table.

### 3. Milestone edit behavior

Milestones sent by the Business before contract creation should still be stored
as the authoritative editable draft milestone set.

If the draft update flow replaces milestones, it must do so intentionally and
consistently with SoW persistence.

## Validation Expectations

At minimum, prove:

1. create draft job with SoW and milestones
2. edit milestone(s) through the intended draft-edit flow
3. verify the job still has a persisted SoW row
4. publish succeeds without `JOB_MUST_HAVE_AI_SOW`

If possible, also prove the failure case:

* a job with no actual SoW row still fails publish with `JOB_MUST_HAVE_AI_SOW`

## Suggested Commands / Checks

Useful verification steps for the next model:

```sql
select job_id, title, status from jobs order by job_id;
select sow_id, job_id, title from sow order by sow_id;
select milestone_id, job_id, milestone_name, status from milestones order by milestone_id;
```

And a focused code review of:

* `MarketplaceController`
* `MarketplaceService`
* `SowRepository`
* any draft job update flow already present in the repo

## Acceptance Criteria

* Editing draft milestones does not accidentally make publish fail due to missing SoW persistence.
* The chosen draft-save flow persists SoW and milestones consistently.
* `JOB_MUST_HAVE_AI_SOW` remains only a genuine missing-SoW guard.
* Docs and API contract are updated if a new draft update endpoint is introduced.

## Non-goals

US-022 does not automatically include:

* changing publish quota rules
* changing SoW AI generation output format
* changing milestone snapshot rules after contract creation
* changing public job visibility rules

## Handoff Notes for the Next Model

1. This is not primarily a quota bug.
2. This is not primarily a milestone validation bug.
3. The likely root cause is missing SoW persistence across draft edits.
4. Confirm the real frontend/backend save path before choosing the endpoint shape.

# SPEC - US-AUTH-006 Harden Login Lockout Implementation

<<<<<<< HEAD
## Muc Tieu

Sua cac loi logic/security con lai trong implementation lockout/reset-password
hien tai, sau review:
=======
This file is intentionally focused for the next sub-agent.

Read `AGENTS.md` and all Harness-required documents before implementation.
Only implement the story below unless a blocking dependency is discovered.
>>>>>>> feat/week7-be-SOW

1. Failed-login counter phai an toan khi co nhieu request dang nhap sai dong
   thoi.
2. Lockout/security-lock khong duoc phu thuoc vao viec gui email thanh cong.
3. Reset-password token khong bi xoa qua som khi save password that bai.
4. Public API docs va Harness proof phai duoc cap nhat day du truoc khi mark
   implemented.

<<<<<<< HEAD
Day la handoff spec cho mot story fix duy nhat. Khong implement lai toan bo
flow auth lockout tu dau.
=======
# US-032 - Generate Non-Blocking AI SoW Drafts With Assumptions

## Status

planned

## Lane

normal
>>>>>>> feat/week7-be-SOW

## Boi Canh Hien Tai

<<<<<<< HEAD
Implementation hien co cac thanh phan chinh:

- `AuthServiceImpl.login(...)` da tang failed-login counter va tao temporary
  lock/security lock.
- `AccountEntity` da co:
  - `failedLoginAttempts`
  - `lockoutCount`
  - `lockedUntil`
  - `lockReason`
  - `lastFailedLoginAt`
  - `statusBeforeLock`
- Migration `V43__add_login_lockout_and_password_reset.sql` da them cac cot
  lien quan.
- `SecurityEmailService` gui email lockout, security lock, reset password, va
  password changed.
- `AuthController` da co:
  - `POST /api/auth/forgot-password`
  - `POST /api/auth/reset-password`
- Unit test `AuthServiceImplTest` pass theo luong tuan tu.

Review phat hien:

- P1: Failed-login counter co race condition do read-modify-save account row
  khong lock.
- P1: Lockout/security-lock duoc set trong entity nhung email duoc gui truoc
  save; neu SMTP loi thi transaction fail va lock khong duoc persist.
- P2: API docs chua cap nhat day du endpoint moi.
- P2: Harness proof chua du: validation evidence con `TBD`, matrix evidence
  trong, implementation trace truoc do con `partial`.

## User Story

### US-AUTH-006 - Harden Auth Lockout Persistence And Proof

As a platform operator,
I want failed-login lockout state to be persisted atomically and independently
from email delivery,
so that brute-force protection remains reliable under concurrent attacks and
provider failures.

## Scope

In scope:

- Make login failed-attempt updates concurrency-safe.
- Make temporary lock/security lock persist even when email sending fails.
- Make reset-password token consumption safer when DB save or notification
  fails.
- Add or update focused tests for the fixes.
- Update Swagger/OpenAPI-facing docs and manual API test guides for the two new
  endpoints if still missing.
- Fill Harness story validation evidence and durable matrix evidence.
- Record a final completed trace only after proof is current.

Out of scope:

- Changing product thresholds: 5 attempts, 5 minutes, 15-minute reset token TTL.
- Adding CAPTCHA, MFA, IP rate limiting, device fingerprinting.
- Redesigning the existing account schema.
- Replacing Redis reset tokens with JWT or another provider.
- Changing JWT auth behavior.
- Implementing frontend UI.
=======
Make AI job generation return a usable SoW draft immediately from the
information already supplied by the user while still offering one optional
clarification round.

When information is missing, the AI must make reasonable assumptions, place
them in the existing `sow.assumptions` field, and may return one batch of
questions so the user can improve the draft. The questions must never block or
replace the generated SoW.

## Product Decision

The desired interaction is:

1. The user enters the currently required job-generation fields.
2. The backend generates one complete SoW draft and milestone list.
3. Missing details are represented as explicit assumptions.
4. The same response may include one optional batch of clarification questions.
5. The generated draft is returned immediately even when questions exist.
6. The user may use the draft immediately or answer the questions once to
   improve it.
7. After that optional round, the user continues with the latest draft and may
   edit it manually.

There is no blocking or repeated clarification loop.

## Current Problem

The current implementation allows the model to return:

```json
{
  "needMoreInfo": true,
  "questions": ["..."],
  "sow": null,
  "milestones": []
}
```

`AiSowGenerationService.generateSow` then clears `sow` and `milestones` when
`needMoreInfo=true`. This forces the frontend to ask the user for more
information before it can continue.

The endpoint is stateless and the request does not carry clarification history,
so the model may ask for remaining information or repeat a previous question.
The RAG files under `src/main/resources/knowledge/sow` also explicitly tell the
model to ask about missing information.

## Required Behavior

### Generate a draft with optional clarification

- The AI must always attempt to return a complete `sow` and non-empty
  `milestones`.
- Missing information must be resolved through reasonable, domain-appropriate
  assumptions.
- Assumptions must be returned through the existing
  `sow.assumptions: List<String>` field.
- When material information is missing, the successful response may return
  `needMoreInfo=true` and a single batch of at most three concise questions.
- `needMoreInfo=true` is advisory only. It must not mean that generation failed
  or that the user is required to answer.
- When no useful clarification is needed, return `needMoreInfo=false` and
  `questions=[]`.
- The backend must not clear a valid generated `sow` or `milestones`.

### Keep the API contract compatible

Do not add any new request or response fields.

Specifically, do not add:

- `skipClarification`
- `clarificationAnswers`
- `questionHistory`
- `generationMode`
- conversation or session identifiers

Keep the existing request fields:

- `projectTitle`
- `rawRequirement`
- `budget`
- `duration`
- `durationUnit`
- `supportFields`
- `requiredSkills`

Keep the existing response fields:

- `needMoreInfo`
- `questions`
- `sow`
- `milestones`

The existing `needMoreInfo` and `questions` fields carry the optional
clarification batch. They do not control whether `sow` and `milestones` are
returned.

### Normalize assumptions

- If the model returns assumptions, preserve them.
- If the model omits `sow.assumptions` or returns `null`, normalize it to `[]`.
- Do not use one hard-coded assumption list for every job.
- Assumptions must be inferred from the current job domain and requirement.
- Do not silently present uncertain details as confirmed requirements.

### Enforce one optional user-facing round

The implementation must not create a user-facing or recursive question loop.

- The first successful generation may return one question batch alongside the
  complete draft.
- Questions must be limited to information that would materially improve the
  draft and must not repeat information already present in the request.
- The frontend should display the optional batch at most once per job-creation
  attempt.
- If the user answers, the frontend should merge those answers with the full
  original requirement before requesting an improved draft.
- Because this endpoint is stateless and no new round/session field is being
  added, the backend alone cannot know whether a request is the first or second
  user-facing round. The one-round display limit is therefore a consuming-UI
  responsibility.
- After the optional answer round, the frontend must accept the latest returned
  draft and must not start another clarification loop, even if another
  independent generation response contains advisory questions.
- If the first model response contains only questions and does not contain a
  valid SoW/milestone result, the backend may perform at most one internal
  recovery retry. The retry prompt must require a complete draft, assumptions,
  and no more than the same single optional question batch.
- Do not retry recursively or indefinitely.
- If the retry still lacks a valid SoW or milestones, return the existing
  provider/application error style.

The internal recovery retry is not a second user-facing clarification round.

## Prompt Requirements

Update `AiSowGenerationService.buildPrompt` so it clearly instructs the model:

- Always produce the best usable draft from the supplied input.
- Infer missing details conservatively.
- Record every inferred detail in `sow.assumptions`.
- If material information is missing, return at most three concise,
  non-duplicated questions alongside the draft.
- Return `needMoreInfo=true` only when `questions` is non-empty.
- Return `needMoreInfo=false` and `questions=[]` when no useful clarification is
  needed.
- Never omit `sow` or `milestones` because questions exist.
- Preserve the existing rules that prevent milestone guidance from being
  duplicated inside SoW text fields.
- Continue returning valid JSON only.

Do not weaken the current milestone budget and duration normalization.

## RAG Requirements

Update all relevant files under:

```text
src/main/resources/knowledge/sow/
```

Replace instructions such as `Ask missing questions about ...` with guidance
that tells the model to:

- infer reasonable defaults when those details are absent;
- list those inferred details under `sow.assumptions`;
- ask only the highest-impact missing questions, with at most three questions
  in one batch;
- avoid blocking generation or turning the domain checklist into a fixed form.

Domain-specific information may remain as guidance for what the model should
consider, but it must not become a fixed questionnaire.
>>>>>>> feat/week7-be-SOW

## Required Code Changes

<<<<<<< HEAD
### 1. Lock Account Row During Login State Transition

Add a repository method that fetches the account with role and obtains a write
lock for the duration of the login transaction.

Recommended shape:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from AccountEntity a join fetch a.role where lower(a.email) = lower(:email)")
Optional<AccountEntity> findByEmailWithRoleForUpdate(@Param("email") String email);
```

Rules:

- `AuthServiceImpl.login(...)` must use this locked query for email/password
  login.
- Keep existing `findByEmailWithRole(...)` for read-only flows such as current
  session, forgot password, Google login, and other non-mutating lookups unless
  they need a lock.
- The login method must remain transactional so the DB lock is held until the
  state transition is saved.
- Do not lock rows for non-existing emails.

Acceptance criteria:

- Concurrent wrong-password requests for the same existing email cannot lose
  increments through read-modify-save overwrite.
- The 5th wrong password still creates temporary lockout.
- The second 5-attempt cycle still security-locks the account.
- Focused test or code-level assertion verifies `login()` uses the locked
  repository path.

### 2. Persist Lock State Before Email Side Effects

Current risk: if `securityEmailService.sendTemporaryLockoutEmail(...)` or
`sendSecurityLockEmail(...)` throws, the transaction can fail and the lock state
may not be saved.

Required design:

- Split failed-login state mutation from email delivery.
- `handleFailedLogin(...)` should update the account and return an event or
  command describing which email should be sent.
- Save account state before attempting the email side effect.
- Send email in best-effort mode:
  - Catch mail exceptions.
  - Log a warning.
  - Do not rollback the persisted lock state.

Recommended internal model:

```java
enum LoginSecurityEventType {
    NONE,
    TEMPORARY_LOCKOUT,
    SECURITY_LOCK
}
```

or an equivalent private record/object that can carry `resetLink` for the
security lock email.

Acceptance criteria:

- If temporary-lockout email throws, `locked_until` and `lockout_count` are still
  saved.
- If security-lock email throws, `status = Lock`, `lock_reason =
  TOO_MANY_FAILED_LOGIN_ATTEMPTS`, and `status_before_lock` are still saved.
- The client still receives the expected unauthorized response.
- Mail failure is logged or otherwise recorded for operations.

### 3. Make Reset Token Consumption Safer

Current risk: `resetPassword(...)` deletes the Redis token before password save.
If DB save fails after token deletion, user may lose the valid reset link without
the password being changed.

Required design:

- Read token value from Redis.
- Validate and load account.
- Hash and save new password plus unlock/counter changes.
- Delete reset token only after account save succeeds.
- Send password-changed email best-effort after save. Email failure must not
  rollback the password reset.

Optional stronger design:

- Use Redis `GETDEL` or a small atomic consume mechanism only if it can still
  preserve good UX on DB failure. Do not introduce a complex provider-specific
  dependency unless needed.

Acceptance criteria:

- If account save fails, reset token is not deleted.
- If password-changed email fails, password reset remains saved and token is
  deleted after successful save.
- Valid reset token still works once.
- Expired/unknown token is still rejected.
- `ADMIN_LOCKED` accounts are not auto-unlocked by reset password.

### 4. Public API Docs Must Be Current

If not already done, update every public API contract surface that lists auth
routes:

- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json` if this repo treats it as checked-in generated
  API truth
- `README.md` if route inventory changes

Acceptance criteria:

- `POST /api/auth/forgot-password` appears in auth route inventory and test
  guide with sample request.
- `POST /api/auth/reset-password` appears in auth route inventory and test
  guide with sample request.
- Login docs mention lockout behavior at a high level.

## Required Tests

Add or update focused tests. Minimum expected coverage:

1. Login uses the locked account lookup.
2. Temporary-lockout email failure does not prevent account save.
3. Security-lock email failure does not prevent account save.
4. Reset-password save failure does not delete token.
5. Password-changed email failure does not rollback successful password reset.
6. Existing sequential lockout/reset tests still pass.

Run:

```powershell
.\mvnw.cmd -Dtest=AuthServiceImplTest test
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test
```

If a required command cannot run, do not mark the story implemented. Record the
blocker in story validation evidence and trace.

## Harness And Proof Requirements

This story must use the updated implemented-story gate.

Before marking `US-AUTH-006` implemented:

1. Add or update story docs under `docs/stories/high-risk-auth-lockout-reset/`
   or create a focused `US-AUTH-006` story file/folder.
2. Fill `Acceptance Evidence` with exact commands and results. Do not leave
   `TBD`.
3. Update durable matrix evidence with command results.
4. The final implementation trace must have outcome `completed`.
5. Public API docs must include the new auth endpoints, or the story remains
   partial with the doc gap explicitly named.
6. Each acceptance criterion above must map to test proof, manual proof, or an
   explicit blocker.

Suggested durable story row:

```powershell
.\scripts\bin\harness-cli.exe story add --id US-AUTH-006 --title "Harden Auth Lockout Persistence And Proof" --lane high-risk
```

After validation, update with evidence. Example shape:

```powershell
.\scripts\bin\harness-cli.exe story update --id US-AUTH-006 --status implemented --unit 1 --integration 1 --e2e 0 --platform 0 --evidence "<commands and results>"
```

Only run the implemented update after the gate passes.

## Expected Files To Inspect

- `src/main/java/com/aitasker/be/repository/AccountRepository.java`
- `src/main/java/com/aitasker/be/service/auth/AuthServiceImpl.java`
- `src/main/java/com/aitasker/be/service/auth/SecurityEmailService.java`
- `src/main/java/com/aitasker/be/entity/AccountEntity.java`
- `src/test/java/com/aitasker/be/service/auth/AuthServiceImplTest.java`
- `docs/stories/high-risk-auth-lockout-reset/*`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Stop Conditions

Pause and report instead of forcing completion if:

- Pessimistic locking conflicts with the active test/database setup.
- A required public API doc source is generated and cannot be safely edited by
  hand.
- Redis token consumption cannot be made safer without changing provider
  behavior.
- Validation command fails for unrelated existing failures.
=======
At minimum inspect and update:

- `src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java`
- `src/main/java/com/aitasker/be/dto/sow/SowDto.java`
- `src/test/java/com/aitasker/be/service/core/AiSowGenerationServiceTest.java`
- `src/main/resources/knowledge/sow/api-testing.md`
- `src/main/resources/knowledge/sow/bi-dashboard.md`
- `src/main/resources/knowledge/sow/computer-vision.md`
- `src/main/resources/knowledge/sow/customer-support-chatbot.md`
- `src/main/resources/knowledge/sow/data-pipeline.md`
- `src/main/resources/knowledge/sow/generic-ai-project.md`

Update Swagger/test documentation only where its behavior description claims
that generation may stop for clarification. The JSON schema itself should not
change.

## Out of Scope

- Adding new database tables or Flyway migrations.
- Persisting AI generation sessions or conversations.
- Building a multi-turn chatbot flow.
- Adding new API fields.
- Making budget, duration, title, or raw requirement optional.
- Changing job publishing validation.
- Changing job draft persistence.
- Refactoring recommendation, proposal, or contract flows.
- Frontend implementation.

## Required Tests

Add or update focused tests covering at least:

1. The prompt always requires a draft and allows at most three optional
   clarification questions.
2. The prompt still prevents milestone duplication in SoW fields.
3. A valid response with `needMoreInfo=true` keeps its SoW, milestones, and
   optional question batch.
4. Missing or null `sow.assumptions` becomes an empty list.
5. Domain-specific assumptions returned by the model are preserved.
6. More than three model-generated questions are limited to three.
7. A question-only first response triggers no more than one internal recovery
   retry.
8. A failed retry returns an error and does not loop.
9. Budget normalization still produces the requested total.
10. Duration normalization still produces the requested total and unit.

## Validation

Run the focused test:

```powershell
.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test
```

Then run:

```powershell
.\mvnw.cmd -DskipTests compile
```
>>>>>>> feat/week7-be-SOW

If the local Docker/PostgreSQL environment is available, run the full suite:

<<<<<<< HEAD
Read `AGENTS.md` and this `SPEC.md`, then implement **US-AUTH-006 only**.
Preserve the existing lockout/reset-password product behavior while hardening
concurrency, email failure handling, reset-token safety, docs, and proof.
=======
```powershell
docker compose up -d
.\mvnw.cmd test
```

Record the exact result in the Harness story and trace. Do not claim the full
suite passed unless it was actually run.

## Acceptance Criteria

- A successful `POST /api/jobs/generate-sow` always returns a usable SoW and
  non-empty milestones.
- Missing information may produce one advisory batch containing at most three
  questions alongside the generated draft.
- The user is never required to answer the questions before using the draft.
- `needMoreInfo=true` never causes `sow` or `milestones` to be cleared.
- Missing details are visible in the existing `sow.assumptions` list.
- No `skipClarification` or other new field is introduced.
- The backend performs at most one internal recovery retry for a question-only
  model response.
- The spec explicitly assigns the one-user-facing-round limit to the frontend
  because the unchanged endpoint is stateless.
- Existing milestone de-duplication, budget normalization, and duration
  normalization continue to work.
- Focused tests pass.
- No unrelated product behavior is changed.

## Handoff Instruction For The Sub-Agent

Read `AGENTS.md`, the Harness-required documents, and this `SPEC.md`. Implement
US-032 only. Preserve the current public JSON shape, make clarification
optional and non-blocking, return the draft even when questions exist, validate
with the focused test, update the Harness story evidence, and record a trace
before finishing.
>>>>>>> feat/week7-be-SOW

# SPEC - US-AUTH-006 Harden Login Lockout Implementation

## Muc Tieu

Sua cac loi logic/security con lai trong implementation lockout/reset-password
hien tai, sau review:

1. Failed-login counter phai an toan khi co nhieu request dang nhap sai dong
   thoi.
2. Lockout/security-lock khong duoc phu thuoc vao viec gui email thanh cong.
3. Reset-password token khong bi xoa qua som khi save password that bai.
4. Public API docs va Harness proof phai duoc cap nhat day du truoc khi mark
   implemented.

Day la handoff spec cho mot story fix duy nhat. Khong implement lai toan bo
flow auth lockout tu dau.

## Boi Canh Hien Tai

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

## Required Code Changes

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

## Handoff Instruction For Next Model

Read `AGENTS.md` and this `SPEC.md`, then implement **US-AUTH-006 only**.
Preserve the existing lockout/reset-password product behavior while hardening
concurrency, email failure handling, reset-token safety, docs, and proof.

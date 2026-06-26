# Validation - Failed Login Lockout And Password Reset

## Proof Strategy

Unit tests cover all state machine transitions and business logic in isolation. Integration tests verify full HTTP flows with database. Manual Swagger checks confirm API behavior from client perspective.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | 23 cases (see below) |
| Integration | 3 full flows |
| E2E | Manual Swagger verification |
| Platform | N/A |
| Performance | N/A |
| Logs/Audit | Verify audit entries for lockout and reset events |

### Unit Test Cases

1. **Wrong password increments counter**: Login with wrong password → `failed_login_attempts` increments.
2. **5th wrong password sets temporary lockout**: 5 wrong attempts → `locked_until` set, `lockout_count = 1`.
3. **Login during temporary lockout blocked**: Login before `locked_until` → rejected even with correct password.
4. **Correct password after lockout expiry resets counters**: After `locked_until` passes, correct password → `failed_login_attempts = 0`, JWT issued.
5. **Wrong password second cycle triggers security lock**: After first lockout expires, 5 more wrong → `status = Lock`, `lock_reason = TOO_MANY_FAILED_LOGIN_ATTEMPTS`.
6. **Security-locked account cannot login**: `status = Lock` + `lock_reason = TOO_MANY_FAILED` → login rejected.
7. **Forgot password returns same response for existing/non-existing email**: Both return `{ success: true, message: "Neu email ton tai..." }`.
8. **Forgot password creates token in Redis for existing email**: Token exists in Redis with correct TTL.
9. **Forgot password rate-limited**: Second request within 60s returns same success response but skips email.
10. **Reset password rejects expired/unknown token**: Missing/expired token → error response.
11. **Reset password rejects invalid newPassword**: Short password → validation error.
12. **Reset password accepts valid token**: Valid token + good password → password hashed, token deleted, counters reset.
13. **Reset password unlocks TOO_MANY_FAILED account**: Unlock happens, status restored to `status_before_lock`.
14. **Reset password does not unlock ADMIN_LOCKED account**: `lock_reason = ADMIN_LOCKED` → status unchanged.

### Integration Test Cases

1. **Full temporary lockout flow**: POST /api/auth/login with wrong password 5 times → verify locked_until in DB → login blocked → wait/lower lockout → login with correct password succeeds.
2. **Forgot-password creates token and sends email**: POST /api/auth/forgot-password → verify Redis token → verify email sent (capture mail sender).
3. **Reset-password changes password and allows login**: POST /api/auth/reset-password with valid token → login with new password succeeds.

## Fixtures

- Test account: `test-user@example.com`, password `password123`, role `BUSINESS`, status `Approved`.
- Test account locked: same but `status = Lock`, `lock_reason = ADMIN_LOCKED`.
- Redis test instance (same as existing tests).
- Mocked/memory `JavaMailSender` for email capture.
- Time manipulation: use `Clock` or override `locked_until` timestamps directly.

## Commands

```
docker compose up -d
.\mvnw.cmd test -Dtest=AuthServiceImplTest
.\mvnw.cmd test
```

## Acceptance Evidence

All unit tests pass (2026-06-24):

```
.\mvnw.cmd -Dtest=AuthServiceImplTest test
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
```

Including US-AUTH-006 hardening tests:
- login_shouldUseLockedAccountLookup - verifies PESSIMISTIC_WRITE
- temporaryLockoutEmailFailure_shouldStillPersistLockState
- securityLockEmailFailure_shouldStillPersistLockState
- resetPassword_saveFailure_shouldNotDeleteToken
- resetPassword_dbFailure_restoresTokenToOriginalKey
- resetPassword_transactionRollbackAfterMethodReturn_restoresPendingToken
- resetPassword_sameTokenCannotBeUsedTwice
- resetPassword_concurrentClaim_rejectsSecondCaller
- resetPassword_emailFailure_shouldStillPersistPasswordChange
- forgotPassword_emailFailure_shouldStillStoreToken

Full compile passes:
```
.\mvnw.cmd -DskipTests compile
BUILD SUCCESS
```

Full Maven suite passes:
```
.\mvnw.cmd test
Tests run: 147, Failures: 0, Errors: 0, Skipped: 0
```

API docs updated:
- docs/swagger-api-overview.md: added forgot-password and reset-password routes with notes
- docs/swagger-api-test-guide.md: added test instructions with sample bodies
- docs/postman-api-test-guide.md: added test entries with notes
- docs/openapi/openapi-v1.json: added paths and schemas for ForgotPasswordRequest, ResetPasswordRequest, ApiResponseVoid

Integration tests require Docker/Postgres - deferred.

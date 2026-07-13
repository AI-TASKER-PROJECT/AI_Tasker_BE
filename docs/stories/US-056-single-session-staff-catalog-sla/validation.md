# Validation

## Proof Strategy

Focused auth unit tests must prove current tokens work and old tokens are rejected after a newer login. Migration proof must compile with JPA schema validation where possible and be covered by Flyway during the full test suite.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | AuthService refresh rejects stale token version; login increments token version; JWT filter rejects stale token and accepts current token. |
| Integration | Full Maven suite/Flyway validates migration when Docker Postgres is available. |
| E2E | Not required; frontend already responds to `401`. |
| Platform | Not required. |
| Performance | Not required. |
| Logs/Audit | No new audit event expected. |

## Fixtures

- Existing `test@mail.com` auth unit fixture.
- Seeded Staff accounts `staff01@aitasker.local` through `staff10@aitasker.local`.

## Commands

```text
command: .\mvnw.cmd clean "-Dtest=AuthServiceImplTest,JwtAuthenticationFilterTest" test
result: PASS, 30 tests, 0 failures, 0 errors
notes: Clean compile of 232 main sources plus focused auth/filter tests.

command: .\mvnw.cmd "-Dtest=AuthServiceImplTest,JwtAuthenticationFilterTest" test
result: PASS, 30 tests, 0 failures, 0 errors
notes: Same focused suite before clean verification.

command: .\scripts\bin\harness-cli.exe story verify US-056
result: PASS, 30 tests, 0 failures, 0 errors
notes: Story verify command is the focused auth/filter suite.

command: git diff --check
result: PASS
notes: Only LF-to-CRLF working-copy warnings were printed.

command: docker compose ps
result: BLOCKED
notes: Docker Desktop daemon pipe dockerDesktopLinuxEngine was unavailable, so full Docker-backed Maven suite and Flyway migration proof could not run in this environment.
```

## Acceptance Evidence

- `AuthServiceImplTest.refreshToken_withStaleTokenVersion_shouldReject` proves old refresh tokens are rejected after `active_token_version` advances.
- `AuthServiceImplTest.correctPasswordAfterLockoutExpiry_shouldResetCounters` also proves successful login increments `activeTokenVersion`.
- `JwtAuthenticationFilterTest.currentTokenVersion_shouldAuthenticate` proves current-version access tokens authenticate.
- `JwtAuthenticationFilterTest.staleTokenVersion_shouldReturnUnauthorized` proves stale access tokens receive `401`.
- `JwtAuthenticationFilterTest.staleTokenVersion_onAuthEndpoint_shouldContinueAnonymously` preserves `/api/auth/**` behavior for refresh/login requests that carry a stale access-token header.
- Full Flyway migration proof remains pending because Docker/Postgres was unavailable.

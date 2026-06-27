# Validation

## Proof Strategy

Focused unit tests must prove refresh behavior, invalid token type rejection,
locked-account rejection, and existing login lockout behavior. Compile must pass
after the API and DTO changes.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | `AuthServiceImplTest` covers valid refresh token, access-token rejection, locked-account rejection, login lockout regressions. |
| Integration | Not required for this focused backend change; route is public under existing `/api/auth/**` security matcher. |
| E2E | Frontend refresh/retry flow is out of scope. |
| Platform | Compile validates Spring wiring and request DTO/controller signatures. |
| Performance | Not applicable. |
| Logs/Audit | No state-changing audit record is expected for refresh. |

## Fixtures

- Mockito account fixture `test@mail.com` with role `BUSINESS`.
- Mocked JWT service responses for valid and invalid refresh-token paths.

## Commands

```text
command: .\mvnw.cmd -Dtest=AuthServiceImplTest test
result: passed on 2026-06-27, 26 tests, 0 failures, 0 errors
notes: Covers valid refresh token issuing a new access token, access-token/non-refresh rejection, locked-account rejection, and existing login/password-reset regressions.
```

```text
command: .\mvnw.cmd -DskipTests compile
result: passed on 2026-06-27
notes: Confirms Spring/controller/service/DTO code compiles after adding POST /api/auth/refresh and changing default access token TTL.
```

```text
command: .\scripts\bin\harness-cli.exe story verify US-034
result: passed on 2026-06-27
notes: Re-ran the configured AuthServiceImplTest verification through Harness.
```

```text
command: .\mvnw.cmd test
result: passed on 2026-06-27, 164 tests, 0 failures, 0 errors
notes: Full Maven suite passed; Flyway validated 44 migrations against local PostgreSQL.
```

```text
command: Get-Content docs/openapi/openapi-v1.json -Raw | ConvertFrom-Json | Out-Null
result: passed on 2026-06-27
notes: Static OpenAPI snapshot remains valid JSON after adding /api/auth/refresh and RefreshTokenRequest.
```

## Acceptance Evidence

Focused auth validation passed with 26 tests, compile passed, story verification
passed, the full Maven suite passed with 164 tests, and the OpenAPI JSON
snapshot parses successfully. The public API contract is documented in
`README.md`, `docs/swagger-api-overview.md`, `docs/swagger-api-test-guide.md`,
and `docs/openapi/openapi-v1.json`. Completed Harness trace: #46.

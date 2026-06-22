# Validation

## Proof Strategy

Use focused unit tests for service behavior and compile proof for API/controller wiring. Keep Swagger/OpenAPI route count aligned with controller source.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | EXPERT portfolio upload uses `expert-portfolios/accounts/{accountId}`, returns Firebase path, and records audit action against `expert_profiles`. |
| Integration | Not run locally; live Firebase and database are outside the focused unit proof. |
| E2E | Not run; frontend change is out of scope. |
| Platform | Not applicable. |
| Performance | Not applicable. |
| Logs/Audit | Unit test verifies `ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE` record call. |

## Fixtures

- Mock current EXPERT account id `20`.
- Mock expert profile id `2`.
- Mock multipart file and Firebase storage path.

## Commands

```text
.\mvnw.cmd -Dtest=ProfileServiceTest test
.\mvnw.cmd -DskipTests compile
```

## Acceptance Evidence

- `.\mvnw.cmd -Dtest=ProfileServiceTest test` passed on 2026-06-22: 12 tests, 0 failures, 0 errors.
- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-22.
- `.\mvnw.cmd test` passed on 2026-06-22: 70 tests, 0 failures, 0 errors.
- Route check confirmed `/api/v1/profiles/expert/portfolio-file` is present in both `ProfileController` and `docs/openapi/openapi-v1.json`.

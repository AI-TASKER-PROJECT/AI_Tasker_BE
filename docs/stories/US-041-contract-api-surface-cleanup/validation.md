# Validation

## Proof Strategy

Use runtime OpenAPI as the public contract proof, then run focused and full
Maven tests to prove the preserved service behavior still passes.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | `ContractExecutionServiceTest` covers milestone escrow, dispute, termination, settlement, support, and legacy service methods. |
| Integration | Full Maven suite includes existing Spring integration flows and Flyway validation. |
| E2E | Not run; no frontend or Postman automation requested. |
| Platform | Runtime `/v3/api-docs` fetched from Spring Boot on port 8081 with Docker profile. |
| Performance | Not applicable. |
| Logs/Audit | Flyway validated 50 migrations during runtime OpenAPI boot; audit behavior not changed. |

## Fixtures

- Local Docker PostgreSQL on `127.0.0.1:5433`.
- Existing test fixtures and mocks in the Maven test suite.

## Commands

```text
command: .\mvnw.cmd -DskipTests compile
result: pass
notes: Compile succeeded before and after route cleanup.

command: docker compose up -d
result: pass
notes: aitasker-postgres and redis-otp were running.

command: runtime fetch GET http://localhost:8081/v3/api-docs
result: pass
notes: OpenAPI regenerated from Spring Boot docker profile; Flyway validated 50 migrations.

command: OpenAPI count/parse checks
result: pass
notes: total=151; Contract Execution Flow=47; overview rows=151; Swagger sections=151; Postman sections=151; removed-route scan clean.

command: .\mvnw.cmd -Dtest=ContractExecutionServiceTest test
result: pass
notes: 45 tests, 0 failures, 0 errors.

command: .\mvnw.cmd test
result: pass
notes: 195 tests, 0 failures, 0 errors.
```

## Acceptance Evidence

- Public API total reduced from 170 to 151.
- Contract Execution Flow reduced from 66 to 47.
- Runtime OpenAPI no longer includes removed duplicate/legacy/support routes.
- Product docs and generated guides no longer reference removed routes as public
  flow endpoints.
- Service behavior remains covered by passing focused and full Maven tests.

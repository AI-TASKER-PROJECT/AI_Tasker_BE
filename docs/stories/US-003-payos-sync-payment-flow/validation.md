# Validation

## Proof Strategy

Verify that the Java application compiles without PayOS webhook imports and
that existing focused service tests still pass.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Existing contract execution service regression tests. |
| Integration | Full suite requires local PostgreSQL on `127.0.0.1:5433`; not rerun in this slice. |
| E2E | Manual PayOS return/sync flow should be checked with provider credentials. |
| Platform | Not applicable. |
| Performance | Not applicable. |
| Logs/Audit | Confirmed no new audit behavior was introduced. |

## Fixtures

- Existing mocked service tests.
- PayOS provider status sync requires configured PayOS credentials and a real
  or sandbox order code for manual proof.

## Commands

```text
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd -Dtest=ContractExecutionServiceTest test
```

## Acceptance Evidence

- `.\mvnw.cmd -DskipTests compile` passed.
- `.\mvnw.cmd -Dtest=ContractExecutionServiceTest test` passed with 11 tests.

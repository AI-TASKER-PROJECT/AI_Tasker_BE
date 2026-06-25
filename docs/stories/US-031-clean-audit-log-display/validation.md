# Validation

## Proof Strategy

Focused unit tests prove the response mapper returns clean Vietnamese actions,
clean object display labels, and raw debug fields. Compile proves the expanded
service dependencies remain wired.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Action translation, fallback API path mapping, clean business/profile/job/proposal/payment labels, raw field preservation. |
| Integration | Not required; no schema change. |
| E2E | Not run in this backend slice. |
| Platform | Compile. |
| Performance | N/A. |
| Logs/Audit | Verify known historical raw action/entity values map to clean response fields. |

## Fixtures

Mockito repositories with deterministic account, profile, job, proposal,
contract, payment order, wallet transaction, membership purchase, quota usage,
withdrawal, and catalog rows.

## Commands

```text
.\mvnw.cmd clean "-Dtest=AuditLogServiceTest" test
.\mvnw.cmd "-Dtest=AuditLogServiceTest,PaymentWalletServiceTest,AdminServiceTest" test
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd clean "-Dtest=AuditLogServiceTest" test` passed on 2026-06-26:
  4 tests, 0 failures.
- `.\mvnw.cmd "-Dtest=AuditLogServiceTest,PaymentWalletServiceTest,AdminServiceTest" test`
  passed on 2026-06-26: 21 tests, 0 failures.
- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-26.
- `.\mvnw.cmd test` passed on 2026-06-26: 134 tests, 0 failures.

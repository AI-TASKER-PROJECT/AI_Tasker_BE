# Validation

## Proof Strategy

Use source inventory checks plus compile/runtime verification to ensure every
Swagger-visible endpoint is still present, appears under the intended flow tag,
and that the two markdown guides follow the same order.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | N/A unless source changes require new focused tests. |
| Integration | Runtime OpenAPI reflects the intended tag ordering and endpoint grouping. |
| E2E | Manual Swagger UI gut-check for tag order and representative endpoint placement. |
| Platform | `.\mvnw.cmd -DskipTests compile` passes after Swagger annotation/customizer changes. |
| Performance | N/A. |
| Logs/Audit | Harness trace records route inventory and documentation sync. |

## Fixtures

- Local backend with Swagger enabled.
- Existing seed accounts for protected endpoint spot-checks:
  `business@aitasker.local`, `expert@aitasker.local`, `admin@aitasker.local`, `staff@aitasker.local`.

## Commands

```text
.\mvnw.cmd -DskipTests compile
Invoke-RestMethod http://localhost:8081/v3/api-docs
```

## Acceptance Evidence

- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-23.
- Runtime OpenAPI on `http://localhost:8081/v3/api-docs` returned `130` operations.
- Runtime tag order matched exactly:
  `Auth Flow -> Profile Verification Flow -> Job Draft & Publish Flow -> Proposal Flow -> Wallet & Payment Flow -> Contract Execution Flow -> Notification Flow -> Catalog & Reference Flow -> AI & Matching Flow -> Admin & Governance Flow -> System & Test Flow`.
- `docs/swagger-api-test-guide.md` contains `130` endpoint sections and `130` per-endpoint response blocks.
- `docs/swagger-api-overview.md` was regenerated from the same runtime inventory so the docs order matches Swagger tag grouping.

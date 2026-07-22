# Validation

## Proof Strategy

Focused unit tests must prove proportional allocation, exact totals, whole-VND
rounding, deterministic ordering, no negative remainder for very small totals,
and rejection of duplicate indexes. Compile and OpenAPI contract checks must
also pass.

## Commands And Results

```text
command: .\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test
result: PASS - 36 tests, 0 failures, 0 errors, 0 skipped

command: .\mvnw.cmd -DskipTests compile
result: PASS - BUILD SUCCESS

command: PowerShell ConvertFrom-Json assertions for docs/openapi/openapi-v1.json
result: PASS - 186 operations and custom reallocation request/response refs present

command: route inventory counts in Swagger overview/test guide/Postman guide
result: PASS - 186/186/186

command: git diff --check
result: PASS - CRLF normalization warnings only
```

## Integration Scope

Database integration is not required because the endpoint has no persistence,
transaction, repository, or provider call. Runtime OpenAPI may be refreshed
when local PostgreSQL is available; otherwise use the repository's structured
snapshot-update precedent and record the environment limitation.

## Acceptance Evidence

| Behavior | Evidence | Result |
| --- | --- | --- |
| Proportional allocation | 40m/100m references with 110m selection | Pass: 31,428,571 and 78,571,429 VND. |
| Exact total | `allocationTotal == selectedBudget` assertions | Pass. |
| Deterministic mapping | Reversed request order, response asserted by index | Pass: response sorted by `milestoneIndex`. |
| Safe small total | 2 VND across four equal references | Pass: 0, 0, 0, 2; no negative value. |
| Invalid input | Duplicate-index and fractional-VND tests | Pass: rejected with `AppException`; DTO validation covers required/positive/maximum constraints. |
| No provider call | Mockito `verifyNoInteractions(restTemplate)` | Pass. |
| No persistence | Changed-file inspection | Pass: no entity, repository, migration, table, or index change. |
| Public contract | OpenAPI schema/path assertions and three 186-route inventories | Pass. |

Runtime Swagger and the database-backed full suite were not required for this
pure calculation endpoint. Docker Desktop remains unavailable and local
PostgreSQL port 5433 is closed; the OpenAPI snapshot was therefore updated with
the repository's structured JSON transformation precedent.

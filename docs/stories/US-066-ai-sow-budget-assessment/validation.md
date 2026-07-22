# Validation

## Proof Strategy

Focused unit tests must prove provider parsing, independent advisory output,
backend-owned comparison status, safe fallback, and exact totals for both
milestone allocations. Compile and public OpenAPI schemas must also pass.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Prompt contract, formatted price parsing, valid/invalid range normalization, status boundaries, fallback, factor cleanup, exact milestone totals. |
| Integration | Not required because the feature adds no persistence; full suite remains regression proof. |
| E2E | Frontend implementation is out of scope; API schema and handoff table are required. |
| Platform | Runtime OpenAPI refresh when the local application can boot. |
| Performance | No additional provider call; no separate performance gate. |
| Logs/Audit | No new audit record because estimates are stateless and advisory. |

## Fixtures

- Mocked OpenAI responses with valid VND range.
- Mocked responses with formatted text amounts.
- Missing and contradictory range responses.
- Business budgets below, inside, and above the advisory range.

## Commands

```text
command: .\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test
result: PASS - 32 tests, 0 failures, 0 errors, 0 skipped
notes: prompt, parsing, range repair, all status bands, both fallback sources,
       exact Business/recommended milestone totals, and Business-final behavior

command: .\mvnw.cmd -DskipTests compile
result: PASS - BUILD SUCCESS
notes: main source compilation proof

command: .\mvnw.cmd test
result: NOT RUN - local PostgreSQL is unavailable
notes: Docker Desktop daemon pipe is unavailable and TCP 127.0.0.1:5433 is closed;
       integration is not required for this response-only, no-persistence change
```

## Acceptance Evidence

| Check | Command / Evidence | Result |
| --- | --- | --- |
| Focused service tests | `.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test` | Pass: 32 tests, 0 failures/errors. |
| Compile | `.\mvnw.cmd -DskipTests compile` | Pass: `BUILD SUCCESS`. |
| OpenAPI snapshot parses and exposes additions | PowerShell `ConvertFrom-Json` assertions against `docs/openapi/openapi-v1.json` | Pass: 185 operations; `BudgetAssessmentDto`, response reference, `recommendedBudget`, status/source enums present. |
| Frontend handoff | `docs/product/ai-sow-budget-assessment.md` | Pass: request/response/status/source/mapping tables and confirmation flow documented. |
| Database scope | Changed-file inspection | Pass: no entity, repository, migration, table, or index change. |
| Whitespace | `git diff --check` | Pass; Git reports only expected CRLF normalization warnings. |
| Runtime OpenAPI / full suite | `docker compose up -d`; `Test-NetConnection 127.0.0.1 -Port 5433` | Environment blocked: Docker Desktop daemon unavailable and port 5433 closed. |

The checked-in OpenAPI snapshot was updated through a structured JSON
transformation from the current controller/DTO contract because the local
runtime could not boot without PostgreSQL. No database-backed integration proof
is required by this story because the estimate is response-only and stateless.

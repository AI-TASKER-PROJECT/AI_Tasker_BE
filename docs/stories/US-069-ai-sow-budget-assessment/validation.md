# Validation

## Proof Strategy

Focused unit tests must prove provider parsing, full-VND scale repair,
independent advisory output without Business-budget or sample-price prompt
anchoring, backend-owned comparison status, HIGH confirmation semantics,
AI-milestone fallback, invalid-price rejection, and exact totals for both
milestone allocations.
Frontend tests must prove that HIGH hides the advisory card while retaining the
Business amount. Compile, build, and public OpenAPI schemas must also pass.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Prompt has no Business budget or sample price, formatted price parsing, bare-million scale repair, valid/invalid range normalization, status boundaries, HIGH confirmation semantics, AI-milestone fallback, invalid-price rejection, factor cleanup, exact milestone totals. |
| Integration | Not required because the feature adds no persistence; full suite remains regression proof. |
| E2E | HIGH hides the advisory card and implicitly retains Business budget; non-HIGH displays full VND values. |
| Platform | Frontend production build, local browser page/console checks, and runtime OpenAPI refresh when the local application can boot. |
| Performance | No additional provider call; no separate performance gate. |
| Logs/Audit | No new audit record because estimates are stateless and advisory. |

## Fixtures

- Mocked OpenAI responses with valid VND range.
- Mocked responses with formatted text amounts.
- Mocked responses with bare `80/100/130` million shorthand.
- Missing and contradictory range responses.
- Business budgets below, inside, and above the advisory range.

## Commands

```text
command: .\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test
result: PASS on 2026-07-24 - 38 tests, 0 failures, 0 errors, 0 skipped
notes: includes prompt without Business budget/sample prices, preservation of
       distinct AI ranges, invalid-price rejection, scale repair, and exact totals

command: .\mvnw.cmd -DskipTests compile
result: PASS - BUILD SUCCESS
notes: main source compilation proof

command: .\mvnw.cmd test
result: PASS on 2026-07-24 - 393 tests, 0 failures, 0 errors, 0 skipped
notes: PostgreSQL/Flyway-backed integration suite and all backend regressions

command: npm.cmd test
result: PASS on 2026-07-23 - 13 tests, 0 failures
notes: HIGH visibility, implicit Business authority, VND formatting, custom
       allocation, reset, and payload-integrity behavior

command: npm.cmd run build
result: PASS on 2026-07-23
notes: TypeScript and Vite production build completed; existing large-chunk
       warning remains non-blocking

command: npx.cmd eslint src/pages/MarketplacePages/CreateJobPage/CreateJobPage.tsx src/pages/MarketplacePages/CreateJobPage/sowBudget.ts tests/sowBudget.test.mjs
result: PASS on 2026-07-23
notes: no lint findings in changed frontend files

command: Chrome local flow at http://localhost:5173/app/jobs/new
result: PASS on 2026-07-24
notes: authenticated through the normal Business login and generated three real
       SoWs against the updated backend. With the same 1,100,000 VND Business
       input, the AI ranges were 30-50m for a small API, 30-70m for a BI
       platform, and 80-120m for a multi-factory Computer Vision platform.
```

## Acceptance Evidence

| Check | Command / Evidence | Result |
| --- | --- | --- |
| Focused service tests | `.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test` | Pass: 38 tests, 0 failures/errors. |
| Full backend regression | `.\mvnw.cmd test` | Pass: 393 tests, 0 failures/errors. |
| Frontend automated proof | `npm.cmd test`; targeted ESLint; `npm.cmd run build` | Pass: 13 tests, lint clean, production build success. |
| OpenAPI snapshot parses and exposes additions | PowerShell `ConvertFrom-Json` assertions against `docs/openapi/openapi-v1.json` | Description reflects conditional HIGH confirmation; route/schema inventory remains parseable. |
| Frontend handoff | Product guide plus `sowBudget.test.mjs` | Pass: HIGH hidden/implicit keep, full VND formatting, AI read-only, and custom allocation documented/tested. |
| Database scope | Changed-file inspection | Pass: no entity, repository, migration, table, or index change. |
| Rendered Create Job flow | Chrome local flow with the real OpenAI-backed endpoint | Pass: three different project scopes returned 30-50m, 30-70m, and 80-120m VND; screenshots captured. |
| Whitespace | Scoped `git diff --check` for changed backend/frontend files | Pass; only expected CRLF normalization warnings. |

The checked-in OpenAPI snapshot was refreshed from the running application and
its source enum now contains only `AI_ADVISORY` and
`AI_MILESTONE_FALLBACK`. No database change is required because the estimate
remains response-only and stateless. The current rendered target flow passed
through the normal Business login and real OpenAI-backed generation. Harness
intake `#107` and trace `#139` record the implementation and verification.

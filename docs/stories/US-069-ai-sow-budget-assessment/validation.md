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
result: PASS on 2026-07-24 - 18 tests, 0 failures
notes: HIGH visibility, implicit Business authority, VND formatting, custom
       allocation, confirmed-custom allocation retention across milestone edits
       and deletion, manual milestone edits, reset, and payload-integrity behavior

command: npm.cmd run build
result: PASS on 2026-07-24
notes: TypeScript and Vite production build completed; existing large-chunk
       warning remains non-blocking

command: npx.cmd eslint src/pages/MarketplacePages/CreateJobPage/CreateJobPage.tsx src/pages/MarketplacePages/CreateJobPage/sowBudget.ts tests/sowBudget.test.mjs
result: PASS on 2026-07-24
notes: no lint findings in changed frontend files

command: in-app Browser at http://127.0.0.1:5173/app/jobs/new with local mock API
result: PASS on 2026-07-24
notes: generated two AI milestones at 20m/30m, unlocked milestone editing,
       changed them to 25m/40m, observed both inputs enabled while editing,
       confirmed the edit, and verified both values persisted with the Job
       total synchronized to exactly 65m; no application console errors

command: in-app Browser at http://localhost:5173/app/jobs/new with deterministic mock API
result: PASS on 2026-07-24
notes: generated three milestones from a 100m Business budget, confirmed a
       custom 120m budget with allocations 24m/36m/60m, changed the second
       milestone content without losing those values, deleted the first
       milestone, and verified the remaining allocations were recalculated to
       45m/75m while the Job total stayed exactly 120m; no new application
       console errors occurred during this test flow

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
| Frontend automated proof | `npm.cmd test`; targeted ESLint; `npm.cmd run build` | Pass: 18 tests, lint clean, production build success. |
| OpenAPI snapshot parses and exposes additions | PowerShell `ConvertFrom-Json` assertions against `docs/openapi/openapi-v1.json` | Description reflects conditional HIGH confirmation; route/schema inventory remains parseable. |
| Frontend handoff | Product guide plus `sowBudget.test.mjs` | Pass: HIGH hidden/implicit keep, full VND formatting, AI read-only, and custom allocation documented/tested. |
| Database scope | Changed-file inspection | Pass: no entity, repository, migration, table, or index change. |
| Rendered Create Job flow | Chrome local flow with the real OpenAI-backed endpoint | Pass: three different project scopes returned 30-50m, 30-70m, and 80-120m VND; screenshots captured. |
| AI milestone budget edit regression | In-app Browser local flow with deterministic mock API | Pass: generated 20m/30m, unlocked both fields, edited 25m/40m, confirmed, and retained the synchronized 65m Job total; no app console errors. |
| Confirmed custom budget retention | In-app Browser local flow with deterministic mock API | Pass: confirmed 120m as 24m/36m/60m, edited milestone content without rollback, deleted the first milestone, and retained the 120m total as 45m/75m across the remaining milestones; no new app console errors. |
| Whitespace | Scoped `git diff --check` for changed backend/frontend files | Pass; only expected CRLF normalization warnings. |

The checked-in OpenAPI snapshot was refreshed from the running application and
its source enum now contains only `AI_ADVISORY` and
`AI_MILESTONE_FALLBACK`. No database change is required because the estimate
remains response-only and stateless. The current rendered target flow passed
through the normal Business login and real OpenAI-backed generation. Harness
intakes `#107` and `#109` record the implementation scope. Trace `#139`
records the editable milestone budget change; completed trace `#141` records
the confirmed-custom retention regression and its 18-test/browser proof.

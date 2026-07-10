# US-032 Generate Non-Blocking AI SoW Drafts With Assumptions

## Status

implemented

## Lane

normal

## Product Contract

`POST /api/jobs/generate-sow` always returns a usable SoW draft and non-empty
milestones from the information already supplied by the user. Missing details
are resolved through reasonable domain-appropriate assumptions recorded in the
existing `sow.assumptions` list. A single optional batch of at most three concise
clarification questions may accompany the draft; `needMoreInfo=true` is advisory
only and must never cause `sow` or `milestones` to be cleared, nor block the user.

The public JSON request/response shape is unchanged (no new fields such as
`skipClarification`, `clarificationAnswers`, `questionHistory`, or
`generationMode`). The existing `needMoreInfo` and `questions` fields carry the
optional clarification batch.

The one-user-facing-round display limit is a frontend responsibility because the
endpoint is stateless and carries no clarification history.

## Relevant Product Docs

- `docs/ARCHITECTURE.md` (AI, RAG, and file boundaries)
- `src/main/resources/knowledge/sow/*.md` (RAG knowledge)
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Acceptance Criteria

- A successful `POST /api/jobs/generate-sow` always returns a usable SoW and
  non-empty milestones.
- Missing information may produce one advisory batch containing at most three
  questions alongside the generated draft.
- The user is never required to answer the questions before using the draft.
- `needMoreInfo=true` never causes `sow` or `milestones` to be cleared.
- Missing details are visible in the existing `sow.assumptions` list.
- `sow.assumptions` is normalized to `[]` when the model omits or returns null.
- Domain-specific assumptions returned by the model are preserved (no hard-coded
  assumption list for every job).
- Clarification questions are filtered for null, blank, and duplicates
  (case- and whitespace-insensitive) before limiting to three.
- No `skipClarification` or other new request/response field is introduced.
- The backend performs at most one internal recovery retry for a question-only
  model response; a failed retry returns an error and does not loop.
- The one-user-facing-round limit is assigned to the frontend because the
  endpoint is stateless.
- Existing milestone de-duplication, budget normalization, and duration
  normalization continue to work.
- No unrelated product behavior is changed.

## Design Notes

- Commands:
  - `POST /api/jobs/generate-sow` (controller `SowGenerationController`, unchanged
    route/contract).
- Queries: none.
- API: `GenerateSowRequest` / `GenerateSowResponse` / `SowDto` unchanged; no new
  fields. `openapi-v1.json` schema unchanged.
- Tables: none (out of scope).
- Domain rules:
  - `AiSowGenerationService.generateSow` no longer clears `sow`/`milestones` on
    `needMoreInfo=true`.
  - `buildPrompt` requires a complete draft + milestones, instructs the model to
    infer assumptions into `sow.assumptions`, and to return at most three advisory
    questions. `needMoreInfo=true` only when `questions` is non-empty.
  - `buildRecoveryPrompt` is a single internal recovery prompt (not a second
    user-facing round) used only when the first response lacks a valid
    SoW/milestone draft.
  - `limitQuestions` trims each question, drops null/blank entries, dedupes
    (case- and whitespace-insensitive via `LinkedHashSet`), then limits to three.
  - `finalizeResponse` derives `needMoreInfo` from the (filtered) questions list,
    normalizes `sow.assumptions` null -> `[]`, and keeps milestone budget/duration
    normalization.
  - `parseAiResponse` ensures `sow.assumptions` is an array at parse time too.
- UI surfaces: frontend (out of scope) must show the optional batch at most once
  per job-creation attempt and must not restart a clarification loop after the
  optional answer round.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id US-032 --unit 1 --integration 0 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Focused `AiSowGenerationServiceTest` covers prompt, draft retention, assumptions normalization, question filtering/limit, recovery retry (success + fail), milestone budget/duration normalization. |
| Integration | Full Maven suite (requires Docker/Postgres 127.0.0.1:5433). NOT run: Docker daemon not running. |
| E2E | Out of scope (no frontend). |
| Platform | n/a. |
| Release | Full suite deferred until Docker available. |

Verify command (focused):
```powershell
.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test
```

## Harness Delta

- Intake #37 recorded (`Spec slice`, lane `normal`).
- Story US-032 durable record set to `implemented` with unit proof = 1.
- Trace #45 (initial) and #47 (clarification filter refine) recorded.
- No new harness backlog item; no decision record needed (no API shape, auth,
  data, or external-provider boundary change). Docker-unavailable full-suite gap
  recorded in story evidence and trace friction.

## Evidence

```text
.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test -> 24 tests, 0 failures, 0 errors
.\mvnw.cmd -DskipTests compile -> BUILD SUCCESS
scripts/bin/harness-cli story verify US-032 -> pass
```

Full Maven suite NOT run: Docker daemon not running (Postgres 127.0.0.1:5433
unavailable). Per SPEC this is the optional "if available" run, not a hard gate.

Changed files:
- `src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java`
- `src/test/java/com/aitasker/be/service/core/AiSowGenerationServiceTest.java`
- `src/main/resources/knowledge/sow/api-testing.md`
- `src/main/resources/knowledge/sow/bi-dashboard.md`
- `src/main/resources/knowledge/sow/computer-vision.md`
- `src/main/resources/knowledge/sow/customer-support-chatbot.md`
- `src/main/resources/knowledge/sow/data-pipeline.md`
- `src/main/resources/knowledge/sow/generic-ai-project.md`
# Focused Handoff Spec

This file is intentionally focused for the next sub-agent.

Read `AGENTS.md` and all Harness-required documents before implementation.
Only implement the story below unless a blocking dependency is discovered.

---

# US-032 - Generate Non-Blocking AI SoW Drafts With Assumptions

## Status

planned

## Lane

normal

## Goal

Make AI job generation return a usable SoW draft immediately from the
information already supplied by the user while still offering one optional
clarification round.

When information is missing, the AI must make reasonable assumptions, place
them in the existing `sow.assumptions` field, and may return one batch of
questions so the user can improve the draft. The questions must never block or
replace the generated SoW.

## Product Decision

The desired interaction is:

1. The user enters the currently required job-generation fields.
2. The backend generates one complete SoW draft and milestone list.
3. Missing details are represented as explicit assumptions.
4. The same response may include one optional batch of clarification questions.
5. The generated draft is returned immediately even when questions exist.
6. The user may use the draft immediately or answer the questions once to
   improve it.
7. After that optional round, the user continues with the latest draft and may
   edit it manually.

There is no blocking or repeated clarification loop.

## Current Problem

The current implementation allows the model to return:

```json
{
  "needMoreInfo": true,
  "questions": ["..."],
  "sow": null,
  "milestones": []
}
```

`AiSowGenerationService.generateSow` then clears `sow` and `milestones` when
`needMoreInfo=true`. This forces the frontend to ask the user for more
information before it can continue.

The endpoint is stateless and the request does not carry clarification history,
so the model may ask for remaining information or repeat a previous question.
The RAG files under `src/main/resources/knowledge/sow` also explicitly tell the
model to ask about missing information.

## Required Behavior

### Generate a draft with optional clarification

- The AI must always attempt to return a complete `sow` and non-empty
  `milestones`.
- Missing information must be resolved through reasonable, domain-appropriate
  assumptions.
- Assumptions must be returned through the existing
  `sow.assumptions: List<String>` field.
- When material information is missing, the successful response may return
  `needMoreInfo=true` and a single batch of at most three concise questions.
- `needMoreInfo=true` is advisory only. It must not mean that generation failed
  or that the user is required to answer.
- When no useful clarification is needed, return `needMoreInfo=false` and
  `questions=[]`.
- The backend must not clear a valid generated `sow` or `milestones`.

### Keep the API contract compatible

Do not add any new request or response fields.

Specifically, do not add:

- `skipClarification`
- `clarificationAnswers`
- `questionHistory`
- `generationMode`
- conversation or session identifiers

Keep the existing request fields:

- `projectTitle`
- `rawRequirement`
- `budget`
- `duration`
- `durationUnit`
- `supportFields`
- `requiredSkills`

Keep the existing response fields:

- `needMoreInfo`
- `questions`
- `sow`
- `milestones`

The existing `needMoreInfo` and `questions` fields carry the optional
clarification batch. They do not control whether `sow` and `milestones` are
returned.

### Normalize assumptions

- If the model returns assumptions, preserve them.
- If the model omits `sow.assumptions` or returns `null`, normalize it to `[]`.
- Do not use one hard-coded assumption list for every job.
- Assumptions must be inferred from the current job domain and requirement.
- Do not silently present uncertain details as confirmed requirements.

### Enforce one optional user-facing round

The implementation must not create a user-facing or recursive question loop.

- The first successful generation may return one question batch alongside the
  complete draft.
- Questions must be limited to information that would materially improve the
  draft and must not repeat information already present in the request.
- The frontend should display the optional batch at most once per job-creation
  attempt.
- If the user answers, the frontend should merge those answers with the full
  original requirement before requesting an improved draft.
- Because this endpoint is stateless and no new round/session field is being
  added, the backend alone cannot know whether a request is the first or second
  user-facing round. The one-round display limit is therefore a consuming-UI
  responsibility.
- After the optional answer round, the frontend must accept the latest returned
  draft and must not start another clarification loop, even if another
  independent generation response contains advisory questions.
- If the first model response contains only questions and does not contain a
  valid SoW/milestone result, the backend may perform at most one internal
  recovery retry. The retry prompt must require a complete draft, assumptions,
  and no more than the same single optional question batch.
- Do not retry recursively or indefinitely.
- If the retry still lacks a valid SoW or milestones, return the existing
  provider/application error style.

The internal recovery retry is not a second user-facing clarification round.

## Prompt Requirements

Update `AiSowGenerationService.buildPrompt` so it clearly instructs the model:

- Always produce the best usable draft from the supplied input.
- Infer missing details conservatively.
- Record every inferred detail in `sow.assumptions`.
- If material information is missing, return at most three concise,
  non-duplicated questions alongside the draft.
- Return `needMoreInfo=true` only when `questions` is non-empty.
- Return `needMoreInfo=false` and `questions=[]` when no useful clarification is
  needed.
- Never omit `sow` or `milestones` because questions exist.
- Preserve the existing rules that prevent milestone guidance from being
  duplicated inside SoW text fields.
- Continue returning valid JSON only.

Do not weaken the current milestone budget and duration normalization.

## RAG Requirements

Update all relevant files under:

```text
src/main/resources/knowledge/sow/
```

Replace instructions such as `Ask missing questions about ...` with guidance
that tells the model to:

- infer reasonable defaults when those details are absent;
- list those inferred details under `sow.assumptions`;
- ask only the highest-impact missing questions, with at most three questions
  in one batch;
- avoid blocking generation or turning the domain checklist into a fixed form.

Domain-specific information may remain as guidance for what the model should
consider, but it must not become a fixed questionnaire.

## Likely Files

At minimum inspect and update:

- `src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java`
- `src/main/java/com/aitasker/be/dto/sow/SowDto.java`
- `src/test/java/com/aitasker/be/service/core/AiSowGenerationServiceTest.java`
- `src/main/resources/knowledge/sow/api-testing.md`
- `src/main/resources/knowledge/sow/bi-dashboard.md`
- `src/main/resources/knowledge/sow/computer-vision.md`
- `src/main/resources/knowledge/sow/customer-support-chatbot.md`
- `src/main/resources/knowledge/sow/data-pipeline.md`
- `src/main/resources/knowledge/sow/generic-ai-project.md`

Update Swagger/test documentation only where its behavior description claims
that generation may stop for clarification. The JSON schema itself should not
change.

## Out of Scope

- Adding new database tables or Flyway migrations.
- Persisting AI generation sessions or conversations.
- Building a multi-turn chatbot flow.
- Adding new API fields.
- Making budget, duration, title, or raw requirement optional.
- Changing job publishing validation.
- Changing job draft persistence.
- Refactoring recommendation, proposal, or contract flows.
- Frontend implementation.

## Required Tests

Add or update focused tests covering at least:

1. The prompt always requires a draft and allows at most three optional
   clarification questions.
2. The prompt still prevents milestone duplication in SoW fields.
3. A valid response with `needMoreInfo=true` keeps its SoW, milestones, and
   optional question batch.
4. Missing or null `sow.assumptions` becomes an empty list.
5. Domain-specific assumptions returned by the model are preserved.
6. More than three model-generated questions are limited to three.
7. A question-only first response triggers no more than one internal recovery
   retry.
8. A failed retry returns an error and does not loop.
9. Budget normalization still produces the requested total.
10. Duration normalization still produces the requested total and unit.

## Validation

Run the focused test:

```powershell
.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest" test
```

Then run:

```powershell
.\mvnw.cmd -DskipTests compile
```

If the local Docker/PostgreSQL environment is available, run the full suite:

```powershell
docker compose up -d
.\mvnw.cmd test
```

Record the exact result in the Harness story and trace. Do not claim the full
suite passed unless it was actually run.

## Acceptance Criteria

- A successful `POST /api/jobs/generate-sow` always returns a usable SoW and
  non-empty milestones.
- Missing information may produce one advisory batch containing at most three
  questions alongside the generated draft.
- The user is never required to answer the questions before using the draft.
- `needMoreInfo=true` never causes `sow` or `milestones` to be cleared.
- Missing details are visible in the existing `sow.assumptions` list.
- No `skipClarification` or other new field is introduced.
- The backend performs at most one internal recovery retry for a question-only
  model response.
- The spec explicitly assigns the one-user-facing-round limit to the frontend
  because the unchanged endpoint is stateless.
- Existing milestone de-duplication, budget normalization, and duration
  normalization continue to work.
- Focused tests pass.
- No unrelated product behavior is changed.

## Handoff Instruction For The Sub-Agent

Read `AGENTS.md`, the Harness-required documents, and this `SPEC.md`. Implement
US-032 only. Preserve the current public JSON shape, make clarification
optional and non-blocking, return the draft even when questions exist, validate
with the focused test, update the Harness story evidence, and record a trace
before finishing.

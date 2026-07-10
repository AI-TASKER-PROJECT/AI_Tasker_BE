# US-047 Limit SoW Clarification To One Cycle

## Status

implemented

## Lane

normal

## Product Contract

`POST /api/jobs/generate-sow` still returns a usable SoW and milestones even
when information is missing. The AI may ask one optional clarification batch on
the first client cycle only. If the frontend calls generate again after that
first question round, it must send `clarificationAlreadyAsked=true`; the backend
then suppresses any further questions, forces `needMoreInfo=false`, returns
`questions=[]`, and relies on reasonable assumptions in `sow.assumptions`.

## Relevant Product Docs

- `docs/swagger-api-overview.md`
- `docs/stories/US-032-non-blocking-ai-sow-drafts-with-assumptions.md`
- `src/main/java/com/aitasker/be/dto/sow/GenerateSowRequest.java`
- `src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java`

## Acceptance Criteria

- First generate cycle can still return at most three optional questions.
- Request field `clarificationAlreadyAsked=true` means no more user-facing
  questions are returned.
- When `clarificationAlreadyAsked=true`, backend sets `questions=[]` and
  `needMoreInfo=false` even if the model returns questions.
- SoW and milestones are still preserved; missing details are handled as
  assumptions rather than a repeated question loop.
- Prompt tells the model not to return more questions after the first client
  clarification cycle.

## Design Notes

- API: add optional Boolean `GenerateSowRequest.clarificationAlreadyAsked`.
- Service: `AiSowGenerationService.buildPrompt` includes a no-more-questions
  instruction when the flag is true.
- Service: `finalizeResponse` suppresses `questions` and `needMoreInfo` when
  the flag is true, so backend enforces the contract even if the model ignores
  the prompt.
- No database state is added; frontend owns the client-cycle state.

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `AiSowGenerationServiceTest` covers first-cycle questions, no-more-questions prompt, and suppression when the flag is true. |
| Integration | Not required; generate-sow behavior is service-level and no database state is added. |
| E2E | Not run. |
| Platform | Not run. |

## Harness Delta

- Intake #58 recorded.
- Durable story `US-047` added with verify command
  `sh mvnw -Dtest=AiSowGenerationServiceTest test`.

## Evidence

```text
sh mvnw -Dtest=AiSowGenerationServiceTest test
-> Tests run: 27, Failures: 0, Errors: 0, Skipped: 0

scripts/bin/harness-cli story verify US-047
-> pass; sh mvnw -Dtest=AiSowGenerationServiceTest test = 27 tests, 0 failures

jq empty docs/openapi/openapi-v1.json
-> pass

jq '.components.schemas.GenerateSowRequest.properties.clarificationAlreadyAsked' docs/openapi/openapi-v1.json
-> { "type": "boolean" }

sh mvnw test
-> Tests run: 212, Failures: 0, Errors: 0, Skipped: 0
-> Flyway validated 52 migrations, schema public is at version 52
```

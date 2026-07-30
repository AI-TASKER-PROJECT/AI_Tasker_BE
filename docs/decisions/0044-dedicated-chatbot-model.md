# 0044 Dedicated Chatbot Model

## Context

The chatbot used the shared `openai.model` setting through
`AiCompletionService`. After SoW generation and expert recommendation moved to
`gpt-5.6-terra`, changing the shared setting also changed the chatbot even
though its response quality and cost profile are independent.

## Decision

- Add `openai.chatbot-model` with the environment override
  `OPENAI_CHATBOT_MODEL`.
- Default the chatbot model to `gpt-4o-mini`.
- Keep `openai.model` defaulted to `gpt-5.6-terra` for SoW generation and
  expert recommendation.
- Route only `AiCompletionService` through the chatbot-specific model.

## Consequences

- Chatbot and SoW/recommendation models can be changed independently.
- Existing deployments need no new environment variable because both
  properties have defaults.
- Operators can change only the chatbot by setting `OPENAI_CHATBOT_MODEL` and
  restarting the backend.

## Validation

- Focused chatbot, SoW, and expert recommendation suite: 54 tests passed.
- The chatbot request-body test confirms `gpt-4o-mini` while the shared model
  remains `gpt-5.6-terra`.
- Full suite: 403 of 404 tests passed. The only failure is the pre-existing
  local database fixture mismatch in `CoherentDemoDataMigrationTest` (expected
  31 accounts, found 32).

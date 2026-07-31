# 0043 Default OpenAI Model GPT-5.6 Terra

Chatbot-specific routing in this decision was superseded by decision 0044.

## Context

The backend used `gpt-4o-mini` as the shared default for SoW generation,
expert recommendation, and Responses API completion. A live comparison on
representative SoW requests showed that GPT-5.6 Terra produced more complete
scope coverage, more measurable acceptance criteria, and more explicit
assumptions and exclusions. It was slower and has a higher token price.

Changing only `OPENAI_MODEL` was not sufficient. The Chat Completions request
builders sent `temperature=0.1`, while GPT-5.6 Terra accepts only its default
temperature and rejected those requests with HTTP 400.

## Decision

- Use `gpt-5.6-terra` as the default `openai.model` for SoW generation and
  expert recommendation.
- Keep `OPENAI_MODEL` as an environment override.
- Omit custom `temperature` for GPT-5.6 models in SoW generation and expert
  recommendation requests.
- Preserve strict Structured Outputs and all backend validation.
- Keep Business-entered money authoritative; model-generated budget remains
  advisory.

## Alternatives

1. Keep `gpt-4o-mini`. Rejected because its generated acceptance criteria were
   materially less measurable and its advisory estimates were consistently
   too low in the evaluated cases.
2. Use the `gpt-5.6` alias for GPT-5.6 Sol. Rejected as the default because
   Terra provides the intended quality/cost balance for routine production
   SoW generation.
3. Configure separate models per AI feature. Initially deferred, then adopted
   for the chatbot in decision 0044.

## Consequences

- Generated SoWs should be more detailed and testable.
- AI latency and token cost will increase and must be monitored.
- Deployments that explicitly set `OPENAI_MODEL` continue to override the
  default.
- Future GPT-5.6 migrations must not reintroduce unsupported custom
  temperature values.

## Validation

- Focused SoW and expert recommendation suite: 50 tests passed.
- Live default-model smoke test generated a valid one-week SoW with one
  one-week milestone and a 14,000,000 VND advisory recommendation.

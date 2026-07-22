# Exec Plan

## Goal

Return a safe advisory budget comparison and recommended milestone allocation
from AI SoW generation while preserving Business price authority.

## Scope

In scope:

- Additive response DTO fields.
- Prompt, parsing, normalization, fallback, and comparison behavior.
- Focused unit tests.
- OpenAPI, Swagger/Postman, product, and frontend handoff documentation.

Out of scope:

- Database changes.
- Historical estimate calibration.
- Frontend source implementation.
- Proposal and contract pricing changes.

## Risk Classification

Risk flags:

- External systems.
- Public contracts.
- Existing behavior.
- Weak proof until provider responses are normalized and tested.

Hard gates:

- External OpenAI provider behavior.

## Work Phases

1. Confirm existing SoW budget normalization and contract authority.
2. Record decision 0040 and define the additive response contract.
3. Implement DTO, prompt, parser, normalization, and fallback behavior.
4. Add focused provider-response tests and documentation.
5. Run focused tests, compile, full suite where available, and refresh OpenAPI.
6. Record Harness proof and final trace.

## Stop Conditions

Pause for human confirmation if:

- The implementation would need to persist estimates.
- Business budget authority would be weakened.
- Proposal or contract pricing would need to change.
- Required validation must be weakened.

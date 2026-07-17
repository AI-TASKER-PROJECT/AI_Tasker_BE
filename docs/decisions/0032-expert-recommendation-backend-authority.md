# 0032 Expert Recommendation Backend Authority

Date: 2026-07-13

## Status

Accepted

## Context

Broad text queries create taxonomy collisions, while provider-generated rank,
score, and evidence make recommendations change when OpenAI is unavailable.
Normalizing all portfolio data now would expand DB and FE risk unnecessarily.

## Decision

Use structured-first requirements and exact numeric taxonomy tokens over the
current portfolio storage for Release A. The backend is the sole authority for
eligibility, identity, score, rank, and evidence. OpenAI is explanation-only.
Defer normalized portfolio joins to Release B.

## Alternatives Considered

1. Keep broad `LIKE` retrieval with Java post-filtering.
2. Let the LLM select or rerank Top 5.
3. Perform full taxonomy normalization in the same release.

## Consequences

Positive:

- Deterministic results with OpenAI on or off.
- No required FE change and limited additive DB impact.
- Exact matching eliminates numeric substring collisions.

Tradeoffs:

- Legacy free-text taxonomy names are ignored unless accompanied by numeric IDs.
- Release B is still needed for enforced relational portfolio taxonomy.

## Follow-Up

- Measure quality using labeled recommendation fixtures before changing weights.
- Design normalized portfolio taxonomy only after Release A data audit.

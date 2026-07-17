# Overview

## Current Behavior

Expert matching uses a small keyword list, repeated broad text `LIKE` queries,
and lets the optional LLM replace backend rank, score, portfolio, and evidence.
Regeneration also resets `business_selected`.

## Target Behavior

Resolve structured job taxonomy first, fill only missing groups from SoW text,
retrieve eligible Experts by exact numeric catalog IDs, calculate deterministic
scores in the backend, and limit AI output to explanation text. Regeneration
preserves an eligible Business-selected Expert.

## Affected Users

- Business users generating or selecting recommendations.
- Experts eligible to appear in recommendations.

## Affected Product Docs

- `docs/product/expert-recommendation.md`
- `SPEC.md`
- `docs/ARCHITECTURE.md`

## Non-Goals

- No frontend contract change.
- No normalized portfolio taxonomy tables in Release A.
- No embeddings or semantic-vector retrieval.

# Exec Plan

## Goal

Make Expert recommendations relevant, deterministic, eligibility-safe, and
provider-independent without changing the existing FE contract.

## Scope

In scope:

- Structured-first requirement resolution.
- Exact-ID candidate retrieval and eligibility filters.
- Deterministic scoring including technology coverage.
- AI explanation-only boundary.
- Selection-preserving regeneration.
- Additive recommendation integrity migration and focused benchmark tests.

Out of scope:

- Portfolio normalization and embeddings.
- New endpoints or response fields.

## Risk Classification

Risk flags: existing behavior, authorization/eligibility, AI boundary,
persistence, migration, multi-domain quality.

Hard gates: focused tests, full tests, fresh PostgreSQL migration proof.

## Work Phases

1. Resolve requirements and candidate eligibility.
2. Implement exact retrieval and scoring.
3. Harden AI and selection persistence.
4. Add migration and benchmark fixtures.
5. Run focused/full/fresh-DB proof and record Harness trace.

## Stop Conditions

Pause if API compatibility must change, migration audit finds dirty production
data, or Release B normalization becomes required for correctness.

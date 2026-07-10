# Overview — US-043

## Current Behavior

The v2 specification covers milestone escrow, progress checkpoints, disputes,
Staff decisions, termination, and closure, but does not contain the newly
reported SLA, feedback, candidate-ranking, and overdue rules.

## Target Behavior

The v2.1 specification includes those backend rules, requires `/api/v1` for all
public routes, keeps Staff as final dispute decision-maker, and excludes every
fixed 10% immediate-termination penalty.

## Affected Users

- Business, Expert, Admin, Staff, and system operators.

## Affected Product Docs

- `SPEC-MILESTONE-DISPUTER.md`
- `docs/product/contract-management.md` during implementation synchronization.

## Non-Goals

- No production code or migration implementation in this story.

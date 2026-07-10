# Overview — US-044

## Current Behavior

SPEC v2.1 incorrectly creates dispute from rejection and excludes a fixed
immediate-termination penalty.

## Target Behavior

Rejection returns work to normal correction; disputes are explicit. Both parties
pre-fund deposits and immediate termination pays 10% of contract value to the
counterparty without Staff review.

## Affected Users

- Business, Expert, Admin, Staff.

## Affected Product Docs

- `SPEC-MILESTONE-DISPUTER.md`
- Decision 0025.

## Non-Goals

- No backend implementation claim.

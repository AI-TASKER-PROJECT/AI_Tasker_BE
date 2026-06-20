# Overview

## Current Behavior

Payment, contract, job, and milestone flows contain mixed-case and obsolete
status values across schema constraints, services, tests, and docs. Contracts
can still be pushed through a negotiation/change-request lifecycle.

## Target Behavior

Wallet ledger rows are always `POSTED`. Contracts use `DRAFT`, `PENDING`,
`ACTIVE`, `COMPLETED`, and `CANCELLED`. Jobs use `DRAFT`, `OPEN`,
`IN_PROGRESS`, and `CLOSED`. Milestones use `PENDING`, `DEPOSITED`,
`IN_PROGRESS`, `UNDER_REVIEW`, `DISPUTED`, and `COMPLETED`.

## Affected Users

- Business users publishing jobs, signing contracts, paying deposits, and
  completing milestones.
- Expert users signing/rejecting contracts and submitting deliverables.
- Admin users resolving deposits and viewing analytics/reviews.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`
- `docs/data-dictionary.md`

## Non-Goals

- Reintroduce the old contract activation endpoint.
- Replace legacy `transactions` status semantics.
- Drop historical change-request tables.

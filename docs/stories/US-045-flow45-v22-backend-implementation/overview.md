# Overview — US-045

## Current Behavior

The backend implements the earlier Flow 4–5 foundation, but contract activation
still depends only on the Business deposit, deliverable rejection automatically
creates a dispute, progress-report requests and structured feedback are absent,
and the v2.2 immediate-termination/deposit rules are not implemented.

## Target Behavior

Implement `SPEC-MILESTONE-DISPUTER.md` v2.2 across additive Flyway schema,
entities, repositories, services, `/api/v1` routes, authorization, wallet
movements, audit evidence, and regression tests.

## Affected Users

- Business
- Expert
- Admin
- Staff

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `SPEC-MILESTONE-DISPUTER.md`
- `docs/decisions/0025-flow45-v22-reject-resubmit-and-dual-deposit-termination.md`

## Non-Goals

- PayOS settlement or bank withdrawal automation.
- Appeal or Admin override after a valid Staff decision.
- Reintroduction of invoices or use of legacy `transactions` for new movement.

# Overview

## Current Behavior

PayOS wallet top-up exposed both a public webhook endpoint and an order-code
sync endpoint. The product direction is to stop using the PayOS webhook path.

## Target Behavior

PayOS wallet top-up payment state is confirmed by active sync through
`POST /api/payments/payos/{orderCode}/sync`. The PayOS public webhook endpoint
is removed from controller and security configuration.

## Affected Users

- Authenticated accounts creating wallet top-up payments.
- Operators reviewing payment behavior and API surface.

## Affected Product Docs

- `docs/ARCHITECTURE.md`

## Non-Goals

- Legacy contract transaction webhook simulation is not changed.
- Escrow debit, payout, refund, and reconciliation behavior are not redesigned.

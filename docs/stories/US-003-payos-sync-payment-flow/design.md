# Design

## Domain Model

No entity or migration change. Existing `payment_order` rows keep provider
metadata, status, amount, and `paid_at`. `wallet_transactions` remains the
idempotency record for wallet top-up posting.

## Application Flow

1. Client creates a top-up order with `POST /api/payments/payos/create`.
2. Client returns from PayOS or asks the backend to sync status by order code.
3. Backend calls PayOS `paymentRequests().get(orderCode)`.
4. When PayOS reports `PAID`, backend validates amount, marks the order paid,
   and posts the wallet top-up once.

## Interface Contract

- Removed: `POST /api/payments/payos/webhook`.
- Kept: `GET /api/payments/payos/return?orderCode=...`.
- Kept: `POST /api/payments/payos/{orderCode}/sync`.

## Data Model

No schema change.

## UI / Platform Impact

Frontend or client code should call the sync endpoint after PayOS return or
from a payment status refresh action.

## Observability

No new audit event is added in this slice. Payment order state and wallet
transaction records remain the main durable evidence.

## Alternatives Considered

1. Keep webhook and sync in parallel. Rejected because the product direction is
   to remove the webhook path and avoid duplicate confirmation routes.

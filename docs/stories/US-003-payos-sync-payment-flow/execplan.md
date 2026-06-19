# Exec Plan

## Goal

Remove the PayOS webhook flow and keep order-code sync as the single PayOS
payment confirmation path.

## Scope

In scope:

- Remove `POST /api/payments/payos/webhook`.
- Remove `PayOSPaymentService.handleWebhook` and webhook-only helpers.
- Remove the public security permit rule for the PayOS webhook route.
- Update architecture notes for the selected flow.

Out of scope:

- Legacy `/api/v1/transactions/{transactionId}/webhook` simulation.
- Database migrations.
- Provider payout, refund, escrow ledger, and dispute fund locking.

## Risk Classification

Risk flags:

- External systems.
- Public contracts.
- Existing behavior.
- Weak proof.

Hard gates:

- External provider behavior.

## Work Phases

1. Discovery of existing PayOS and legacy transaction webhook surfaces.
2. Confirm scope is only PayOS webhook removal.
3. Remove endpoint, service path, and public route permit.
4. Update architecture documentation.
5. Compile and run available focused tests.
6. Record Harness evidence.

## Stop Conditions

Pause for human confirmation if:

- PayOS provider callback support must be retained in parallel.
- Existing clients still depend on `POST /api/payments/payos/webhook`.
- Removing sync access control or changing wallet ledger semantics is required.

# Exec Plan

## Goal

Implement the payment and wallet MVP described by `SPEC-PAYMENT.md` while
preserving the existing PayOS top-up sync flow.

## Scope

In scope:

- Wallet ledger-backed membership and credit purchase.
- User quota creation, grant, consume, and read APIs.
- Job publishing quota and SoW gate.
- Proposal submission quota.
- Premium recommendation visibility.
- Contract security deposit hold and admin refund/resolution.
- Withdrawal request, approval, and rejection.
- Schema, docs, service tests, and Harness proof records.

Out of scope:

- Automatic bank transfer.
- Production-grade provider reconciliation beyond existing PayOS sync.
- Milestone escrow ledger replacement.
- Dispute fund lock and penalties.
- Hot-job package behavior.

## Risk Classification

Risk flags:

- Authorization.
- Data model.
- Audit/security.
- External systems.
- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- Authorization.
- Data migration.
- External provider behavior.
- Audit/security.

## Work Phases

1. Discovery: read Harness docs, payment spec, existing PayOS/wallet/contract
   code, migrations, docs, and tests.
2. Design: record high-risk story and durable decision.
3. Validation planning: add focused service tests around wallet/quota/deposit
   rules and run Maven tests.
4. Implementation: add schema/entities/repositories/services/controllers and
   wire quota/deposit behavior into existing services.
5. Verification: compile/test and update proof matrix.
6. Harness update: update product docs, decision record, story evidence, and
   trace.

## Stop Conditions

Pause for human confirmation if:

- The requested behavior requires deleting existing payment/order data.
- Provider-backed automatic payout or refund is required.
- Validation must be weakened to ship.
- Contract lifecycle must preserve signature-only activation instead of the
  security-deposit gate.

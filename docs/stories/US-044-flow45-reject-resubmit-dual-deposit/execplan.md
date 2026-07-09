# Exec Plan — US-044

## Goal

Correct SPEC v2.1 into v2.2 for reject/resubmit and dual-deposit termination.

## Scope

In scope:

- Non-dispute final rejection and normal resubmission.
- Explicit-only dispute creation.
- Business 20% and Expert 10% contract deposits.
- Immediate termination with 10% counterparty compensation.
- Standard Staff-reviewed termination without the fixed penalty.

Out of scope:

- Backend implementation.
- Migration execution.
- Admin override of Staff decisions.

## Risk Classification

High-risk: financial custody, wallet movement, state machines, authorization,
database schema, and public API.

## Work Phases

1. Record correction and intake.
2. Update financial and state contracts.
3. Update schema, service, API, authorization, and proof requirements.
4. Run static consistency checks.
5. Record decision and trace.

## Stop Conditions

Pause if penalty beneficiary, deposit owner, or Staff authority becomes
ambiguous.

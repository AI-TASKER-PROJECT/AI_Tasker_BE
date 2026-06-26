# Exec Plan

## Goal

Make wallet transaction history and active realtime notification text
presentation-ready in Vietnamese, without losing raw ledger/debug fields.

## Scope

In scope:

- Replace raw wallet transaction API response with a transaction history DTO.
- Enrich contract deposit rows with business, expert, job, and contract names.
- Enrich withdrawal rows with requester, bank, admin, and rejection reason.
- Enrich membership, credit, and PayOS top-up rows with readable Vietnamese
  titles/descriptions.
- Normalize active websocket notification titles/messages to Vietnamese with
  diacritics.
- Normalize new credit-purchase audit action and keep legacy read translation.
- Add focused unit proof.

Out of scope:

- Database migration.
- Historical row rewrite.
- Frontend layout changes.

## Risk Classification

Risk flags:

- Public API response shape.
- Finance ledger display.
- Realtime notifications.
- Audit/security.

Hard gates:

- Public contracts.
- Audit/security.

## Work Phases

1. Inspect wallet ledger, withdrawal, deposit, notification, and audit flows.
2. Design response DTO and lookup strategy.
3. Implement mapper and repository lookups.
4. Normalize active notification/audit text.
5. Add focused unit tests.
6. Run focused and full Maven verification.
7. Update Harness story, decision, trace, and architecture notes.

## Stop Conditions

Pause for human confirmation if:

- The frontend requires the old raw entity response shape unchanged.
- A database migration becomes necessary.
- A transaction type cannot be enriched without ambiguous ownership.

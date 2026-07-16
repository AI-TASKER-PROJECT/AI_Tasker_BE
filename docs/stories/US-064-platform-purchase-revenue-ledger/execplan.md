# Exec Plan

## Goal

Make membership and retail-credit purchases contribute exactly once to the
platform wallet revenue while preserving transactional entitlement grants.

## Scope

In scope:

- Membership, job-post credit, and proposal credit purchase paths.
- Atomic platform revenue ledger credit.
- Admin wallet revenue synchronization for current and historical purchases.
- Partial revenue aggregation index and regression tests.

Out of scope:

- PayOS provider changes.
- Client idempotency-key contract.
- General Admin dashboard revenue-chart redesign.

## Risk Classification

Risk flags:

- Financial ledger mutation.
- Concurrent wallet updates.
- Historical aggregate correction.
- Replay/double-credit risk.

Hard gates:

- Monetary movement and persisted financial reporting.
- PostgreSQL migration and index change.

## Work Phases

1. Trace purchase, ledger, synchronization, and reporting paths.
2. Define paired ledger operation identity and authoritative aggregation.
3. Add regression tests for all three purchase sources and replay safety.
4. Implement service/repository/migration changes.
5. Run compile, focused tests, full tests, migration proof, and diff checks.
6. Update Harness story evidence and final trace.

## Stop Conditions

Pause for human confirmation if:

- Existing product behavior requires top-ups to count as revenue.
- A historical refund flow for package/credit purchases is discovered.
- Correct proof would require deleting or rewriting existing ledger rows.
- API request contracts must change to support client idempotency.

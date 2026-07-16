# Overview

## Current Behavior

Successful membership and retail-credit purchases debit the Business or Expert
wallet and grant the purchased entitlement. The purchase appears in
`wallet_transactions`, but no counter-entry credits the platform wallet.
`SystemWalletService.syncWallet()` rebuilds Admin `totalRevenue` from successful
contract commission fees only, so package and credit revenue is omitted.

## Target Behavior

- Every successful membership or retail-credit purchase posts an atomic buyer
  debit and platform revenue credit with one shared operation key.
- Platform revenue credits are replay-safe per operation key and leg.
- Admin wallet synchronization includes successful contract commission,
  membership, job-post credit, and proposal credit revenue.
- Existing successful purchase debits are included automatically when the Admin
  wallet is synchronized; no synthetic historical balance snapshots are added.
- Platform business-history output continues to show one purchase event rather
  than both accounting legs.

## Affected Users

- Admin viewing platform wallet revenue and finance reporting.
- Business purchasing membership packages or job-post credits.
- Expert purchasing membership packages or proposal credits.

## Affected Product Docs

- `docs/swagger-api-overview.md` only if the response contract changes. This
  story is expected to preserve the existing API contract.

## Non-Goals

- Redesigning PayOS top-up processing.
- Treating wallet top-ups, escrow holds, refunds, or withdrawals as revenue.
- Adding a client-facing idempotency header in this story.
- Reconstructing synthetic historical platform credit rows with unreliable
  `balance_before` and `balance_after` values.

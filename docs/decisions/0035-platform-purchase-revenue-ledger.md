# 0035 Platform Purchase Revenue Ledger

Date: 2026-07-17

## Status

Accepted

## Context

Membership and retail-credit purchases debit user wallets and grant quota but
do not credit the platform wallet. Admin wallet synchronization derives revenue
only from contract commission fees. This makes platform wallet balances omit
realized package and credit revenue even though purchase history exists.

## Decision

New purchases use paired wallet ledger legs under one operation key: purchaser
available debit and platform revenue credit. Admin `totalRevenue` is rebuilt
from successful commission fees plus posted purchaser debits for membership and
credit purchases. Source debits are authoritative for historical purchase
revenue, preventing new platform credit counterparts from being counted twice.

Do not insert synthetic historical platform-credit rows because the legacy
model cannot reconstruct trustworthy historical `balance_before` and
`balance_after` snapshots. Synchronization will persist corrected aggregate
balances from existing source transactions.

## Alternatives Considered

1. Aggregate purchases during sync without new platform credit legs. Rejected
   because new purchases would remain single-entry.
2. Credit Admin without changing sync. Rejected because synchronization would
   overwrite the credit.
3. Backfill synthetic credit rows. Rejected because it would invent historical
   balance snapshots and complicate double-count prevention.

## Consequences

Positive:

- Platform revenue includes commission, package, and retail-credit sources.
- New purchases are represented by balanced, linked ledger events.
- Existing purchases are reflected after the next Admin wallet sync.
- Unique operation identity protects each leg from replay.

Tradeoffs:

- Historical purchases affect aggregate totals without synthetic counterpart
  rows in the raw ledger.
- Client retries are not deduplicated across requests because the current API
  has no client-provided idempotency key.

## Follow-Up

- Consider adding a client idempotency-key contract for purchase commands.
- If package/credit refunds are introduced, subtract them using explicit
  platform revenue reversal legs and include them in synchronization.

# Wallet Ledger Idempotency & Escrow History Consolidation

## Status

planned

## Lane

high-risk (financial ledger, concurrency, data migration, public wallet history)

## Problem Statement

`GET /api/wallet/transactions` currently consolidates withdrawal ledger rows,
but returns milestone escrow and contract-deposit ledger rows individually.
One valid balance transfer creates multiple accounting legs, so users can see
two rows that appear to describe the same action:

- `MILESTONE_ESCROW_DEPOSIT`: debit `AVAILABLE` and hold `ESCROW`;
- `CONTRACT_SECURITY_DEPOSIT_REFUND`: release `ESCROW` and credit `AVAILABLE`;
- equivalent Expert-deposit, milestone-refund, payout, dispute-settlement, and
  termination-settlement operations.

The ledger writer also has no durable idempotency key. Business-state guards
reduce ordinary retries, but they do not provide a database-enforced guarantee
against concurrent or replayed financial commands.

This creates two separate risks:

1. **Presentation duplication:** legitimate accounting legs are exposed as
   multiple user-facing history items.
2. **Write duplication:** the same logical financial operation can insert a
   second set of ledger legs if an unguarded or concurrent path is executed.

## Product Contract

- The ledger remains append-only and retains every accounting leg required to
  reconstruct wallet balances.
- User wallet history returns one consolidated item per logical financial
  operation, not one item per internal ledger leg.
- Retrying the same logical operation must not change a balance or create a
  second set of ledger legs.
- Different legitimate operations for the same contract, milestone, dispute,
  or deposit must remain distinguishable.
- Admin/platform ledger history may continue exposing raw accounting legs for
  audit, but each leg must expose its operation identity.
- Existing withdrawal-history behavior remains unchanged.
- Existing API paths and response DTO shape remain backward compatible unless
  this document explicitly adds an optional field.

## Important Accounting Rule

Do **not** create a unique constraint on only:

```text
reference_type + reference_id + transaction_type
```

A valid transfer intentionally produces two or more rows sharing those fields.
For example, an escrow deposit creates both an `AVAILABLE/DEBIT` leg and an
`ESCROW/HOLD` leg.

Idempotency must identify the logical operation and its individual accounting
leg separately.

## Target Design

### 1. Logical Operation Identity

Add the following nullable fields to `wallet_transactions` through a new
Flyway migration:

```text
operation_key VARCHAR(255)
operation_leg VARCHAR(50)
```

Definitions:

- `operation_key` identifies one logical financial command.
- `operation_leg` identifies one accounting leg within that command.

Create a partial unique index:

```text
UNIQUE (operation_key, operation_leg)
WHERE operation_key IS NOT NULL AND operation_leg IS NOT NULL
```

The index allows multiple legitimate legs while preventing the same leg from
being inserted twice.

### 2. Canonical Operation Keys

Operation keys must be deterministic and built by a single component, not
assembled independently in controllers or services.

Required patterns:

```text
MILESTONE_ESCROW_DEPOSIT:{contractId}:{milestoneId}
MILESTONE_ESCROW_REFUND:{contractId}:{milestoneId}:{settlementSource}
MILESTONE_ESCROW_RELEASE:{contractId}:{milestoneId}:{settlementSource}
CONTRACT_DEPOSIT_HOLD:{depositId}
CONTRACT_DEPOSIT_REFUND:{depositId}:{resolutionType}
CONTRACT_DEPOSIT_PENALTY:{depositId}:{resolutionType}
DISPUTE_SETTLEMENT:{disputeId}
TERMINATION_SETTLEMENT:{terminationRequestId}:{milestoneId}
WITHDRAWAL:{withdrawalId}:{operationType}
```

Required leg names include:

```text
AVAILABLE_DEBIT
AVAILABLE_CREDIT
ESCROW_HOLD
ESCROW_RELEASE
ESCROW_DEBIT
HOLDING_HOLD
HOLDING_RELEASE
HOLDING_DEBIT
```

Keys must use durable database identifiers. Timestamps, random UUIDs generated
per retry, descriptions, localized text, and monetary amounts must not be used
as the idempotency identity.

### 3. Atomic Idempotent Ledger Writer

Refactor `WalletLedgerService` so every financial transfer accepts an
`operationKey` and records all required legs atomically.

Required behavior:

1. Lock the affected wallet row before reading balances.
2. Check whether every expected `(operationKey, operationLeg)` already exists.
3. If all expected legs exist, return the persisted operation result without
   changing balances or inserting rows.
4. If no expected leg exists, apply the balance mutation and insert every leg
   in the same transaction.
5. If only part of an operation exists, throw
   `INCOMPLETE_WALLET_LEDGER_OPERATION`; do not guess, repair, or apply another
   balance mutation inside the request.
6. Rely on the database unique index as the final concurrency guard.
7. Convert a unique-key race into an idempotent read of the committed operation
   when the complete expected leg set exists.

No caller may write escrow/refund ledger rows directly through
`walletTransactionRepository.save()` after the refactor. Metadata enrichment
must be supplied to the atomic writer before persistence.

### 4. Business-State Guards

Database idempotency complements, rather than replaces, domain guards.

- Milestone deposit still requires the locked contract milestone to be
  `PENDING`.
- Milestone release/refund still requires `escrowReleasedAt == null` and must
  persist settlement source fields atomically with ledger changes.
- Contract-deposit refund still requires a locked deposit in `HELD` or the
  explicitly allowed partial-resolution state.
- Completed/refunded operations return the existing durable result when called
  with the same operation key.
- A request that reuses an operation key with a different account, amount,
  reference, transaction type, or expected leg set must fail with
  `WALLET_OPERATION_KEY_CONFLICT`.

Repositories must provide pessimistic-lock queries for contract deposits and
contract milestones used by financial mutation paths.

### 5. Consolidated User Wallet History

Update `listCurrentWalletTransactions()` to group ledger rows by
`operationKey` after preserving the existing withdrawal-specific contract.

For every supported operation:

- return exactly one user-facing `WalletTransactionHistoryResponse`;
- choose the user-visible amount and direction from the economic result, not
  from an arbitrary first row;
- use the earliest leg timestamp as operation creation time;
- expose the existing contract, milestone, dispute, withdrawal, counterparty,
  and settlement metadata;
- never sum internal debit/hold or release/credit legs as though they were two
  separate payments;
- maintain deterministic order by operation timestamp descending, then stable
  transaction ID descending.

Recommended optional response field:

```text
operationKey: string | null
```

It is optional for backward compatibility and allows frontend troubleshooting
without exposing internal balance calculations.

Legacy rows without an operation key must use a conservative read-time grouping
key containing at least:

```text
accountId + referenceType + referenceId + transactionType
```

and a bounded time window. Rows must only be consolidated when their leg types
form one known accounting operation. Ambiguous rows remain separate rather than
being incorrectly merged.

### 6. Existing Data Audit And Repair

Do not delete wallet ledger rows automatically in the schema migration.

Create a repeatable audit/report command that classifies historical data into:

1. valid multi-leg operation;
2. exact duplicate leg;
3. repeated complete operation;
4. incomplete operation;
5. ambiguous legacy rows.

The report must include transaction IDs, account ID, reference, transaction
type, direction, balance type, amount, timestamps, and calculated operation
group.

Repair procedure:

- back up affected rows before mutation;
- run in a transaction with a dry-run mode by default;
- never delete the only leg representing a balance movement;
- recompute the balance chain for every affected wallet;
- abort when persisted wallet balances do not match the reconstructed chain;
- archive removed duplicate IDs and the reason in an audit table or immutable
  repair report;
- require explicit operator confirmation before applying production cleanup.

New operation keys may be backfilled only for unambiguous known leg pairs.
Ambiguous historical records remain nullable and visible to Admin audit.

## Affected Code Paths

At minimum, implementation must audit and migrate:

- milestone escrow deposit;
- milestone approval payout;
- milestone cancellation/termination refund;
- Business and Expert contract-deposit hold;
- standard and immediate-termination deposit refund;
- immediate-termination penalty/compensation;
- dispute settlement;
- termination settlement;
- withdrawal hold/approve/reject;
- wallet top-up and membership/credit purchase callbacks.

No financial writer is assumed safe until its retry and concurrency behavior is
covered explicitly.

## Migration Strategy

1. Add `operation_key` and `operation_leg` as nullable columns.
2. Add lookup indexes supporting operation reads.
3. Backfill only deterministic, unambiguous recent operations.
4. Produce the legacy-data audit report.
5. Resolve exact duplicates through an explicit repair step, not Flyway.
6. Add the partial unique index after the backfill/repair validation proves no
   conflicting rows remain. If deployment requires a single migration release,
   use separate additive and enforcement migrations with an operational gate
   between them.
7. Deploy idempotent writers before making operation identity mandatory.
8. Keep columns nullable until all writers and legacy-data rules are verified.

Never modify a migration that may already have run.

## Acceptance Criteria

| # | Criterion | Required proof |
|---|---|---|
| AC1 | A milestone escrow deposit produces one logical operation containing exactly one `AVAILABLE_DEBIT` and one `ESCROW_HOLD` leg. | PostgreSQL integration test |
| AC2 | Retrying the same milestone deposit returns the existing result and does not change balances or row count. | Service + integration test |
| AC3 | Concurrent calls with the same operation key result in one balance mutation and one complete leg set. | Concurrency integration test |
| AC4 | Standard contract-deposit refund produces one release/credit operation per participant and retry creates no new rows. | Payment wallet integration test |
| AC5 | Dispute and termination settlement preserve all legitimate payout/refund legs while rejecting duplicate legs. | Settlement regression tests |
| AC6 | Reusing an operation key with different financial inputs fails with `WALLET_OPERATION_KEY_CONFLICT`. | Service test |
| AC7 | An incomplete stored leg set fails safely without another balance mutation. | Corruption-safety integration test |
| AC8 | User wallet history returns one item for each escrow deposit/refund logical operation. | History service test |
| AC9 | Withdrawal history remains one item per withdrawal across PENDING, APPROVED, REJECTED, and CANCELLED. | Existing regression tests |
| AC10 | Admin ledger history retains raw legs and exposes operation identity for audit. | Admin history test |
| AC11 | Migration validates on PostgreSQL and the unique index allows different legs but rejects the same operation leg twice. | Migration test |
| AC12 | Dry-run data audit identifies valid, duplicate, repeated, incomplete, and ambiguous groups without changing data. | Audit fixture test |
| AC13 | Repair mode preserves the reconstructed closing balance and records every removed duplicate. | PostgreSQL repair test |
| AC14 | Full Maven suite, story verification, route/OpenAPI validation, and high-risk Harness trace pass. | Release proof |

## Execution Plan

| Step | Area | Action |
|---|---|---|
| 1 | Harness | Create a high-risk story and map every financial writer to proof before editing. |
| 2 | Inventory | Enumerate all ledger writers, expected legs, references, guards, and retry entrypoints. |
| 3 | Data model | Add operation identity columns, repository queries, and additive indexes. |
| 4 | Ledger core | Implement atomic idempotent multi-leg operations and conflict/incomplete-operation handling. |
| 5 | Domain locking | Add locked milestone/deposit reads and keep state changes in the ledger transaction. |
| 6 | Callers | Migrate every affected payment, escrow, refund, withdrawal, dispute, and termination writer. |
| 7 | History | Consolidate known operation legs for user history while retaining raw Admin audit history. |
| 8 | Data audit | Implement dry-run classification and balance-chain verification for existing rows. |
| 9 | Repair | Add separately approved cleanup mode and enforcement migration after audit proof. |
| 10 | Tests | Run focused unit, retry, rollback, concurrency, migration, and PostgreSQL integration tests. |
| 11 | Documentation | Update wallet/escrow flows, data dictionary, Swagger/OpenAPI descriptions, and operator runbook. |
| 12 | Release | Run full suite, story verify, diff checks, and record a detailed high-risk Harness trace. |

## Non-Goals

- Do not collapse or delete legitimate accounting legs from the internal ledger.
- Do not calculate wallet balances from the user-facing consolidated history.
- Do not silently repair ambiguous production records.
- Do not change payout percentages, escrow ownership, settlement authority, or
  withdrawal approval authority.
- Do not redesign wallet UI beyond the response behavior required to remove
  duplicate-looking operations.
- Do not make `referenceType + referenceId + transactionType` uniquely indexed.

## Stop Conditions

Stop implementation and request a decision if:

- historical duplicates cannot be distinguished safely from legitimate legs;
- reconstructed balances disagree with persisted balances;
- an existing caller cannot supply a deterministic operation identity;
- the proposed unique index conflicts with valid production rows;
- compatibility requires changing transaction amounts or directions exposed by
  the public API;
- a repair would delete or rewrite financial history without an auditable backup.

## Completion Gate

This plan is not complete with compile-only or unit-only evidence. Completion
requires Docker-backed PostgreSQL migration proof, concurrent retry proof,
balance-chain verification, focused and full test suites, story verification,
updated product/API/operator documentation, and a detailed high-risk Harness
trace.

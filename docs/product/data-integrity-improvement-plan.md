# Data Integrity Improvement Plan

## Purpose

This document records non-conflicting improvement guidance for two currently
loose persistence areas:

- `expert_recommendations`
- `payment_order`

This is a documentation-only plan. It does not change runtime behavior,
existing migrations, or current table names. Future implementation should add a
new Flyway migration instead of editing historical migrations.

## Non-Conflict Rules

Future schema work should follow these rules to avoid migration and runtime
conflicts:

- Add only new `Vn__...sql` migrations.
- Do not edit `V22__create_payment_order.sql`,
  `V24__create_expert_recommendations.sql`, or
  `V26__wallet_transactions_and_payment_order_wallet_topup.sql`.
- Use `ALTER TABLE ... DROP CONSTRAINT IF EXISTS` only when replacing a known
  check constraint.
- Use new constraint names that have not appeared before.
- Before adding foreign keys, verify existing rows do not contain orphan ids.
- Align numeric types before adding foreign keys. Some current reference ids are
  stored as `BIGINT` while the referenced tables use `INT`/`SERIAL`.
- Prefer nullable foreign keys for optional workflow references.
- Do not rename tables in the same migration that adds constraints.

## expert_recommendations

### Current Shape

Table: `expert_recommendations`

Created by:

- `src/main/resources/db/migration/V24__create_expert_recommendations.sql`

Current columns used as references:

- `job_posting_id`
- `expert_id`
- `portfolio_id`

Current runtime behavior:

- `ExpertRecommendationService` validates the job through `JobRepository`.
- Candidate data is produced by `ExpertCandidateRankingService`.
- Saved recommendations are stored as a snapshot of selected ids, rank, score,
  reason, matched skills, and matched domains.

Current limitation:

- The database does not enforce foreign keys from `expert_recommendations` to
  `jobs`, `expert_profiles`, or `portfolios`.
- `ExpertRecommendationEntity` stores scalar ids only and has no JPA
  relationship mapping.

### Recommended Target

Keep `expert_recommendations` as a snapshot table, but enforce referential
integrity for ids that should always exist.

Recommended foreign keys:

```text
expert_recommendations.job_posting_id -> jobs.job_id
expert_recommendations.expert_id      -> expert_profiles.expert_id
expert_recommendations.portfolio_id   -> portfolios.portfolio_id
```

Recommended delete behavior:

- `job_posting_id`: `ON DELETE CASCADE`
  - Recommendation rows are derived from a job and should disappear when the job
    disappears.
- `expert_id`: `ON DELETE RESTRICT`
  - Prevent deleting an expert profile while recommendations still reference it,
    unless product rules define profile deletion cleanup.
- `portfolio_id`: `ON DELETE SET NULL`
  - Portfolio is useful context but optional in the recommendation response.

Recommended constraints:

```text
rank_position > 0
match_score IS NULL OR (match_score >= 0 AND match_score <= 100)
```

Recommended unique index:

```text
unique(job_posting_id, expert_id)
```

Reason:

- A job should not store duplicate recommendation rows for the same expert after
  each generation pass.

### Safe Future Migration Shape

Use a future migration such as:

```text
V30__expert_recommendations_integrity.sql
```

Suggested order:

1. Audit orphan rows.
2. Align column types if needed.
3. Add check constraints.
4. Add unique index.
5. Add foreign keys.

Do not combine this with payment-order changes unless the release needs both at
the same time.

## payment_order

### Current Shape

Table: `payment_order`

Created and extended by:

- `src/main/resources/db/migration/V22__create_payment_order.sql`
- `src/main/resources/db/migration/V26__wallet_transactions_and_payment_order_wallet_topup.sql`

Current columns used as references:

- `account_id`
- `business_id`
- `job_id`
- `milestone_id`

Current real relationship:

- `wallet_transactions.payment_order_id -> payment_order.id`

Current runtime behavior:

- `PayOSPaymentService.createPayment` creates wallet top-up orders.
- Wallet top-up orders set `account_id`, optional `business_id`, provider
  fields, amount, purpose, and status.
- `WalletLedgerService.postWalletTopup` uses `payment_order.account_id` and
  `payment_order.id` to credit the account wallet and create a wallet
  transaction.

Current limitation:

- `payment_order.account_id`, `business_id`, `job_id`, and `milestone_id` are
  not enforced by database foreign keys.
- `job_id` and `milestone_id` are currently unused by wallet top-up creation,
  but may be needed for future escrow or milestone deposits.
- Table name is singular (`payment_order`) while architecture docs sometimes
  mention `payment_orders`.

### Recommended Target

Keep the existing table name for now to avoid broad rename risk. Improve
integrity through additive constraints and future-purpose rules.

Recommended foreign keys:

```text
payment_order.account_id   -> account.account_id
payment_order.business_id  -> business_profiles.business_id
payment_order.job_id       -> jobs.job_id
payment_order.milestone_id -> milestones.milestone_id
```

Recommended delete behavior:

- `account_id`: `ON DELETE RESTRICT`
  - A payment order is financial evidence and should not silently disappear.
- `business_id`: `ON DELETE SET NULL`
  - Useful business context, but wallet top-up belongs primarily to the account.
- `job_id`: `ON DELETE SET NULL`
  - Optional until milestone-deposit behavior is finalized.
- `milestone_id`: `ON DELETE SET NULL`
  - Optional until escrow/deposit behavior is finalized.

Recommended constraints:

```text
amount > 0
provider IN ('PAYOS')
purpose IN ('WALLET_TOPUP', 'MILESTONE_DEPOSIT')
status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'EXPIRED')
```

Recommended workflow checks:

```text
purpose = 'WALLET_TOPUP'
  -> account_id IS NOT NULL
  -> job_id IS NULL
  -> milestone_id IS NULL

purpose = 'MILESTONE_DEPOSIT'
  -> account_id IS NOT NULL
  -> job_id IS NOT NULL
  -> milestone_id IS NOT NULL
```

Only add the `MILESTONE_DEPOSIT` checks when the service actually creates that
kind of order. Until then, keep the rule documented but not enforced.

Recommended indexes:

```text
idx_payment_order_account_status(account_id, status)
idx_payment_order_business_status(business_id, status)
idx_payment_order_job_milestone(job_id, milestone_id)
```

### Safe Future Migration Shape

Use a future migration such as:

```text
V31__payment_order_integrity.sql
```

Suggested order:

1. Audit orphan rows for `account_id`, `business_id`, `job_id`, and
   `milestone_id`.
2. Confirm whether `job_id` and `milestone_id` are still intentionally nullable.
3. Align column types if needed.
4. Add `amount > 0` and enum-like check constraints.
5. Add `account_id` foreign key first.
6. Add optional foreign keys for `business_id`, `job_id`, and `milestone_id`.
7. Add composite indexes for query paths.

Avoid renaming `payment_order` to `payment_orders` in this migration. If a table
rename is desired, make it a separate high-risk migration with code, docs, and
rollback planning.

## Implementation Priority

Recommended order:

1. `payment_order.account_id` FK and `amount > 0` check.
2. `expert_recommendations` check constraints and unique `(job_posting_id,
   expert_id)`.
3. `expert_recommendations` FKs.
4. Optional `payment_order` FKs for `business_id`, `job_id`, and
   `milestone_id`.
5. Future table naming cleanup only if the team accepts the migration risk.

## Validation Plan For Future Implementation

Before adding constraints:

```sql
-- expert_recommendations orphan checks
SELECT er.*
FROM expert_recommendations er
LEFT JOIN jobs j ON j.job_id = er.job_posting_id
WHERE j.job_id IS NULL;

SELECT er.*
FROM expert_recommendations er
LEFT JOIN expert_profiles ep ON ep.expert_id = er.expert_id
WHERE ep.expert_id IS NULL;

SELECT er.*
FROM expert_recommendations er
LEFT JOIN portfolios p ON p.portfolio_id = er.portfolio_id
WHERE er.portfolio_id IS NOT NULL
  AND p.portfolio_id IS NULL;

-- payment_order orphan checks
SELECT po.*
FROM payment_order po
LEFT JOIN account a ON a.account_id = po.account_id
WHERE po.account_id IS NOT NULL
  AND a.account_id IS NULL;

SELECT po.*
FROM payment_order po
LEFT JOIN business_profiles bp ON bp.business_id = po.business_id
WHERE po.business_id IS NOT NULL
  AND bp.business_id IS NULL;

SELECT po.*
FROM payment_order po
LEFT JOIN jobs j ON j.job_id = po.job_id
WHERE po.job_id IS NOT NULL
  AND j.job_id IS NULL;

SELECT po.*
FROM payment_order po
LEFT JOIN milestones m ON m.milestone_id = po.milestone_id
WHERE po.milestone_id IS NOT NULL
  AND m.milestone_id IS NULL;
```

After adding constraints:

- Run Flyway migration on a local database.
- Run wallet top-up tests.
- Run expert recommendation generation and saved-read tests.
- Run the full backend test suite with PostgreSQL available.


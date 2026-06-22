# Focused Handoff Spec

This file is intentionally trimmed down for the next model.

Only work on the story below unless a blocking dependency is discovered.

---

# US-023 - Update Membership Package Prices

## Goal

Update the current membership package prices to the new low-price test values requested by the user.

This is a pricing/configuration change. The next model should avoid unrelated refactors.

## Current Observed Prices

From the current local database / package seed:

- `BUSINESS_STANDARD` = `200000`
- `BUSINESS_PLUS` = `500000`
- `BUSINESS_PREMIUM` = `1000000`
- `EXPERT_STANDARD` = `150000`
- `EXPERT_PLUS` = `400000`
- `EXPERT_PREMIUM` = `800000`

## Target Prices

Change prices to:

- `BUSINESS_STANDARD` = `200`
- `BUSINESS_PLUS` = `500`
- `BUSINESS_PREMIUM` = `1000`
- `EXPERT_STANDARD` = `100`
- `EXPERT_PLUS` = `200`
- `EXPERT_PREMIUM` = `600`

All values are VND integer-style prices stored in the existing numeric price column.

## Expected Scope

The next model should check and update all relevant sources of truth:

1. Flyway seed / upsert migration for `membership_packages`
2. Any docs or test guides that mention package prices
3. Any tests that assert package price values
4. Local verification query against `membership_packages`

## Likely Files

At minimum inspect:

- `src/main/resources/db/migration/V30__payment_wallet_membership_quota_deposit_withdrawal.sql`
- `src/main/java/com/aitasker/be/entity/MembershipPackageEntity.java`
- any service/test/docs referencing package price values

## Rules

- Do not change package codes.
- Do not change quota values unless a real inconsistency is discovered.
- Do not change wallet credit prices unless explicitly required.
- Do not mix this task with unrelated auth/profile/job fixes.

## Validation

At minimum:

1. run focused checks or tests if price assertions exist
2. verify the package rows after migration / update logic

Suggested DB verification query:

```sql
select package_code, package_name, role_type, price
from membership_packages
order by package_id;
```

## Acceptance Criteria

- Business package prices match:
  - Standard `200`
  - Plus `500`
  - Premium `1000`
- Expert package prices match:
  - Standard `100`
  - Plus `200`
  - Premium `600`
- Docs/tests are updated if they reference old prices.
- No unrelated behavior is changed.

## Handoff Instruction For Next Model

Read `AGENTS.md` and this `SPEC.md`, then implement `US-023` only.

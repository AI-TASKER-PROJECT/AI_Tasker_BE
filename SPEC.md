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

---

# US-024 - Prevent Duplicated Milestone Guidance Inside Generated SoW

## Goal

Fix AI SoW generation so milestone guidance appears only in the structured
`milestones` array and is not repeated inside `sow` text fields.

## Problem Summary

Current behavior can return:

- a correct `milestones` array for frontend milestone suggestion
- plus duplicated milestone-like guidance inside `sow.overview`,
  `sow.scopeOfWork`, or `sow.deliverables`

This makes the generated SoW look like it contains an extra milestone section
even though the actual duplicate is inside the free-text SoW content.

## Likely Root Cause

- Prompt wording asks the model to both write the SoW and split milestones, but
  does not explicitly forbid milestone duplication inside `sow` fields.
- RAG knowledge files include `Recommended milestones`, which the model may
  echo into SoW text.

## Expected Scope

The next model should inspect and update:

1. prompt shaping in `AiSowGenerationService`
2. any response normalization needed after AI JSON parse
3. focused tests for duplicated milestone text inside `sow`

## Likely Files

- `src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java`
- `src/test/java/com/aitasker/be/service/core/AiSowGenerationServiceTest.java`
- `src/main/resources/knowledge/sow/*.md` only if prompt-only mitigation is not enough

## Rules

- Keep the API contract unchanged: `sow` + `milestones` remain in the response.
- Do not remove or weaken valid `milestones` output.
- Do not break `needMoreInfo=true` behavior.
- Prefer prompt-level correction first; add post-parse cleanup only if needed.
- If post-parse cleanup is added, use block-based cleanup only.
- Do not aggressively strip milestone-like words from normal prose in
  `sow.overview`.
- Prefer preserving ambiguous text over removing too much content.
- Avoid unrelated SoW, job draft, or marketplace changes.

## Acceptance Criteria

- Generated response still includes the structured `milestones` array.
- `sow.overview`, `sow.scopeOfWork`, and `sow.deliverables` do not repeat
  milestone guidance already represented in `milestones`.
- The response does not echo `Recommended milestones` from RAG into SoW text.
- Focused tests cover the duplication guard.
- Cleanup logic only removes clearly delimited milestone guidance blocks and
  does not damage normal SoW prose.

## Handoff Instruction For Next Model

Read `AGENTS.md` and this `SPEC.md`, then implement `US-024` only if assigned
to the duplicated-milestone-in-SoW issue.

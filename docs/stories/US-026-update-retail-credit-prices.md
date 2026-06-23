# US-026 Update Retail Credit Prices

## Status

implemented

## Lane

normal

## Product Contract

Retail credit purchase prices must use the low test values:

- Business job-post credit: 100 VND per credit.
- Expert proposal credit: 50 VND per credit.

The runtime settings table and code fallback defaults must agree, so existing
databases and fresh/test contexts calculate the same required wallet amount.

## Relevant Product Docs

- `docs/ARCHITECTURE.md`
- `docs/stories/US-004-payment-wallet-flow/overview.md`

## Acceptance Criteria

- `POST /api/credits/job-post/purchase` charges 100 VND per requested credit when the DB setting is missing or set to the new value.
- `POST /api/credits/proposal/purchase` charges 50 VND per requested credit when the DB setting is missing or set to the new value.
- A Flyway migration updates existing `system_settings` rows for both retail credit prices.
- Unit tests cover the new fallback arithmetic.

## Design Notes

- Service: `PaymentWalletService`
- Table: `system_settings`
- Setting keys:
  - `credit.job_post.price_vnd`
  - `credit.proposal.price_vnd`

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `.\mvnw.cmd -Dtest=PaymentWalletServiceTest test` |
| Integration | Flyway migration is additive and compile validates SQL resource inclusion. |
| E2E | Not required for this scoped price change. |
| Platform | Not required. |
| Release | Not required. |

## Harness Delta

None.

## Evidence

- Changed `PaymentWalletService` retail credit fallback prices to 100 VND for
  Business job-post credits and 50 VND for Expert proposal credits.
- Added Flyway migration `V40__update_retail_credit_prices.sql` to update
  existing `system_settings` rows.
- Focused validation passed on 2026-06-23:
  `.\mvnw.cmd -Dtest=PaymentWalletServiceTest test` -> 10 tests, 0 failures.
- Compile validation passed on 2026-06-23:
  `.\mvnw.cmd -DskipTests compile`.

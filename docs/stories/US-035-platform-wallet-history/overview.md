# Platform Wallet History

## Current Behavior

Admin can read the platform wallet snapshot through `GET /api/v1/admin/wallet`,
but there is no dedicated API that lists platform-wide wallet transaction
history. User wallet history exists at `GET /api/wallet/transactions` and
returns transparent Vietnamese presentation fields for the current account only.

## Target Behavior

Admin can call a separate endpoint for platform wallet history. The response
uses the existing wallet history DTO, keeps technical IDs for reconciliation,
and renders every supported finance event with Vietnamese titles and
descriptions with diacritics.

Supported event families:

- Membership purchases.
- Job-post and proposal credit purchases.
- Contract security deposit hold, refund, and admin resolution.
- Withdrawal hold, approval, and rejection.
- Wallet top-up.

## Affected Users

- Admin.
- Finance/support operators reviewing platform money movement.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Non-Goals

- No schema migration or ledger rewrite.
- No payment-provider payout/refund reconciliation.
- No frontend implementation.

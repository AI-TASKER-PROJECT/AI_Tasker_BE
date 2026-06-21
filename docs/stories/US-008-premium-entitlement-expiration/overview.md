# Overview

## Current Behavior

Premium access is calculated from a display flag and `badge_expired_at`. Buying
a lower-tier package can overwrite the flag and immediately remove Premium
recommendation access even if the Premium package has not expired.

## Target Behavior

Premium entitlement is based only on `premium_expired_at`. Premium purchases
extend that timestamp cumulatively. Standard and Plus purchases do not clear or
shorten it. `/api/users/me/quota` returns the authoritative package/quota view
for frontend display and permission decisions.

## Affected Users

- Business users with Premium recommendation access.
- Business and Expert users buying membership packages.
- Frontend clients reading quota and active package display data.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/data-dictionary.md`
- `docs/swagger-api-overview.md`
- `docs/stories/US-004-payment-wallet-flow/*`

## Non-Goals

- Modify frontend code.
- Change wallet ledger, PayOS, contract deposit, or status enum behavior.
- Store active package code as duplicated state.

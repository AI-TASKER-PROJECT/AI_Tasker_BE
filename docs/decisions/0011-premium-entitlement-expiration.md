# 0011 Premium Entitlement Expiration

Date: 2026-06-21

## Status

Accepted

## Context

Premium recommendation access was calculated from a display flag plus
`badge_expired_at`. Buying a lower-tier package could overwrite the flag and
remove Premium access even while the Premium period should still be active.

## Decision

Use `user_quotas.premium_expired_at` as the source of truth for Premium
entitlement. Buying a Premium package extends `premium_expired_at` from the
current future value when present, or from the current time when expired or
missing. Buying Standard or Plus updates normal badge/quota behavior but never
clears or shortens Premium entitlement.

`GET /api/users/me/quota` returns authoritative package/quota data:

- `premiumActive`
- `premiumExpiredAt`
- `activePackageCode`
- `activePackageName`
- remaining quota and badge fields

The response no longer exposes the old recommendation visibility flag. Active
package display is derived from successful active `membership_purchases` joined
to `membership_packages`, using priority `PREMIUM > PLUS > STANDARD > BASIC`.

## Alternatives Considered

1. Keep using badge expiration plus a boolean flag. Rejected because lower-tier
   purchases can overwrite the flag and break active Premium access.
2. Store active package code on `user_quotas`. Rejected because purchase
   history already has the package intervals needed to derive display state.

## Consequences

Positive:

- Premium permissions no longer disappear when a lower package is purchased.
- Frontend can use one backend endpoint for authoritative package display and
  permission state.
- Historical users are migrated by backfilling `premium_expired_at` from the
  old flag and badge expiration.

Tradeoffs:

- Existing clients must stop reading the removed recommendation visibility
  field.
- Active package display now depends on successful membership purchase rows
  remaining available.

## Follow-Up

- Regenerate checked-in OpenAPI snapshots if frontend tooling consumes them
  directly instead of live `/v3/api-docs`.

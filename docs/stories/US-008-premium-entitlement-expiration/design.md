# Design

## Domain Model

Add `user_quotas.premium_expired_at` and remove the old recommendation
visibility flag. `premium_expired_at` is the only source of truth for Premium
entitlement.

## Application Flow

1. User buys Premium.
2. Backend extends `premium_expired_at` from the current future expiration, or
   from now when missing or expired.
3. User buys Standard or Plus.
4. Backend updates badge/quota behavior but leaves `premium_expired_at`
   untouched.
5. `/api/users/me/quota` calculates `premiumActive` from `premium_expired_at`
   and derives active package display from active successful membership
   purchases.

## Interface Contract

`GET /api/users/me/quota` returns:

- `accountId`
- `jobPostQuotaBalance`
- `proposalQuotaBalance`
- `badgeExpiredAt`
- `premiumExpiredAt`
- `premiumActive`
- `activePackageCode`
- `activePackageName`

The response does not include the old recommendation visibility flag.

## Data Model

Add `V32__premium_expiration_entitlement.sql`. The migration:

- adds `premium_expired_at`
- backfills it from `badge_expired_at` for rows with the old Premium flag set
- drops the old flag
- indexes `premium_expired_at`

## UI / Platform Impact

The frontend must treat `/api/users/me/quota` as the authoritative source for
package, quota, and Premium permission state. Local storage keys such as
`aitasker_active_package` are only cache/display hints and must not be used as
business truth.

## Observability

Membership purchases and quota changes continue to write durable database rows
and audit events through existing service paths.

## Alternatives Considered

1. Keep the boolean and protect it from lower-tier packages. Rejected because
   entitlement is naturally an expiration interval, not a mutable display flag.

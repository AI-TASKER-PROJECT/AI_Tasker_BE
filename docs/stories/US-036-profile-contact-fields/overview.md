# Overview

## Current Behavior

Profile read endpoints return incomplete account-derived contact data. Business
profile reads miss `email` and/or `phone` depending on route, and Expert profile
reads miss `email` on list/current reads and `email`/`phone` on by-id reads.

## Target Behavior

All requested Business and Expert profile read APIs return the contact fields
needed for profile pages, user-info views, and expanded panels:

- Business reads: `fullName`, `email`, `phone`.
- Expert reads: `fullName`, `email`, `phone`, `title`.

## Affected Users

- BUSINESS viewing Expert information.
- EXPERT viewing Business information.
- STAFF reviewing profile lists.
- Guests viewing public Business profile/job Business info.

## Affected Product Docs

- `docs/product/profile-contact-fields.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Non-Goals

- No profile table migration.
- No authorization broadening beyond existing public/private route rules.

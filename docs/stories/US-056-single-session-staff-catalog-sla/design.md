# Design

## Domain Model

`account.active_token_version` is the server-side source of truth for whether a JWT belongs to the current login session. Successful token-issuing authentication increments the version and returns tokens containing the same version claim.

## Application Flow

Login/register/Google auth:

1. Load or create the account.
2. Verify account can receive tokens.
3. Increment `activeTokenVersion`.
4. Persist the account.
5. Issue access and refresh tokens with the new version.

HTTP request authentication:

1. Parse and validate JWT signature/expiry.
2. Extract email and token version.
3. Load account with role.
4. Accept the token only when `token.version == account.activeTokenVersion`.

Refresh:

1. Verify submitted token is a refresh token.
2. Load account.
3. Reject stale version.
4. Return a new access token bound to the same active version.

WebSocket CONNECT follows the same version check as HTTP.

## Interface Contract

No route shape changes. Stale access/refresh tokens now produce `401`.

## Data Model

New migration:

- Adds `account.active_token_version INT NOT NULL DEFAULT 0`.
- Updates `system_settings.default_sla_days` to `3`.
- Updates `domains.description` and `skills.description` to Vietnamese by stable catalog code.
- Inserts 10 Staff accounts and `staffs` rows idempotently.
- Inserts `staff_domains` and `staff_skills` mappings for those Staff rows.

## UI / Platform Impact

Existing frontend `401` handling clears local session and redirects to login. No immediate frontend change is required.

## Observability

No audit log is recorded for token validation failures. Auth error response remains the existing `401` path.

## Alternatives Considered

1. Store individual session rows and JWT IDs. More flexible for device lists, but larger than the requested one-active-session behavior.
2. Store active refresh token hashes. Stronger refresh revocation, but access-token invalidation still needs a server-side version/session check.
3. Redis session key. Fast, but account-table versioning matches current auth-state persistence and survives Redis restarts.

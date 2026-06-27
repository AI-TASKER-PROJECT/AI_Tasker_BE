# Refresh Token Renewal Endpoint

Date: 2026-06-27

## Status

Accepted

## Context

The backend already issues refresh tokens during login/register, but no endpoint
uses them. Frontend sessions therefore expire when the short-lived access token
expires, even though a 7-day refresh token exists.

## Decision

Set the default access token lifetime to 3 hours and add
`POST /api/auth/refresh`. The endpoint accepts only signed, unexpired refresh
tokens with `type=refresh`, reloads the account and current role from the
database, rejects locked/invalid accounts, and returns a new access token while
preserving the submitted refresh token.

## Alternatives Considered

1. Keep the 15-minute access token lifetime and require re-login. Rejected
   because the user requested longer sessions and refresh-token usage.
2. Rotate refresh tokens on every call. Deferred because refresh-token
   persistence/revocation is not part of the current data model.
3. Let refresh tokens authorize protected APIs directly. Rejected because
   refresh tokens are longer-lived and should be constrained to token renewal.

## Consequences

Positive:

- Frontend can recover from access-token expiry without asking the user to log
  in again.
- Account role/status is re-read before issuing the new access token.
- The change does not require a schema migration.

Tradeoffs:

- A stolen refresh token remains usable until expiry because rotation and
  revocation are out of scope.

## Follow-Up

- Add refresh-token rotation and server-side revocation if logout-all-devices or
  suspicious reuse detection becomes a product requirement.

# Single Active Account Session

Date: 2026-07-13

## Status

Accepted

## Context

Access and refresh JWTs were intentionally stateless. That allowed a user to log
in on two machines and keep both machines authenticated until token expiry. The
product now requires one active login session per account: a newer login should
make older access and refresh tokens invalid.

## Decision

Add an `active_token_version` integer to `account`. Every successful token-issuing
authentication increments the account version and issues access/refresh tokens
containing that version claim. HTTP authentication, WebSocket CONNECT, current
session, and refresh-token renewal must reject tokens whose version does not
match the current account row.

## Alternatives Considered

1. Create a full `account_sessions` table with JWT IDs. Rejected for now because
   the current product asks for one active session, not device/session inventory.
2. Store only active refresh token hashes. Rejected because old access tokens
   would remain valid until expiry unless the request filter also checked server
   state.
3. Store the active version in Redis. Rejected because account auth state is
   already persisted in the account table and should survive Redis restarts.

## Consequences

Positive:

- New login invalidates older access and refresh tokens immediately on the next
  protected API, refresh, or WebSocket reconnect.
- The frontend can keep its existing `401` session-clearing behavior.
- The design stays additive and compact.

Tradeoffs:

- Each protected request now performs an account lookup in the JWT filter.
- No device list or per-device revocation is available yet.

## Follow-Up

- Add a logout endpoint or device/session management only if the product needs
  explicit manual revocation beyond "latest login wins".

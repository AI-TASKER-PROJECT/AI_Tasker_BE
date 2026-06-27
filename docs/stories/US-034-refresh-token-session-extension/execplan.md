# Exec Plan

## Goal

Extend authenticated user sessions by increasing access token lifetime and
adding a safe refresh-token endpoint for frontend renewal.

## Scope

In scope:

- Change default access token TTL to 3 hours.
- Add `POST /api/auth/refresh`.
- Validate that the submitted token is signed, unexpired, and has refresh-token
  type.
- Re-check the account in the database before issuing a new access token.
- Update API docs and focused auth tests.

Out of scope:

- Persisting refresh tokens in the database.
- Refresh token rotation or reuse detection.
- Logout/revocation endpoint.
- Frontend changes.

## Risk Classification

Risk flags:

- Auth.
- Public contracts.
- Existing behavior.
- Weak proof unless focused auth tests pass.

Hard gates:

- Auth.

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- Refresh token rotation or server-side revocation becomes required.
- A database migration is needed for session storage.
- Existing login/register token shape must change incompatibly.

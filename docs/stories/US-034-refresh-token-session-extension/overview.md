# Overview

## Current Behavior

Login, register, and Google auth return an access token and a refresh token, but
the backend does not expose a refresh endpoint. The default access token lifetime
is 15 minutes.

## Target Behavior

The default access token lifetime is 3 hours. `POST /api/auth/refresh` accepts a
valid refresh token and returns a new access token for the same account, while
rejecting expired, invalid, non-refresh, or locked-account tokens.

## Affected Users

- BUSINESS, EXPERT, ADMIN, and STAFF users whose frontend sessions need token
  renewal without forcing a login after access token expiry.

## Affected Product Docs

- `README.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/openapi/openapi-v1.json`
- `docs/ARCHITECTURE.md`

## Non-Goals

- Refresh token rotation and server-side refresh token revocation.
- Database-backed session storage.
- Frontend implementation of the retry-on-401 flow.

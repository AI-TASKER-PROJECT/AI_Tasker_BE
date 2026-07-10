# Design

## Domain Model

JWT remains stateless. Access tokens carry the account email and role claim.
Refresh tokens carry the account email plus `type=refresh`.

## Application Flow

`POST /api/auth/login`, registration, and Google auth keep returning both
tokens. `POST /api/auth/refresh` receives a refresh token, extracts the email,
checks signature/expiry, verifies `type=refresh`, loads the current account and
role from the database, then returns a new access token with the existing refresh
token.

Locked accounts are rejected during refresh using the same lock guard used by
login token issuance.

## Interface Contract

Route:

- `POST /api/auth/refresh`

Request:

```json
{
  "refreshToken": "refresh-token-from-login-response"
}
```

Success response:

```json
{
  "success": true,
  "message": "Refresh token success",
  "data": {
    "accessToken": "new-access-token",
    "refreshToken": "same-refresh-token",
    "role": "BUSINESS",
    "accountStatus": "Approved",
    "email": "business@aitasker.local",
    "fullName": "Business User"
  }
}
```

Errors:

- `401` when the refresh token is invalid, expired, not a refresh token, or the
  account is invalid/locked.
- `400` when request validation fails.

## Data Model

No schema change. The endpoint is stateless and does not store refresh token
records.

## UI / Platform Impact

Frontend can call refresh after an access-token `401`, store the new access
token, and retry the original request. Default access token expiry is now 3
hours unless overridden by `APP_JWT_ACCESS_EXPIRATION_MS`.

## Observability

No new audit event is recorded for refresh because it does not change product
state. Invalid/expired token attempts are handled through the existing auth
error path.

## Alternatives Considered

1. Rotate refresh tokens on every refresh. Deferred because the current app has
   no refresh-token persistence or revocation table.
2. Accept refresh tokens on normal protected APIs. Rejected because long-lived
   refresh tokens should only be used to request a new access token.

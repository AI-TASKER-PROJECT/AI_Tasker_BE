# Design - Failed Login Lockout And Password Reset

## Domain Model

### Account Entity Extensions

New columns added to `AccountEntity` (mapped to `account` table):

| Field | Column | Type | Default | Purpose |
|-------|--------|------|---------|---------|
| `failedLoginAttempts` | `failed_login_attempts` | int | 0 | Counter of consecutive wrong passwords |
| `lockoutCount` | `lockout_count` | int | 0 | Number of temporary lockouts triggered |
| `lockedUntil` | `locked_until` | Timestamp | NULL | When temporary lockout expires |
| `lockReason` | `lock_reason` | String(80) | NULL | `TOO_MANY_FAILED_LOGIN_ATTEMPTS`, `ADMIN_LOCKED`, or NULL |
| `lastFailedLoginAt` | `last_failed_login_at` | Timestamp | NULL | Timestamp of most recent failed attempt |
| `statusBeforeLock` | `status_before_lock` | String(20) | NULL | Previous status before security lock (for restoring correct status on unlock) |

### Lock Reasons (enum/constants)

```java
public static final String LOCK_REASON_TOO_MANY_FAILED = "TOO_MANY_FAILED_LOGIN_ATTEMPTS";
public static final String LOCK_REASON_ADMIN_LOCKED = "ADMIN_LOCKED";
```

### Redis Keys

- `PASSWORD_RESET:<token>` -> `email` (String), TTL 15 minutes.
- `PASSWORD_RESET_PENDING:<token>` -> `email` (String), temporary claimed reset token while DB transaction is pending.
- `PASSWORD_RESET_RATE:<normalizedEmail>` -> "true", TTL 60 seconds (rate limit).

### Config Properties (new in application.properties)

```properties
app.frontend-url=${APP_FRONTEND_URL:http://localhost:5173}
app.password-reset-token-ttl-minutes=${APP_PASSWORD_RESET_TOKEN_TTL_MINUTES:15}
app.password-reset-rate-limit-seconds=${APP_PASSWORD_RESET_RATE_LIMIT_SECONDS:60}
app.login-temporary-lock-minutes=${APP_LOGIN_TEMPORARY_LOCK_MINUTES:5}
app.login-failed-attempt-threshold=${APP_LOGIN_FAILED_ATTEMPT_THRESHOLD:5}
```

### Flyway Migration

New `V43__add_login_lockout_and_password_reset.sql`:

```sql
ALTER TABLE account ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE account ADD COLUMN lockout_count INT NOT NULL DEFAULT 0;
ALTER TABLE account ADD COLUMN locked_until TIMESTAMP NULL;
ALTER TABLE account ADD COLUMN lock_reason VARCHAR(80) NULL;
ALTER TABLE account ADD COLUMN last_failed_login_at TIMESTAMP NULL;
ALTER TABLE account ADD COLUMN status_before_lock VARCHAR(20) NULL;

CREATE INDEX idx_account_locked_until ON account(locked_until);
CREATE INDEX idx_account_lock_reason ON account(lock_reason);

-- Backfill existing Lock accounts with ADMIN_LOCKED reason
UPDATE account SET lock_reason = 'ADMIN_LOCKED' WHERE status = 'Lock';
```

## Application Flow

### Login State Machine

```
                    +-- correct password --> issue JWT, reset counters
                    |
[not locked] -------+-- wrong password --> increment failed_login_attempts
                    |                      if == 5:
                    |                        lockout_count++,
                    |                        locked_until = now+5min,
                    |                        send temp lockout email
                    |
[temporary lockout]-+-- any password --> reject (before locked_until)
                    |
                    +-- after locked_until:
                         correct password --> reset counters, issue JWT
                         wrong password --> increment failed_login_attempts
                           if == 5 AND lockout_count >= 1:
                             status=Lock, lock_reason=TOO_MANY_FAILED,
                             status_before_lock=old_status,
                             send security lock email
```

### Forgot Password Flow

1. User calls `POST /api/auth/forgot-password { email }`.
2. Always return `{ success: true, message: "Neu email ton tai..." }`.
3. If email exists:
   - Generate secure random token (UUID + SecureRandom).
   - Store in Redis: `PASSWORD_RESET:<token>` with TTL 15 min.
   - Check rate limit via `PASSWORD_RESET_RATE:<email>` (60s TTL).
   - Send email with reset link: `${app.frontend-url}/reset-password?token=<token>`.

### Reset Password Flow

1. User calls `POST /api/auth/reset-password { token, newPassword }`.
2. Validate `newPassword` (min 8 chars).
3. Atomically claim the token by renaming `PASSWORD_RESET:<token>` to `PASSWORD_RESET_PENDING:<token>` in Redis.
4. If missing/expired → reject ("Token khong hop le hoac da het han").
   A token already moved to pending is treated as invalid to enforce one-time use.
5. Read email from `PASSWORD_RESET_PENDING:<token>`.
6. Find account by email.
7. Hash new password with BCrypt.
8. Reset `failed_login_attempts = 0`, `lockout_count = 0`, `locked_until = NULL`, `last_failed_login_at = NULL`.
9. If `status = Lock` AND `lock_reason = TOO_MANY_FAILED_LOGIN_ATTEMPTS`:
    - Restore status from `status_before_lock` (default to `Approved` if saved or NULL).
10. Do NOT unlock if `lock_reason = ADMIN_LOCKED`.
11. Save account in the current DB transaction.
12. After DB commit succeeds, delete `PASSWORD_RESET_PENDING:<token>` and send password-changed email best-effort.
13. If save fails or the transaction rolls back after method return, restore `PASSWORD_RESET_PENDING:<token>` back to `PASSWORD_RESET:<token>` so the user does not lose a valid reset link.

## Interface Contract

### POST /api/auth/login (modified)

Existing behavior preserved for normal login. Added:

- Wrong password: increment counters, check thresholds.
- Temporary lockout: reject with message "Tai khoan dang bi tam khoa, vui long thu lai sau".
- Security lock: reject with message "Tai khoan da bi khoa, vui long dat lai mat khau".
- Correct password during lock: blocked.

### POST /api/auth/forgot-password (new)

Request:
```json
{ "email": "user@mail.com" }
```

Response:
```json
{ "success": true, "message": "Neu email ton tai, huong dan dat lai mat khau se duoc gui" }
```

Rate limit: 1 request per 60 seconds per email.

### POST /api/auth/reset-password (new)

Request:
```json
{ "token": "opaque-reset-token", "newPassword": "newPassword123" }
```

Response (success):
```json
{ "success": true, "message": "Dat lai mat khau thanh cong" }
```

Errors:
- Missing/expired/invalid token: error message.
- Invalid password: validation error.

## Email Contract

Use existing `JavaMailSender` via a new helper in `EmailOtpService` or a dedicated `SecurityEmailService`.

### Emails to send:

1. **Temporary Lockout**: Subject "Canh bao dang nhap sai nhieu lan". Body: failed attempts warning, 5-min block, reset suggestion.
2. **Security Lock**: Subject "Tai khoan da bi khoa vi ly do bao mat". Body: account locked, use forgot-password flow.
3. **Reset Password**: Subject "Dat lai mat khau AITASKER". Body: reset link, 15-min expiry.
4. **Password Changed**: Subject "Mat khau da duoc thay doi". Body: confirmation, contact support if not user.

## Observability

- Audit log entries for: temporary lockout trigger, security lock trigger, password reset success.
- Application logs at each state transition.
- Use existing `AuditRequestFilter` for request tracing.

## Alternatives Considered

1. **IP-based rate limiting instead of per-account**: Rejected per SPEC non-goals.
2. **Using JWT tokens for reset instead of Redis**: Rejected because Redis is already used for OTP, simpler to manage TTL.
3. **Not storing status_before_lock**: Rejected because we need to restore the correct previous status on unlock.
4. **Separate SecurityEmailService vs extending EmailOtpService**: Extending EmailOtpService to add generic email methods keeps code DRY and reuses existing `JavaMailSender` config.

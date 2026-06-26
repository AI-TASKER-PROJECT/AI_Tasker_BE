# 0018 Auth Lockout And Password Reset Design

Date: 2026-06-24

## Status

Accepted

## Context

Backend currently has no brute-force protection on login. Users can attempt unlimited wrong passwords. There is no self-service password recovery flow. An attacker could brute-force common passwords, and legitimate users who forget passwords or get locked out have no recovery path.

The SPEC mandates:
1. Temporary lockout after 5 consecutive wrong passwords (5-minute block).
2. Security lock (status=Lock, reason=TOO_MANY_FAILED_LOGIN_ATTEMPTS) after a second 5-attempt cycle following temporary lockout expiry.
3. Forgot-password endpoint that sends email reset link without revealing email existence.
4. Reset-password endpoint with one-time token (15-min TTL).
5. Successful login resets all failure counters.

## Decision

We will implement this as a high-risk auth feature with the following design choices:

1. **Redis for reset tokens**: Reuse existing Redis infrastructure (already used for OTP) instead of JWT-based reset tokens. This gives us native TTL and one-time-use semantics via DEL.

2. **status_before_lock column**: Store the account's previous status before security lock, so password reset can restore it correctly. Accounts locked by Admin (ADMIN_LOCKED) are never auto-unlocked by reset password.

3. **SecurityEmailService as separate class**: Rather than extending EmailOtpService, create a dedicated `SecurityEmailService` that injects `JavaMailSender` independently. This keeps responsibilities separate and avoids coupling OTP concerns with lockout/reset concerns.

4. **Account entity fields, not a separate table**: Add columns directly to `account` table rather than creating a `login_attempts` table. This keeps queries simple (single row for login decisions) and matches the existing pattern where `AccountEntity` already has status/auth fields.

5. **Rate limiting via Redis**: Use `PASSWORD_RESET_RATE:<email>` key with 60s TTL for forgot-password rate limiting, matching the existing OTP Redis pattern.

6. **Configurable via application.properties**: All thresholds (5 attempts, 5-min lock, 15-min token TTL) are configurable via environment variables with sensible defaults.

## Alternatives Considered

1. **JWT-based reset tokens**: Would not need Redis lookup, but harder to enforce one-time use and TTL. Rejected because Redis is already in the stack and provides native TTL + DELETE for one-time use.

2. **Separate login_attempts table**: More normalized, but adds complexity for a counter that's always queried with the account. Rejected in favor of simpler single-table approach.

3. **Reusing EmailOtpService for security emails**: Would mix OTP concerns with security notifications. Rejected to keep responsibilities separate.

4. **Not storing status_before_lock**: Would require heuristic to determine what status to restore. Rejected because we need reliable status restoration.

## Consequences

Positive:
- Self-service password recovery without Admin intervention.
- Brute-force protection with escalating lock mechanisms.
- Clean separation of concerns between OTP and security email services.
- All thresholds configurable via environment variables.
- Existing Admin lock behavior preserved (not auto-unlocked).

Tradeoffs:
- Account table grows 6 new columns.
- Login flow becomes more complex with multiple state checks.
- Redis dependency deepens (but already present).
- Email service must handle 4 new email types.

## Follow-Up

- Consider audit log entries for lockout/reset events after core implementation.
- Evaluate whether Admin lock flow should also set `lock_reason = ADMIN_LOCKED` (backfill migration handles existing accounts, but new Admin lock flows should also set it).
- Monitor Redis memory usage for reset tokens at scale.

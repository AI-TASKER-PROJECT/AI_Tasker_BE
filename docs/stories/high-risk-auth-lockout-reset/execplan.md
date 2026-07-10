# Exec Plan - Failed Login Lockout And Password Reset

## Goal

Implement complete login lockout and password reset flow: temporary lockout after 5 wrong attempts, security lock after second 5-attempt cycle, forgot-password email with reset token, and reset-password with unlock capability.

## Scope

In scope:
- US-AUTH-001: Temporary lockout after 5 wrong passwords
- US-AUTH-002: Security lock after second 5-attempt cycle
- US-AUTH-003: Forgot-password sends reset link via email
- US-AUTH-004: Reset password with token validation
- US-AUTH-005: Login resets failed counters on success
- Flyway migration V43 for new columns
- 4 new emails (temp lockout, security lock, reset link, password changed)
- Unit tests for all new logic
- Integration tests for full flows
- Swagger/OpenAPI updates

Out of scope:
- Multi-factor auth, CAPTCHA, device fingerprinting, IP rate limiting
- OAuth/Google password reset
- Admin UI changes beyond lock_reason field
- JWT token format changes
- Registration OTP behavior changes

## Risk Classification

Risk flags:
- Auth (login, password, JWT)
- Authorization (roles, permissions)
- Data model (schema, new columns, migration)
- Audit/security (password reset, lockout, email)
- External systems (email via JavaMailSender)
- Public contracts (new API endpoints, changed login behavior)
- Existing behavior (login flow modified)
- Weak proof (new feature, no existing tests)

Hard gates:
- Auth
- Authorization
- Data loss or migration
- Audit/security
- External provider behavior

Lane: **HIGH-RISK**

## Work Phases

1. **Discovery** (done): Read SPEC.md, codebase analysis, identified all touchpoints.
2. **Design** (done): Design document covers domain model, state machine, API contracts, data model.
3. **Validation planning**: Define test plan with concrete test cases.
4. **Implementation**:
   a. Flyway migration V43 (new columns + indexes + backfill)
   b. Update `AccountEntity` with new fields
   c. Add config properties to `application.properties`
   d. Create `SecurityEmailService` (reuses JavaMailSender)
   e. Create request DTOs (`ForgotPasswordRequest`, `ResetPasswordRequest`)
   f. Add forgot-password and reset-password endpoints to `AuthController`
   g. Add forgot-password and reset-password methods to `AuthService`/`AuthServiceImpl`
   h. Modify `login()` method to track attempts and enforce lockout
   i. Handle existing Admin lock flows (set lock_reason = ADMIN_LOCKED)
   j. Update Swagger/OpenAPI annotations
   k. Update `README.md` with new endpoints
5. **Verification**: Run unit tests, integration tests, manual Swagger checks.
6. **Harness update**: Record traces, decisions, update story proof matrix.

## Stop Conditions

Pause for human confirmation if:
- Product behavior is ambiguous (which status to restore on unlock for non-Approved accounts).
- Data migration risk appears (existing Lock accounts without lock_reason).
- Validation requirements need to be weakened.
- Architecture direction changes (e.g., switching from Redis to JWT for reset tokens).

# Exec Plan

## Goal

Make backend authentication enforce one active session per account, add deterministic Staff/catalog/SLA data migration, and keep proof/doc surfaces current.

## Scope

In scope:

- Add account token-version persistence through Flyway and JPA.
- Bind access and refresh JWTs to the active token version.
- Validate token version in HTTP security, WebSocket auth, current-session, and refresh paths.
- Add idempotent migration data for 10 Staff accounts and structured specializations.
- Update domain/skill descriptions to Vietnamese.
- Change milestone review auto-approve SLA setting to 3 days.
- Add focused unit tests and docs.

Out of scope:

- UI changes.
- Logout endpoint or device/session management screens.
- Scheduled SLA auto-approval.

## Risk Classification

Risk flags:

- Auth
- Authorization
- Data model
- Audit/security
- Public contracts
- Existing behavior
- Multi-domain

Hard gates:

- Auth
- Data migration

## Work Phases

1. Discovery.
2. Decision and story packet.
3. Auth token-version implementation.
4. Data migration.
5. Tests and docs.
6. Validation and trace.

## Stop Conditions

Pause for human confirmation if:

- Requirement changes from one active session to multiple managed devices.
- Token revocation must preserve old refresh tokens for a grace window.
- Migration requires deleting production data.

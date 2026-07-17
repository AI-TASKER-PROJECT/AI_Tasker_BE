# Exec Plan

## Goal

Harden profile review state transitions, complete the requested audit-log coverage, and narrow system settings to backend-supported controls.

## Scope

In scope:

- Business/Expert profile submission and update rules.
- Staff profile approval/rejection finality.
- PayOS wallet top-up terminal audit events and sync fallback spam suppression.
- Audit display normalization for dispute participants, contract completion, and remaining known raw entities/actions.
- Backend setting allowlist and cleanup migration for obsolete seed/demo keys.
- Focused tests and docs.

Out of scope:

- FE implementation changes.
- New payment provider behavior.
- New database columns for profile history.

## Risk Classification

Risk flags:

- Authorization.
- Audit/security.
- External systems.
- Public contracts.
- Existing behavior.
- Multi-domain.
- Weak proof.

Hard gates:

- Audit/security behavior.
- External payment sync behavior.
- Authorization and state-transition behavior.

## Work Phases

1. Discovery of profile, audit, payment, dispute, and settings surfaces.
2. Design state guards and supported setting list.
3. Validation planning for focused unit tests and compile/full suite when possible.
4. Implementation in services/filter/migration/docs.
5. Verification with focused tests, compile, and available full suite.
6. Harness trace and story proof update.

## Stop Conditions

Pause for human confirmation if:

- Required behavior would need a new public endpoint.
- Existing dirty/conflicted files cannot be safely reconciled.
- Validation cannot run and no focused proof can be collected.

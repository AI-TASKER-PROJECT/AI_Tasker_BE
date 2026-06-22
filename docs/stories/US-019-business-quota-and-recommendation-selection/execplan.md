# Exec Plan

## Goal

Grant Business initial free quota and add Business-to-Expert AI recommendation selection notifications.

## Scope

In scope:

- Add migration `V37`.
- Update quota creation and admin account creation logic.
- Add selection flags to entities and DTOs.
- Add recommendation select endpoint.
- Send Expert notification on first selection.
- Update API docs and Harness records.
- Add focused unit tests.

Out of scope:

- Frontend button implementation.
- Proposal invitation table redesign.
- Full E2E WebSocket verification.

## Risk Classification

Risk flags:

- Authorization.
- Data model.
- Audit/security.
- Public contracts.
- Existing behavior.
- Multi-domain.

Hard gates:

- Authorization.
- Data model.

## Work Phases

1. Discovery.
2. Design.
3. Migration and code update.
4. Documentation update.
5. Verification.
6. Harness trace.

## Stop Conditions

Pause for human confirmation if:

- Business selection must create a proposal automatically.
- Non-Premium Business users should be allowed to select recommendations.
- The selection must be exclusive to one Expert per job.

# Exec Plan

## Goal

Implement safe editability for jobs/proposals, add context-rich milestone API
aliases, and restore a guarded contract change request workflow.

## Scope

In scope:

- Add preferred job/contract-context milestone routes.
- Allow Business edits for `DRAFT`/`OPEN` jobs without contracts.
- Allow Expert edits for `Pending`/`Accepted` proposals without contracts.
- Add contract change request create/list/accept/reject APIs.
- Add audit logs, notifications, tests, migrations, and docs.

Out of scope:

- Changing `PATCH /api/v1/jobs/{jobId}/status`.
- Normalizing `/api/jobs` legacy prefixes.
- Direct mutation of active contracts without counterparty approval.

## Risk Classification

Risk flags:

- Authorization
- Data model
- Audit/security
- Public contracts
- Existing behavior
- Multi-domain

Hard gates:

- Authorization
- Audit/security

## Work Phases

1. Inspect current controllers, services, entity schema, audit and notification
   formats.
2. Add migration/DTO/entity/repository support.
3. Implement route aliases and edit workflows.
4. Implement change request review/apply guards.
5. Add focused unit tests.
6. Refresh OpenAPI and docs.
7. Run focused and full validation.
8. Record trace and mark story implemented only after proof is written.

## Stop Conditions

Pause for human confirmation if:

- Contract change application needs to alter already completed, cancelled, or
  escrow-released milestones.
- Tests reveal existing clients depend on deleted routes.
- Validation must be weakened.

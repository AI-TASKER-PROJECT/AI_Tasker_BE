# Exec Plan

## Goal

Return complete account-derived contact fields from profile read APIs and sync
Swagger-facing documentation/test guidance.

## Scope

In scope:

- Add transient response fields where missing.
- Enrich Business and Expert profile read methods from `account`.
- Add focused service regression tests.
- Update OpenAPI-facing docs and manual test guides.

Out of scope:

- New DTO split between public and private profile surfaces.
- Database migrations.
- Changing route authorization.

## Risk Classification

Risk flags:

- Public contracts.
- Existing behavior.
- Authorization.
- Audit/security/privacy.
- Weak proof.

Hard gates:

- Audit/security/privacy due to intentionally exposing account email/phone on
  public Business profile reads.

## Work Phases

1. Discovery of profile service/entity/controller/test/docs.
2. Design response enrichment and data-boundary decision.
3. Implement transient fields and service enrichment.
4. Add focused tests for every requested endpoint class.
5. Update Swagger/OpenAPI docs and test guides.
6. Run focused and broader verification.

## Stop Conditions

Pause for human confirmation if:

- Contact exposure should be restricted by profile status or requester role.
- A DTO split becomes required before shipping the enrichment.
- Existing authorization boundaries need to change.

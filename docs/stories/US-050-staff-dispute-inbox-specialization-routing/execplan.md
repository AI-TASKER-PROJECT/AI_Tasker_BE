# Exec Plan

## Goal

Give Staff a dedicated inbox of assigned disputes, route disputes only to domain-qualified Staff, and let Admin manage structured Staff specialization.

## Scope

In scope:

- Flyway V54 migration for `staff_domains` and `staff_skills` tables.
- Admin Staff API: create/update with `domainIds`/`skillIds`, structured responses.
- Domain-mandatory routing: filter + rank by domain/skill match, guarded manual routing, no-match behavior.
- Staff inbox: `GET /api/v1/staff/disputes` with pagination, status filter, JWT-derived identity.
- Authorization: tighten dispute, contract-dispute, and case-data reads to assigned scope.
- Tests: unit, auth, integration covering AC1-AC10.
- Swagger/OpenAPI and documentation refresh.

Out of scope:

- Frontend implementation.
- Termination-request Staff routing.
- Removal of legacy `specialization` field.
- Fallback routing outside job domain.

## Risk Classification

Risk flags: Authorization, Data model, Public contract, Existing behavior, Financial workflow.

Hard gates: Authorization boundary changes, data model migration, public API contract, existing dispute settlement invariants.

## Work Phases

1. Harness: intake, story, decision record.
2. Data model: V54 migration, entities, repositories.
3. Admin Staff API: DTOs, controller, service with domain/skill validation.
4. Routing: domain eligibility, deterministic ranking, manual guard, no-match behavior.
5. Staff inbox: DTO, service, controller.
6. Authorization: tighten reads to assigned scope.
7. Tests: unit, auth, integration.
8. Documentation: Swagger/OpenAPI, guides, Harness trace.

## Stop Conditions

Pause for human confirmation if:

- Data migration or deletion risk appears.
- Validation requirements need to be weakened.
- Architecture direction changes.

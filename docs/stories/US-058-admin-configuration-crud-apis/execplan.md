# Exec Plan

## Goal

Add safe, admin-only CRUD APIs for system settings and membership packages, plus delete/deactivate APIs for domain, skill, and technology catalogs.

## Scope

In scope:

- Admin system setting create, update, delete/deactivate.
- Catalog delete/deactivate for domains, skills, technologies.
- Admin membership package list, create, update, delete/deactivate.
- Service tests for new admin/config behavior.
- Swagger/OpenAPI-facing docs and manual guide updates.

Out of scope:

- Physical deletes that break FK references.
- Frontend changes.
- Changing user purchase flow except respecting inactive packages.

## Risk Classification

Risk flags:

- Authorization.
- Public contracts.
- Data model/data retention.
- Existing behavior.
- Multi-domain.
- Weak proof if Docker/runtime Swagger cannot run.

Hard gates:

- Authorization.
- Data retention / delete behavior.

## Work Phases

1. Discover current service, controller, DTO, repository, and docs.
2. Record decision for soft-delete semantics.
3. Implement service/controller/DTO/repository changes.
4. Add/extend unit tests.
5. Update Swagger-facing docs and README/architecture/product docs.
6. Run focused tests, compile, docs scans, and Docker-backed checks if available.

## Stop Conditions

Pause for human confirmation if:

- Physical delete is required despite existing FK/history references.
- API response shape needs a dedicated DTO instead of entity-shaped responses.
- Runtime Swagger refresh is required but Docker/Postgres remains unavailable.

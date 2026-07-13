# Overview

## Current Behavior

Admin can create and update domains, skills, technologies, existing system settings, staff, and accounts. Admin cannot create/delete system settings, cannot delete catalog values through API, and cannot CRUD membership packages. Membership package changes are currently handled through migrations.

## Target Behavior

Admin has API coverage for configuration surfaces used by the admin panel:

- Full CRUD for system settings.
- Delete/deactivate APIs for domain, skill, and technology catalogs.
- Full CRUD for membership packages.
- Swagger-facing docs and manual API guides list the new routes.

Deletes for catalog and package data are safe deactivations (`isActive=false`) so historical jobs, staff assignments, purchases, and wallet history remain valid.

## Affected Users

- Admin: can manage settings, catalogs, and membership packages from the backend API.
- Business/Expert: only active packages/catalog values remain visible to active public/user flows.

## Affected Product Docs

- `docs/product/admin-configuration.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `README.md`
- `docs/ARCHITECTURE.md`

## Non-Goals

- Physical hard delete of referenced catalog/package rows.
- Frontend admin panel implementation.
- Database schema changes unless required by JPA validation.

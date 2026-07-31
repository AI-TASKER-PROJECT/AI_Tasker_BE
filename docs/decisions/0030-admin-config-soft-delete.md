# 0030 Admin Configuration Soft Delete

Date: 2026-07-14

## Status

Accepted

## Context

Admin needs delete APIs for domain, skill, technology, and membership packages. These rows are referenced by jobs, staff specialization mappings, portfolios, purchases, wallet history, and other business records.

Physical deletes would risk FK failures or historical data loss.

## Decision

Implement delete endpoints for catalog and membership package configuration as soft deletes by setting `isActive=false`.

System setting delete also deactivates the setting by setting `isActive=false`; existing rows remain available for audit and possible reactivation.

## Alternatives Considered

1. Physical delete. Rejected because it can break references and history.
2. Add archive tables. Rejected as unnecessary schema complexity for this admin-panel requirement.
3. Frontend-only hide. Rejected because the backend should enforce active visibility and purchase rules.

## Consequences

Positive:

- Delete APIs are safe against FK and history issues.
- Existing active-only user flows continue to work.
- Admin can reactivate rows later through update APIs.

Tradeoffs:

- Rows remain in the database.
- Admin list endpoints need active-only filtering when the UI wants a clean list.

## Follow-Up

- Consider explicit `deletedAt`/`deletedBy` fields if audit requirements become stricter.

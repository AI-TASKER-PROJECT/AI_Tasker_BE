# Design

## Delete Semantics

Catalog and package delete endpoints are deactivation operations:

- `domains.is_active = false`
- `skills.is_active = false`
- `technologies.is_active = false`
- `membership_packages.is_active = false`

This preserves FK references from jobs, staff mappings, portfolios, purchases, and wallet history.

## System Settings

Admin can:

- List settings.
- Create a setting with `settingKey`, `settingValue`, `valueType`, `description`, and `isActive`.
- Update the same fields.
- Delete/deactivate a setting by setting `isActive=false`.

`settingKey` remains immutable once created.

## Catalogs

Existing create/update/list APIs are retained. New delete endpoints call service methods that require `ADMIN`, load the row, set inactive, save, and record audit evidence.

## Membership Packages

Membership package admin APIs live under `/api/v1/admin/membership/packages` to avoid changing the user-facing `/api/membership/packages` purchase surface.

Admin can:

- List all packages, optionally `activeOnly=true`.
- Create package.
- Update package.
- Delete/deactivate package.

User package listing and purchase continue to use only active packages.

## Audit

System setting changes continue to use `ACTION_UPDATE_SYSTEM_SETTING` for setting updates and deactivations. Catalog and membership package changes record audit entries with their table names.

# Admin Configuration

Admin configuration APIs manage platform settings, catalogs, and membership packages.

## System Settings

Admin can list, create, update, and deactivate system settings.

Delete semantics are soft-delete: deleting a setting sets `isActive=false`.

## Catalogs

Admin can list, create, update, and deactivate:

- Domains.
- Skills.
- Technologies.

Delete semantics are soft-delete through `isActive=false` so historical jobs and staff assignments remain valid.

## Membership Packages

Admin can list all membership packages, create packages, update packages, and deactivate packages.

Business and Expert purchase flows only expose active packages and cannot purchase inactive packages.

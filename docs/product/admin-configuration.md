# Admin Configuration

Admin configuration APIs manage platform settings, catalogs, and membership packages.

## System Settings

Admin can list, create, update, and deactivate supported system settings. The
automatic milestone review SLA is always active and only its duration is mutable.

Delete semantics are soft-delete: deleting a setting sets `isActive=false`.

Backend-supported operational settings are intentionally allowlisted:

- `milestone_review_sla_duration`: positive `<value>:<MINUTE|HOUR|DAY>` window
  used for new final-deliverable review rounds. It cannot be deactivated or deleted.
- `dispute_staff_max_active_cases`: maximum concurrent `STAFF_REVIEWING` disputes for one Staff account.
- `credit.job_post.price_vnd`: Business job-post credit unit price.
- `credit.proposal.price_vnd`: Expert proposal credit unit price.

Seed/demo keys that are not read by backend flows, including `platform_fee_percent`,
`auto_assign_staff_enabled`, and `max_open_jobs_per_business`, are hidden from
the Admin settings API and soft-deactivated by migration.

## Catalogs

Admin can list, create, update, and deactivate:

- Domains.
- Skills.
- Technologies.

Delete semantics are soft-delete through `isActive=false` so historical jobs and staff assignments remain valid.

## Membership Packages

Admin can list all membership packages, create packages, update packages, and deactivate packages.

Business and Expert purchase flows only expose active packages and cannot purchase inactive packages.

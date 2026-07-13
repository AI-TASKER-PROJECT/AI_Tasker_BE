# 0029 Internal Profile Review Domain

Date: 2026-07-13

## Status

Accepted

## Context

Profile review currently fans out to all Staff and authorizes approval by role only. The cleaner capability/scope model would add tables, but the current constraint is to avoid new tables and reuse existing Staff domain assignment.

Using a normal marketplace domain would leak the review specialization into Business job creation unless the backend filters and guards it.

## Decision

Use `domains.domain_code = PROFILE_REVIEW` as an internal Staff specialization domain.

Backend services must treat it specially:

- Admin can see and assign it.
- Non-admin and anonymous domain catalog responses hide it.
- Non-admin job-domain assignment rejects it.
- Profile submission notification and Staff approval/listing require Staff to be mapped to it through `staff_domains`.

## Alternatives Considered

1. Add dedicated `staff_capabilities` tables. Rejected for this change because the user requested no new tables.
2. Add visibility/scope columns to `domains`. Deferred because it would require schema changes beyond the current request.
3. Only hide the domain in the frontend. Rejected because direct API calls could still assign or use it.

## Consequences

Positive:

- No new table is required.
- Admin keeps using existing Staff specialization management.
- Unrelated Staff no longer receive or perform profile review work.

Tradeoffs:

- `PROFILE_REVIEW` is a special-case domain code and must stay filtered from marketplace flows.
- Future internal domains may need a real `domain_scope` column or capability table.

## Follow-Up

- Consider a first-class internal capability model if Staff operational permissions expand beyond this one review scope.

# Decision 0013: Profile Rejection Reason

## Status

Accepted.

## Context

The week4 identifier branch added a `rejection_reason` field so staff can send
the reason when rejecting KYB/KYC verification. The week6 synthetic branch did
not contain that behavior and already has Flyway migrations through `V32`.

Reusing the old week4 `V19__profile_rejection_reason.sql` would conflict with
the existing week6 migration history.

## Decision

Port the behavior into week6 with a new additive migration:

- `V34__profile_rejection_reason.sql`
- `business_profiles.rejection_reason VARCHAR(500)`
- `expert_profiles.rejection_reason VARCHAR(500)`

The profile approval API accepts an optional `reason` query parameter. The
service requires it when `status=Rejected`, trims it, limits it to 500
characters, stores it on the profile, and clears it when the profile is
approved or resubmitted.

## Consequences

- Frontend can show a staff rejection reason from the profile response.
- Database migration order stays valid for week6.
- Swagger/OpenAPI must document `reason` on
  `POST /api/v1/profiles/approve/{type}/{id}`.

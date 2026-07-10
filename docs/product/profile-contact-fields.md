# Profile Contact Fields

Business and Expert profile read APIs support user-info screens, profile pages,
expanded panels, and contact affordances. Profile entities keep persisted KYB/KYC
data in `business_profiles` and `expert_profiles`; account-owned display and
contact fields are enriched from `account` into transient response fields.

## Business Profile Reads

The following APIs return `BusinessProfileEntity` with `fullName`, `email`, and
`phone` populated from the owning account:

- `GET /api/v1/profiles/business/me` for the current BUSINESS account.
- `GET /api/v1/profiles/business/{businessId}` for public profile view.
- `GET /api/v1/profiles/business/by-job/{jobId}` for OPEN job business info.
- `GET /api/v1/profiles/business` for STAFF operator listing/contact panels.

## Expert Profile Reads

The following APIs return `ExpertProfileEntity` with `fullName`, `email`,
`phone`, and `title` populated from the owning account where applicable:

- `GET /api/v1/profiles/expert/me` for the current EXPERT account.
- `GET /api/v1/profiles/expert/{expertId}` for profile view by authorized users.
- `GET /api/v1/profiles/expert` for STAFF/BUSINESS listing and review panels.

No schema migration is required for these fields because they are transient
response fields sourced from the existing `account` row.

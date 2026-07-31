# Overview

## Current Behavior

Business and Expert profile submissions notify every Staff account. Profile approval only checks the `STAFF` role, so any Staff account with the endpoint can approve or reject KYB/KYC profiles.

Domain catalog APIs also expose every active domain to non-admin callers, so using `domains` for an internal Staff-only review specialization would leak it into marketplace job creation.

## Target Behavior

Profile review is routed through the internal `PROFILE_REVIEW` domain:

- `PROFILE_REVIEW` is seeded as a domain for Staff specialization.
- Non-admin and anonymous domain catalog calls hide `PROFILE_REVIEW`.
- Business job-domain assignment rejects `PROFILE_REVIEW`.
- Profile submission notifications go only to Staff assigned `PROFILE_REVIEW`.
- Staff profile approval and Staff profile review lists require the Staff record to have `PROFILE_REVIEW`.
- `staff@aitasker.local` has only the `PROFILE_REVIEW` domain assignment.

## Affected Users

- Business and Expert: profile submission no longer spams unrelated Staff.
- Staff: only assigned profile-review Staff can receive and act on KYB/KYC work.
- Admin: can see and assign the internal review domain.

## Affected Product Docs

- `docs/product/profile-review-routing.md`
- `docs/ARCHITECTURE.md`

## Non-Goals

- No new database table.
- No frontend UI change in this backend story.
- No split between Business-only and Expert-only profile review scopes.

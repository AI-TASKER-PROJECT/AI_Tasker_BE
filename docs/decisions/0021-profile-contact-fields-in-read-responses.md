# 0021 Profile Contact Fields in Read Responses

Date: 2026-06-30

## Status

Accepted

## Context

Business and Expert profile pages need enough account context for user-info
views, expanded contact panels, and cross-role profile inspection. Existing
profile reads returned persisted KYB/KYC profile fields and only partially
enriched account data: Business public reads had `fullName`, Expert reads had
some combination of `fullName`, `phone`, and `title`, and several current/list
routes missed contact fields.

Older decisions 0015 and 0016 intentionally avoided email/phone exposure for
public Business profile reads. The current product requirement supersedes that
data-minimization tradeoff: the profile read APIs must expose contact fields so
Business and Expert users can inspect and contact each other from profile pages.

## Decision

Populate read response transient fields from the existing `account` row:

- Business profile reads return `fullName`, `email`, and `phone`.
- Expert profile reads return `fullName`, `email`, `phone`, and `title`.
- No database migration is added because the source data already lives in
  `account`.
- Authorization and public route boundaries remain unchanged: public Business
  profile routes stay public, private `/me` and listing routes keep their
  existing service role checks.

## Alternatives Considered

1. Keep email/phone hidden on public profile reads. Rejected because the current
   user-info/profile/contact requirement explicitly needs those fields.
2. Add new profile DTOs and leave entity responses unchanged. Deferred because
   the existing API contract already uses entity schemas and the change is a
   response enrichment, not a persisted data-model change.

## Consequences

Positive:

- Profile pages and contact panels no longer need extra account lookups.
- Swagger/OpenAPI schema lists the account-derived fields on profile responses.

Tradeoffs:

- Public Business profile reads now expose account email and phone for the
  owning Business account.
- The API still returns entity-shaped responses, so sensitive persisted profile
  fields such as rejection/file-path fields should be revisited in a future DTO
  hardening story if public data minimization becomes a priority again.

## Follow-Up

- Consider dedicated public/profile read DTOs when the frontend contract is
  stable enough to split private operator responses from public profile pages.

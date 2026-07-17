# Design

## Domain Model

Profile status remains the existing string state: `Pending`, `Approved`, `Rejected`. The service treats `Pending` as an active review lock, `Rejected` as resubmittable, and `Approved` as final for review but mutable for owner information updates.

Supported system settings are limited to:

- `default_sla_days`
- `dispute_staff_max_active_cases`
- `credit.job_post.price_vnd`
- `credit.proposal.price_vnd`

## Application Flow

Business and Expert upsert flows load any existing profile before mutating:

- No profile: create `Pending`, set account `Pending`, notify profile-review Staff.
- Existing `Pending`: reject resubmission.
- Existing `Rejected`: update profile, set `Pending`, clear rejection, notify Staff.
- Existing `Approved`: update allowed fields, preserve final review fields/status, do not notify Staff.

Staff approval loads the target profile and requires current status `Pending`.

PayOS sync compares previous and mapped provider statuses. It records audit only when the order first reaches a terminal status. The request fallback audit filter skips PayOS sync routes so repeated sync calls with no state transition do not create duplicate route logs.

## Interface Contract

Existing routes are preserved:

- `POST /api/v1/profiles/business`
- `POST /api/v1/profiles/expert`
- `POST /api/v1/profiles/approve/{type}/{id}`
- `POST /api/payments/payos/{orderCode}/sync`
- `GET/POST/PUT/PATCH/DELETE /api/v1/admin/settings...`

Error responses use existing `AppException` behavior with explicit Vietnamese/technical messages.

## Data Model

Add migration `V63__profile_audit_settings_hardening.sql` to soft-disable obsolete settings:

- `platform_fee_percent`
- `auto_assign_staff_enabled`
- `max_open_jobs_per_business`

No destructive schema change is needed.

## UI / Platform Impact

FE is not changed. Admin settings receives fewer backend-returned setting rows because unsupported seed/demo keys are hidden by BE.

## Observability

New audit actions:

- Wallet top-up succeeded.
- Wallet top-up failed/cancelled/expired.
- Automatic Staff dispute assignment uses the same display action as manual assignment but entity detail identifies the assigned Staff and participants.

## Alternatives Considered

1. Add separate profile update endpoints. Rejected because existing FE already uses upsert routes and the behavior can be made state-aware.
2. Keep fallback route logs for every PayOS sync. Rejected because repeated sync polling without state changes is not a meaningful audit event.

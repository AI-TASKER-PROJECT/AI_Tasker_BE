# Exec Plan

## Goal

Restrict KYB/KYC profile review work to Staff assigned an internal profile-review domain without adding new database tables.

## Scope

In scope:

- Seed `PROFILE_REVIEW` in the existing `domains` table.
- Reassign `staff@aitasker.local` to only `PROFILE_REVIEW`.
- Hide `PROFILE_REVIEW` from non-admin catalog domain responses.
- Prevent non-admin job-domain assignment of `PROFILE_REVIEW`.
- Route profile-submitted notifications only to Staff mapped to `PROFILE_REVIEW`.
- Guard Staff profile approval/listing with the same mapping.
- Add focused service tests.

Out of scope:

- New capability/scope tables.
- Frontend changes.
- Scheduler or queue changes for profile review work.

## Risk Classification

Risk flags:

- Authorization.
- Data model / migration data.
- Public contracts.
- Existing behavior.
- Weak proof if Docker/Flyway is unavailable.

Hard gates:

- Authorization.
- Data migration.

## Work Phases

1. Discovery of profile, catalog, and staff-domain code.
2. Decision: internal domain reuse with filtering/guards.
3. Migration update in existing V57 per user request.
4. Service and repository implementation.
5. Focused unit tests and compile/regression checks.
6. Harness trace and proof update.

## Stop Conditions

Pause for human confirmation if:

- The product must support separate Business/Expert profile review teams now.
- Admin should also be allowed to approve profiles directly.
- Editing V57 is no longer acceptable because it has already been applied to a shared database.

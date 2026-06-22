# Exec Plan

## Goal

Notify staff when a profile verification request is submitted and seed enough verified Expert/Business data to test AI recommendation ranking.

## Scope

In scope:

- Add staff notification dispatch for Business KYB submission.
- Add staff notification dispatch for Expert KYC submission.
- Add migration V38 for verified demo accounts, profiles, portfolios, jobs, milestones, proposals, quotas, and wallets.
- Add focused unit tests.
- Record Harness story, validation, and trace evidence.

Out of scope:

- Frontend notification UI changes.
- New staff dashboard routes.
- New recommendation algorithm logic.
- Production data import tooling.

## Risk Classification

High-risk because the change touches notification behavior, profile verification, database seed data, and recommendation testing fixtures.

Hard gates:

- Database migration must apply cleanly.
- Existing profile submission behavior must still set profile/account status to `Pending`.
- Notification dispatch must not block profile submission when no staff rows exist.

## Work Phases

1. Discovery of profile, notification, repository, and schema behavior.
2. Harness story registration.
3. Code implementation and unit tests.
4. Migration seed implementation.
5. Validation with focused tests and full Maven/Flyway run.
6. Harness trace and evidence update.

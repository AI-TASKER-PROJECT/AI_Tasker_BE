# Design

## Notification Flow

`ProfileService.upsertBusiness(...)` and `ProfileService.upsertExpert(...)` remain the submission points for KYB/KYC verification. After the profile is saved and audit is recorded, the service enumerates `staffs` and calls `NotificationService.notifyProfileSubmitted(...)` for each staff account.

The new notification type is `PROFILE_VERIFICATION_SUBMITTED`.

Metadata:

- `profileType`: `BUSINESS` or `EXPERT`
- `profileId`: submitted profile id

Target URL:

- Business profile: `/staff/profiles/business/{profileId}`
- Expert profile: `/staff/profiles/expert/{profileId}`

This reuses `NotificationService.createAndPush(...)`, so notifications are both persisted in `notifications` and pushed to `/queue/notifications`.

## Seed Data

Migration `V38__seed_recommendation_demo_profiles_jobs_proposals.sql` adds deterministic demo data with high numeric IDs to avoid existing local seed collisions.

Seeded Expert data:

- Accounts `8101` to `8120`
- Expert profiles `8201` to `8220`
- Portfolios `8301` to `8320`

Seeded Business data:

- Accounts `8401` to `8410`
- Business profiles `8501` to `8510`
- Jobs `8601` to `8610`
- Milestones `8701` to `8730`
- Proposals `8801` to `8820`

The seeded jobs and portfolios deliberately include matching AI/RAG/chatbot/e-commerce/customer-support keywords and catalog ids so the current recommendation ranking logic can find multiple candidates.

## Validation Boundaries

Unit tests cover notification dispatch from profile submission. Migration validation is covered by the full Maven/Flyway run against local PostgreSQL.

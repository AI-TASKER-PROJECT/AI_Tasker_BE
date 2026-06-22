# Design

## Domain Model

`user_quotas.job_post_quota_balance` now starts at 3 for Business accounts. `quota_usage_logs` records this with `quota_type=JOB_POST`, `action_type=GRANT`, and `reference_type=INITIAL_BUSINESS_GRANT`.

`proposals.business_selected` is added as a default false proposal flag. Because a Business can select an AI recommended Expert before a proposal exists, the actionable selection state is stored on `expert_recommendations.business_selected`.

## Application Flow

1. Register or admin-create a Business account.
2. Backend creates a quota row with 3 job-post credits and logs the initial grant.
3. Business generates or reads AI expert recommendations for a job.
4. Frontend calls `POST /api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select`.
5. Backend validates Business Premium access and job ownership through the existing recommendation guard.
6. Backend marks the saved recommendation selected and sends notification `EXPERT_RECOMMENDATION_SELECTED` to the Expert account.

## Interface Contract

- `POST /api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select`
- Auth: Business with active Premium entitlement and ownership of the job.
- Body: none.
- Response: selected `ExpertRecommendationResponse` with `businessSelected=true`.

## Data Model

- `proposals.business_selected BOOLEAN NOT NULL DEFAULT FALSE`
- `expert_recommendations.business_selected BOOLEAN NOT NULL DEFAULT FALSE`
- Business quota backfill in `user_quotas` and `quota_usage_logs`.

## UI / Platform Impact

Frontend can show a selected state in AI recommendation cards and call the select endpoint from the recommendation list.

## Observability

The Expert receives a persisted notification and realtime WebSocket push through the existing notification service.

## Alternatives Considered

1. Store selection only on `proposals`. Rejected because the selection happens before the Expert submits a proposal, so no proposal row may exist yet.

# 0017 Draft Job Update Flow Preserves SoW and Milestones

Date: 2026-06-22

## Status

Accepted

## Context

`POST /api/v1/jobs` (`MarketplaceService.createJob`) persists `jobs` + `sow` +
`milestones` together at create time. After creation there is no draft update
endpoint: `PATCH /jobs/{jobId}/status` only sets status, and the catalog
`PUT /jobs/{jobId}/{domains,skills,technologies}` endpoints do not touch
`sow` or `milestones`.

Publishing (`updateJobStatus` to `OPEN`) guards with
`sowRepository.findByJobId(jobId).isEmpty()` and throws
`JOB_MUST_HAVE_AI_SOW` when no `sow` row exists. Because there is no way to
re-persist edited `sow` or `milestones` for a draft job, any frontend flow
that edits draft milestones and later publishes can hit `JOB_MUST_HAVE_AI_SOW`
even though the user had a SoW on the client.

Investigation (US-022) confirmed this is Case B: the backend has a create-time
persistence path but no draft update path that preserves/upserts SoW.

`saveSow` currently forces `sowId=null` and inserts, so it cannot be reused
as-is for update (it would violate `sow.job_id` uniqueness). `saveMilestones`
forces `milestoneId=null` and inserts, so re-calling it would append duplicate
milestones rather than replace.

## Decision

1. Add `PUT /api/v1/jobs/{jobId}` as the draft update endpoint, implemented as
   `MarketplaceService.updateDraftJob(jobId, request)`.
2. The flow verifies BUSINESS role + approved KYB + ownership + `DRAFT` status
   + no existing contract for the job, then in one transaction:
   - updates job core fields (`title`, `rawRequirements`, `budget`);
   - upserts `sow` by `jobId` (update existing row if present, else insert);
   - replaces draft milestones by `jobId` (delete existing, insert new set,
     re-link criteria).
3. Do not consume publish quota in the draft update; quota is still consumed
   only by `updateJobStatus` when publishing to `OPEN`.
4. Leave catalog assignments (domains/skills/technologies) to the existing
   `PUT /jobs/{jobId}/{domains,skills,technologies}` endpoints to avoid
   duplicating their ownership logic.
5. Keep the `JOB_MUST_HAVE_AI_SOW` publish guard unchanged; it remains the
   genuine missing-SoW guard.

## Alternatives Considered

1. Extend `PATCH /jobs/{jobId}/status` to accept sow/milestones. Rejected: the
   status endpoint is a narrow status transition; mixing draft-content save
   with status would muddle the contract and risk accidental publish.
2. Separate `PUT /jobs/{jobId}/sow` and `PUT /jobs/{jobId}/milestones`.
   Rejected for US-022: the SPEC requires `jobs` + `sow` + `milestones` to be
   persisted together to stay consistent. Splitting re-introduces the gap.
3. Reuse `POST /jobs` with the existing job id. Rejected: `createJob` forces
   `jobId=null` and inserts new rows; it cannot upsert.

## Consequences

Positive:

- Draft edits no longer break publish due to missing SoW persistence.
- `JOB_MUST_HAVE_AI_SOW` remains a genuine missing-SoW guard.
- One transaction keeps `jobs` + `sow` + `milestones` consistent.

Tradeoffs:

- Public API surface grows by one route (`PUT /api/v1/jobs/{jobId}`).
- Milestone ids change on each draft edit (delete + insert). This is safe for
  drafts (no contract/deliverable references yet) but clients that cache
  milestone ids during drafting must reload after edit.

## Follow-Up

- Consider separate fine-grained sow/milestone edit endpoints if the frontend
  needs partial updates without re-sending the full set.
- Add a DB-free integration test harness (backlog #2) so the
  create->edit->publish flow can be proven end-to-end without Postgres.

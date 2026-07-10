# Overview

## Current Behavior

A Business creates a draft job via `POST /api/v1/jobs`, which persists `jobs` +
`sow` + `milestones` together in `MarketplaceService.createJob`. After that,
there is **no draft update endpoint** that re-persists edited `sow` or
`milestones` for the same draft job. The only job mutation endpoints after
create are:

- `PATCH /api/v1/jobs/{jobId}/status` -> only sets `status`/`publishedAt`.
- `POST /api/v1/jobs/{jobId}/publish` -> delegates to status update (`OPEN`).
- `PUT /api/v1/jobs/{jobId}/{domains,skills,technologies}` -> catalog only.

Publishing checks `sowRepository.findByJobId(jobId).isEmpty()` and throws
`JOB_MUST_HAVE_AI_SOW` when no `sow` row exists (`MarketplaceService:197-198`).

As a result, if the frontend edits milestone content on a draft job and then
publishes, the persisted `sow` row is the one from create-time. If any flow
removes or never re-persists the `sow` row, publish fails with
`JOB_MUST_HAVE_AI_SOW` even though the user "had" a SoW on the client.

## Target Behavior

A Business can edit a draft job's core fields, SoW, and milestones together
through a dedicated draft update flow, and the job remains publishable
afterward. `JOB_MUST_HAVE_AI_SOW` only occurs when the draft genuinely has no
persisted `sow` row.

The draft update flow:

1. verifies `BUSINESS` role and approved KYB;
2. verifies the caller owns the job;
3. allows editing only while the job is in `DRAFT` status;
4. upserts the `sow` row by `jobId` (update if exists, create if missing);
5. replaces draft milestones safely for the same job (only while no contract
   exists);
6. persists job core fields together with `sow` and `milestones` in one
   transaction;
7. does not consume publish quota (publish is still the only quota-consuming
   step).

## Affected Users

- Business users refining a draft job before publishing.
- Frontend draft-edit flows that currently cannot persist edits.

## Affected Product Docs

- `docs/ARCHITECTURE.md` (marketplace rules section)
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`
- `README.md` (API inventory)

## Non-Goals

- Changing publish quota rules.
- Changing SoW AI generation output format.
- Changing milestone snapshot rules after contract creation.
- Changing public job visibility rules.
- Editing milestones that already belong to a contract.
- Editing a job that is `OPEN`/`IN_PROGRESS`/`CLOSED` (draft-only).

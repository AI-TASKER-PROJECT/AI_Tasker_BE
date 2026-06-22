# Design

## Domain Model

No schema changes. `sow` already has `unique(job_id)` and
`milestones.job_id` already exists. The change is a new service flow that
upserts `sow` by `jobId` and replaces draft `milestones` by `jobId`.

## Application Flow

```text
PUT /api/v1/jobs/{jobId}  (BUSINESS, draft-only)
  -> updateDraftJob(jobId, request)
      1. requireRole("BUSINESS") + currentApprovedBusiness()
      2. load job by id; 404 if missing
      3. verify ownership (business owns job)
      4. verify job.status == "DRAFT" (reject non-DRAFT)
      5. verify no contract exists for the job (defensive; draft should not
         have a contract, but block if one exists)
      6. validate job core fields (title, rawRequirements, budget)
      7. update job core fields; save job
      8. upsert sow by jobId:
         - if sow exists for jobId -> update its fields from request.sow
         - else -> insert new sow with jobId
      9. replace milestones for jobId:
         - delete existing draft milestones for jobId
         - insert the new milestone set from request.milestones
         - re-link criteria via replaceMilestoneCriteria
     10. audit log (ACTION_UPDATE_JOB_DRAFT)
     11. return attachJobDetails(saved)
```

## Interface Contract

- Route: `PUT /api/v1/jobs/{jobId}`
- Auth: BUSINESS + approved KYB + ownership.
- Allowed state: `job.status == "DRAFT"`.
- Request body: same `JobEntity` shape as `POST /api/v1/jobs` (carries
  `title`, `rawRequirements`, `budget`, `sow`, `milestones`, `domainIds`,
  `skills`, `technologyIds`). The endpoint focuses on `sow` + `milestones` +
  core job fields; catalog assignments (domains/skills/technologies) are left
  to the existing `PUT /jobs/{jobId}/{domains,skills,technologies}` endpoints
  to avoid duplicating their ownership logic.
- Response: updated `JobEntity` with `sow` + `milestones` attached.
- Errors: `404` job missing; `BAN KHONG CO QUYEN THAO TAC JOB NAY` not owner;
  `JOB KHONG O TRANG THAI DRAFT` non-DRAFT; `SOW TITLE KHONG DUOC DE TRONG`
  when sow provided but invalid; `MILESTONE NAME ...` validation per milestone.

## Data Model

No migration. `sow.job_id` remains unique; the upsert respects that.
`milestones.job_id` rows for the draft job are deleted then re-inserted with
new ids; this is safe because draft milestones have no contract and no
deliverables yet (guard: block if a contract exists for the job).

## UI / Platform Impact

Frontend draft-edit screen can call `PUT /api/v1/jobs/{jobId}` after create to
persist edited milestones + SoW, then call publish without hitting
`JOB_MUST_HAVE_AI_SOW`.

## Observability

Audit record `ACTION_UPDATE_JOB_DRAFT` ("Cập nhật draft job") for traceability.
No new audit fields.

## Alternatives Considered

1. Extend `PATCH /jobs/{jobId}/status` to accept sow/milestones. Rejected: the
   status endpoint is a narrow status transition, not a draft-content save;
   mixing them would muddle the contract and risk accidental publish.
2. Add separate `PUT /jobs/{jobId}/sow` and `PUT /jobs/{jobId}/milestones`.
   Rejected for US-022: the SPEC says the draft-save flow should persist
   `jobs` + `sow` + `milestones` together so they stay consistent. Splitting
   would re-introduce the consistency gap. Separate endpoints can be a
   follow-up if finer-grained edits are needed.
3. Reuse `POST /jobs` with the existing job id. Rejected: `createJob` forces
   `jobId=null` and `status=DRAFT` and inserts new rows; it cannot upsert.

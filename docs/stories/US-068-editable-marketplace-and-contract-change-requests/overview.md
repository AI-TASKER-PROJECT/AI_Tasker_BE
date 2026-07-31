# Overview

## Current Behavior

- Some milestone commands use root-level `/milestones/...` routes even when the
  operation belongs to a job or contract.
- Businesses can edit job details only while the job is `DRAFT`.
- Experts cannot edit a submitted proposal.
- The `contract_change_requests` table exists, but the active API was removed
  because the old flow was disabled.

## Target Behavior

- Add context-rich aliases for job-owned and contract-owned milestone commands,
  while keeping the old routes compatible.
- Businesses can update job information, SoW, metadata, milestones, and
  acceptance criteria while a job is `DRAFT` or `OPEN`, unless a contract exists.
- Experts can update proposal content while it is `Pending` or `Accepted`,
  unless a contract already exists for that proposal.
- Business or Expert can submit a contract change request for `DRAFT`,
  `PENDING`, or `ACTIVE` contracts. The counterparty can accept or reject; only
  accepted requests apply changes to the contract snapshot.

## Affected Users

- Business: edits public jobs and reviews contract change requests.
- Expert: edits proposals and reviews contract change requests.
- Staff/Admin: see audit/notification trail for changed contract data.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `README.md`

## Non-Goals

- Do not change `PATCH /api/v1/jobs/{jobId}/status`.
- Do not normalize `/api/jobs` legacy prefixes in this story.
- Do not allow direct edits of active contracts without counterparty approval.

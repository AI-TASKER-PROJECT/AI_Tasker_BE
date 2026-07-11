# Overview

## Current Behavior

Business deposits milestone escrow from `PENDING`, then Expert must call
`POST /api/v1/milestones/{milestoneId}/start` before the milestone becomes
`IN_PROGRESS`.

## Target Behavior

A successful Business milestone escrow deposit automatically moves the
milestone to `IN_PROGRESS` and starts the execution timeline. The Expert start
endpoint remains as a compatibility/idempotent endpoint for legacy `DEPOSITED`
rows and older clients.

## Affected Users

- Business users funding milestone execution.
- Expert users starting work, submitting progress reports, and submitting
  deliverables.
- Frontend/QA clients following Contract Execution Flow.

## Affected Product Docs

- `SPEC-MILESTONE-DISPUTER.md`
- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`

## Non-Goals

- Removing the start endpoint.
- Changing escrow ledger accounting.
- Changing milestone deliverable, review, rejection, dispute, or payout rules.

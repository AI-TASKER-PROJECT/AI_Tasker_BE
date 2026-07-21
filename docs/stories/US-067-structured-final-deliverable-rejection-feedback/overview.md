# Overview

## Current Behavior

Business rejects a final deliverable through
`POST /api/v1/milestones/{milestoneId}/reject?reason=...`. The backend stores
only one overall text feedback value.

## Target Behavior

Business can reject a final deliverable with an overall reason and optional
criterion-level failed feedback. Each failed criterion references a
milestone-owned acceptance criterion and carries its own reason.

## Affected Users

- Business: gives clearer rejection feedback.
- Expert: sees exactly which acceptance criteria failed and why.

## Affected Product Docs

- `docs/product/contract-management.md`
- Swagger/OpenAPI docs and manual API guides.

## Non-Goals

- Do not add a new database table.
- Do not change dispute creation semantics.
- Do not change progress report feedback.

# Restore Business Progress Report Feedback

## Current Behavior

Business can acknowledge an Expert progress report through
`POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/acknowledge`.
The acknowledgement unlocks the next progress report, but it cannot carry
Business feedback text or structured review context.

The database still has progress-report feedback columns from `V51`, but the
current Java entity and API do not expose them.

## Target Behavior

Business can record feedback on an Expert progress report during milestone
execution. Feedback stores Business text, optional category/severity, optional
DoD item context, and whether adjustment is requested.

Submitting feedback also acknowledges the report when it is still pending
Business acknowledgement, preserving the current report gate. Feedback is a
progress-tracking response only; it must not create dispute, revision, or
deliverable-rejection semantics by itself.

## Affected Users

- Business.
- Expert.

## Affected Product Docs

- `SPEC-MILESTONE-DISPUTER.md`
- `docs/product/contract-management.md`
- `docs/decisions/0026-flow45-v23-routing-and-report-gates.md`
- Swagger/OpenAPI inventory and test guides.

## Non-Goals

- Do not change milestone deliverable rejection behavior.
- Do not create automatic disputes or report revision states from feedback.
- Do not add a database migration unless current schema validation requires it.

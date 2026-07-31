# 0038 Structured Deliverable Rejection Feedback

Date: 2026-07-21

## Status

Accepted

## Context

Business users can reject an Expert's final milestone deliverable from
`UNDER_REVIEW`, but the backend only accepts one free-text `reason` query
parameter. That is not enough to show which milestone-owned acceptance criteria
failed or why each selected criterion failed.

The requested constraint is to avoid adding a new table.

## Decision

Keep the existing `deliverables.rejection_feedback` field as the overall
rejection reason and add one nullable JSONB column on `deliverables` for
criterion-level feedback. The JSON stores a list of objects with
`criteriaId` and `reason`.

Add a request DTO for `POST /api/v1/milestones/{milestoneId}/reject` that
accepts:

- `reason`: required overall rejection feedback.
- `failedCriteria`: optional list of failed criteria with a required
  per-criterion reason.

The service validates every provided `criteriaId` belongs to the rejected
milestone before persisting JSON.

## Alternatives Considered

1. Add a normalized `deliverable_rejection_criteria` table. Rejected by the
   user's explicit constraint.
2. Store only the overall free-text reason. Rejected because it remains unclear
   which acceptance criteria failed.
3. Trust the frontend to send labels only. Rejected because criteria ownership
   must be validated server-side.

## Consequences

Positive:

- Expert users can see exact failed criteria and correction notes.
- No new table is added.
- Existing `reason` query clients can be kept compatible during transition.

Tradeoffs:

- Reporting across failed criteria is weaker than with a normalized table.
- Backend JSON validation and serialization become part of the service
  contract.

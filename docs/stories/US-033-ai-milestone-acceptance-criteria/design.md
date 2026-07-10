# Design

## Domain Model

`AcceptanceCriteriaEntity` becomes a child of one `MilestoneEntity`:

```text
milestones 1 --- * acceptance_criteria
```

Each criterion has:

- `criteriaId`
- `milestoneId`
- `description`
- `sortOrder`
- timestamps

The global `criteriaCode` and `isActive` catalog fields are removed.

## Application Flow

1. AI returns each milestone with `acceptanceCriteria: ["..."]`.
2. Job create/update saves the milestone, then saves its criterion descriptions.
3. Job reads return criterion objects and the simple description list.
4. The owning approved Business can create, update, or delete one criterion.
5. Criterion mutation is rejected after a contract exists for the job.
6. Contract creation joins the milestone-owned descriptions into the existing
   immutable `criteriaSnapshot`.

## Interface Contract

Removed:

- `GET /api/v1/acceptance-criteria`
- `POST /api/v1/criteria`

Retained:

- `GET /api/v1/milestones/{milestoneId}/criteria`

Added:

- `POST /api/v1/milestones/{milestoneId}/criteria`
- `PUT /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- `DELETE /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`

Request:

```json
{
  "description": "API trả đúng schema và mã lỗi đã thống nhất.",
  "sortOrder": 1
}
```

AI milestone:

```json
{
  "name": "Xây dựng API",
  "description": "Triển khai API nghiệp vụ",
  "duration": 2,
  "durationUnit": "WEEK",
  "budget": 30000000,
  "acceptanceCriteria": [
    "API trả đúng schema đã thống nhất.",
    "Kiểm thử tích hợp thành công."
  ]
}
```

## Data Model

`V45`:

1. Add nullable `acceptance_criteria.milestone_id`.
2. Clone every existing join-table link into one milestone-owned criterion.
3. Drop `milestone_acceptance_criteria`.
4. Delete remaining unowned global rows, including the fixed seed catalog.
5. Drop `criteria_code` and `is_active`.
6. Add `NOT NULL`, FK `ON DELETE CASCADE`, and composite index
   `(milestone_id, sort_order, criteria_id)`.

Existing `contract_milestones.criteria_snapshot` is not changed.

## UI / Platform Impact

Frontend should render AI criteria directly under each generated milestone and
use criterion ids after the job draft has been saved for fine-grained edits.

## Observability

Create, update, and delete criterion actions must write audit records.

## Alternatives Considered

1. Keep the global catalog and add custom rows. Rejected because it preserves
   the fixed-form mental model and allows criteria to be shared accidentally.
2. Store criteria as JSON on `milestones`. Rejected because fine-grained CRUD,
   ordering, audit identity, and relational cleanup are weaker.
3. Keep the join table but enforce one milestone per criterion. Rejected as an
   unnecessary many-to-many shape for a one-to-many domain rule.

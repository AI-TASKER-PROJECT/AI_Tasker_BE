# US-033 - AI-Generated Editable Milestone Acceptance Criteria

## Current Behavior

The platform seeds 26 global acceptance-criteria rows. A Business selects their
ids for each milestone through `milestone_acceptance_criteria`. AI-generated
milestones do not contain acceptance criteria, and only Admin can create a new
global criterion.

## Target Behavior

- Every AI-generated milestone includes criteria tailored to that milestone.
- Criteria belong to exactly one milestone, not to a global catalog.
- The owning Business can add, edit, delete, and reorder criteria before a
  contract exists.
- Job create and draft update persist criteria sent with each milestone.
- Existing milestone criteria are migrated into milestone-owned rows.
- Existing contract milestone snapshots remain unchanged.
- The 26 fixed global seed rows and the public catalog endpoint are removed.

## Affected Users

- Business: reviews and edits AI-generated criteria per milestone.
- Expert: reads criteria on published jobs and contract milestone snapshots.
- Admin: no longer maintains a global acceptance-criteria catalog.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`

## Non-Goals

- Changing milestone completion or deliverable-review rules.
- Editing criteria after a contract has been created.
- Regenerating criteria automatically after manual edits.


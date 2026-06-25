# Clean Audit Log Display

Date: 2026-06-26

## Status

Accepted

## Context

The admin audit log currently exposes technical API paths and table/id pairs in
fields that the frontend renders directly. This makes valid audit events look
unfinished and leaks implementation details such as
`/api/jobs/1/expert-recommendations #1`.

## Decision

Keep persisted audit rows unchanged, add raw response fields for debug
visibility, and normalize the existing display fields into Vietnamese business
labels at response mapping time. Known fallback API paths are translated to
explicit audit actions and clean entity labels. Numeric IDs are not shown in
the main display fields.

## Alternatives Considered

1. Rewrite existing audit rows. Rejected because historical audit evidence
   should remain durable and unchanged.
2. Only change frontend rendering. Rejected because backend is the source of
   audit semantics and can prevent technical values from leaking through any
   current client.

## Consequences

Positive:

- Admin audit logs read as business events instead of technical request logs.
- Existing historical rows become cleaner without a migration.
- Debug data remains available through `rawEntityName` and `rawEntityId`.

Tradeoffs:

- API clients that expected `entityName` and `entityId` to be raw technical
  fields should switch to the new raw fields.

## Follow-Up

- Frontend can later move raw audit fields into an expandable detail view.

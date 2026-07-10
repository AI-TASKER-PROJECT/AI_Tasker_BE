# Design

## Domain Model

`audit_logs` remains the durable audit record. Stored `action`, `entity_name`,
and `entity_id` continue to represent the original event, including historical
technical values.

## Application Flow

`AuditLogService.listForAdmin(...)` reads persisted logs, translates legacy
actions, resolves a clean action for known fallback API paths, attaches related
entity owner information, and returns clean display fields.

## Interface Contract

`AuditLogResponse` keeps the existing `action`, `entityName`, `entityId`, and
`entityDisplayName` fields, but these become presentation-safe. New
`rawEntityName` and `rawEntityId` fields carry the persisted technical values
for debug/detail views.

## Data Model

No migration is required. Historical rows are cleaned at response mapping time.

## UI / Platform Impact

The current frontend table reads `action`, `entityDisplayName`, `entityName`,
and `entityId`. After this change those fields no longer expose raw API paths
or numeric IDs for known audit rows. Detail/debug screens can adopt
`rawEntityName` and `rawEntityId` later.

## Observability

Audit data remains durable in `audit_logs`; only admin response presentation is
normalized.

## Alternatives Considered

1. Rewrite historical audit rows. Rejected because audit records should remain
   durable evidence and avoid data migration risk.
2. Hide the raw line only in the frontend. Rejected for this slice because the
   backend is the source of the admin audit response and can provide cleaner
   values without waiting for UI changes.

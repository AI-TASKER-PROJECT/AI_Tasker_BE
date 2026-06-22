# US-014 Improve Deliverable Notification targetUrl and Metadata

## Status

implemented

## Lane

normal

## Product Contract

When an Expert submits a deliverable, the notification sent to the Business must:
- Use `targetUrl = /contracts/{contractId}/workspace?milestoneId={milestoneId}` so the FE can open the correct contract workspace.
- Include a `metadata` field with `contractId`, `milestoneId`, and `deliverableId` so the FE can route to the exact milestone and deliverable context.

Existing notifications continue to work without metadata (backward compatible).

## Relevant Product Docs

- `docs/product/contract-management.md`

## Acceptance Criteria

1. Deliverable notification `targetUrl` uses format `/contracts/{contractId}/workspace?milestoneId={milestoneId}`.
2. Notification response includes `metadata` field.
3. Metadata contains `contractId`, `milestoneId`, `deliverableId`.
4. Existing notification creation calls do not break (no compile errors).
5. Old notifications without metadata still work (metadata is nullable).
6. FE can use `targetUrl` to navigate to the contract workspace.
7. FE can use `metadata` to open the correct milestone/deliverable context.

## Design Notes

- Migration: `V34__add_notification_metadata.sql` adds nullable `metadata TEXT` column to `notifications` table.
- Entity: `NotificationEntity` gains `metadata` (String) field.
- Response: `NotificationResponse` gains `metadata` (Object) field, deserialized from JSON string via `ObjectMapper`.
- Service: Added overloaded `createAndPush(...)` accepting `Map<String, Object> metadata`; serializes to JSON string before save; deserializes in `toResponse()`.
- Backward compatibility: existing `createAndPush` without metadata delegates to the new overload with `null` metadata.
- Deliverable notification: `notifyDeliverableSubmitted` signature updated to accept `contractId` and `deliverableId`; constructs targetUrl and metadata Map.
- Caller: `ContractExecutionService.submitDeliverable()` passes `contract.getContractId()`, `saved.getDeliverableId()` to the notification method.

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` verifies correct args passed to `notifyDeliverableSubmitted` |
| Integration | Requires Docker/Postgres at 127.0.0.1:5433 |
| E2E | — |
| Platform | — |
| Release | — |

## Evidence

```text
$ mvn test -pl . -Dtest=ContractExecutionServiceTest -DfailIfNoTests=false
[INFO] Tests run: 23, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

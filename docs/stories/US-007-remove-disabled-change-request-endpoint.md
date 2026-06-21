# US-007 Remove disabled change-request endpoint

## Status

implemented

## Lane

tiny

## Product Contract

The `POST /api/v1/contracts/change-requests` endpoint has been disabled since
US-005 and always threw an error. The dead endpoint and every reference to it
are now removed from the codebase and documentation, so callers cannot discover
or attempt the removed flow.

## Relevant Product Docs

- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`

## Acceptance Criteria

1. Controller no longer exposes `POST /api/v1/contracts/change-requests`.
2. Service method `requestChange` is removed.
3. OpenAPI spec (`docs/openapi/openapi-v1.json`) no longer includes the path or
   related schemas (`ContractChangeRequestEntity`,
   `ApiResponseContractChangeRequestEntity`).
4. All docs files that listed the disabled endpoint are updated.
5. Backwards-compatible: no existing production client depends on a dead
   endpoint.

## Design Notes

- Commands: none
- Queries: none
- API: removed `POST /api/v1/contracts/change-requests`
- Tables: `contract_change_requests` table is unchanged (historical data +
  migration integrity)
- Domain rules: unchanged; the flow was already disabled
- UI surfaces: none

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ContractExecutionServiceTest` no longer imports `ContractChangeRequestEntity` or mocks `ContractChangeRequestRepository`; `requestChange_shouldThrowBecauseFlowIsDisabled` test removed |
| Integration | `.\mvnw.cmd test` passes (55 tests, 0 failures) |
| E2E | — |
| Platform | — |
| Release | — |

## Harness Delta

None.

## Evidence

```text
[INFO] Tests run: 55, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

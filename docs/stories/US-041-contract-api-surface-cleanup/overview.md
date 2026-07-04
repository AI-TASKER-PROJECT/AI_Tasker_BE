# US-041 - Contract API Surface Cleanup

## Current Behavior

Milestone/dispute/termination v2 behavior is implemented, but the public
Swagger surface includes duplicate aliases, legacy transaction/dispute routes,
and support/system endpoints under Contract Execution Flow.

## Target Behavior

Keep the v2 service behavior intact while exposing one clear v1 route for each
public contract execution action. Runtime Swagger should report about 151 total
operations and about 47 Contract Execution Flow operations.

## Affected Users

- Business
- Expert
- Staff
- Admin
- Backend/API consumers

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`
- `README.md`
- `BACKEND_TEST_GUIDE.md`

## Non-Goals

- Do not rollback milestone escrow, dispute, termination, settlement, or review
  business behavior.
- Do not delete service methods that remain useful for tests or internal
  compatibility.
- Do not introduce `/api/v2` routes.

# Exec Plan

## Goal

Reorganize Swagger by business flow rather than alphabetical controller tags,
then refresh the Swagger overview and test guide so they match the new rendered
order and include per-endpoint response/message guidance sourced from backend
logic.

## Scope

In scope:

- Re-tag or remap Swagger operations into flow-oriented groups.
- Define stable flow ordering in OpenAPI/Swagger UI.
- Rebuild `docs/swagger-api-overview.md` in the same rendered order.
- Rebuild `docs/swagger-api-test-guide.md` with per-endpoint body guidance,
  test conditions, and response code/message sections.
- Verify that every Swagger-visible endpoint is assigned to exactly one flow.

Out of scope:

- Changing endpoint paths, DTO schemas, or business behavior.
- Documenting WebSocket/STOMP flows inside Swagger UI.
- Expanding DB `.docx` reference in this task unless needed by Swagger contract drift.

## Risk Classification

Risk flags:

- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- None.

## Work Phases

1. [x] Inventory every Swagger-visible endpoint and current grouping.
2. [x] Design the flow-to-endpoint mapping and tag ordering.
3. [x] Update source OpenAPI tagging/order.
4. [x] Refresh overview and test guide in the new order.
5. [x] Verify route coverage, order alignment, and runtime/openapi behavior.
6. [x] Record Harness proof and trace.

## Stop Conditions

Pause for human confirmation if:

- A route clearly belongs to more than one main flow and grouping changes would be subjective.
- Runtime Swagger behavior contradicts source annotations in a way that needs framework-level customization beyond the planned scope.
- Validation requires weakening the requirement that every endpoint appears exactly once.

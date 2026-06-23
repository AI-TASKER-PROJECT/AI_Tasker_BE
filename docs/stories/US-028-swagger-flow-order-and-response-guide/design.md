# Design

## Domain Model

This task does not change business entities. It changes how the existing REST
surface is presented and documented. The effective model is a documentation map
from concrete HTTP operations to one business flow tag.

Main flow tags:

- `01. Auth Flow`
- `02. Profile Verification Flow`
- `03. Job Draft & Publish Flow`
- `04. Proposal Flow`
- `05. Wallet & Payment Flow`
- `06. Contract Execution Flow`

Supporting tags are placed after the main six for smaller or cross-cutting
surfaces such as admin, catalog, notifications, chatbot/AI support, health, and
test/demo endpoints.

## Application Flow

Swagger grouping should be derived from endpoint purpose, not controller name.
Because several controllers mix multiple business concerns, method-level tag
assignment or OpenAPI post-processing is preferred over a naive controller-level
retag.

## Interface Contract

HTTP paths and payloads remain unchanged. Only the Swagger tag metadata and the
hand-written overview/test guide structure change.

The test guide response sections should capture:

- HTTP success code(s) used by the controller;
- common business-rule `400` messages thrown via `AppException`;
- `404` messages when entity lookups fail;
- `409` conflict messages where applicable;
- `500` as fallback for unexpected runtime failures.

## Data Model

No Flyway or table changes. Existing docs must stay aligned with source classes,
`SecurityConfig`, and service-layer validation/error messages.

## UI / Platform Impact

Swagger UI tag order is user-visible product behavior for developer tooling.
This task changes first-level navigability and the perceived API workflow.

## Observability

Proof should include route inventory checks, compile coverage, and a runtime or
generated OpenAPI verification that the tag order matches the intended flow
order.

## Alternatives Considered

1. Keep alphabetical tag sort and only reorder markdown guides. Rejected because
   Swagger UI itself would remain confusing and diverge from the guides.
2. Retag entire controllers only. Rejected because some controllers span
   multiple business flows and would still produce mixed groupings.

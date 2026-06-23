# Overview

## Current Behavior

Swagger UI currently sorts tags alphabetically through
`springdoc.swagger-ui.tags-sorter=alpha`. The result is that endpoints are
grouped by controller names rather than by user-facing business flows, which
makes auth, profile verification, draft-job, proposal, wallet/payment, and
contract execution scenarios hard to follow in Swagger.

The current `docs/swagger-api-overview.md` and `docs/swagger-api-test-guide.md`
mirror that controller-first ordering. They do not consistently surface per-API
response code coverage and business messages from backend logic in a structured,
flow-first way.

## Target Behavior

Swagger UI presents the API in this flow order:

1. Auth Flow
2. Profile Verification Flow
3. Job Draft & Publish Flow
4. Proposal Flow
5. Wallet & Payment Flow
6. Contract Execution Flow
7. Supporting flows such as Notification and other smaller groups below the main flows

All endpoints must still appear exactly once, under the correct flow grouping.
`docs/swagger-api-overview.md` and `docs/swagger-api-test-guide.md` must follow
the same order. The test guide must include:

- explanation for each API;
- conditions required to test it;
- per-API request body guidance;
- per-API response codes and notable backend messages collected from code paths.

## Affected Users

- Developers and testers using Swagger UI to execute end-to-end backend flows.
- Business analysts or reviewers tracing the API by business workflow.

## Affected Product Docs

- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/ARCHITECTURE.md`

## Non-Goals

- No REST path or request/response contract change.
- No database schema or migration change.
- No WebSocket flow documentation expansion beyond Swagger-visible APIs.

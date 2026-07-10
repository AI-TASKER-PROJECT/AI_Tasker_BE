# Overview - US-048

## Current Behavior

The v2.2 Flow 4-5 implementation still starts the milestone clock when Expert
starts work, exposes structured progress-report feedback, requires Admin to
assign milestone-dispute Staff, allows Staff to reject intervention, blocks an
early Staff decision, and leaves participant-deposit refunds behind a manual
Admin command. Business also cannot cancel its own unsigned draft contract.

## Target Behavior

Implement the binding v2.3 decisions in `SPEC-MILESTONE-DISPUTER.md`: escrow
deposit starts the milestone timeline, progress reports use a Business
acknowledgement gate, Business can cancel its own draft, milestone disputes are
routed without Admin, Staff cannot reject intervention or be blocked by the
evidence window, and participant deposits are refunded automatically at normal
completion or valid termination.

## Affected Users

- Business
- Expert
- Staff
- Admin audit/retry operators

## Affected Product Docs

- `SPEC-MILESTONE-DISPUTER.md`
- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `docs/openapi/openapi-v1.json`

## Non-Goals

- Changing termination-request Staff assignment.
- Adding appeals or Admin dispute decisions.
- Deleting legacy progress-feedback columns or historical dispute values.
- Changing external payment providers.

# Overview

## Current Behavior

Contract draft creation, signing, NDA signing, deliverable submission, and
termination existed, but activation moved jobs directly to `CLOSED`, expert
contract rejection was missing, completion did not close the contract from
milestone completion, and the database did not allow the job/milestone statuses
required by `SPEC-CONSTRACT.md`.

## Target Behavior

The backend supports the contract lifecycle from accepted proposal through
draft, negotiation, activation, rejection, deliverable review, milestone
completion, and contract completion.

## Affected Users

- Business.
- Expert.
- Admin.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`
- `docs/postman-api-test-guide.md`

## Non-Goals

- Provider-backed payment escrow hardening.
- NDA PDF generation or file storage.
- Full dispute fund locking.

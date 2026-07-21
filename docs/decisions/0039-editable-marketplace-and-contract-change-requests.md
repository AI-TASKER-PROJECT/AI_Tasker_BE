# 0039 Editable Marketplace And Contract Change Requests

Date: 2026-07-21

## Status

Accepted

## Context

The current backend only allows full job edits while a job is `DRAFT`, exposes
some milestone commands without their owning job or contract path context, and
keeps the old `contract_change_requests` table without an active API. The
product now needs safer editability for public jobs and proposals, plus a
two-party approval flow for changing contract scope, milestones, budget, or
timeline after a draft or active contract exists.

## Decision

Add context-rich API aliases while keeping the existing milestone routes as
compatibility routes. Job edits are allowed for owning Businesses while the job
is `DRAFT` or `OPEN`; contract-owned jobs cannot be edited directly. Expert
proposal edits are allowed while status is `Pending` or `Accepted`, but only
before the proposal has produced a contract.

Restore contract change requests through the existing
`contract_change_requests` table. Either contract participant can request a
change while the contract is `DRAFT`, `PENDING`, or `ACTIVE`. The requester
stores a detailed summary and optional proposed budget, timeline, scope, and
milestone snapshot JSON. The counterparty must accept before the backend applies
changes to the contract and editable contract milestones.

## Alternatives Considered

1. Allow direct contract edits by either participant. Rejected because active
   contracts drive deposits, escrow, deadlines, acceptance criteria, and
   disputes.
2. Add a separate append-only table for every field delta. Rejected for this
   slice because the existing `contract_change_requests` table already captures
   the workflow and can be extended.
3. Remove old milestone routes. Rejected because existing frontend clients may
   still call them.

## Consequences

Positive:

- Public API paths can carry the owning job/contract context.
- Proposal and public job edits become flexible without mutating active
  contract truth.
- Contract changes require explicit counterparty approval.
- Audit logs and notifications remain aligned with existing Vietnamese event
  wording.

Tradeoffs:

- The system keeps compatibility routes during migration, so docs must clearly
  prefer the new routes.
- Contract milestone replacement is guarded and cannot modify completed,
  cancelled, or escrow-released milestones.

## Follow-Up

- Frontend should move from compatibility milestone routes to context-rich
  routes.

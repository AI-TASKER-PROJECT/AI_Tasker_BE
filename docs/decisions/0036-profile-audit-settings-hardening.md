# 0036 Profile Audit Settings Hardening

Date: 2026-07-17

## Status

Accepted

## Context

Profile upsert routes were doing double duty as first submission and later update APIs. That made approved profiles reopen review and pending profiles accept repeated submissions. Audit fallback logging also recorded repeated PayOS sync requests even when no payment state changed, while terminal wallet top-up outcomes had no explicit product audit event. Admin settings included seed/demo controls not consumed by backend flows.

## Decision

Keep the existing routes, but make service behavior state-aware. Pending profile submissions are locked, rejected profiles can resubmit, approved profiles can update mutable information without returning to review, and Staff can review only pending profiles. PayOS sync records audit only for terminal status transitions and the fallback filter skips PayOS sync routes. Admin settings is allowlisted to backend-supported operational keys while obsolete seed settings are soft-disabled.

## Alternatives Considered

1. Add separate update-profile routes for approved profiles. Rejected because the current FE already targets the upsert endpoints and service state rules are enough.
2. Audit every PayOS sync call. Rejected because repeated polling is operational noise when the payment order state does not change.
3. Delete obsolete settings. Rejected because soft-disable preserves existing DB history and avoids destructive migration risk.

## Consequences

Positive:

- Approved users no longer lose approval because they updated profile information.
- Staff review actions become final unless the owner resubmits after rejection.
- Audit logs better describe payment outcomes and dispute participants.
- Admin settings focuses on real backend controls.

Tradeoffs:

- Generic arbitrary setting creation is no longer accepted by the backend admin API.
- Existing obsolete setting rows remain in DB but are inactive/hidden.

## Follow-Up

- Consider profile review history if product needs a full audit trail of every resubmission attempt.

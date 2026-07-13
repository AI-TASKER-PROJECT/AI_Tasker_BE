# US-056: Dispute Self-Resolve Negotiation

## Current Behavior

Opening a milestone dispute creates `PENDING_SELF_RESOLVE`, but participants had no persisted negotiation timeline or agreement action.

## Target Behavior

Business and Expert can read and create negotiation replies while a dispute is pending self-resolve. A participant can accept the other participant's proposal. Revision restores the milestone to work; deliverable acceptance and next-milestone agreement release escrow using the existing ledger rules.

## Scope

- `GET` and `POST /api/v1/disputes/{disputeId}/self-resolve-replies`
- `POST /api/v1/disputes/{disputeId}/self-resolve-agreement`
- Persisted reply timeline, audit entries, participant notifications, and locked agreement transition.

## Non-goals

- Partial-refund settlement.
- Frontend implementation in this backend repository.
- Staff/Admin creating participant replies.

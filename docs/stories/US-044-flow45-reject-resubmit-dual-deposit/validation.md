# Validation — US-044

## Proof Strategy

Check financial formulas, state transitions, forbidden automatic dispute
creation, dual-deposit activation, API versioning, and cross-section consistency.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Specification-only; implementation tests deferred. |
| Integration | Specification-only; migration proof deferred. |
| E2E | Specification-only. |
| Platform | Every declared route uses `/api/v1`. |
| Performance | Not applicable. |
| Logs/Audit | Required deposit/reject/penalty events documented. |

## Fixtures

Use a 100,000,000 contract example: Business deposit 20,000,000; Expert deposit
10,000,000; immediate penalty 10,000,000.

## Commands

```text
command:
Static HTTP operation scan
result:
pass — 42 declared HTTP operations, 0 outside /api/v1

command:
Required rule scan
result:
pass — found UNDER_REVIEW -> IN_PROGRESS, Business=20%, Expert=10%,
expert-deposit/pay, totalBudget*10%, normal submission-round increment, and
standard-vs-immediate termination separation

command:
Forbidden positive-rule scan
result:
pass — no rule that rejection creates PENDING_SELF_RESOLVE, no no-penalty
immediate-termination rule, no createDisputeFromBusinessRejection method

command:
git diff --check
result:
pass
```

## Acceptance Evidence

- Reject/resubmit, explicit dispute, dual deposit, and 10% penalty rules appear
  in state, service, schema, API, authorization, invariant, scenario, and
  acceptance sections.
- Decision 0025 supersedes the penalty-free part of decision 0024.
- All 42 declared operations use `/api/v1`.
- No backend implementation is claimed.

# Validation — US-043

## Proof Strategy

Statically verify required v2.1 concepts, forbidden concepts, route prefixes,
state-machine consistency, and section coverage.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Not applicable; specification-only story. |
| Integration | Not applicable until implementation. |
| E2E | Not applicable until implementation. |
| Platform | Check all documented public routes use `/api/v1`. |
| Performance | Not applicable. |
| Logs/Audit | Confirm required events are specified. |

## Fixtures

No runtime fixtures required.

## Commands

```text
command:
PowerShell static route and required-concept scan
result:
pass — 39 HTTP route declarations, 0 routes outside /api/v1
notes:
Verified OVERDUE, 24h/12h report SLA, Staff evidence/SLA fields,
AWAITING_EXPERT_RESPONSE, and penalty-free immediate termination.

command:
Forbidden implementation vocabulary scan
result:
pass — 0 occurrences of ADJUST action code, REPORT_REVISION_REQUESTED constant,
or fixed abrupt-termination penalty transaction types
notes:
Negative explanatory statements remain intentionally present in the spec.

command:
git diff --check
result:
pass
notes:
Only the repository's line-ending conversion warning was printed.
```

## Acceptance Evidence

- SPEC v2.1 contains explicit binding decisions rejecting Admin dispute override
  and every fixed 10% immediate-termination penalty.
- State machines, schema, services, API, authorization, invariants, tests, and
  acceptance criteria include the accepted backend changes.
- Every declared public HTTP route uses `/api/v1`.
- This story validates specification completeness only and does not claim
  backend implementation.

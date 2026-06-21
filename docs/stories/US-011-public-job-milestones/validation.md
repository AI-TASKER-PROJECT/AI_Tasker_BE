# Validation

## Proof Strategy

Verify the application still compiles after the security matcher change and
confirm docs reflect the public milestone contract for `OPEN` jobs.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Existing service tests still compile against unchanged service logic. |
| Integration | Security matcher accepts unauthenticated `GET /api/v1/jobs/{jobId}/milestones`. |
| E2E | Manual public job detail flow can request milestones without login. |
| Platform | N/A |
| Performance | N/A |
| Logs/Audit | Existing protected-path behavior unchanged for non-`OPEN` jobs. |

## Fixtures

Use any existing `OPEN` job record for manual verification.

## Commands

```text
.\mvnw.cmd -DskipTests compile
```

## Acceptance Evidence

Pending verification.

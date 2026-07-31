# Validation

## Proof Strategy

Use focused unit tests around profile state transitions, audit mapping, PayOS audit behavior, fallback filtering, and settings allowlist. Run compile/full Maven tests when the existing worktree conflicts are resolved enough for Java compilation.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Profile pending/approved/rejected behavior; Staff review finality; PayOS terminal audit; audit display normalization; settings allowlist. |
| Integration | Flyway migration parse through full suite if PostgreSQL is available. |
| E2E | Not required for this backend-only change. |
| Platform | OpenAPI/docs route inventory unchanged unless runtime verification is available. |
| Performance | Not applicable. |
| Logs/Audit | Verify no PayOS sync fallback audit spam and terminal wallet top-up audit events are recorded once per transition. |

## Fixtures

Mock Business, Expert, Staff, Admin accounts; mock PayOS payment links; mock dispute contract participants and system settings.

## Commands

```text
command: .\mvnw.cmd clean '-Dtest=ProfileServiceTest,AdminServiceTest,AuditLogServiceTest' test
result: pass, 46 tests, 0 failures, 0 errors
notes: Focused clean compile/test for profile state rules, settings allowlist, and audit display normalization.

command: .\mvnw.cmd '-Dtest=ContractExecutionServiceTest' test
result: pass, 79 tests, 0 failures, 0 errors
notes: Focused regression for dispute auto-assignment audit action.

command: .\mvnw.cmd test
result: pass, 370 tests, 0 failures, 0 errors
notes: Full Maven suite passed after updating the dispute auto-routing assertion.
```

## Acceptance Evidence

- Pending profiles cannot be resubmitted; approved profiles update without reopening review; Staff can review only pending profiles. Covered by `ProfileServiceTest`.
- Admin settings exposes/accepts only backend-supported keys. Covered by `AdminServiceTest`.
- Audit display maps dispute objects to participant context instead of Staff fallback and normalizes new action labels. Covered by `AuditLogServiceTest` and `ContractExecutionServiceTest`.
- PayOS sync fallback spam is prevented by `AuditRequestFilter`; terminal top-up outcomes are recorded by `PayOSPaymentService` on status transition.
- Full regression: `.\mvnw.cmd test` passed 370 tests with Flyway applying migrations through `V63`.
- Harness trace: `#128` recorded the completed high-risk implementation and validation proof.

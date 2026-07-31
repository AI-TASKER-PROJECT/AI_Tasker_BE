# Validation

## Expected Proof

| Layer | Proof |
| --- | --- |
| Unit | Routing fairness, capacity, eligibility, inbox and authorization tests |
| Integration | Flyway V60 and Hibernate validation on PostgreSQL |
| Regression | Full Maven suite |
| Static | `git diff --check` and migration/domain coverage queries |

## Evidence

- Focused routing/inbox/auth suite:
  `.\mvnw.cmd "-Dtest=ContractExecutionServiceTest,StaffDisputeInboxRoutingTest,StaffDisputeInboxTest,StaffDisputeAuthTest" test`
  passed 96 tests, 0 failures, 0 errors.
- `routeDispute_balancesRepeatedAssignmentsAcrossEquallyQualifiedStaff` routed
  six identical disputes 3/3 and verified the pessimistic Staff-pool query was
  used for every assignment.
- `routeDispute_rejectsWhenEveryQualifiedStaffIsAtCapacity` returned
  `NO_AVAILABLE_STAFF_CAPACITY` without saving an assignment.
- `listStaffCandidates_excludesContractParticipantAccountConflict` excluded a
  Staff account linked to a contract participant profile.
- `escalateDispute_shouldRemainRequestedWhenQualifiedStaffIsAtCapacity` kept
  the dispute at `ESCALATION_REQUESTED` with no assigned Staff.
- Full `.\mvnw.cmd test` passed 351 tests, 0 failures, 0 errors.
- PostgreSQL Flyway history applied V60 and V61 successfully.
- Live coverage query returned `undercovered_business_domains = 0`.
- PostgreSQL exposes `idx_disputes_active_staff_workload` and
  `idx_disputes_staff_last_assigned`.
- `git diff --check` passed; only Windows LF-to-CRLF notices were printed.
- Harness story verification passed; implementation traces are `#112` and
  `#113`.

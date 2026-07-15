# US-062 Balanced Staff Dispute Routing

## Status

implemented

## Lane

normal

## Product Contract

Dispute routing must preserve domain expertise while distributing repeated work
across qualified Staff, respecting a configurable active-case capacity, and
preventing concurrent requests from selecting against the same stale workload.

## Acceptance Criteria

- Only approved Staff sharing a job domain and not linked to a contract
  participant account are eligible.
- Specialization is normalized from domain and skill coverage.
- Qualified Staff are ordered by workload before specialization tie-breakers.
- Six identical disputes split 3/3 across two equally qualified Staff.
- Staff at configured capacity receive no additional automatic/manual routing.
- An escalation with no available Staff remains `ESCALATION_REQUESTED`.
- Concurrent routing locks Staff rows in deterministic order.
- Every seeded business domain has at least two Staff mappings.
- Active workload counting has a matching partial PostgreSQL index.

## Files

- `src/main/java/com/aitasker/be/service/core/ContractExecutionService.java`
- `src/main/java/com/aitasker/be/repository/StaffRepository.java`
- `src/main/java/com/aitasker/be/repository/DisputeRepository.java`
- `src/main/resources/db/migration/V60__balance_staff_dispute_routing.sql`
- `src/main/resources/db/migration/V61__index_staff_dispute_assignment_history.sql`
- `src/test/java/com/aitasker/be/service/core/StaffDisputeInboxRoutingTest.java`
- `docs/decisions/0033-balanced-staff-dispute-routing.md`

## Validation

See `validation.md`.

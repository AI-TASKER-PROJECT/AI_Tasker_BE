# Staff Dispute Inbox & Specialization Routing

## Status

planned

## Lane

high-risk (authorization, data model, public API contract, existing dispute flow)

## Product Contract

Staff needs a dedicated inbox containing only assigned milestone disputes, and
the routing workflow must assign every dispute to Staff whose configured domain
specialization matches the disputed job.

`SPEC-MILESTONE-DISPUTER.md` v2.3 remains authoritative for dispute lifecycle
and settlement behavior:

- Assigned Staff is the only role that decides the Expert payout percentage.
- A valid Staff decision triggers settlement automatically.
- Admin does not assign, approve, revise, or cancel a milestone dispute.
- Domain match is mandatory. Workload alone must never route a dispute to Staff
  outside the job's domain.

## User Stories

**As a Staff member**, I want one paginated inbox containing only disputes
assigned to me so I can review and resolve my cases without discovering them
indirectly through contracts.

**As Staff operations**, I want automatic and manual routing to accept only
Staff whose configured domains match the job, then rank those Staff by relevant
skills and workload, so every assigned reviewer has appropriate expertise.

**As an Admin**, I want to configure structured domain and skill assignments
for Staff so routing does not depend on free-text specialization.

## Current Behavior

1. Staff can discover assigned work indirectly through
   `GET /api/v1/contracts`, then list disputes for each contract.
2. There is no dedicated Staff dispute inbox API.
3. Once assigned to one dispute, Staff can currently list or read other
   disputes belonging to the same contract.
4. `staff.specialization` is a free-text field without structured domain or
   skill mappings.
5. Candidate ranking exposes the specialization text but primarily sorts by
   active dispute workload; it does not compare job domains or skills.
6. Automatic routing can therefore select a Staff member outside the job's
   professional domain.

## Target Behavior

### 1. Structured Staff Specialization

Add additive mapping tables:

```text
staff_domains(staff_id, domain_id)
staff_skills(staff_id, skill_id)
```

Rules:

- `(staff_id, domain_id)` and `(staff_id, skill_id)` are composite primary keys.
- Both columns use foreign keys to their owning Staff and catalog records.
- Add indexes supporting lookup by `domain_id` and `skill_id`.
- Every Staff account must have at least one configured domain.
- Skills are optional and are used only for ranking after domain eligibility.
- Keep the legacy `specialization` string for backward-compatible display, but
  never use it as routing truth.
- Backfill demo Staff with explicit domain and skill mappings. Existing Staff
  whose specialization cannot be mapped safely must be reported for Admin
  configuration; the migration must not guess from ambiguous free text.

Update the Admin Staff contract:

```text
POST  /api/v1/admin/staffs
PATCH /api/v1/admin/staffs/{staffId}
```

The request accepts `domainIds` and `skillIds`. `domainIds` is required and
must contain at least one unique, existing domain. `skillIds` may be empty but
every supplied ID must exist. Staff responses expose structured `domains` and
`skills` in addition to the legacy specialization display value.

Admin must not remove a Staff member's final domain mapping. Admin must also be
warned and the update rejected when removing a mapping would leave a domain
used by active jobs without any eligible Staff coverage.

### 2. Domain-Mandatory Staff Routing

Automatic routing is used by escalation and by:

```text
POST /api/v1/disputes/{disputeId}/route-staff
```

when `staffId` is omitted.

Routing algorithm:

1. Resolve the dispute's contract, job, job domains, and required job skills.
2. Reject routing if the job has no configured domain.
3. Keep only Staff having at least one `staff_domains` record matching a job
   domain.
4. Rank eligible Staff deterministically by:
   - matched domain count descending;
   - matched required-skill count descending;
   - active assigned-dispute workload ascending;
   - `staffId` ascending.
5. Assign the first candidate and move the dispute to `STAFF_REVIEWING` using
   the existing access, SLA, audit, and notification behavior.

Mandatory guards:

- Manual routing with `staffId` must apply the same domain eligibility check.
- No workload or free-text fallback is permitted for a domain mismatch.
- If no matching Staff exists, leave the dispute and assignment unchanged,
  keep `ESCALATION_REQUESTED`, and return
  `NO_MATCHING_STAFF_FOR_JOB_DOMAIN`.
- A failed routing attempt must not create access grants, audit assignment
  events, or assignment notifications.

Update the existing candidate API:

```text
GET /api/v1/disputes/{disputeId}/staff-candidates
```

It returns only domain-eligible Staff and includes `matchedDomains`,
`matchedSkills`, `availability`, `activeDisputeWorkloadCount`, and
`conflictEligible`. Ordering must be identical to automatic routing.

### 3. Staff Dispute Inbox

Add a Staff-only API:

```text
GET /api/v1/staff/disputes
```

Query parameters:

| Parameter | Type | Default | Meaning |
|---|---|---:|---|
| `page` | integer >= 0 | `0` | Zero-based page number |
| `size` | integer 1..100 | `20` | Page size |
| `status` | dispute status | - | Exact optional status filter |

Default ordering is `createdAt DESC`, then `disputeId DESC`.

The backend derives `staffId` from the authenticated Staff account. The client
cannot provide or override `assignedStaffId`.

The paginated response uses a purpose-built DTO containing:

- dispute, contract, milestone, and job identifiers;
- dispute status, reason, initiation type, and creation timestamp;
- job domains and required skills;
- matched Staff domains and skills captured from current routing data;
- evidence deadline, Staff SLA deadline, and review start time;
- settlement decision status without unrelated wallet or participant-private
  data.

### 4. Assigned-Case Authorization

- Only role `STAFF` may call `/api/v1/staff/disputes`.
- The inbox returns only disputes whose `assigned_staff_id` equals the current
  Staff profile ID.
- `GET /api/v1/disputes/{disputeId}` must require that exact dispute assignment
  for Staff callers.
- `GET /api/v1/contracts/{contractId}/disputes` must return only disputes
  assigned to the current Staff, not every dispute in the contract.
- Staff access to dispute attachments, deliverables, milestone criteria, and
  supporting contract data must be scoped to the assigned case.
- Assigned Staff retains `READ_EXECUTE` access and remains the only Staff
  allowed to submit that dispute's decision.
- Admin and participant read behavior remains unchanged.

## Data And Compatibility Rules

- Add a new Flyway migration; do not modify migrations that may have run.
- Repository queries must paginate and filter in the database rather than load
  every dispute and filter in memory.
- Candidate matching must use catalog IDs, not localized names or substring
  matching.
- Existing `assigned_staff_id` records remain valid. New routing and reassignment
  operations use the domain guard after deployment.
- Existing notification types and automatic settlement behavior remain intact.
- The Admin dispute dashboard from US-049 remains unchanged.
- Termination-request Staff routing is outside this story.

## Acceptance Criteria

| # | Criteria | Verification |
|---|---|---|
| AC1 | Admin can create or update Staff with valid structured domains and skills; empty or unknown domains are rejected. | Admin service/controller tests |
| AC2 | Flyway creates mapping constraints and backfills demo Staff without guessing ambiguous production specialization text. | PostgreSQL migration test |
| AC3 | Auto-routing only considers Staff sharing at least one job domain. | Routing service test |
| AC4 | Domain count, skill count, workload, and Staff ID produce deterministic candidate ordering. | Ranking test |
| AC5 | Manual routing rejects a Staff member with no matching job domain. | Authorization/business-rule test |
| AC6 | No matching Staff leaves the dispute unchanged in `ESCALATION_REQUESTED` and emits no assignment side effects. | Transactional integration test |
| AC7 | Candidate API returns only eligible Staff with accurate match and workload fields. | Controller/service test |
| AC8 | Staff inbox pagination, status filtering, and ordering are correct and derive Staff identity from JWT. | Controller/integration test |
| AC9 | Staff A cannot list or read Staff B's dispute, attachments, or supporting case data, including disputes in the same contract. | RBAC/data-isolation test |
| AC10 | Assigned Staff can still decide payout and trigger settlement exactly once. | Dispute settlement regression test |
| AC11 | Swagger/OpenAPI and dispute-flow documentation describe the inbox, structured specialization, routing error, and tightened read scope. | Docs/OpenAPI validation |

## Execution Plan

| Step | Area | Action |
|---|---|---|
| 1 | Harness | Create a high-risk story and capture authorization, migration, and dispute-settlement proof requirements. |
| 2 | Data model | Add Staff-domain/skill mappings, repositories, indexes, and safe demo backfill in a new migration. |
| 3 | Admin Staff API | Replace raw entity input with request DTOs, validate catalog mappings, and return structured specialization data. |
| 4 | Routing | Implement domain eligibility, deterministic ranking, manual-route guard, and no-match transaction behavior. |
| 5 | Staff inbox | Add paginated Staff-owned dispute query, response DTO, service, and controller route. |
| 6 | Authorization | Tighten dispute, contract-dispute, attachment, and supporting-case reads to exact assigned-case scope. |
| 7 | Tests | Cover AC1-AC10 with focused unit, authorization, migration, and PostgreSQL-backed integration tests. |
| 8 | Documentation | Refresh Swagger/OpenAPI, API guides, dispute flow, story validation evidence, and Harness trace. |

## Non-Goals

- No frontend implementation.
- No Staff access to the global Admin dispute dashboard.
- No fallback assignment to Staff outside the job domain.
- No change to payout percentages, escrow invariants, or automatic settlement.
- No Admin approval, override, reassignment, or cancellation workflow for
  milestone disputes.
- No redesign of termination-request assignment.
- No removal of the legacy `specialization` display field in this story.

## Risk Checklist

| Flag | Applies? | Reason |
|---|---|---|
| Authorization | Yes | Tightens Staff ownership and supporting-case access. |
| Data model | Yes | Adds Staff-domain and Staff-skill mappings. |
| Public contract | Yes | Adds a Staff inbox and changes Staff administration/candidate DTOs. |
| Existing behavior | Yes | Replaces workload-only routing and narrows existing Staff reads. |
| Financial workflow | Yes | Must preserve assigned-Staff decision and one-time settlement behavior. |

**Classification:** high-risk. Completion requires focused tests, the full test
suite, PostgreSQL-backed migration validation, updated API documentation, story
verification, and a completed high-risk Harness trace.

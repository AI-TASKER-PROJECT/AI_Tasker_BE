# Decision 0027 — Staff Specialization & Routing

## Status

Accepted

## Context

Staff currently lack structured domain/skill specialization. The `specialization` field is free-text and routing (`rankedStaffCandidates`) ranks only by workload. Staff have no dedicated inbox for assigned disputes.

SPEC.md defines structured specialization tables, domain-mandatory routing, a Staff inbox, and tightened assigned-case authorization.

## Decision

1. **Additive mapping tables**: `staff_domains(staff_id, domain_id)` and `staff_skills(staff_id, skill_id)` with composite PKs, foreign keys, and indexes.
2. **Keep legacy `specialization`**: Display-only, never used as routing truth.
3. **Domain-first routing**: Staff must share at least one job domain. Ranking: matched domains DESC, matched skills DESC, workload ASC, staffId ASC.
4. **Manual route guard**: `staffId` parameter also checks domain eligibility.
5. **No-match behavior**: Leave dispute in `ESCALATION_REQUESTED`, return `NO_MATCHING_STAFF_FOR_JOB_DOMAIN`, no assignment side effects.
6. **Staff inbox**: `GET /api/v1/staff/disputes` with pagination, status filter, JWT-derived staff identity.
7. **Tighten authorization**: Staff dispute reads, contract-dispute list, and case-data access scoped to exact assignment.

## Consequences

- Admin gains structured specialization management.
- Routing is domain-expertise-guaranteed instead of workload-only.
- Staff gain a dedicated inbox (no more indirect contract discovery).
- Authorization boundary is narrower and safer.
- New migration V54 required.
- Existing settlement and decision flow is unchanged (regression tested).

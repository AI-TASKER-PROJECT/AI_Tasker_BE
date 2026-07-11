# Design

## Domain Model

### New Entities

- `StaffDomainEntity` — maps `staff_id` to `domain_id` via composite PK `StaffDomainId`.
- `StaffSkillEntity` — maps `staff_id` to `skill_id` via composite PK `StaffSkillId`.

### Modified Entities

- `StaffEntity` — adds no new columns. Keeps legacy `specialization` for display.

### Existing Entities (unchanged)

- `DisputeEntity` — `assignedStaffId` field, status machine, escrow/payout fields.
- `DomainEntity`, `SkillEntity` — catalog tables.
- `JobDomainEntity`, `JobSkillEntity` — job-to-catalog mappings.

## Application Flow

### Admin creates Staff

```
Admin POST /api/v1/admin/staffs { accountId, domainIds, skillIds, specialization }
  -> validate account exists and has STAFF role
  -> validate all domainIds exist
  -> validate all skillIds exist (optional)
  -> validate at least one domainId
  -> create StaffEntity row
  -> insert staff_domains rows
  -> insert staff_skills rows
  -> return StaffResponse with domains, skills, specialization
```

### Admin updates Staff

```
Admin PATCH /api/v1/admin/staffs/{staffId} { domainIds, skillIds, specialization }
  -> validate staff exists
  -> validate domainIds/skillIds exist
  -> validate at least one domainId (cannot remove last domain)
  -> optionally reject if removing domain would orphan active jobs
  -> replace staff_domains rows
  -> replace staff_skills rows
  -> update specialization if provided
  -> return StaffResponse
```

### Routing

```
POST /api/v1/disputes/{disputeId}/route-staff?staffId=... or auto
  -> resolve dispute -> contract -> job -> job domains/skills
  -> filter staff: must have at least one domain matching a job domain
  -> rank: domain count DESC, skill count DESC, workload ASC, staffId ASC
  -> if manual staffId: validate domain match, else reject
  -> if no match: return NO_MATCHING_STAFF_FOR_JOB_DOMAIN, keep ESCALATION_REQUESTED
  -> assign top candidate, move to STAFF_REVIEWING
```

### Staff Inbox

```
Staff GET /api/v1/staff/disputes?page=0&size=20&status=STAFF_REVIEWING
  -> derive staffId from JWT (current STAFF account)
  -> query disputes where assigned_staff_id = staffId
  -> apply optional status filter
  -> paginate: page * size offset, limit size
  -> order by createdAt DESC, disputeId DESC
  -> return paginated StaffDisputeListResponse
```

## Interface Contract

### New Endpoints

| Method | Path | Role | Purpose |
|--------|------|------|---------|
| GET | `/api/v1/staff/disputes` | STAFF | Paginated staff inbox |

### Modified Endpoints

| Method | Path | Change |
|--------|------|--------|
| POST | `/api/v1/admin/staffs` | Accept `domainIds`, `skillIds` |
| PATCH | `/api/v1/admin/staffs/{staffId}` | Accept `domainIds`, `skillIds` |
| GET | `/api/v1/disputes/{disputeId}` | STAFF requires assignment |
| GET | `/api/v1/contracts/{contractId}/disputes` | STAFF sees only assigned |
| GET | `/api/v1/disputes/{disputeId}/staff-candidates` | Domain-eligible only |
| POST | `/api/v1/disputes/{disputeId}/route-staff` | Domain guard on manual |

### New Response DTOs

- `StaffDisputeListItem` — dispute summary for inbox
- `StaffDisputeListResponse` — paginated wrapper
- `StaffDisputeFilter` — query params (page, size, status)

### Modified DTOs

- `StaffResponse` — add `domains`, `skills` lists
- `StaffAssignmentCandidateResponse` — add `matchedDomains`, `matchedSkills` fields

## Data Model

### V54 Migration

```sql
CREATE TABLE staff_domains (
    staff_id INTEGER NOT NULL REFERENCES staffs(staff_id),
    domain_id INTEGER NOT NULL REFERENCES domains(domain_id),
    PRIMARY KEY (staff_id, domain_id)
);

CREATE TABLE staff_skills (
    staff_id INTEGER NOT NULL REFERENCES staffs(staff_id),
    skill_id INTEGER NOT NULL REFERENCES skills(skill_id),
    PRIMARY KEY (staff_id, skill_id)
);

CREATE INDEX idx_staff_domains_domain_id ON staff_domains(domain_id);
CREATE INDEX idx_staff_skills_skill_id ON staff_skills(skill_id);
```

Backfill demo Staff with domains matching demo job domains.

## Observability

- Audit logs for staff creation/update with domain/skill changes remain through existing `AdminService` audit paths.
- No new audit log types required for this story.
- Routing failures (`NO_MATCHING_STAFF_FOR_JOB_DOMAIN`) are surfaced as API errors, not audit events.

## Alternatives Considered

1. Add a JSONB `specialization_config` column to `staffs` — rejected; separate join tables enable efficient indexed queries and referential integrity.
2. Use a single `staff_specializations(domain_id, skill_id)` union table — rejected; separate tables are clearer and more maintainable.
3. Keep workload-only ranking — rejected per spec; domain expertise is mandatory.

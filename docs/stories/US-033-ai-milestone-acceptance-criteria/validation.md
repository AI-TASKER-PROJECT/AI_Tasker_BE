# Validation

## Proof Strategy

Prove AI output, milestone persistence, Business ownership, mutation freeze
after contract creation, legacy migration, and unchanged contract snapshots.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | AI parses criteria; job create/update persists criteria; CRUD ownership and contract guards; snapshot uses milestone-owned rows |
| Integration | Flyway converts linked rows, removes global rows/join table, validates JPA schema |
| E2E | Generate -> save draft -> edit/add/delete criterion -> publish -> create contract -> verify snapshot |
| Platform | OpenAPI no longer exposes global catalog and exposes milestone CRUD |
| Performance | FK/index exists for milestone criterion reads and cascades |
| Logs/Audit | Criterion create/update/delete produces audit entries |

## Fixtures

- Approved Business owning a DRAFT job.
- Two milestones with generated criteria.
- One legacy milestone linked to a seeded criterion.
- One accepted proposal and contract snapshot.

## Commands

```powershell
.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest,MarketplaceServiceTest,ContractExecutionServiceTest" test
.\mvnw.cmd -DskipTests compile
docker compose up -d
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd "-Dtest=AiSowGenerationServiceTest,MarketplaceServiceTest,ContractExecutionServiceTest" test`
  passed 70 tests with 0 failures and 0 errors.
- Regression coverage verifies both criterion CRUD and bulk milestone update
  reject acceptance-criteria changes after a contract has been created.
- `.\mvnw.cmd -DskipTests compile` passed.
- A fresh PostgreSQL database `aitasker_us033_test` applied 42 migrations
  through `V48__milestone_owned_acceptance_criteria.sql`.
- Hibernate `ddl-auto=validate` initialized successfully against the V45 schema.
- Database readback showed four migrated criteria across three milestones,
  zero unowned criteria, and no `milestone_acceptance_criteria` table.
- Runtime `/v3/api-docs` confirmed:
  - old `/api/v1/acceptance-criteria` absent;
  - old `/api/v1/criteria` absent;
  - milestone criterion POST, PUT, and DELETE routes present.
- Full `.\mvnw.cmd test` remains red against the existing `aitasker_db` because
  its Flyway history contains older checksum drift for V30/V40 and migrations
  V42-V44 that are not present in this checkout. No Flyway repair or destructive
  database reset was performed.

# US-001 Architecture Backfill

## Status

implemented

## Lane

normal

## Product Contract

The repository architecture document should describe the backend that exists
today, including product domains, runtime stack, module boundaries, API surface,
authorization rules, data/migration rules, provider boundaries, known MVP
limitations, and validation expectations.

## Relevant Product Docs

- `docs/ARCHITECTURE.md`
- `docs/backend-logic-notes.md`
- `docs/swagger-api-overview.md`
- `docs/data-dictionary.md`
- `README.md`

## Acceptance Criteria

- `docs/ARCHITECTURE.md` no longer claims that no application code exists.
- Architecture notes include the active Spring Boot/PostgreSQL/Flyway/JWT stack.
- Service boundaries and core product domains are captured.
- Marketplace, contract execution, catalog, finance, AI/RAG, file, notification,
  and audit rules from existing docs are represented.
- Known MVP/non-production areas are called out.
- Validation guidance references current local backend commands.

## Design Notes

- Commands: no product command changes.
- Queries: no product query changes.
- API: documentation summarizes the current Swagger groups and public API rules.
- Tables: documentation summarizes the current domain model and Flyway rule.
- Domain rules: documentation preserves role, ownership, marketplace, contract,
  and finance rules from the source notes.
- UI surfaces: backend only; Swagger and Postman/manual checklists are the
  relevant surfaces.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 0 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Markdown/content review; no code unit tests required. |
| Integration | Not required for docs-only change. |
| E2E | Not required for docs-only change. |
| Platform | Not required for docs-only change. |
| Release | Harness trace and changed-file review. |

## Harness Delta

The Harness durable database was initialized because it was missing. This lets
future agents record intake, stories, traces, and proof matrix state.

## Evidence

- `rg -n "No application|No application stack|/activate|/api/v1/invoices|Direct invoice" docs\ARCHITECTURE.md docs\stories\US-001-architecture-backfill.md`
  found only the intentional note that older `/activate` references are legacy.
- Durable story proof recorded with `harness-cli story update`.

# Design

## Domain Model

The affected records are deterministic demo fixtures for recommendation testing:

- `account` rows `8101-8120` for Experts and `8401-8410` for Businesses.
- `expert_profiles`, `business_profiles`, `portfolios`, `jobs`, `sow`,
  job catalog mappings, `milestones`, `proposals`, `user_quotas`,
  `quota_usage_logs`, and `system_wallet` rows created from those IDs.

## Application Flow

Runtime services are unchanged. The fix only changes database seed state after
Flyway applies the new migration.

## Interface Contract

No route, DTO, or response envelope changes.

## Data Model

The new migration keeps the schema unchanged. It deletes the deterministic V39
demo rows in foreign-key-safe order, then inserts the corrected rows with
`lpad(n::text, width, '0')` for zero padding.

Migration assertions check:

- Seeded account emails have no spaces and are already trimmed.
- Seeded account phones have no spaces and are already trimmed.
- Seeded Expert national IDs have no spaces and are already trimmed.
- Seeded Business tax codes have no spaces and are already trimmed.

## UI / Platform Impact

No UI or platform-shell impact.

## Observability

Harness story and trace record the cleanup. The migration itself fails fast with
an exception if corrected seed identifiers still contain whitespace.

## Alternatives Considered

1. Edit `V39` directly. Rejected because the repo migration policy says not to
   edit old migrations.
2. Update malformed strings in place. Rejected because the request explicitly
   asked to remove bad data and seed again.

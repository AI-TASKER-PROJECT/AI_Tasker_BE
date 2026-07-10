# Clean Audit Log Display

## Current Behavior

Admin audit logs can expose technical request paths and raw entity keys such as
`/api/jobs/1/expert-recommendations #1`, `business_profiles #1`, and
`wallet_transactions #11`. Some legacy finance actions are also stored without
Vietnamese diacritics.

## Target Behavior

Audit log responses keep raw entity identifiers for debugging, but the fields
currently rendered by the admin UI are clean Vietnamese business text. The main
object display avoids numeric IDs and uses business names, expert names, job
titles, contract parties, or neutral fallback labels when related rows no
longer exist.

## Affected Users

- ADMIN reviewing platform activity.
- STAFF when reviewing audit evidence shared from admin workflows.

## Affected Product Docs

- `docs/ARCHITECTURE.md`

## Non-Goals

- No database schema changes.
- No deletion or migration of historical audit log rows.
- No frontend layout change in this backend slice.

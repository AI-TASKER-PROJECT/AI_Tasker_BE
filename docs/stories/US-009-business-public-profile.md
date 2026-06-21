# US-009 Business Public Profile Endpoint

## Status

implemented

## Lane

normal

## Product Contract

Experts (and other authenticated users) can view a business's public profile by
its `businessId`. The endpoint returns the business profile entity enriched with
the associated account's display name, so experts can see who owns the business
without needing a job reference.

## Relevant Product Docs

- `docs/product/README.md`

## Acceptance Criteria

1. `GET /api/v1/profiles/business/{businessId}` returns a `BusinessProfileEntity`
   with attached account `fullName` for any authenticated user (EXPERT, BUSINESS,
   STAFF, ADMIN).
2. Returns 404 when the business profile does not exist.
3. Returns 401 when the user is not authenticated.

## Design Notes

- Commands: none
- Queries: `GET /api/v1/profiles/business/{businessId}`
- API: new endpoint in `ProfileController`
- Tables: none (uses existing `business_profiles` and `account`)
- Domain rules: only returns existing approved/rejected/pending profiles; no
  status filter needed since the caller just needs the profile data
- UI surfaces: business public profile page viewed by experts

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ProfileServiceTest` covers `businessProfileById` |
| Integration | `.\mvnw.cmd test` passes |
| E2E | — |
| Platform | — |
| Release | — |

## Harness Delta

None.

## Evidence

```text
[INFO] Tests run: 60, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

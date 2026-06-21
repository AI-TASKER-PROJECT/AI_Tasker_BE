# US-010 Expert Public Profile Endpoint

## Status

implemented

## Lane

normal

## Product Contract

Businesses and other authenticated users can view an expert's public profile by
its `expertId`. The endpoint returns the expert profile entity enriched with
the associated account's display name and public title, so a business can render
an expert profile page without relying on proposal-specific data.

## Relevant Product Docs

- `docs/product/README.md`

## Acceptance Criteria

1. `GET /api/v1/profiles/expert/{expertId}` returns an `ExpertProfileEntity`
   with public account `fullName` and `title` for any authenticated user
   (EXPERT, BUSINESS, STAFF, ADMIN).
2. The public endpoint does not expose account `phone`.
3. Returns 404 when the expert profile does not exist.
4. Returns 401 when the user is not authenticated.

## Design Notes

- Commands: none
- Queries: `GET /api/v1/profiles/expert/{expertId}`
- API: new endpoint in `ProfileController`
- Tables: none (uses existing `expert_profiles` and `account`)
- Domain rules: only returns existing approved/rejected/pending profiles; no
  status filter needed since the caller just needs the profile data
- UI surfaces: expert public profile page viewed by businesses

## Validation

| Layer | Expected proof |
| --- | --- |
| Unit | `ProfileServiceTest` covers `expertProfileById` |
| Integration | focused Maven test for profile service |
| E2E | - |
| Platform | - |
| Release | - |

## Harness Delta

None.

## Evidence

```text
.\mvnw.cmd test
[INFO] Tests run: 66, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

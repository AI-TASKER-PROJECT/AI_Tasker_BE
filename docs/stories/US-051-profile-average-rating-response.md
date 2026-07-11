# US-051 Profile Average Rating Response Field

## Status

implemented

## Lane

normal

## Product Contract

Business and Expert profile read responses expose `averageRating` for frontend
profile display. The value is calculated from existing `reviews.rating` rows
where `reviews.reviewee_id` matches the profile owner `accountId`; it is not
stored as a new database column.

## Relevant Product Docs

- `docs/product/profile-contact-fields.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Acceptance Criteria

- Business profile responses include `averageRating`.
- Expert profile responses include `averageRating`.
- `averageRating` is calculated from `reviews` by reviewed account id.
- Swagger/OpenAPI docs expose the updated profile schema and current API list.
- No Flyway migration or persisted average-rating column is added.

## Design Notes

- Commands: profile reads call `ReviewRepository.averageRatingByRevieweeId`.
- Queries: native SQL `SELECT AVG(rating) FROM reviews WHERE reviewee_id = :revieweeId`.
- API: existing profile endpoints keep their paths and envelopes.
- Tables: existing `reviews`, `account`, `business_profiles`, and `expert_profiles`.
- Domain rules: `reviews` remains the source of truth; `averageRating` is a transient response summary.
- UI surfaces: frontend profile cards/pages can read `data.averageRating`.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | `ProfileServiceTest` verifies Business and Expert profile reads set `averageRating` from `ReviewRepository`. |
| Integration | Full Maven test suite with Flyway-backed Spring context. |
| E2E | Not required; response schema and service behavior are covered. |
| Platform | Runtime `/v3/api-docs` snapshot and docs inventory counts match. |
| Release | `git diff --check` and OpenAPI JSON parse. |

## Harness Delta

No Harness policy changes.

## Evidence

- `.\mvnw.cmd -Dtest=ProfileServiceTest test` passed: 26 tests, 0 failures, 0 errors.
- `.\scripts\bin\harness-cli.exe story verify US-051` passed the same focused ProfileService proof.
- `.\mvnw.cmd test` passed: 278 tests, 0 failures, 0 errors; Spring/Flyway context validated against local Docker PostgreSQL.
- Runtime `/v3/api-docs` was fetched from Spring Boot on port 8081 and saved to `docs/openapi/openapi-v1.json`; `BusinessProfileEntity` and `ExpertProfileEntity` schemas include `averageRating`.
- API inventory proof: OpenAPI operations = 165; `docs/swagger-api-overview.md` rows = 165; `docs/swagger-api-test-guide.md` sections = 165; `docs/postman-api-test-guide.md` sections = 165; missing/extra counts = 0.
- `git diff --check` passed with LF/CRLF warnings only.

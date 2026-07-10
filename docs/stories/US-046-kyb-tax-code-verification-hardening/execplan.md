# Execution Plan

## Steps

1. Create Flyway migration `V52__business_profile_verified_data.sql` adding `verified_representative` column.
2. Add `verifiedRepresentative` field to `BusinessProfileEntity`.
3. Add `existsByTaxCodeExcludingAccount` query to `BusinessProfileRepository`.
4. Inject `TaxCheckService` into `ProfileService` and rewrite `upsertBusiness()` to:
   - Validate MST format (10 or 13 digits)
   - Check duplicate MST across accounts
   - Call VietQR API for verification
   - Auto-fill `companyName`/`address` from VietQR
   - Store `verifiedRepresentative` from VietQR
5. Add unit tests for new MST validation logic in `ProfileServiceTest`.
6. Create story folder at `docs/stories/US-046-kyb-tax-code-verification-hardening/`.
7. Update relevant docs (`ARCHITECTURE.md`, `swagger-api-overview.md`).
8. Run `./mvnw compile` and `./mvnw test -Dtest=ProfileServiceTest`.

## Validation

- `./mvnw -DskipTests compile` — BUILD SUCCESS on 2026-07-10.
- `./mvnw test -Dtest=ProfileServiceTest` — all tests pass.

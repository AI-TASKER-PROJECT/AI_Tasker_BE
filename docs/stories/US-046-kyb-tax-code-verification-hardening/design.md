# Design

## API

No controller changes. Existing endpoints remain:

- `POST /api/v1/profiles/business` — request body only needs `taxCode` + `businessLicenseUrl` (companyName/address auto-filled from VietQR).
- `GET /api/v1/profiles/business` (list) and `GET /api/v1/profiles/business/{id}` (detail) now return `verifiedRepresentative` field in response.
- `POST /api/v1/profiles/approve/BUSINESS/{id}` — unchanged.

## Database

Migration `V52__business_profile_verified_data.sql`:

```sql
ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS verified_representative VARCHAR(255);
```

## Entity

`BusinessProfileEntity` gains 1 field:

```java
@Column(name = "verified_representative", length = 255)
private String verifiedRepresentative;
```

## Repository

`BusinessProfileRepository` gains 1 query method:

```java
@Query("SELECT COUNT(b) > 0 FROM BusinessProfileEntity b WHERE b.taxCode = :taxCode AND b.accountId <> :accountId")
boolean existsByTaxCodeExcludingAccount(@Param("taxCode") String taxCode, @Param("accountId") Integer accountId);
```

## Service Rules (`ProfileService.upsertBusiness`)

| Step | Rule |
|------|------|
| 1 | Require `BUSINESS` role |
| 2 | `taxCode` must not be blank |
| 3 | `taxCode` must match `\d{10}\|\d{13}` |
| 4 | `taxCode` must not be used by another account (`existsByTaxCodeExcludingAccount`) |
| 5 | `taxCode` must be verified via `TaxCheckService.checkTaxCode()` (VietQR API) |
| 6 | `companyName` and `address` are overwritten from VietQR response |
| 7 | `verifiedRepresentative` is stored from VietQR response |
| 8 | `kyb_status = "Pending"`, account status = `"Pending"`, staff notified |

## VietQR Integration

`TaxCheckService` is injected into `ProfileService`. No changes to `TaxCheckService` itself. Errors propagate naturally:

- `AppException("MA SO THUE KHONG HOP LE")` — invalid format
- `NotFoundException` — MST not found in VietQR
- `BadGatewayException` — VietQR API unreachable

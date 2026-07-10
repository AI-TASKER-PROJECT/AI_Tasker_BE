# US-046: KYB Tax Code Verification Hardening

## Status

planned

## Lane

normal (risk flags: Data model, External systems, Public contracts, Existing behavior)

## Product Contract

Khi Business nộp hồ sơ KYB, hệ thống tự động kiểm tra mã số thuế (MST) với cơ quan nhà nước qua VietQR API trước khi đưa vào hàng đợi duyệt của Staff. Hệ thống cũng tự động điền thông tin doanh nghiệp từ dữ liệu verified và kiểm tra trùng MST. Staff chỉ việc đối chiếu giấy phép kinh doanh với dữ liệu đã được verified.

## User Story

**Là một Business user**, tôi muốn hệ thống tự động xác thực MST của tôi khi nộp hồ sơ, để tôi không thể nộp MST sai hoặc trùng với doanh nghiệp khác, và thông tin công ty được tự động điền chính xác từ nguồn chính thức.

**Là một Staff**, tôi muốn thấy dữ liệu MST đã được hệ thống verified từ VietQR, để tôi chỉ cần đối chiếu giấy phép kinh doanh thay vì tự tra cứu MST thủ công.

## Current Behavior

1. Business gọi `POST /api/v1/profiles/business` với `taxCode`, `companyName`, `address`, `businessLicenseUrl` — toàn bộ do user tự nhập.
2. `ProfileService.upsertBusiness()` chỉ kiểm tra `taxCode` không blank, không kiểm tra MST có tồn tại hay không.
3. `BusinessProfileRepository.existsByTaxCode()` đã có nhưng **không được gọi** trong `upsertBusiness()` — 2 doanh nghiệp có thể dùng chung MST.
4. `TaxCheckService` đã có (gọi VietQR API `https://api.vietqr.io/v2/business/{mst}`) nhưng chỉ truy cập qua endpoint public `GET /api/auth/tax-check/{mst}`, không được tích hợp vào luồng nộp hồ sơ.
5. Staff duyệt hồ sơ qua `POST /api/v1/profiles/approve/BUSINESS/{id}` mà không có dữ liệu verified để đối chiếu.

## Target Behavior

### 1. Business Submit Profile (`POST /api/v1/profiles/business`)

**Bước 1 — Validate MST format:**

- MST phải khớp `\d{10}|\d{13}`. Nếu sai -> `AppException("MA SO THUE KHONG HOP LE")`

**Bước 2 — Check duplicate MST:**

- Gọi `BusinessProfileRepository.existsByTaxCodeExcludingAccount(taxCode, accountId)` — loại trừ profile của chính account đang nộp (để cho phép update lại hồ sơ đã có).
- Nếu đã có account khác dùng MST này -> `AppException("MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC")`

**Bước 3 — Verify MST via VietQR:**

- Gọi `TaxCheckService.checkTaxCode(taxCode)` — nếu MST không tồn tại trong database nhà nước -> `NotFoundException("KHONG TIM THAY DOANH NGHIEP VOI MA SO THUE: {mst}")`
- Nếu VietQR API không reachable -> `BadGatewayException("KHONG GOI DUOC API VIETQR")` — **không cho nộp hồ sơ** khi không verify được.

**Bước 4 — Auto-fill verified data:**

- Ghi đè `companyName` và `address` từ response VietQR (single source of truth).
- Lưu `verifiedRepresentative` (người đại diện pháp lý) từ VietQR vào cột mới.
- User **không cần** tự nhập `companyName` / `address` — hệ thống tự điền từ VietQR.
- Request body chỉ cần `taxCode` + `businessLicenseUrl`.

**Bước 5 — Continue existing flow:**

- `kyb_status = "Pending"`, account status = `"Pending"`, notify staff (như hiện tại).

### 2. Staff Review

- `GET /api/v1/profiles/business` (list) và `GET /api/v1/profiles/business/{id}` (detail) trả response `BusinessProfileEntity` có thêm field `verifiedRepresentative`.
- `companyName` và `address` đã là verified data từ VietQR — Staff chỉ cần đối chiếu giấy phép kinh doanh.
- Staff approve/reject qua `POST /api/v1/profiles/approve/BUSINESS/{id}` — logic approval giữ nguyên.

### 3. Tax Check Endpoint

- `GET /api/auth/tax-check/{mst}` giữ nguyên — frontend dùng endpoint này để preview MST trước khi user bấm submit (UX improvement, không bắt buộc).

## Acceptance Criteria

| #  | Tiêu chi                                                                               | Cách verify              |
|----|----------------------------------------------------------------------------------------|--------------------------|
| AC1 | Nộp hồ sơ với MST không hợp lệ (không phải 10/13 số) -> bị chặn `MA SO THUE KHONG HOP LE` | Unit test                |
| AC2 | Nộp hồ sơ với MST đã được dùng bởi account khác -> bị chặn `MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC` | Unit test                |
| AC3 | Nộp hồ sơ với MST không tồn tại trong VietQR -> bị chặn `KHONG TIM THAY DOANH NGHIEP...` | Unit test (mock VietQR)  |
| AC4 | Nộp hồ sơ hợp lệ -> `companyName`, `address` ghi đè từ VietQR, `verifiedRepresentative` được lưu | Unit test (mock VietQR)  |
| AC5 | Nộp lại hồ sơ với cùng MST (cùng account) -> thành công, không bị chặn duplicate        | Unit test                |
| AC6 | Staff xem danh sách/chi tiết hồ sơ -> response có `verifiedRepresentative`             | API test                 |
| AC7 | VietQR API không reachable -> hồ sơ bị chặn, không vào Pending                         | Unit test (mock BadGateway) |
| AC8 | Existing flow (notify staff, audit log, kyb_status=Pending) vẫn hoạt động              | Unit test                |
| AC9 | Migration V52 chạy thành công, thêm cột `verified_representative` vào `business_profiles` | Integration / Flyway     |

## Database Changes

**Migration: `V52__business_profile_verified_data.sql`**

```sql
ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS verified_representative VARCHAR(255);

COMMENT ON COLUMN business_profiles.verified_representative
    IS 'Nguoi dai dien phap ly tu VietQR API, dung de staff doi chieu giay phep kinh doanh';
```

> Khong migrate du lieu cu — cac ban ghi hien co se co NULL cho `verified_representative`, chap nhan duoc vi ho so da Approved khong can verify lai.

## Entity Changes

**File: `src/main/java/com/aitasker/be/entity/BusinessProfileEntity.java`**

Them 1 field moi:

```java
@Column(name = "verified_representative", length = 255)
private String verifiedRepresentative;
```

## Repository Changes

**File: `src/main/java/com/aitasker/be/repository/BusinessProfileRepository.java`**

Them method check duplicate loai truong account hien tai:

```java
@Query("SELECT COUNT(b) > 0 FROM BusinessProfileEntity b WHERE b.taxCode = :taxCode AND b.accountId <> :accountId")
boolean existsByTaxCodeExcludingAccount(@Param("taxCode") String taxCode, @Param("accountId") Integer accountId);
```

> Giu nguyen `existsByTaxCode(String)` hien co.

## Service Changes

**File: `src/main/java/com/aitasker/be/service/core/ProfileService.java`**

Inject `TaxCheckService` vao constructor.

 thay doi logic trong `upsertBusiness()`:

| Step | Action                                                                            | Status     |
|------|-----------------------------------------------------------------------------------|------------|
| 1    | `requireRole("BUSINESS")`                                                         | Giu nguyen |
| 2    | Validate input not null                                                           | Giu nguyen |
| 3    | Validate `taxCode` not blank                                                      | Giu nguyen |
| 4    | Validate `taxCode` format: matches `\d{10}\|\d{13}` -> `MA SO THUE KHONG HOP LE`  | **Moi**    |
| 5    | Check duplicate: `existsByTaxCodeExcludingAccount(taxCode, accountId)` -> `MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC` | **Moi**    |
| 6    | Call `TaxCheckService.checkTaxCode(taxCode)` -> `KHONG TIM THAY DOANH NGHIEP...` hoac `KHONG GOI DUOC API VIETQR` | **Moi**    |
| 7    | Ghi de `entity.companyName = VietQR.companyName`                                   | **Moi**    |
| 8    | Ghi de `entity.address = VietQR.address`                                           | **Moi**    |
| 9    | `entity.verifiedRepresentative = VietQR.representative`                            | **Moi**    |
| 10   | `entity.businessLicenseUrl = input.businessLicenseUrl`                            | Giu nguyen |
| 11   | `kyb_status = "Pending"`, account status = `"Pending"`                           | Giu nguyen |
| 12   | Notify staff                                                                       | Giu nguyen |

**File: `src/main/java/com/aitasker/be/service/auth/TaxCheckService.java`**

Khong thay doi — service hien tai da du dung. Chi can inject vao `ProfileService`.

## Controller Changes

Khong thay doi controller — cac endpoint hien tai du. Response `BusinessProfileEntity` tu dong co them field `verifiedRepresentative` nho entity change.

## Execution Plan

| Step | File                                                                                          | Action   | Details                                                                                          |
|------|-----------------------------------------------------------------------------------------------|----------|--------------------------------------------------------------------------------------------------|
| 1    | `src/main/resources/db/migration/V52__business_profile_verified_data.sql`                     | Tao moi  | Migration them cot `verified_representative`                                                     |
| 2    | `src/main/java/com/aitasker/be/entity/BusinessProfileEntity.java`                            | Sua      | Them field `verifiedRepresentative` voi `@Column` mapping                                         |
| 3    | `src/main/java/com/aitasker/be/repository/BusinessProfileRepository.java`                    | Sua      | Them method `existsByTaxCodeExcludingAccount(taxCode, accountId)` voi `@Query`                    |
| 4    | `src/main/java/com/aitasker/be/service/core/ProfileService.java`                             | Sua      | Inject `TaxCheckService`; them logic validate MST format, check duplicate, call VietQR, auto-fill |
| 5    | `src/test/.../ProfileServiceTest.java`                                                        | Sua/Tao  | Them unit tests cho AC1-AC9, mock `TaxCheckService` va `BusinessProfileRepository`                |
| 6    | `docs/stories/US-046-kyb-tax-code-verification-hardening/`                                    | Tao      | Tao story folder voi overview.md, design.md, execplan.md, validation.md                           |
| 7    | `docs/swagger-api-overview.md`, `docs/ARCHITECTURE.md`                                        | Sua      | Cap nhat note ve verified fields trong response                                                   |
| 8    | —                                                                                             | Run      | `./mvnw test -Dtest=ProfileServiceTest` va `./mvnw compile`                                       |

## Validation

| Layer       | Expected proof                                                                                                    |
|-------------|-------------------------------------------------------------------------------------------------------------------|
| Unit        | `ProfileServiceTest` covers: MST format, duplicate check, VietQR call success/failure, auto-fill, resubmit       |
| Integration | `./mvnw test` voi PostgreSQL chay V52 migration thanh cong                                                        |
| E2E         | —                                                                                                                  |
| Platform    | —                                                                                                                  |

## Non-Goals

- Khong thay doi luong approval cua Staff (approve/reject logic giu nguyen).
- Khong migrate du lieu verified cho ho so da Approved.
- Khong thay doi endpoint `GET /api/auth/tax-check/{mst}` (public API giu nguyen).
- Khong them frontend changes.
- Khong tu dong approve ho so khi MST verified — Staff van phai duyet de check giay phep kinh doanh.
- Khong ap dung cho Expert KYC flow (chi ap dung cho Business KYB).

## Risk Checklist

| Flag               | Applies? | Reason                                                              |
|--------------------|----------|---------------------------------------------------------------------|
| Data model         | Co       | Them 1 cot DB moi qua migration V52                                 |
| External systems   | Co       | Goi VietQR API trong luong submit (khong chi public endpoint)       |
| Public contracts   | Co       | Response `BusinessProfileEntity` them 1 field moi                   |
| Existing behavior  | Co       | `upsertBusiness()` thay doi logic, them validation chan MST sai/trung |
| Auth               | Khong    |                                                                     |
| Authorization      | Khong    |                                                                     |
| Audit/security     | Khong    |                                                                     |

**Classification:** normal (4 flags, khong cham hard gate nao ngoai external systems).
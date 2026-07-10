# Luồng xử lý mã số thuế (MST) - KYB Business Profile

## Tổng quan

Khi Business nộp hồ sơ KYB, hệ thống tự động kiểm tra MST với VietQR trước khi vào hàng đợi duyệt của Staff.
Hệ thống tự điền companyName/address từ VietQR. Staff chỉ đối chiếu giấy phép kinh doanh.

## So sánh trước / sau thay đổi

| Bước                     | Trước                          | Sau                                      |
|--------------------------|--------------------------------|------------------------------------------|
| MST format               | Không kiểm tra                 | Chặn nếu không 10/13 số                  |
| MST trùng                | Không kiểm tra                 | Chặn nếu account khác đã dùng            |
| MST verification         | Staff tự tra cứu thủ công      | Hệ thống tự gọi VietQR API               |
| companyName              | User tự nhập                   | Tự điền từ VietQR (ghi đè)               |
| address                  | User tự nhập                   | Tự điền từ VietQR (ghi đè)               |
| representative           | Không có                       | Lưu verifiedRepresentative từ VietQR     |
| Staff review             | Mù - chỉ thấy data user nhập   | Thấy data đã verified, chỉ đối chiếu GPKD|
| VietQR API lỗi           | Không phát hiện                | Chặn nộp hồ sơ, không cho vào Pending    |

## Diagram

Business User
    |
    | POST /api/v1/profiles/business
    | Body: { taxCode, businessLicenseUrl }
    | (companyName, address KHÔNG cần nhập - tự điền từ VietQR)
    |
    v
ProfileService.upsertBusiness()
    |
    +-- 1. requireRole("BUSINESS")
    |
    +-- 2. Validate taxCode not blank
    |       +-- Blank -> throw "TAX CODE KHONG DUOC DE TRONG"
    |
    +-- 3. Validate taxCode format: \d{10}|\d{13}
    |       +-- Sai format -> throw "MA SO THUE KHONG HOP LE"
    |
    +-- 4. Check duplicate: existsByTaxCodeExcludingAccount(taxCode, accountId)
    |       +-- Đã có account khác dùng MST này -> throw "MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC"
    |
    +-- 5. Gọi TaxCheckService.checkTaxCode(taxCode)
    |       +-- VietQR API: https://api.vietqr.io/v2/business/{mst}
    |       +-- MST không tồn tại -> throw NotFoundException
    |       +-- API không reachable -> throw BadGatewayException
    |       +-- Thành công -> trả TaxCheckResponse { taxCode, companyName, address, representative, status }
    |
    +-- 6. Auto-fill từ VietQR:
    |       +-- entity.companyName = VietQR.companyName           <- GHI ĐÈ (single source of truth)
    |       +-- entity.address = VietQR.address                    <- GHI ĐÈ
    |       +-- entity.verifiedRepresentative = VietQR.representative  <- CỘT MỚI
    |
    +-- 7. entity.businessLicenseUrl = input.businessLicenseUrl
    |
    +-- 8. kyb_status = "Pending", account.status = "Pending"
    |
    +-- 9. Lưu businessProfileRepository.save(entity)
    |
    +-- 10. Audit log: ACTION_UPSERT_BUSINESS_PROFILE
    |
    +-- 11. Notify tất cả staff: notifyStaffProfileSubmitted()
              |
              v
         Staff nhận notification
              |
              v
         Staff review (GET /api/v1/profiles/business)
         +---------------------------------------------------------------+
         | Dữ liệu staff thấy:                                           |
         | + taxCode (MST)                                               |
         | + companyName <- đã verified từ VietQR                        |
         | + address <- đã verified từ VietQR                            |
         | + verifiedRepresentative <- từ VietQR                        |
         | + businessLicenseUrl <- file upload                          |
         | + kyb_status = "Pending"                                     |
         +---------------------------------------------------------------+
              |
              | POST /api/v1/profiles/approve/BUSINESS/{id}
              | ?status=Approved|Rejected&reason=...
              |
              v
         Approved -> Account mở khóa toàn bộ chức năng
         Rejected -> Business sửa và nộp lại (quay lại bước 1)

## Các file liên quan

| File                                                           | Vai trò                                          |
|----------------------------------------------------------------|--------------------------------------------------|
| V52__business_profile_verified_data.sql                        | Migration thêm cột verified_representative       |
| entity/BusinessProfileEntity.java                              | Entity thêm field verifiedRepresentative         |
| repository/BusinessProfileRepository.java                      | existsByTaxCodeExcludingAccount()               |
| service/core/ProfileService.java                               | upsertBusiness() - logic chính                   |
| service/auth/TaxCheckService.java                              | Gọi VietQR API                                   |
| dto/auth/TaxCheckResponse.java                                 | DTO response từ VietQR                            |
| controller/auth/TaxCheckController.java                        | GET /api/auth/tax-check/{mst} (public, giữ nguyên)|
| controller/core/ProfileController.java                         | POST /api/v1/profiles/business (không đổi)        |
| test/.../ProfileServiceTest.java                               | 26 tests (5 mới)                                  |
| docs/stories/US-046-kyb-tax-code-verification-hardening/       | Story folder                                      |

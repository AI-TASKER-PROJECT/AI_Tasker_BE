# Admin Dispute & Settlement Dashboard — US-049

**Story:** `US-049`  
**Trace gần nhất:** `#85`  
**Phạm vi:** Backend Admin dashboard, chỉ đọc  
**API prefix:** `/api/v1/admin`

## 1. Mục tiêu

US-049 bổ sung một màn hình dữ liệu tập trung để Admin có thể:

- Xem toàn bộ tranh chấp trên hệ thống, không cần biết trước `contractId`.
- Lọc và phân trang danh sách tranh chấp.
- Xem quyết định của Staff, bằng chứng, file đính kèm và ledger settlement.
- Nhận thông báo tài chính khi settlement tranh chấp hoàn tất.
- Theo dõi đúng số lượng tranh chấp đang mở trên analytics dashboard.

US-049 không thay đổi quyền ra quyết định. **Staff quyết định tỷ lệ chia tiền và
settlement vẫn chạy tự động; Admin chỉ xem và nhận báo cáo.**

## 2. Các phần đã triển khai

### 2.1 API danh sách tranh chấp

Đã thêm:

```http
GET /api/v1/admin/disputes
```

API trả về danh sách tranh chấp toàn hệ thống, hỗ trợ các query parameter:

| Parameter | Kiểu | Mặc định | Ý nghĩa |
|---|---:|---:|---|
| `page` | integer | `0` | Trang hiện tại, bắt đầu từ 0 |
| `size` | integer | `20` | Số phần tử, từ 1 đến 100 |
| `status` | string | null | Lọc theo trạng thái dispute |
| `assignedStaffId` | integer | null | Lọc theo Staff được giao |
| `from` | ISO datetime | null | Thời điểm tạo bắt đầu |
| `to` | ISO datetime | null | Thời điểm tạo kết thúc |
| `q` | string | null | Tìm theo `disputeId` hoặc `contractId` |

Các trạng thái được hỗ trợ:

- `PENDING_SELF_RESOLVE`
- `ESCALATION_REQUESTED`
- `STAFF_REVIEWING`
- `STAFF_DECIDED`
- `RESOLVED`
- `CANCELLED`

Response chính:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Mỗi phần tử có thông tin dispute, contract, milestone, trạng thái, Staff được
giao và dữ liệu payout/refund đã được lưu. Tranh chấp chưa settlement không hiển
thị số tiền ước tính; các field settlement tương ứng trả `null`.

### 2.2 API chi tiết tranh chấp

Đã thêm:

```http
GET /api/v1/admin/disputes/{disputeId}
```

API tổng hợp:

- Thông tin dispute, contract và milestone.
- `escalationReason`, `evidenceReport`, `escalationEvidenceFile`.
- Staff được giao và các mốc thời gian review/decision.
- Tỷ lệ payout, tiền trả Expert, tiền hoàn Business.
- Staff decision note và Staff report.
- Nguồn settlement trên milestone.
- Danh sách file trong `case_attachments` với `ownerType=DISPUTE` và đúng
  `ownerId=disputeId`.
- Ledger trong `wallet_transactions` với `referenceType=DISPUTE` và đúng
  `referenceId=disputeId`.

Nếu không tìm thấy dispute, service trả lỗi `KHONG TIM THAY DISPUTE`.

### 2.3 Phân quyền

Cả hai API đều gọi:

```java
accessService.requireRole("ADMIN");
```

Do đó:

- Chưa đăng nhập: bị từ chối.
- Business/Expert/Staff: không được sử dụng dashboard Admin.
- Admin: chỉ được đọc dữ liệu qua hai endpoint GET.

US-049 không tạo `POST`, `PUT`, `PATCH` hoặc `DELETE` dưới
`/api/v1/admin/disputes`.

### 2.4 Validation được sửa sau review

Pagination trước đây âm thầm dùng giá trị mặc định khi input không hợp lệ. Trace
`#85` đã sửa thành trả lỗi rõ ràng:

| Trường hợp | Kết quả |
|---|---|
| `page < 0` | `PAGE KHONG DUOC AM` |
| `size < 1` hoặc `size > 100` | `SIZE PHAI NAM TRONG KHOANG 1 DEN 100` |
| Status ngoài danh sách cho phép | `DISPUTE STATUS KHONG HOP LE` |
| `from > to` | `FROM KHONG DUOC LON HON TO` |
| `q` dài hơn 100 ký tự | `Q KHONG DUOC VUOT QUA 100 KY TU` |

### 2.5 Mapping evidence được sửa sau review

`DisputeEntity` không có `dispute_reason` hoặc `initiation_evidence_file`.
US-049 không suy diễn dữ liệu từ field khác và không lấy attachment đầu tiên để
giả lập hai field này.

DTO chi tiết hiện:

- Không còn `disputeReason`.
- Không còn `initiationEvidenceFile`.
- Map `DisputeEntity.evidenceReport` thành response field `evidenceReport`.
- Giữ `escalationReason`, `escalationEvidenceFile` và `attachments[]`.

## 3. Luồng đọc dữ liệu

```text
Admin gửi JWT
    |
    v
AdminController
    |
    +-- GET /admin/disputes ----------> listDisputes(filter)
    |                                      |
    |                                      +-- kiểm tra role ADMIN
    |                                      +-- validate filter/pagination
    |                                      +-- đọc disputes theo createdAt giảm dần
    |                                      +-- filter + paginate
    |                                      +-- map Staff và settlement summary
    |
    +-- GET /admin/disputes/{id} -----> getDisputeDetail(id)
                                           |
                                           +-- kiểm tra role ADMIN
                                           +-- đọc DisputeEntity
                                           +-- đọc ContractMilestone
                                           +-- đọc case_attachments của dispute
                                           +-- đọc wallet_transactions của dispute
                                           +-- trả AdminDisputeDetail
```

## 4. Luồng chia tiền và báo cáo Admin

Admin không nhập hoặc duyệt tỷ lệ chia tiền trong US-049. Luồng thực tế:

```text
Assigned Staff gọi staff-decision
    |
    +-- nhập expertPercent từ 0 đến 100
    +-- expertPayout = escrow * expertPercent / 100
    +-- businessRefund = escrow - expertPayout
    +-- lưu quyết định Staff
    |
    v
executeDisputeSettlementInternal
    |
    +-- debit toàn bộ milestone escrow
    +-- credit payout cho Expert
    +-- credit refund cho Business
    +-- đánh dấu escrow đã release
    +-- dispute -> RESOLVED
    +-- milestone -> COMPLETED
    +-- audit DISPUTE_SETTLEMENT_EXECUTED
    +-- notify Business/Expert
    +-- notify Admin: DISPUTE_SETTLEMENT_REPORTED
```

Thông báo Admin là báo cáo tài chính, gồm tối thiểu:

- `disputeId`, `contractId`, `milestoneId`.
- Tỷ lệ Staff quyết định.
- Số tiền trả Expert.
- Số tiền hoàn Business.
- Wallet transaction ID của settlement.

Thông báo này không phải yêu cầu Admin phê duyệt và không chặn settlement.

## 5. Analytics fix

`AdminService.analyticsOverview()` đã được sửa để đếm `openDisputes` bằng các
trạng thái active thực tế:

- `PENDING_SELF_RESOLVE`
- `ESCALATION_REQUESTED`
- `STAFF_REVIEWING`
- `STAFF_DECIDED`

Không còn dùng các chuỗi legacy `Open` hoặc `UnderReview`.

## 6. Swagger và bằng chứng hiện có

OpenAPI hiện có hai path:

- `/api/v1/admin/disputes`
- `/api/v1/admin/disputes/{disputeId}`

Evidence được ghi trong trace `#85`:

- `AdminDisputeDashboardTest`: 19 tests.
- `AdminDisputeDashboardAuthTest`: 2 tests.
- `AdminDisputeDashboardIntegrationTest`: 4 tests.
- Focused story verification: 25 tests pass.
- Full Maven suite: 252 tests, 0 failures.
- `openapi-v1.json`: 141 paths và có đủ hai Admin dispute endpoints.

## 7. Giới hạn còn lại sau review trace #85

Các mục dưới đây chưa nên được mô tả là đã hoàn tất:

1. `DISPUTE_SETTLEMENT_REPORTED` hiện được gọi bên trong transaction settlement,
   chưa có cơ chế `afterCommit` thực sự.
2. Chưa có test executable chứng minh notification Admin chỉ gửi đúng một lần
   khi retry settlement.
3. Chưa có test rollback chứng minh transaction thất bại sẽ không phát
   notification Admin.
4. Tên public field hiện là `evidenceReport`; nếu contract mong muốn
   `evidenceSummary` thì cần đổi DTO, test và OpenAPI đồng bộ trong một thay đổi
   tiếp theo.

Vì vậy Admin dashboard và hai API đọc đã có thể sử dụng, nhưng AC về
post-commit notification và idempotency notification vẫn cần proof bổ sung.

## 8. Các file chính

- Controller: `src/main/java/com/aitasker/be/controller/core/AdminController.java`
- Service: `src/main/java/com/aitasker/be/service/core/AdminDisputeDashboardService.java`
- Detail DTO: `src/main/java/com/aitasker/be/dto/admin/AdminDisputeDetail.java`
- List DTO: `src/main/java/com/aitasker/be/dto/admin/AdminDisputeListResponse.java`
- Settlement flow: `src/main/java/com/aitasker/be/service/core/ContractExecutionService.java`
- Notification: `src/main/java/com/aitasker/be/service/core/NotificationService.java`
- Story validation: `docs/stories/US-049-admin-dispute-settlement-dashboard/validation.md`
- OpenAPI: `docs/openapi/openapi-v1.json`

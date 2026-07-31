# Bảng hướng dẫn chỉnh sửa Frontend - AI SoW Budget

## Phạm vi

Backend đã hoàn thành API đánh giá ngân sách và phân bổ milestone. Frontend cần
thực hiện các đầu việc dưới đây trên màn hình generate/xác nhận SoW trước khi
gọi `POST /api/v1/jobs`.

| ID | Ưu tiên | Khu vực frontend | Đầu việc cần sửa | API/field sử dụng | Logic bắt buộc | Tiêu chí hoàn thành |
| --- | --- | --- | --- | --- | --- | --- |
| FE-SOW-01 | P0 | API types/interfaces | Bổ sung model đánh giá ngân sách | `GenerateSowResponse.budgetAssessment` | Khai báo đủ `businessBudget`, `estimatedMin`, `recommendedBudget`, `estimatedMax`, `status`, `gapToMinimum`, `confidence`, `source`, `requiresBusinessConfirmation`, `message`, `factors` | Response generate SoW được parse đầy đủ, không dùng `any` cho các field ngân sách mới |
| FE-SOW-02 | P0 | Milestone model | Bổ sung ngân sách AI cho từng milestone | `milestones[].recommendedBudget` | Giữ `milestones[].budget` là phân bổ theo giá Business ban đầu; không ghi đè hai field cho nhau | Tổng `budget` bằng `businessBudget`; tổng `recommendedBudget` bằng mức AI đề xuất |
| FE-SOW-03 | P0 | Budget assessment card | Hiển thị so sánh ngân sách | `businessBudget`, `estimatedMin`, `recommendedBudget`, `estimatedMax`, `currency` | Chỉ hiện khi status khác `HIGH`; khoảng AI chỉ đọc; định dạng đầy đủ số VND; không tự thay đổi giá Business | Các mức 80/100/130 triệu hiển thị thành `80.000.000 ₫`/`100.000.000 ₫`/`130.000.000 ₫`, không phải `80 ₫`/`100 ₫`/`130 ₫` |
| FE-SOW-04 | P0 | Status/warning UI | Mapping trạng thái sang badge/cảnh báo | `TOO_LOW`, `LOW`, `SUITABLE`, `HIGH`, `gapToMinimum`, `message` | `TOO_LOW` màu đỏ; `LOW` cam; `SUITABLE` xanh; với `HIGH` ẩn toàn bộ card và giữ ngân sách Business; chỉ nhấn mạnh `gapToMinimum` khi lớn hơn 0 | Ba trạng thái cần tham khảo hiển thị đúng; `HIGH` không hiển thị phần AI đề xuất |
| FE-SOW-05 | P1 | Confidence/source UI | Hiển thị chất lượng ước tính | `confidence`, `source`, `factors` | Với `AI_MILESTONE_FALLBACK`, phải thông báo độ tin cậy thấp; nếu backend báo thiếu giá AI hợp lệ thì hiển thị lỗi và cho phép tạo lại | Người dùng phân biệt được ước tính AI và lỗi provider |
| FE-SOW-06 | P0 | Confirmation state | Xác nhận ngân sách Business | `KEEP_BUSINESS_BUDGET`, `USE_CUSTOM_BUDGET`, `requiresBusinessConfirmation` | Với status khác `HIGH`, Business chọn giữ hoặc tự chỉnh; với `HIGH`, backend trả `requiresBusinessConfirmation=false` và frontend tự giữ ngân sách Business | Không chặn lưu Job khi card bị ẩn; không dùng ngân sách AI làm giá Job |
| FE-SOW-07 | P0 | Keep Business option | Mapping lựa chọn giữ giá ban đầu | `budgetAssessment.businessBudget`, `milestones[].budget` | Job `budget = businessBudget`; mỗi `fundsAllocated = milestone.budget` | Payload tạo Job có tổng milestone đúng bằng giá Business ban đầu |
| FE-SOW-08 | P0 | AI advisory only | Không biến giá AI thành giá Job | `budgetAssessment.recommendedBudget`, `milestones[].recommendedBudget` | Chỉ hiển thị tham khảo và dùng `recommendedBudget` làm trọng số khi chia ngân sách custom; không có lựa chọn dùng trực tiếp giá AI | Payload tạo Job không gán `budget = recommendedBudget` |
| FE-SOW-09 | P0 | Custom budget input | Thêm ô nhập giá tùy chỉnh | Giá trị local `customBudget` | Chỉ nhận số VND nguyên lớn hơn 0; hiển thị khoảng AI ở chế độ chỉ đọc; nếu thấp hơn `estimatedMin` thì cảnh báo nhưng không chặn quyền xác nhận | Business nhập và xác nhận được một mức khác giá ban đầu/giá AI |
| FE-SOW-10 | P0 | Custom allocation request | Gọi API chia lại milestone | `POST /api/jobs/reallocate-sow-budget` | Gửi `selectedBudget=customBudget`; với mỗi milestone gửi `{ milestoneIndex, referenceBudget: milestone.recommendedBudget }`; gửi đủ milestone và index duy nhất | API được gọi một lần sau khi Business xác nhận giá tùy chỉnh |
| FE-SOW-11 | P0 | Custom allocation response | Map kết quả chia lại | `selectedBudget`, `allocationTotal`, `allocations[].milestoneIndex`, `allocations[].fundsAllocated` | Map bằng `milestoneIndex`; không tự tính/làm tròn lại trên frontend | Tổng tiền custom trên UI và payload bằng chính xác `allocationTotal` |
| FE-SOW-12 | P0 | Create Job payload | Tạo payload cuối cùng | `POST /api/v1/jobs` | Giữ nguyên các field Job/SoW hiện có; chỉ chọn đúng `budget` và `milestones[].fundsAllocated` theo option đã xác nhận | Backend nhận đúng một bộ ngân sách tương ứng với lựa chọn Business |
| FE-SOW-13 | P0 | Validation trước submit | Kiểm tra tổng tiền | Job `budget` và toàn bộ `fundsAllocated` | Chặn submit nếu thiếu milestone, index không map được hoặc tổng milestone khác Job budget; dùng phép tính số nguyên VND/decimal-safe | Không có payload sai tổng tiền được gửi tới API tạo Job |
| FE-SOW-14 | P0 | Regenerate/edit flow | Quản lý xác nhận khi SoW thay đổi | Generate SoW response mới | Khi generate lại hoặc thay đổi scope: xóa option cũ và custom allocation cũ. Khi chỉ sửa nội dung/thời lượng/thứ tự milestone, giữ allocation đã xác nhận. Khi xóa milestone trong chế độ custom, gọi lại API phân bổ với cùng `selectedBudget` và danh sách milestone còn lại. Khi Business sửa trực tiếp `fundsAllocated`, chuyển sang phân bổ thủ công và đồng bộ Job budget bằng tổng milestone | Không tái sử dụng allocation của bản SoW trước; sửa/xóa milestone không làm quay về `businessBudget` cũ |
| FE-SOW-15 | P1 | Error handling | Xử lý lỗi API custom | HTTP `400`, `401`, `403`, `500` từ reallocation API | `400`: giữ dữ liệu nhập và hiển thị validation; `401/403`: xử lý auth; `500`: cho phép thử lại; không tiếp tục tạo Job khi reallocation lỗi | Người dùng nhận thông báo rõ ràng và không tạo Job với dữ liệu allocation cũ |
| FE-SOW-16 | P0 | Frontend tests | Bổ sung unit/component/integration tests | Hai lựa chọn hiển thị, bốn status, ba source và custom API | Test `HIGH` ẩn card và tự giữ Business budget; custom 110m từ tỷ trọng 40m/100m trả 31,428,571 và 78,571,429; test regenerate reset; test chặn sai tổng | Toàn bộ test ngân sách chạy đạt và bao phủ nhánh ẩn/hiện |

## Luồng hoàn chỉnh

```text
POST /api/jobs/generate-sow
  -> nếu status=HIGH: ẩn budgetAssessment, tự giữ milestones[].budget
  -> nếu status khác HIGH: hiển thị budgetAssessment
     -> KEEP: dùng milestones[].budget
     -> CUSTOM: gọi /api/jobs/reallocate-sow-budget và dùng fundsAllocated trả về
        -> sửa nội dung/thời lượng/thứ tự: giữ fundsAllocated mới
        -> xóa milestone: phân bổ lại cùng selectedBudget trên các mốc còn lại
  -> hoặc Business mở khóa milestone và sửa trực tiếp từng fundsAllocated
     -> giữ phân bổ thủ công, Job budget = tổng fundsAllocated
  -> kiểm tra tổng milestone = Job budget
  -> POST /api/v1/jobs
```

## Lưu ý bàn giao

- Frontend không sửa `estimatedMin`, `recommendedBudget`, `estimatedMax` của AI.
- Frontend không tự chia hoặc làm tròn ngân sách custom.
- Phân bổ thủ công chỉ dùng các số VND do Business nhập trực tiếp cho từng
  milestone; không biến `recommendedBudget` thành giá thực thi.
- API custom chỉ tính toán, không tạo Job và không ghi database.
- Business vẫn là người xác nhận ngân sách cuối cùng.

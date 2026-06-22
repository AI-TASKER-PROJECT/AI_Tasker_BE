# Hướng dẫn test API Back-end bằng Swagger

Tài liệu này dùng để test thủ công API AITASKER trên Swagger UI và được tạo lại từ controller source hiện tại sau khi xử lý merge conflict.

## 1. Chuẩn bị

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html. Với API cần đăng nhập, gọi POST /api/auth/login, copy accessToken, bấm Authorize, dán chỉ accessToken vào ô token.

## 2. Tài khoản seed thường dùng

| Role | Email | Mật khẩu |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | `12345678` |
| EXPERT | `expert@aitasker.local` | `12345678` |
| ADMIN | `admin@aitasker.local` | `12345678` |
| STAFF | `staff@aitasker.local` | `12345678` |

## 3. Danh sách API test theo luồng

### Admin vận hành hệ thống

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/admin/accounts` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 2 | POST | `/api/v1/admin/accounts` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 3 | DELETE | `/api/v1/admin/accounts/{accountId}` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 4 | PATCH | `/api/v1/admin/accounts/{accountId}` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 5 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 6 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 7 | GET | `/api/v1/admin/analytics/overview` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 8 | GET | `/api/v1/admin/audit-logs` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 9 | POST | `/api/v1/admin/reviews` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 10 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 11 | GET | `/api/v1/admin/settings` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 12 | PATCH | `/api/v1/admin/settings/{key}` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 13 | GET | `/api/v1/admin/staffs` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 14 | POST | `/api/v1/admin/staffs` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 15 | PATCH | `/api/v1/admin/staffs/{staffId}` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 16 | GET | `/api/v1/admin/wallet` | admin-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 17 | POST | `/api/v1/admin/wallet/sync` | admin-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 18 | POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 19 | GET | `/api/v1/admin/withdrawal-requests` | withdrawal-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 20 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | withdrawal-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 21 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | withdrawal-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Auth, OTP và session

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 22 | GET | `/api/auth/check-email` | auth-controller | Không cần token | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 23 | POST | `/api/auth/google/login` | auth-controller | Không cần token | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 24 | POST | `/api/auth/google/register` | auth-controller | Không cần token | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 25 | POST | `/api/auth/login` | auth-controller | Không cần token | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 26 | GET | `/api/auth/me` | auth-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 27 | POST | `/api/auth/register` | auth-controller | Không cần token | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 28 | POST | `/api/auth/email/send-otp` | email-otp-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 29 | POST | `/api/auth/email/verify-otp` | email-otp-controller | Không cần token | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 30 | GET | `/api/auth/tax-check/{mst}` | tax-check-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Catalog, skill, technology

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 31 | GET | `/api/v1/acceptance-criteria` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 32 | GET | `/api/v1/domains` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 33 | POST | `/api/v1/domains` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 34 | PATCH | `/api/v1/domains/{domainId}` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 35 | GET | `/api/v1/skills` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 36 | POST | `/api/v1/skills` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 37 | PATCH | `/api/v1/skills/{skillId}` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 38 | GET | `/api/v1/technologies` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 39 | POST | `/api/v1/technologies` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 40 | PATCH | `/api/v1/technologies/{technologyId}` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Job marketplace và proposal

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 41 | GET | `/api/v1/jobs/{jobId}/domains` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 42 | PUT | `/api/v1/jobs/{jobId}/domains` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 43 | GET | `/api/v1/jobs/{jobId}/skills` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 44 | PUT | `/api/v1/jobs/{jobId}/skills` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 45 | GET | `/api/v1/jobs/{jobId}/technologies` | catalog-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 46 | PUT | `/api/v1/jobs/{jobId}/technologies` | catalog-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 47 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 48 | GET | `/api/v1/jobs/{jobId}/matching` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 49 | GET | `/api/v1/jobs/{jobId}/milestones` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 50 | POST | `/api/credits/job-post/purchase` | credit-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 51 | POST | `/api/credits/proposal/purchase` | credit-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 52 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | expert-candidate-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 53 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | expert-recommendation-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 54 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | expert-recommendation-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 55 | POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | expert-recommendation-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | Sau khi business chọn expert: proposal có businessSelected=true và expert nhận notification/websocket nếu đang online. |
| 56 | GET | `/api/v1/jobs` | marketplace-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 57 | POST | `/api/v1/jobs` | marketplace-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 58 | GET | `/api/v1/jobs/my` | marketplace-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 59 | GET | `/api/v1/jobs/{jobId}` | marketplace-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 60 | GET | `/api/v1/jobs/{jobId}/proposals` | marketplace-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 61 | POST | `/api/v1/jobs/{jobId}/publish` | marketplace-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 62 | PATCH | `/api/v1/jobs/{jobId}/status` | marketplace-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 63 | POST | `/api/v1/proposals` | marketplace-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 64 | POST | `/api/v1/proposals/file` | marketplace-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 65 | GET | `/api/v1/proposals/my` | marketplace-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 66 | PATCH | `/api/v1/proposals/{proposalId}/status` | marketplace-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 67 | POST | `/api/jobs/generate-sow` | sow-generation-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Chat và messaging

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 68 | POST | `/api/chatbot/ask` | chatbot-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Contract, milestone, deliverable và dispute

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 69 | GET | `/api/v1/contracts` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 70 | GET | `/api/v1/contracts/{contractId}` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 71 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 72 | GET | `/api/v1/contracts/{contractId}/disputes` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 73 | GET | `/api/v1/contracts/{contractId}/milestones` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 74 | POST | `/api/v1/contracts/{contractId}/nda-sign` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 75 | POST | `/api/v1/contracts/{contractId}/reject` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 76 | POST | `/api/v1/contracts/{contractId}/sign` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 77 | POST | `/api/v1/contracts/{contractId}/terminate` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 78 | POST | `/api/v1/criteria` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 79 | POST | `/api/v1/deliverables` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 80 | POST | `/api/v1/disputes` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 81 | GET | `/api/v1/disputes/{disputeId}` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 82 | PATCH | `/api/v1/disputes/{disputeId}/assign` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 83 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 84 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 85 | POST | `/api/v1/disputes/{disputeId}/technical-report` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 86 | POST | `/api/v1/milestones` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 87 | POST | `/api/v1/milestones/sla-auto-approve` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 88 | PATCH | `/api/v1/milestones/{milestoneId}` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 89 | POST | `/api/v1/milestones/{milestoneId}/complete` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 90 | GET | `/api/v1/milestones/{milestoneId}/criteria` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 91 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 92 | GET | `/api/v1/milestones/{milestoneId}/transactions` | contract-execution-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 93 | POST | `/api/v1/transactions` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 94 | PATCH | `/api/v1/transactions/{transactionId}/status` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 95 | POST | `/api/v1/transactions/{transactionId}/webhook` | contract-execution-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Payment, wallet, quota và withdrawal

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 96 | GET | `/api/membership/packages` | membership-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 97 | POST | `/api/membership/packages/{packageId}/purchase` | membership-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 98 | POST | `/api/payments/payos/create` | pay-ospayment-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 99 | GET | `/api/payments/payos/return` | pay-ospayment-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 100 | POST | `/api/payments/payos/{orderCode}/sync` | pay-ospayment-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 101 | GET | `/api/wallet/current` | wallet-api-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 102 | GET | `/api/wallet/transactions` | wallet-api-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 103 | GET | `/api/v1/wallet/me` | wallet-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 104 | GET | `/api/v1/withdrawal-requests` | withdrawal-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 105 | POST | `/api/v1/withdrawal-requests` | withdrawal-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Notification

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 106 | GET | `/api/v1/notifications` | notification-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 107 | PATCH | `/api/v1/notifications/read-all` | notification-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 108 | GET | `/api/v1/notifications/unread-count` | notification-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 109 | PATCH | `/api/v1/notifications/{notificationId}/read` | notification-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Tài khoản, hồ sơ và quota

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 110 | POST | `/api/v1/profiles/approve/{type}/{id}` | profile-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 111 | GET | `/api/v1/profiles/business` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 112 | POST | `/api/v1/profiles/business` | profile-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 113 | GET | `/api/v1/profiles/business/by-job/{jobId}` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 114 | POST | `/api/v1/profiles/business/license-file` | profile-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 115 | GET | `/api/v1/profiles/business/me` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 116 | GET | `/api/v1/profiles/business/{businessId}` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 117 | GET | `/api/v1/profiles/expert` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 118 | POST | `/api/v1/profiles/expert` | profile-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 119 | GET | `/api/v1/profiles/expert/me` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 120 | POST | `/api/v1/profiles/expert/portfolio-file` | profile-controller | Cần Bearer JWT | Multipart form-data, field file | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 121 | GET | `/api/v1/profiles/expert/{expertId}` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 122 | GET | `/api/v1/profiles/files/view-url` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 123 | GET | `/api/v1/profiles/portfolio` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 124 | POST | `/api/v1/profiles/portfolio` | profile-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 125 | POST | `/api/v1/profiles/portfolio/certificate-file` | profile-controller | Cần Bearer JWT | Có JSON body/query theo Swagger nếu endpoint yêu cầu | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 126 | GET | `/api/v1/profiles/portfolio/me` | profile-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |
| 127 | GET | `/api/users/me/quota` | user-quota-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

### Khác / hạ tầng

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 128 | GET | `/api/test/secure` | test-controller | Cần Bearer JWT | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra success, message, data. |

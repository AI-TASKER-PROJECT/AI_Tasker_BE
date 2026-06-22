# Hướng Dẫn Test API Back-end Bằng Swagger

Tài liệu này dùng để test thủ công API AITASKER trên Swagger UI. Danh sách endpoint đã được đồng bộ từ controller source hiện tại và static OpenAPI snapshot.

## 1. Chuẩn bị

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`. Với API cần đăng nhập, gọi `POST /api/auth/login`, copy `accessToken`, bấm `Authorize`, dán **chỉ accessToken** vào ô token. Không tự thêm chữ `Bearer`.

## 2. Tài khoản seed thường dùng

| Role | Email | Mật khẩu |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | `12345678` |
| EXPERT | `expert@aitasker.local` | `12345678` |
| ADMIN | `admin@aitasker.local` | `12345678` |
| STAFF | `staff@aitasker.local` | `12345678` |

## 3. Thứ tự test khuyến nghị

1. Kiểm tra health và đăng nhập để lấy token cho từng role.
2. Test KYB/KYC và profile để có business/expert hợp lệ.
3. Test catalog, tạo job draft, publish job, expert gửi proposal, business duyệt proposal.
4. Tạo contract từ proposal, ký contract/NDA, chuẩn bị wallet, trả deposit, tạo/hoàn tất milestone.
5. Test membership, credit, quota, wallet transaction, withdrawal và admin vận hành.

## 4. Danh sách API test theo luồng

### Auth, OTP và session

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | GET | `/api/auth/check-email` | auth-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 2 | GET | `/api/auth/me` | auth-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 3 | POST | `/api/auth/google/login` | auth-controller | Không cần token; Public | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 4 | POST | `/api/auth/google/register` | auth-controller | Không cần token; Public | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 5 | POST | `/api/auth/login` | auth-controller | Không cần token; Public | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 6 | POST | `/api/auth/register` | auth-controller | Không cần token; Public | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 7 | POST | `/api/auth/email/send-otp` | email-otp-controller | Không cần token; Public | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 8 | POST | `/api/auth/email/verify-otp` | email-otp-controller | Không cần token; Public | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 9 | GET | `/api/auth/tax-check/{mst}` | tax-check-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

Body mẫu hay dùng trong luồng này:

**POST `/api/auth/login`**

```json
{
  "email": "admin@aitasker.local",
  "password": "12345678"
}
```

**POST `/api/auth/register`**

```json
{
  "email": "expert.manual@example.com",
  "password": "12345678",
  "fullName": "Expert Manual",
  "phone": "0900000001",
  "role": "EXPERT"
}
```

**POST `/api/auth/email/send-otp`**

```json
{
  "email": "expert.manual@example.com"
}
```

**POST `/api/auth/email/verify-otp`**

```json
{
  "email": "expert.manual@example.com",
  "otp": "123456"
}
```

### KYB/KYC, profile và file

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 10 | GET | `/api/v1/profiles/business` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 11 | GET | `/api/v1/profiles/business/by-job/{jobId}` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 12 | GET | `/api/v1/profiles/business/me` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 13 | GET | `/api/v1/profiles/business/{businessId}` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 14 | GET | `/api/v1/profiles/expert` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 15 | GET | `/api/v1/profiles/expert/me` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 16 | GET | `/api/v1/profiles/expert/{expertId}` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 17 | GET | `/api/v1/profiles/files/view-url` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 18 | GET | `/api/v1/profiles/portfolio` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 19 | GET | `/api/v1/profiles/portfolio/me` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 20 | POST | `/api/v1/profiles/approve/{type}/{id}` | profile-controller | Cần Bearer JWT; STAFF/ADMIN | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 21 | POST | `/api/v1/profiles/business` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 22 | POST | `/api/v1/profiles/business/license-file` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | form-data key `file` | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 23 | POST | `/api/v1/profiles/expert` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 24 | POST | `/api/v1/profiles/portfolio` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 25 | POST | `/api/v1/profiles/portfolio/certificate-file` | profile-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | form-data key `file` | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

### Catalog job metadata

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 26 | GET | `/api/v1/acceptance-criteria` | catalog-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 27 | GET | `/api/v1/domains` | catalog-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 28 | GET | `/api/v1/jobs/{jobId}/domains` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 29 | GET | `/api/v1/jobs/{jobId}/skills` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 30 | GET | `/api/v1/jobs/{jobId}/technologies` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 31 | GET | `/api/v1/skills` | catalog-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 32 | GET | `/api/v1/technologies` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 33 | POST | `/api/v1/domains` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 34 | POST | `/api/v1/skills` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 35 | POST | `/api/v1/technologies` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 36 | PUT | `/api/v1/jobs/{jobId}/domains` | catalog-controller | Cần Bearer JWT; BUSINESS | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 37 | PUT | `/api/v1/jobs/{jobId}/skills` | catalog-controller | Cần Bearer JWT; BUSINESS | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 38 | PUT | `/api/v1/jobs/{jobId}/technologies` | catalog-controller | Cần Bearer JWT; BUSINESS | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 39 | PATCH | `/api/v1/domains/{domainId}` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 40 | PATCH | `/api/v1/skills/{skillId}` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 41 | PATCH | `/api/v1/technologies/{technologyId}` | catalog-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

### Marketplace job và proposal

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 42 | GET | `/api/v1/jobs/{jobId}/milestones` | contract-execution-controller | Không cần token nếu job `OPEN`; cần Bearer JWT nếu job chưa public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 43 | GET | `/api/v1/jobs` | marketplace-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 44 | GET | `/api/v1/jobs/my` | marketplace-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 45 | GET | `/api/v1/jobs/{jobId}` | marketplace-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 46 | GET | `/api/v1/jobs/{jobId}/proposals` | marketplace-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 47 | GET | `/api/v1/proposals/my` | marketplace-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 48 | POST | `/api/v1/jobs` | marketplace-controller | Cần Bearer JWT; BUSINESS | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 49 | POST | `/api/v1/jobs/{jobId}/publish` | marketplace-controller | Cần Bearer JWT; BUSINESS | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 50 | POST | `/api/v1/proposals` | marketplace-controller | Cần Bearer JWT; EXPERT | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 51 | POST | `/api/v1/proposals/file` | marketplace-controller | Cần Bearer JWT; EXPERT | form-data key `file` | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 52 | PATCH | `/api/v1/jobs/{jobId}/status` | marketplace-controller | Cần Bearer JWT; BUSINESS | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 53 | PATCH | `/api/v1/proposals/{proposalId}/status` | marketplace-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

Body mẫu hay dùng trong luồng này:

**POST `/api/v1/jobs`**

```json
{
  "title": "Tích hợp RAG chatbot",
  "rawRequirements": "Chatbot trả lời FAQ và chuyển lead.",
  "structuredSow": "Xây dựng RAG chatbot và dashboard quản trị.",
  "budget": 90000000,
  "plannedDurationValue": 6,
  "plannedDurationUnit": "WEEK",
  "domainIds": [2, 3],
  "skills": [{"skillId": 2, "isMandatory": true}],
  "technologyIds": [1, 2]
}
```

**POST `/api/v1/proposals`**

```json
{
  "jobId": 1,
  "technicalSolution": "Triển khai RAG, backend Spring Boot và frontend React.",
  "proposalDescription": "Chia dự án thành 2 giai đoạn.",
  "proposalFileUrl": "proposal-files/experts/1/example.pdf",
  "bidAmount": 90000000
}
```

### AI hỗ trợ job và matching

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 54 | POST | `/api/chatbot/ask` | chatbot-controller | Không cần token; Public | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 55 | GET | `/api/v1/jobs/{jobId}/matching` | contract-execution-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 56 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | Expert Candidates | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 57 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Expert Recommendations | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 58 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Expert Recommendations | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 59 | POST | `/api/jobs/generate-sow` | SoW Generation | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

Body mẫu hay dùng trong luồng này:

**POST `/api/jobs/generate-sow`**

```json
{
  "projectTitle": "Tích hợp RAG chatbot",
  "rawRequirement": "Cần chatbot trả lời FAQ.",
  "budget": 90000000,
  "duration": 6,
  "durationUnit": "WEEK",
  "supportFields": ["E-commerce"],
  "requiredSkills": ["RAG Architecture"]
}
```

### Contract, milestone, deliverable và dispute

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 60 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 61 | GET | `/api/v1/contracts` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 62 | GET | `/api/v1/contracts/{contractId}` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 63 | GET | `/api/v1/contracts/{contractId}/disputes` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 64 | GET | `/api/v1/contracts/{contractId}/milestones` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 65 | GET | `/api/v1/disputes/{disputeId}` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 66 | GET | `/api/v1/milestones/{milestoneId}/criteria` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 67 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 68 | GET | `/api/v1/milestones/{milestoneId}/transactions` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 69 | POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | contract-execution-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 70 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 71 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 72 | POST | `/api/v1/contracts/{contractId}/nda-sign` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 73 | POST | `/api/v1/contracts/{contractId}/reject` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 74 | POST | `/api/v1/contracts/{contractId}/sign` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 75 | POST | `/api/v1/contracts/{contractId}/terminate` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 76 | POST | `/api/v1/criteria` | contract-execution-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 77 | POST | `/api/v1/deliverables` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 78 | POST | `/api/v1/disputes` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 79 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 80 | POST | `/api/v1/disputes/{disputeId}/technical-report` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 81 | POST | `/api/v1/milestones` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 82 | POST | `/api/v1/milestones/sla-auto-approve` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 83 | POST | `/api/v1/milestones/{milestoneId}/complete` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 84 | PATCH | `/api/v1/disputes/{disputeId}/assign` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 85 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | contract-execution-controller | Cần Bearer JWT; BUSINESS/EXPERT/STAFF/ADMIN theo flow | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

### Payment, wallet, membership, quota và withdrawal

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 86 | GET | `/api/v1/admin/wallet` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 87 | POST | `/api/v1/admin/wallet/sync` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 88 | POST | `/api/v1/transactions` | contract-execution-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 89 | POST | `/api/v1/transactions/{transactionId}/webhook` | contract-execution-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 90 | PATCH | `/api/v1/transactions/{transactionId}/status` | contract-execution-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 91 | POST | `/api/credits/job-post/purchase` | credit-controller | Cần Bearer JWT; BUSINESS | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 92 | POST | `/api/credits/proposal/purchase` | credit-controller | Cần Bearer JWT; EXPERT | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 93 | GET | `/api/membership/packages` | membership-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 94 | POST | `/api/membership/packages/{packageId}/purchase` | membership-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 95 | GET | `/api/payments/payos/return` | pay-o-s-payment-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 96 | POST | `/api/payments/payos/create` | pay-o-s-payment-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 97 | POST | `/api/payments/payos/{orderCode}/sync` | pay-o-s-payment-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 98 | GET | `/api/users/me/quota` | user-quota-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 99 | GET | `/api/wallet/current` | wallet-api-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 100 | GET | `/api/wallet/transactions` | wallet-api-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 101 | GET | `/api/v1/wallet/me` | wallet-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 102 | GET | `/api/v1/admin/withdrawal-requests` | withdrawal-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 103 | GET | `/api/v1/withdrawal-requests` | withdrawal-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 104 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | withdrawal-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 105 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | withdrawal-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 106 | POST | `/api/v1/withdrawal-requests` | withdrawal-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Có body mẫu bên dưới | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

Body mẫu hay dùng trong luồng này:

**POST `/api/credits/job-post/purchase`**

```json
{
  "quantity": 5
}
```

**POST `/api/credits/proposal/purchase`**

```json
{
  "quantity": 5
}
```

**POST `/api/payments/payos/create`**

```json
{
  "amount": 50000,
  "description": "Nạp ví AITASKER"
}
```

**POST `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve`**

```json
{
  "adminNote": "Đã chuyển khoản thủ công",
  "bankTxCode": "BANK-TX-001"
}
```

**POST `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject`**

```json
{
  "adminNote": "Thông tin tài khoản ngân hàng không hợp lệ"
}
```

**POST `/api/v1/withdrawal-requests`**

```json
{
  "amount": 100000,
  "bankName": "VCB",
  "bankAccountNumber": "0123456789",
  "bankAccountName": "AITASKER USER"
}
```

### Admin vận hành hệ thống

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 107 | GET | `/api/v1/admin/accounts` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 108 | GET | `/api/v1/admin/analytics/overview` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 109 | GET | `/api/v1/admin/audit-logs` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 110 | GET | `/api/v1/admin/settings` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 111 | GET | `/api/v1/admin/staffs` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 112 | POST | `/api/v1/admin/accounts` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 113 | POST | `/api/v1/admin/reviews` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 114 | POST | `/api/v1/admin/staffs` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 115 | PATCH | `/api/v1/admin/accounts/{accountId}` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 116 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 117 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 118 | PATCH | `/api/v1/admin/settings/{key}` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 119 | PATCH | `/api/v1/admin/staffs/{staffId}` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Có JSON body theo schema Swagger | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 120 | DELETE | `/api/v1/admin/accounts/{accountId}` | admin-controller | Cần Bearer JWT; ADMIN/STAFF tùy API | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

### Notification

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 121 | GET | `/api/v1/notifications` | notification-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 122 | GET | `/api/v1/notifications/unread-count` | notification-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 123 | PATCH | `/api/v1/notifications/read-all` | notification-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 124 | PATCH | `/api/v1/notifications/{notificationId}/read` | notification-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

### Dev/Test và health check

| STT | Method | API | Swagger tag | Token/Role | Body khi test | Kết quả cần kiểm tra |
| --- | --- | --- | --- | --- | --- | --- |
| 125 | GET | `/api/health` | health-controller | Không cần token; Public | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |
| 126 | GET | `/api/test/secure` | test-controller | Cần Bearer JWT; User đã đăng nhập phù hợp quyền | Không có body | HTTP 2xx hoặc lỗi nghiệp vụ rõ ràng; kiểm tra `success`, `message`, `data`. |

## 5. Lưu ý payment/contract flow

- PayOS top-up hiện không dùng public webhook endpoint trong controller. Test tạo order bằng `POST /api/payments/payos/create`, sau đó dùng return hoặc sync theo `orderCode`.
- Membership/credit/deposit/withdrawal dùng wallet hiện tại, nên cần chuẩn bị số dư phù hợp.
- `GET /api/users/me/quota` là nguồn chính để frontend đọc quota, active package và premium entitlement.
- Contract chỉ ACTIVE sau khi đủ chữ ký contract, đủ NDA và business trả deposit thành công.

## 6. Kiểm tra sau khi chạy

- Với API tạo/cập nhật, gọi lại API GET tương ứng để xác nhận dữ liệu đã đổi.
- Với API finance, kiểm tra thêm `GET /api/wallet/current`, `GET /api/wallet/transactions`, `GET /api/users/me/quota` hoặc admin wallet tùy flow.
- Với notification, sau hành động nghiệp vụ chính hãy kiểm tra `GET /api/v1/notifications` và `GET /api/v1/notifications/unread-count`.

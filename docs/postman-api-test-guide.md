# Hướng Dẫn Test API Back-end Bằng Postman

Tài liệu này liệt kê các API back-end hiện có và hướng dẫn test bằng Postman. Danh sách API được đồng bộ từ `/v3/api-docs` hiện tại.

## 1. Chuẩn Bị

Chạy hạ tầng và back-end:
```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```
Base URL:
```text
http://localhost:8080
```
Environment khuyến nghị:

| Key | Value |
| --- | --- |
| `baseUrl` | `http://localhost:8080` |
| `businessToken` | Token BUSINESS |
| `expertToken` | Token EXPERT |
| `adminToken` | Token ADMIN |
| `staffToken` | Token STAFF |

Header cho API cần đăng nhập:
```text
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
Với upload file, chọn `Body -> form-data`, key là `file`, type là `File`; không tự set `Content-Type`.

## 2. Tài Khoản Seed Thường Dùng

| Role | Email | Mật khẩu | Token env gợi ý |
| --- | --- | --- | --- |
| BUSINESS | `business@aitasker.local` | `12345678` | `businessToken` |
| EXPERT | `expert@aitasker.local` | `12345678` | `expertToken` |
| ADMIN | `admin@aitasker.local` | `12345678` | `adminToken` |
| STAFF | `staff@aitasker.local` | `12345678` | `staffToken` |

## 3. Danh Sách API Theo Swagger

### admin-controller

#### 1. DELETE /api/v1/admin/accounts/{accountId}

```http
DELETE {{baseUrl}}/api/v1/admin/accounts/{accountId}
```

- Mục đích: Xóa, khóa hoặc vô hiệu hóa dữ liệu theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 2. GET /api/v1/admin/staffs

```http
GET {{baseUrl}}/api/v1/admin/staffs
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 3. GET /api/v1/admin/accounts

```http
GET {{baseUrl}}/api/v1/admin/accounts
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 4. GET /api/v1/admin/wallet

```http
GET {{baseUrl}}/api/v1/admin/wallet
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 5. GET /api/v1/admin/settings

```http
GET {{baseUrl}}/api/v1/admin/settings
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 6. GET /api/v1/admin/reviews/contracts/{contractId}

```http
GET {{baseUrl}}/api/v1/admin/reviews/contracts/{contractId}
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 7. GET /api/v1/admin/audit-logs

```http
GET {{baseUrl}}/api/v1/admin/audit-logs
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 8. GET /api/v1/admin/analytics/overview

```http
GET {{baseUrl}}/api/v1/admin/analytics/overview
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 9. PATCH /api/v1/admin/staffs/{staffId}

```http
PATCH {{baseUrl}}/api/v1/admin/staffs/{staffId}
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "accountId": 4,
  "specialization": "KYB"
}
```

#### 10. PATCH /api/v1/admin/settings/{key}

```http
PATCH {{baseUrl}}/api/v1/admin/settings/{key}
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 11. PATCH /api/v1/admin/accounts/{accountId}

```http
PATCH {{baseUrl}}/api/v1/admin/accounts/{accountId}
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "email": "staff2@aitasker.local",
  "phone": "0900000003",
  "fullName": "Staff Two Updated",
  "role": "STAFF",
  "status": "Approved",
  "specialization": "KYB"
}
```

#### 12. PATCH /api/v1/admin/accounts/{accountId}/status

```http
PATCH {{baseUrl}}/api/v1/admin/accounts/{accountId}/status
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 13. PATCH /api/v1/admin/accounts/{accountId}/active

```http
PATCH {{baseUrl}}/api/v1/admin/accounts/{accountId}/active
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 14. POST /api/v1/admin/wallet/sync

```http
POST {{baseUrl}}/api/v1/admin/wallet/sync
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 15. POST /api/v1/admin/staffs

```http
POST {{baseUrl}}/api/v1/admin/staffs
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "accountId": 4,
  "specialization": "KYC"
}
```

#### 16. POST /api/v1/admin/reviews

```http
POST {{baseUrl}}/api/v1/admin/reviews
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "contractId": 1,
  "reviewerId": 1,
  "revieweeId": 2,
  "rating": 4.5,
  "comment": "Hoàn thành đúng phạm vi."
}
```

#### 17. POST /api/v1/admin/accounts

```http
POST {{baseUrl}}/api/v1/admin/accounts
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Quản lý admin, account, staff, setting, audit log và ví hệ thống.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "email": "staff2@aitasker.local",
  "password": "12345678",
  "phone": "0900000002",
  "fullName": "Staff Two",
  "role": "STAFF",
  "status": "Approved",
  "specialization": "KYC"
}
```

### auth-controller

#### 18. GET /api/auth/me

```http
GET {{baseUrl}}/api/auth/me
```

- Mục đích: Lấy thông tin của account đang đăng nhập.
- Phục vụ: Xác thực, đăng nhập, đăng ký và session.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 19. GET /api/auth/check-email

```http
GET {{baseUrl}}/api/auth/check-email
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Xác thực, đăng nhập, đăng ký và session.
- Token: Không cần token.
- Body: Không có.

#### 20. POST /api/auth/register

```http
POST {{baseUrl}}/api/auth/register
```

- Mục đích: Đăng ký tài khoản sau khi email đã xác thực OTP.
- Phục vụ: Xác thực, đăng nhập, đăng ký và session.
- Token: Không cần token.
- Body raw mẫu:
```json
{
  "email": "expert.manual@example.com",
  "password": "12345678",
  "fullName": "Expert Manual",
  "phone": "0900000001",
  "role": "EXPERT"
}
```

#### 21. POST /api/auth/login

```http
POST {{baseUrl}}/api/auth/login
```

- Mục đích: Đăng nhập và nhận accessToken/refreshToken.
- Phục vụ: Xác thực, đăng nhập, đăng ký và session.
- Token: Không cần token.
- Body raw mẫu:
```json
{
  "email": "admin@aitasker.local",
  "password": "12345678"
}
```

### catalog-controller

#### 22. GET /api/v1/jobs/{jobId}/technologies

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}/technologies
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 23. GET /api/v1/jobs/{jobId}/skills

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}/skills
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 24. GET /api/v1/jobs/{jobId}/domains

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}/domains
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 25. GET /api/v1/technologies

```http
GET {{baseUrl}}/api/v1/technologies
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 26. GET /api/v1/skills

```http
GET {{baseUrl}}/api/v1/skills
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 27. GET /api/v1/domains

```http
GET {{baseUrl}}/api/v1/domains
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 28. GET /api/v1/acceptance-criteria

```http
GET {{baseUrl}}/api/v1/acceptance-criteria
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 29. PATCH /api/v1/technologies/{technologyId}

```http
PATCH {{baseUrl}}/api/v1/technologies/{technologyId}
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "technologyCode": "REACT_TS",
  "technologyName": "React TypeScript",
  "description": "Công nghệ frontend dùng React và TypeScript.",
  "isActive": true,
  "sortOrder": 1
}
```

#### 30. PATCH /api/v1/skills/{skillId}

```http
PATCH {{baseUrl}}/api/v1/skills/{skillId}
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "skillCode": "RAG_ARCH",
  "skillName": "RAG Architecture",
  "description": "Thiết kế retrieval, chunking, embedding và evaluation cho RAG.",
  "isActive": true
}
```

#### 31. PATCH /api/v1/domains/{domainId}

```http
PATCH {{baseUrl}}/api/v1/domains/{domainId}
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "domainCode": "AI_PRODUCT",
  "domainName": "AI Product Strategy",
  "description": "Discovery, feasibility, roadmap và outcome sản phẩm AI.",
  "isActive": true,
  "sortOrder": 1
}
```

#### 32. POST /api/v1/technologies

```http
POST {{baseUrl}}/api/v1/technologies
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "technologyCode": "REACT_TS",
  "technologyName": "React TypeScript",
  "description": "Công nghệ frontend dùng React và TypeScript.",
  "isActive": true,
  "sortOrder": 1
}
```

#### 33. POST /api/v1/skills

```http
POST {{baseUrl}}/api/v1/skills
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "skillCode": "RAG_ARCH",
  "skillName": "RAG Architecture",
  "description": "Thiết kế retrieval, chunking, embedding và evaluation cho RAG.",
  "isActive": true
}
```

#### 34. POST /api/v1/domains

```http
POST {{baseUrl}}/api/v1/domains
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "domainCode": "AI_PRODUCT",
  "domainName": "AI Product Strategy",
  "description": "Discovery, feasibility, roadmap và outcome sản phẩm AI.",
  "isActive": true,
  "sortOrder": 1
}
```

#### 35. PUT /api/v1/jobs/{jobId}/technologies

```http
PUT {{baseUrl}}/api/v1/jobs/{jobId}/technologies
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
[
  1,
  2,
  3
]
```

#### 36. PUT /api/v1/jobs/{jobId}/skills

```http
PUT {{baseUrl}}/api/v1/jobs/{jobId}/skills
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
[
  {
    "skillId": 2,
    "isMandatory": true
  },
  {
    "skillId": 3,
    "isMandatory": false
  }
]
```

#### 37. PUT /api/v1/jobs/{jobId}/domains

```http
PUT {{baseUrl}}/api/v1/jobs/{jobId}/domains
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
[
  2,
  3
]
```

### chatbot-controller

#### 38. POST /api/chatbot/ask

```http
POST {{baseUrl}}/api/chatbot/ask
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Chatbot hỗ trợ người dùng.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "question": "Tôi cần hướng dẫn tạo hồ sơ doanh nghiệp"
}
```

### contract-execution-controller

#### 39. GET /api/v1/milestones/{milestoneId}/transactions

```http
GET {{baseUrl}}/api/v1/milestones/{milestoneId}/transactions
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 40. GET /api/v1/milestones/{milestoneId}/deliverables

```http
GET {{baseUrl}}/api/v1/milestones/{milestoneId}/deliverables
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 41. GET /api/v1/milestones/{milestoneId}/criteria

```http
GET {{baseUrl}}/api/v1/milestones/{milestoneId}/criteria
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 42. GET /api/v1/jobs/{jobId}/milestones

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}/milestones
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 43. GET /api/v1/jobs/{jobId}/matching

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}/matching
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 44. GET /api/v1/disputes/{disputeId}

```http
GET {{baseUrl}}/api/v1/disputes/{disputeId}
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 45. GET /api/v1/contracts

```http
GET {{baseUrl}}/api/v1/contracts
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 46. GET /api/v1/contracts/{contractId}

```http
GET {{baseUrl}}/api/v1/contracts/{contractId}
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 47. GET /api/v1/contracts/{contractId}/milestones

```http
GET {{baseUrl}}/api/v1/contracts/{contractId}/milestones
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 48. GET /api/v1/contracts/{contractId}/disputes

```http
GET {{baseUrl}}/api/v1/contracts/{contractId}/disputes
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 49. PATCH /api/v1/transactions/{transactionId}/status

```http
PATCH {{baseUrl}}/api/v1/transactions/{transactionId}/status
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 50. PATCH /api/v1/disputes/{disputeId}/resolve

```http
PATCH {{baseUrl}}/api/v1/disputes/{disputeId}/resolve
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 51. PATCH /api/v1/disputes/{disputeId}/assign

```http
PATCH {{baseUrl}}/api/v1/disputes/{disputeId}/assign
```

- Mục đích: Cập nhật một phần dữ liệu theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 52. POST /api/v1/transactions

```http
POST {{baseUrl}}/api/v1/transactions
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "milestoneId": 1,
  "amount": 30000000,
  "commissionFee": 3000000,
  "transactionType": "ESCROW",
  "status": "Pending"
}
```

#### 53. POST /api/v1/transactions/{transactionId}/webhook

```http
POST {{baseUrl}}/api/v1/transactions/{transactionId}/webhook
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 54. POST /api/v1/milestones

```http
POST {{baseUrl}}/api/v1/milestones
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "jobId": 1,
  "milestoneName": "Kiểm thử nghiệm thu",
  "description": "Kiểm thử UAT và hoàn thiện tài liệu.",
  "fundsAllocated": 20000000,
  "orderIndex": 3,
  "status": "Pending",
  "criteriaIds": [
    1,
    2
  ]
}
```

#### 55. POST /api/v1/milestones/sla-auto-approve

```http
POST {{baseUrl}}/api/v1/milestones/sla-auto-approve
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 56. POST /api/v1/disputes

```http
POST {{baseUrl}}/api/v1/disputes
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "contractId": 1,
  "milestoneId": 1,
  "evidenceReport": "Deliverable chưa đạt tiêu chí nghiệm thu.",
  "proposedAction": "Yêu cầu chỉnh sửa trong 3 ngày.",
  "status": "Open"
}
```

#### 57. POST /api/v1/disputes/{disputeId}/technical-report

```http
POST {{baseUrl}}/api/v1/disputes/{disputeId}/technical-report
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 58. POST /api/v1/disputes/{disputeId}/demo-testing

```http
POST {{baseUrl}}/api/v1/disputes/{disputeId}/demo-testing
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 59. POST /api/v1/deliverables

```http
POST {{baseUrl}}/api/v1/deliverables
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "milestoneId": 1,
  "sourceCodeUrl": "https://github.com/example/repo",
  "demoLink": "https://demo.example.com",
  "submissionNotes": "Đã nộp source code, demo và tài liệu cài đặt."
}
```

#### 60. POST /api/v1/criteria

```http
POST {{baseUrl}}/api/v1/criteria
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "criteriaCode": "RAG_ANSWER_QUALITY",
  "description": "Chatbot trả lời đúng tối thiểu 80% bộ câu hỏi kiểm thử.",
  "isActive": true,
  "sortOrder": 1
}
```

#### 61. POST /api/v1/contracts/{contractId}/terminate

```http
POST {{baseUrl}}/api/v1/contracts/{contractId}/terminate
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 62. POST /api/v1/contracts/{contractId}/sign

```http
POST {{baseUrl}}/api/v1/contracts/{contractId}/sign
```

- Mục đích: Ký xác nhận hợp đồng hoặc NDA.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 63. POST /api/v1/contracts/{contractId}/nda-sign

```http
POST {{baseUrl}}/api/v1/contracts/{contractId}/nda-sign
```

- Mục đích: Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 64. POST /api/v1/contracts/from-proposals/{proposalId}

```http
POST {{baseUrl}}/api/v1/contracts/from-proposals/{proposalId}
```

- Mục đích: Tạo hợp đồng nháp từ proposal đã được chấp nhận.
- Phục vụ: Hợp đồng, milestone, deliverable, tranh chấp và giao dịch.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "contractTitle": "Hợp đồng triển khai RAG chatbot",
  "timelineDays": 45
}
```

### email-otp-controller

#### 66. POST /api/auth/email/verify-otp

```http
POST {{baseUrl}}/api/auth/email/verify-otp
```

- Mục đích: Xác thực OTP email.
- Phục vụ: Gửi và xác thực OTP email.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "email": "expert.manual@example.com",
  "otp": "123456"
}
```

#### 67. POST /api/auth/email/send-otp

```http
POST {{baseUrl}}/api/auth/email/send-otp
```

- Mục đích: Gửi OTP xác thực email.
- Phục vụ: Gửi và xác thực OTP email.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "email": "expert.manual@example.com"
}
```

### Expert Candidates

#### 68. GET /api/jobs/{jobPostingId}/expert-candidates

```http
GET {{baseUrl}}/api/jobs/{jobPostingId}/expert-candidates
```

- Mục đích: Lấy danh sách ứng viên chuyên gia phù hợp với job.
- Phục vụ: AI đề xuất và lọc chuyên gia phù hợp với job.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

### Expert Recommendations

#### 69. GET /api/jobs/{jobPostingId}/expert-recommendations

```http
GET {{baseUrl}}/api/jobs/{jobPostingId}/expert-recommendations
```

- Mục đích: Sinh hoặc xem danh sách chuyên gia AI đề xuất cho job.
- Phục vụ: AI đề xuất và lọc chuyên gia phù hợp với job.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 70. POST /api/jobs/{jobPostingId}/expert-recommendations

```http
POST {{baseUrl}}/api/jobs/{jobPostingId}/expert-recommendations
```

- Mục đích: Sinh hoặc xem danh sách chuyên gia AI đề xuất cho job.
- Phục vụ: AI đề xuất và lọc chuyên gia phù hợp với job.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

### health-controller

#### 71. GET /api/health

```http
GET {{baseUrl}}/api/health
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Kiểm tra trạng thái backend.
- Token: Không cần token.
- Body: Không có.

### marketplace-controller

#### 72. GET /api/v1/jobs

```http
GET {{baseUrl}}/api/v1/jobs
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 73. GET /api/v1/proposals/my

```http
GET {{baseUrl}}/api/v1/proposals/my
```

- Mục đích: Gửi, xem hoặc duyệt proposal.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 74. GET /api/v1/jobs/{jobId}

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 75. GET /api/v1/jobs/{jobId}/proposals

```http
GET {{baseUrl}}/api/v1/jobs/{jobId}/proposals
```

- Mục đích: Gửi, xem hoặc duyệt proposal.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 76. GET /api/v1/jobs/my

```http
GET {{baseUrl}}/api/v1/jobs/my
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 77. PATCH /api/v1/proposals/{proposalId}/status

```http
PATCH {{baseUrl}}/api/v1/proposals/{proposalId}/status
```

- Mục đích: Gửi, xem hoặc duyệt proposal.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 78. PATCH /api/v1/jobs/{jobId}/status

```http
PATCH {{baseUrl}}/api/v1/jobs/{jobId}/status
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 79. POST /api/v1/proposals

```http
POST {{baseUrl}}/api/v1/proposals
```

- Mục đích: Gửi, xem hoặc duyệt proposal.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "jobId": 1,
  "technicalSolution": "Triển khai RAG với embedding, vector search, backend Spring Boot và frontend React.",
  "proposalDescription": "Chia dự án thành 2 giai đoạn, ưu tiên dữ liệu FAQ và đánh giá chất lượng câu trả lời.",
  "proposalFileUrl": "proposal-files/experts/1/example.pdf",
  "bidAmount": 90000000,
  "proposalMilestone": [
    {
      "milestoneId": 1,
      "proposedBudget": 30000000
    },
    {
      "milestoneId": 2,
      "proposedBudget": 60000000
    }
  ]
}
```

#### 80. POST /api/v1/proposals/file

```http
POST {{baseUrl}}/api/v1/proposals/file
```

- Mục đích: Upload file hoặc lấy URL xem file.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body form-data: key `file`, type `File`, chọn file cần upload.

#### 81. POST /api/v1/jobs

```http
POST {{baseUrl}}/api/v1/jobs
```

- Mục đích: Xem, tạo hoặc cập nhật dữ liệu job.
- Phục vụ: Luồng job posting, job public, proposal và review proposal.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "title": "Tích hợp RAG chatbot cho chăm sóc khách hàng",
  "rawRequirements": "Cần chatbot trả lời câu hỏi sản phẩm, lấy dữ liệu từ FAQ và chuyển lead cho nhân viên.",
  "structuredSow": "Triển khai RAG chatbot, dashboard quản trị nội dung và API tích hợp CRM.",
  "budget": 90000000,
  "plannedDurationValue": 6,
  "plannedDurationUnit": "WEEK",
  "domainIds": [
    2,
    3
  ],
  "skills": [
    {
      "skillId": 2,
      "isMandatory": true
    },
    {
      "skillId": 3,
      "isMandatory": false
    }
  ],
  "technologyIds": [
    1,
    2
  ],
  "milestones": [
    {
      "milestoneName": "Phân tích yêu cầu và thiết kế RAG",
      "description": "Khảo sát FAQ, thiết kế luồng dữ liệu và kiến trúc retrieval.",
      "fundsAllocated": 30000000,
      "orderIndex": 1,
      "criteriaIds": [
        1,
        2
      ]
    },
    {
      "milestoneName": "Triển khai chatbot và bàn giao",
      "description": "Xây dựng API, giao diện chat, kiểm thử và tài liệu bàn giao.",
      "fundsAllocated": 60000000,
      "orderIndex": 2,
      "criteriaIds": [
        3,
        4
      ]
    }
  ]
}
```

### notification-controller

#### 82. GET /api/v1/notifications

```http
GET {{baseUrl}}/api/v1/notifications
```

- Mục đích: Lấy hoặc cập nhật trạng thái thông báo.
- Phục vụ: Thông báo realtime và trạng thái đã đọc.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 83. GET /api/v1/notifications/unread-count

```http
GET {{baseUrl}}/api/v1/notifications/unread-count
```

- Mục đích: Lấy hoặc cập nhật trạng thái thông báo.
- Phục vụ: Thông báo realtime và trạng thái đã đọc.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 84. PATCH /api/v1/notifications/{notificationId}/read

```http
PATCH {{baseUrl}}/api/v1/notifications/{notificationId}/read
```

- Mục đích: Lấy hoặc cập nhật trạng thái thông báo.
- Phục vụ: Thông báo realtime và trạng thái đã đọc.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 85. PATCH /api/v1/notifications/read-all

```http
PATCH {{baseUrl}}/api/v1/notifications/read-all
```

- Mục đích: Lấy hoặc cập nhật trạng thái thông báo.
- Phục vụ: Thông báo realtime và trạng thái đã đọc.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

### pay-os-payment-controller

#### 86. GET /api/payments/payos/return

```http
GET {{baseUrl}}/api/payments/payos/return
```

- Mục đích: Tạo, đồng bộ hoặc nhận callback thanh toán.
- Phục vụ: Thanh toán PayOS và nạp ví.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 87. POST /api/payments/payos/{orderCode}/sync

```http
POST {{baseUrl}}/api/payments/payos/{orderCode}/sync
```

- Mục đích: Tạo, đồng bộ hoặc nhận callback thanh toán.
- Phục vụ: Thanh toán PayOS và nạp ví.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 88. POST /api/payments/payos/webhook

```http
POST {{baseUrl}}/api/payments/payos/webhook
```

- Mục đích: Tạo, đồng bộ hoặc nhận callback thanh toán.
- Phục vụ: Thanh toán PayOS và nạp ví.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "code": "00",
  "desc": "success",
  "success": true,
  "data": {
    "orderCode": 123456,
    "amount": 50000,
    "description": "Nạp ví AITASKER",
    "accountNumber": "123456789",
    "reference": "PAYOS_REF",
    "transactionDateTime": "2026-06-18 10:00:00",
    "currency": "VND",
    "paymentLinkId": "link-id",
    "code": "00",
    "desc": "success"
  },
  "signature": "test-signature"
}
```

#### 89. POST /api/payments/payos/create

```http
POST {{baseUrl}}/api/payments/payos/create
```

- Mục đích: Tạo, đồng bộ hoặc nhận callback thanh toán.
- Phục vụ: Thanh toán PayOS và nạp ví.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "amount": 50000,
  "description": "Nạp ví AITASKER"
}
```

### profile-controller

#### 90. GET /api/v1/profiles/portfolio

```http
GET {{baseUrl}}/api/v1/profiles/portfolio
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 91. GET /api/v1/profiles/expert

```http
GET {{baseUrl}}/api/v1/profiles/expert
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 92. GET /api/v1/profiles/business

```http
GET {{baseUrl}}/api/v1/profiles/business
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 93. GET /api/v1/profiles/business/{businessId}

```http
GET {{baseUrl}}/api/v1/profiles/business/{businessId}
```

- Mục đích: Lấy thông tin business profile theo ID để xem trang cá nhân doanh nghiệp.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 94. GET /api/v1/profiles/expert/{expertId}

```http
GET {{baseUrl}}/api/v1/profiles/expert/{expertId}
```

- Mục đích: Lấy thông tin expert profile theo ID để xem trang cá nhân chuyên gia.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 95. GET /api/v1/profiles/portfolio/me

```http
GET {{baseUrl}}/api/v1/profiles/portfolio/me
```

- Mục đích: Lấy thông tin của account đang đăng nhập.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 96. GET /api/v1/profiles/files/view-url

```http
GET {{baseUrl}}/api/v1/profiles/files/view-url
```

- Mục đích: Upload file hoặc lấy URL xem file.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 97. GET /api/v1/profiles/expert/me

```http
GET {{baseUrl}}/api/v1/profiles/expert/me
```

- Mục đích: Lấy thông tin của account đang đăng nhập.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 98. GET /api/v1/profiles/business/me

```http
GET {{baseUrl}}/api/v1/profiles/business/me
```

- Mục đích: Lấy thông tin của account đang đăng nhập.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 99. GET /api/v1/profiles/business/by-job/{jobId}

```http
GET {{baseUrl}}/api/v1/profiles/business/by-job/{jobId}
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

#### 100. POST /api/v1/profiles/portfolio

```http
POST {{baseUrl}}/api/v1/profiles/portfolio
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "domainIds": "2,3",
  "skillIds": "2,3,6",
  "technologyIds": "1,2,3",
  "yearsExperience": 5,
  "certificates": "expert-certificates/accounts/2/certificate-demo.pdf",
  "selfDescription": "Tôi có kinh nghiệm triển khai RAG, backend Spring Boot và frontend React."
}
```

#### 101. POST /api/v1/profiles/portfolio/certificate-file

```http
POST {{baseUrl}}/api/v1/profiles/portfolio/certificate-file
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body form-data: key `file`, type `File`, chọn file cần upload.

#### 102. POST /api/v1/profiles/expert

```http
POST {{baseUrl}}/api/v1/profiles/expert
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "nationalId": "079201000001",
  "portfolioUrl": "https://portfolio.example.com/expert-ai",
  "yearsOfExperience": 5,
  "title": "AI Engineer"
}
```

#### 103. POST /api/v1/profiles/business

```http
POST {{baseUrl}}/api/v1/profiles/business
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "taxCode": "0312345678",
  "companyName": "Nova Retail",
  "address": "Quận 1, TP. Hồ Chí Minh",
  "businessLicenseUrl": "business-licenses/accounts/1/license-demo.pdf"
}
```

#### 104. POST /api/v1/profiles/business/license-file

```http
POST {{baseUrl}}/api/v1/profiles/business/license-file
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body form-data: key `file`, type `File`, chọn file cần upload.

#### 105. POST /api/v1/profiles/approve/{type}/{id}

```http
POST {{baseUrl}}/api/v1/profiles/approve/{type}/{id}
```

- Mục đích: Xem, tạo hoặc cập nhật hồ sơ người dùng.
- Phục vụ: Hồ sơ business, expert, portfolio và file Firebase.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

### SoW Generation

#### 106. POST /api/jobs/generate-sow

```http
POST {{baseUrl}}/api/jobs/generate-sow
```

- Mục đích: AI sinh SoW, milestone gợi ý và cấu trúc dự án.
- Phục vụ: AI generate SoW, milestone gợi ý và cấu trúc dự án.
- Token: Cần Bearer token theo role phù hợp.
- Body raw mẫu:
```json
{
  "projectTitle": "Tích hợp RAG chatbot cho chăm sóc khách hàng",
  "rawRequirement": "Cần chatbot trả lời câu hỏi sản phẩm, lấy dữ liệu từ FAQ và chuyển lead cho nhân viên.",
  "budget": 90000000,
  "duration": 6,
  "durationUnit": "WEEK",
  "supportFields": [
    "E-commerce",
    "Customer Support"
  ],
  "requiredSkills": [
    "RAG Architecture",
    "React TypeScript",
    "Java Spring Boot"
  ]
}
```

### tax-check-controller

#### 107. GET /api/auth/tax-check/{mst}

```http
GET {{baseUrl}}/api/auth/tax-check/{mst}
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: Kiểm tra mã số thuế.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

### test-controller

#### 108. GET /api/test/secure

```http
GET {{baseUrl}}/api/test/secure
```

- Mục đích: Lấy dữ liệu hoặc danh sách theo endpoint này.
- Phục vụ: API kiểm thử bảo mật trong môi trường dev.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

### wallet-controller

#### 109. GET /api/v1/wallet/me

```http
GET {{baseUrl}}/api/v1/wallet/me
```

- Mục đích: Lấy thông tin của account đang đăng nhập.
- Phục vụ: Ví của người dùng.
- Token: Cần Bearer token theo role phù hợp.
- Body: Không có.

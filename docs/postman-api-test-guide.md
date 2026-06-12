# Hướng Dẫn Test API Back-end Bằng Postman

Tài liệu này liệt kê các API đang sử dụng được trong back-end hiện tại và hướng dẫn test bằng Postman theo luồng thực tế của dự án.

## 1. Chuẩn Bị

Chạy database và back-end:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Base URL mặc định:

```text
http://localhost:8080
```

Header dùng cho các API cần đăng nhập:

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

Với API upload file, không đặt `Content-Type` thủ công. Postman sẽ tự tạo `multipart/form-data`.

## 2. Tài Khoản Seed Hiện Có

Các tài khoản seed trong database hiện tại đều có mật khẩu:

```text
12345678
```

| Role | Email | Account ID | Ghi chú |
| --- | --- | ---: | --- |
| BUSINESS | `business@aitasker.local` | 1 | Có business profile `businessId=1`, trạng thái `Approved` |
| EXPERT | `expert@aitasker.local` | 2 | Có expert profile `expertId=1`, trạng thái `Approved` |
| ADMIN | `admin@aitasker.local` | 3 | Dùng để quản trị account, setting, audit log |
| STAFF | `staff@aitasker.local` | 4 | Có staff profile `staffId=1`, dùng duyệt KYC/KYB |

ID seed hay dùng:

| Loại dữ liệu | ID mẫu |
| --- | --- |
| `businessId` | `1` |
| `expertId` | `1` |
| `jobId` | `1`, `2`, `3` |
| `proposalId` | `1`, `2`, `3` |
| `contractId` | `1`, `2` |
| `milestoneId` | `1`, `2`, `3`, `4` |
| `transactionId` | `1`, `2`, `3`, `4` |
| `disputeId` | `1`, `2` |
| `domainId` | `2`, `3`, `4`, `8` |
| `skillId` | `2`, `3`, `6`, `8`, `9`, `14`, `15` |

## 3. Cách Lấy Token Trong Postman

Gọi API login, sau đó copy token trả về trong `data.accessToken` hoặc field token tương ứng của response.

```http
POST {{baseUrl}}/api/auth/login
```

Raw body:

```json
{
  "email": "business@aitasker.local",
  "password": "12345678"
}
```

Trong Postman tạo Environment:

| Key | Value |
| --- | --- |
| `baseUrl` | `http://localhost:8080` |
| `businessToken` | token của business |
| `expertToken` | token của expert |
| `adminToken` | token của admin |
| `staffToken` | token của staff |

Khi test API theo role nào thì dùng:

```text
Authorization: Bearer {{businessToken}}
```

hoặc đổi sang token role tương ứng.

## 4. Auth API

### Register

```http
POST {{baseUrl}}/api/auth/register
```

Raw body:

```json
{
  "email": "new.business@aitasker.local",
  "password": "12345678",
  "fullName": "New Business",
  "phone": "0909000001",
  "role": "BUSINESS"
}
```

Role hợp lệ thường dùng: `BUSINESS`, `EXPERT`, `ADMIN`, `STAFF`.

### Login

```http
POST {{baseUrl}}/api/auth/login
```

Raw body:

```json
{
  "email": "expert@aitasker.local",
  "password": "12345678"
}
```

### Lấy phiên đăng nhập hiện tại

```http
GET {{baseUrl}}/api/auth/me
```

Cần token.

### Kiểm tra email đã tồn tại

```http
GET {{baseUrl}}/api/auth/check-email?email=expert@aitasker.local
```

### Gửi OTP email

```http
POST {{baseUrl}}/api/auth/email/send-otp
```

Raw body:

```json
{
  "email": "expert@aitasker.local"
}
```

### Xác minh OTP email

```http
POST {{baseUrl}}/api/auth/email/verify-otp
```

Raw body:

```json
{
  "email": "expert@aitasker.local",
  "otp": "123456"
}
```

### Kiểm tra mã số thuế

```http
GET {{baseUrl}}/api/auth/tax-check/0312345678
```

## 5. Profile, Portfolio Và Firebase File API

### Business tạo hoặc cập nhật hồ sơ KYB

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/profiles/business
```

Raw body:

```json
{
  "taxCode": "0312345678",
  "companyName": "Nova Retail",
  "address": "Quan 1, TP. Ho Chi Minh",
  "businessLicenseUrl": "business-licenses/accounts/1/license-demo.pdf"
}
```

Lưu ý: Khi submit lại hồ sơ, account và hồ sơ sẽ về `Pending` để staff duyệt.

### Upload giấy phép kinh doanh

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/profiles/business/license-file
```

Postman body:

| Type | Key | Value |
| --- | --- | --- |
| form-data | `file` | chọn file PDF/JPG/PNG |

Response trả về path Firebase. Dùng path đó gán vào `businessLicenseUrl` khi gọi API business profile.

### Lấy hồ sơ business của tài khoản hiện tại

Token: BUSINESS.

```http
GET {{baseUrl}}/api/v1/profiles/business/me
```

### Lấy business profile theo job

Public nếu job đang `OPEN`.

```http
GET {{baseUrl}}/api/v1/profiles/business/by-job/1
```

### Staff xem danh sách business profile

Token: STAFF.

```http
GET {{baseUrl}}/api/v1/profiles/business
```

### Expert tạo hoặc cập nhật hồ sơ KYC

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/profiles/expert
```

Raw body:

```json
{
  "nationalId": "079203009999",
  "portfolioUrl": "https://portfolio.aitasker.local/expert-demo",
  "yearsOfExperience": 5
}
```

Lưu ý: `nationalId` là unique, nếu dùng lại số đã tồn tại sẽ lỗi.

### Lấy hồ sơ expert của tài khoản hiện tại

Token: EXPERT.

```http
GET {{baseUrl}}/api/v1/profiles/expert/me
```

### Staff hoặc Business xem danh sách expert profile

Token: STAFF hoặc BUSINESS.

```http
GET {{baseUrl}}/api/v1/profiles/expert
```

### Staff duyệt hoặc từ chối hồ sơ

Token: STAFF.

```http
POST {{baseUrl}}/api/v1/profiles/approve/BUSINESS/1?status=Approved
```

```http
POST {{baseUrl}}/api/v1/profiles/approve/EXPERT/1?status=Rejected
```

Giá trị `type`: `BUSINESS`, `EXPERT`.

Giá trị `status`: `Approved`, `Rejected`.

### Expert tạo hoặc cập nhật portfolio

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/profiles/portfolio
```

Raw body dựa trên dữ liệu seed:

```json
{
  "domainIds": "2,3,5",
  "skillIds": "2,3,5,8",
  "yearsExperience": 5,
  "certificates": "expert-certificates/accounts/2/certificate-demo.pdf",
  "selfDescription": "Chuyên gia AI có kinh nghiệm xây dựng RAG, xử lý dữ liệu, thiết kế API và triển khai giải pháp AI cho doanh nghiệp."
}
```

### Upload chứng chỉ expert

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/profiles/portfolio/certificate-file
```

Postman body:

| Type | Key | Value |
| --- | --- | --- |
| form-data | `file` | chọn file PDF/JPG/PNG |

Response trả về path Firebase. Dùng path đó gán vào `certificates`.

### Lấy portfolio của expert hiện tại

Token: EXPERT.

```http
GET {{baseUrl}}/api/v1/profiles/portfolio/me
```

### Staff hoặc Business xem danh sách portfolio

Token: STAFF hoặc BUSINESS.

```http
GET {{baseUrl}}/api/v1/profiles/portfolio
```

### Tạo link xem file Firebase

Token: STAFF, ADMIN, BUSINESS hoặc EXPERT.

```http
GET {{baseUrl}}/api/v1/profiles/files/view-url?path=expert-certificates/accounts/2/certificate-demo.pdf
```

Nếu `path` đã là URL `http/https`, API trả lại nguyên URL.

## 6. Catalog API

### Xem domains

Public.

```http
GET {{baseUrl}}/api/v1/domains?activeOnly=true
```

### Admin tạo domain

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/domains
```

Raw body:

```json
{
  "domainCode": "AI_TESTING",
  "domainName": "AI Testing",
  "description": "Kiểm thử và đánh giá hệ thống AI.",
  "isActive": true,
  "sortOrder": 21
}
```

### Admin cập nhật domain

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/domains/1
```

Raw body:

```json
{
  "domainName": "AI Product Strategy",
  "description": "Discovery, feasibility, roadmap và outcome sản phẩm AI.",
  "isActive": true,
  "sortOrder": 1
}
```

### Xem skills

Public.

```http
GET {{baseUrl}}/api/v1/skills?activeOnly=true
```

### Admin tạo skill

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/skills
```

Raw body:

```json
{
  "skillCode": "AI_TEST_CASE_DESIGN",
  "skillName": "AI Test Case Design",
  "description": "Thiết kế bộ test case cho hệ thống AI.",
  "isActive": true
}
```

### Admin cập nhật skill

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/skills/2
```

Raw body:

```json
{
  "skillName": "RAG Architecture",
  "description": "Retrieval, reranking, grounding và knowledge-base design.",
  "isActive": true
}
```

### Xem domain của job

Public nếu biết `jobId`.

```http
GET {{baseUrl}}/api/v1/jobs/1/domains
```

### Business hoặc Admin thay domain của job

Token: BUSINESS sở hữu job hoặc ADMIN.

```http
PUT {{baseUrl}}/api/v1/jobs/1/domains
```

Raw body:

```json
[2, 3]
```

### Xem skill của job

Public nếu biết `jobId`.

```http
GET {{baseUrl}}/api/v1/jobs/1/skills
```

### Business hoặc Admin thay skill của job

Token: BUSINESS sở hữu job hoặc ADMIN.

```http
PUT {{baseUrl}}/api/v1/jobs/1/skills
```

Raw body:

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

## 7. Job Và Proposal API

### Business tạo job nháp

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/jobs
```

Raw body:

```json
{
  "title": "Tích hợp RAG chatbot cho chăm sóc khách hàng",
  "rawRequirements": "Cần chatbot trả lời câu hỏi sản phẩm, lấy dữ liệu từ FAQ và chuyển lead cho nhân viên.",
  "structuredSow": "Scope: ingestion FAQ, RAG chatbot, handoff CRM, analytics dashboard.",
  "budget": 90000000,
  "plannedDurationValue": 6,
  "plannedDurationUnit": "WEEK",
  "isHot": false
}
```

Lưu ý: Service luôn set `status` thành `DRAFT`, không cần gửi `businessId` hay `status`.

Sau khi tạo job, dùng thêm:

```http
PUT {{baseUrl}}/api/v1/jobs/{{jobId}}/domains
PUT {{baseUrl}}/api/v1/jobs/{{jobId}}/skills
POST {{baseUrl}}/api/v1/milestones
```

để gán domain, skill và milestone cho job.

### Xem job public

Public. API chỉ trả job `OPEN`.

```http
GET {{baseUrl}}/api/v1/jobs
```

Response job có field `proposalsCount` để FE hiển thị tổng số proposal.

### Business xem job của mình

Token: BUSINESS.

```http
GET {{baseUrl}}/api/v1/jobs/my
```

API trả cả `DRAFT`, `OPEN`, `CLOSED`, `CANCELLED`.

### Xem chi tiết job

Public với job `OPEN`. Job nháp chỉ business sở hữu được xem.

```http
GET {{baseUrl}}/api/v1/jobs/1
```

### Business mở job

Token: BUSINESS.

```http
PATCH {{baseUrl}}/api/v1/jobs/1/status?status=OPEN
```

Status hợp lệ:

```text
DRAFT, OPEN, CLOSED, CANCELLED
```

### Expert gửi proposal

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/proposals
```

Raw body dựa trên seed `jobId=1`, portfolio `domainIds=2,3,5`, `skillIds=2,3,5,8`:

```json
{
  "jobId": 1,
  "domainId": 3,
  "skillId": 2,
  "technicalSolution": "Triển khai RAG chatbot bằng PostgreSQL metadata, vector retrieval, reranking và CRM handoff.",
  "bidAmount": 110000000
}
```

Điều kiện:

- Job phải `OPEN`.
- Expert phải `Approved`.
- Expert phải có portfolio.
- `domainId` phải thuộc job và portfolio của expert.
- `skillId` phải thuộc job và portfolio của expert.
- Một expert chỉ được gửi 1 proposal cho 1 job.

### Expert xem proposal của mình

Token: EXPERT.

```http
GET {{baseUrl}}/api/v1/proposals/my
```

### Business xem proposal theo job

Token: BUSINESS sở hữu job.

```http
GET {{baseUrl}}/api/v1/jobs/1/proposals
```

### Business duyệt hoặc từ chối proposal

Token: BUSINESS sở hữu job.

```http
PATCH {{baseUrl}}/api/v1/proposals/2/status?status=Accepted
```

Status hợp lệ:

```text
Accepted, Rejected
```

## 8. Contract Và Execution API

### Tạo contract draft từ proposal

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/contracts/from-proposals/2
```

Raw body:

```json
{
  "technologyUsed": "Spring Boot, PostgreSQL, React, Firebase Storage",
  "totalBudget": 78000000,
  "timelineDays": 42,
  "ndaSigned": false,
  "status": "Draft"
}
```

### Tạo yêu cầu thay đổi contract

Token: BUSINESS hoặc EXPERT.

```http
POST {{baseUrl}}/api/v1/contracts/change-requests
```

Raw body:

```json
{
  "contractId": 1,
  "requestedByAccountId": 1,
  "changeType": "ScopeAdjustment",
  "changeSummary": "Bổ sung thêm bộ test tiếng Việt và báo cáo nghiệm thu.",
  "proposedBudget": 120000000,
  "proposedTimelineDays": 60,
  "status": "Pending"
}
```

### Kích hoạt contract

Token: BUSINESS hoặc role được service cho phép.

```http
POST {{baseUrl}}/api/v1/contracts/1/activate
```

### Ký NDA

Token: BUSINESS hoặc EXPERT thuộc contract.

```http
POST {{baseUrl}}/api/v1/contracts/1/nda-sign
```

### Chấm dứt contract

Token: BUSINESS, EXPERT hoặc ADMIN tùy rule service.

```http
POST {{baseUrl}}/api/v1/contracts/1/terminate?reason=Không tiếp tục triển khai
```

### Tạo milestone

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/milestones
```

Raw body:

```json
{
  "jobId": 1,
  "contractId": 1,
  "milestoneName": "Bàn giao MVP chatbot",
  "fundsAllocated": 52000000,
  "orderIndex": 2,
  "status": "Pending"
}
```

Nếu milestone thuộc job nháp chưa có contract, có thể để `contractId` là `null`:

```json
{
  "jobId": 1,
  "contractId": null,
  "milestoneName": "Khảo sát và chốt yêu cầu",
  "fundsAllocated": 15000000,
  "orderIndex": 1,
  "status": "Pending"
}
```

### Tạo acceptance criteria

```http
POST {{baseUrl}}/api/v1/criteria
```

Raw body:

```json
{
  "milestoneId": 2,
  "description": "Chatbot trả lời từ nguồn dữ liệu được duyệt và có trích dẫn.",
  "isPassed": false
}
```

### Expert nộp deliverable

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/deliverables
```

Raw body:

```json
{
  "milestoneId": 2,
  "sourceCodeUrl": "https://github.com/demo/aitasker-rag-chatbot",
  "demoLink": "https://demo.aitasker.local/rag-chatbot",
  "submissionNotes": "Đã nộp MVP kèm tài liệu cài đặt và bộ test mẫu."
}
```

### Tạo transaction

```http
POST {{baseUrl}}/api/v1/transactions
```

Raw body:

```json
{
  "milestoneId": 2,
  "amount": 52000000,
  "commissionFee": 5200000,
  "transactionType": "Deposit",
  "status": "Pending"
}
```

### Cập nhật trạng thái transaction

```http
PATCH {{baseUrl}}/api/v1/transactions/2/status?status=Success
```

### Payment webhook giả lập

```http
POST {{baseUrl}}/api/v1/transactions/2/webhook?paymentStatus=Success&bankTxCode=VNPAY-DEMO-001&receiptImgUrl=https://storage.aitasker.local/receipts/demo.png
```

### Tạo dispute

```http
POST {{baseUrl}}/api/v1/disputes
```

Raw body:

```json
{
  "contractId": 1,
  "milestoneId": 2,
  "assignedStaffId": 1,
  "evidenceReport": "Business yêu cầu kiểm tra thêm bộ test hallucination trước khi nghiệm thu.",
  "proposedAction": "Hold escrow",
  "status": "UnderReview"
}
```

### Gán dispute cho staff

```http
PATCH {{baseUrl}}/api/v1/disputes/1/assign?staffId=1
```

### Resolve dispute

```http
PATCH {{baseUrl}}/api/v1/disputes/1/resolve?proposedAction=Release payout after correction accepted
```

### Staff ghi kết quả demo testing

```http
POST {{baseUrl}}/api/v1/disputes/1/demo-testing?testResult=Passed 18/20 scenarios, cần bổ sung 2 case fallback
```

### Staff tạo technical report

```http
POST {{baseUrl}}/api/v1/disputes/1/technical-report?reportContent=Kết quả kiểm thử đạt yêu cầu sau chỉnh sửa&proposedAction=Release escrow
```

### Chạy SLA auto approve

```http
POST {{baseUrl}}/api/v1/milestones/sla-auto-approve
```

### Danh sách contract

```http
GET {{baseUrl}}/api/v1/contracts
```

### Milestone theo contract

```http
GET {{baseUrl}}/api/v1/contracts/1/milestones
```

### Milestone theo job

```http
GET {{baseUrl}}/api/v1/jobs/1/milestones
```

### Criteria theo milestone

```http
GET {{baseUrl}}/api/v1/milestones/2/criteria
```

### Deliverable theo milestone

```http
GET {{baseUrl}}/api/v1/milestones/2/deliverables
```

### Transaction theo milestone

```http
GET {{baseUrl}}/api/v1/milestones/2/transactions
```

### Dispute theo contract

```http
GET {{baseUrl}}/api/v1/contracts/1/disputes
```

### Chi tiết dispute

```http
GET {{baseUrl}}/api/v1/disputes/1
```

### Matching theo job

```http
GET {{baseUrl}}/api/v1/jobs/1/matching
```

## 9. Admin API

### Tạo review

Token: BUSINESS hoặc EXPERT thuộc contract đã kết thúc.

```http
POST {{baseUrl}}/api/v1/admin/reviews
```

Raw body:

```json
{
  "contractId": 2,
  "rating": 4.9,
  "comment": "Hợp tác tốt, phản hồi nhanh và bàn giao đúng phạm vi."
}
```

Service tự set `reviewerId` và `revieweeId` theo token đang đăng nhập.

### Xem review theo contract

Token: ADMIN, STAFF, BUSINESS hoặc EXPERT.

```http
GET {{baseUrl}}/api/v1/admin/reviews/contracts/2
```

### Admin xem system settings

Token: ADMIN.

```http
GET {{baseUrl}}/api/v1/admin/settings
```

### Admin cập nhật setting

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/admin/settings/platform_fee_percent?value=12&isActive=true
```

### Admin xem audit log

Token: ADMIN.

```http
GET {{baseUrl}}/api/v1/admin/audit-logs
```

Lọc theo nhóm role:

```http
GET {{baseUrl}}/api/v1/admin/audit-logs?actorGroup=INTERNAL
GET {{baseUrl}}/api/v1/admin/audit-logs?actorGroup=EXTERNAL
```

### Admin xem staff

Token: ADMIN.

```http
GET {{baseUrl}}/api/v1/admin/staffs
```

### Admin tạo staff profile

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/admin/staffs
```

Raw body:

```json
{
  "accountId": 4,
  "specialization": "KYB/KYC profile verification and AI dispute review"
}
```

### Admin cập nhật staff profile

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/admin/staffs/1
```

Raw body:

```json
{
  "specialization": "KYB/KYC verification, dispute testing and technical report"
}
```

### Admin xem analytics overview

Token: ADMIN.

```http
GET {{baseUrl}}/api/v1/admin/analytics/overview
```

### Admin xem ví hệ thống

Token: ADMIN.

```http
GET {{baseUrl}}/api/v1/admin/wallet
```

### Admin sync ví hệ thống

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/admin/wallet/sync
```

### Admin xem account

Token: ADMIN.

```http
GET {{baseUrl}}/api/v1/admin/accounts
```

### Admin tạo account

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/admin/accounts
```

Raw body:

```json
{
  "email": "staff.new@aitasker.local",
  "password": "12345678",
  "phone": "0909000004",
  "fullName": "Staff New",
  "role": "STAFF",
  "status": "Approved",
  "specialization": "KYB/KYC verification"
}
```

### Admin cập nhật account

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/admin/accounts/4
```

Raw body:

```json
{
  "phone": "0901000999",
  "fullName": "Pham Quoc Huy",
  "status": "Approved",
  "specialization": "KYB/KYC profile verification"
}
```

### Admin bật hoặc khóa account

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/admin/accounts/4/active?active=true
```

### Admin đổi status account

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/admin/accounts/4/status?status=Approved
```

Status hợp lệ:

```text
Pending, Approved, Rejected, Lock
```

### Admin khóa account bằng DELETE mềm

Token: ADMIN.

```http
DELETE {{baseUrl}}/api/v1/admin/accounts/4
```

## 10. Wallet API

### Xem ví của tài khoản hiện tại

Token: BUSINESS, EXPERT, ADMIN hoặc STAFF.

```http
GET {{baseUrl}}/api/v1/wallet/me
```

## 11. Chatbot, Health Và Test API

### Health check

Public.

```http
GET {{baseUrl}}/api/health
```

### Chatbot ask

Public.

```http
POST {{baseUrl}}/api/chatbot/ask
```

Raw body:

```json
{
  "question": "AITasker hỗ trợ doanh nghiệp tìm chuyên gia AI như thế nào?"
}
```

### Test secure endpoint

Cần token.

```http
GET {{baseUrl}}/api/test/secure
```

## 12. Luồng Test Khuyến Nghị Trong Postman

### Luồng 1: Auth và profile

1. Login `business@aitasker.local`.
2. Login `expert@aitasker.local`.
3. Login `staff@aitasker.local`.
4. Gọi `GET /api/auth/me` với từng token để chắc chắn token đúng role.
5. Business gọi `POST /api/v1/profiles/business`.
6. Expert gọi `POST /api/v1/profiles/expert`.
7. Staff gọi `POST /api/v1/profiles/approve/BUSINESS/{id}?status=Approved`.
8. Staff gọi `POST /api/v1/profiles/approve/EXPERT/{id}?status=Approved`.

### Luồng 2: Job posting và proposal

1. Business gọi `POST /api/v1/jobs`.
2. Business gọi `PUT /api/v1/jobs/{jobId}/domains`.
3. Business gọi `PUT /api/v1/jobs/{jobId}/skills`.
4. Business gọi `POST /api/v1/milestones` để tạo milestone cho job.
5. Business gọi `PATCH /api/v1/jobs/{jobId}/status?status=OPEN`.
6. Expert gọi `GET /api/v1/jobs`.
7. Expert gọi `POST /api/v1/proposals`.
8. Business gọi `GET /api/v1/jobs/{jobId}/proposals`.
9. Business gọi `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted`.

### Luồng 3: Portfolio và file Firebase

1. Expert upload file qua `POST /api/v1/profiles/portfolio/certificate-file`.
2. Copy path trả về vào field `certificates`.
3. Expert gọi `POST /api/v1/profiles/portfolio`.
4. Expert gọi `GET /api/v1/profiles/portfolio/me`.
5. Business hoặc Staff gọi `GET /api/v1/profiles/files/view-url?path=...` để lấy link xem file.

### Luồng 4: Admin audit log

1. Login admin.
2. Gọi `GET /api/v1/admin/audit-logs`.
3. Test thêm `actorGroup=INTERNAL`.
4. Test thêm `actorGroup=EXTERNAL`.
5. Thực hiện một hành động như business tạo job hoặc expert nộp proposal.
6. Gọi lại audit log để kiểm tra log mới.

## 13. Lỗi Thường Gặp Khi Test

### 401 Unauthorized

Nguyên nhân thường gặp:

- Chưa thêm header `Authorization`.
- Token hết hạn.
- Dán token sai role.

### 403 Forbidden hoặc lỗi quyền

Nguyên nhân thường gặp:

- Dùng token EXPERT để tạo job.
- Dùng token BUSINESS để tạo domain/skill admin.
- Dùng token không phải STAFF để duyệt profile.
- Business xem proposal của job không thuộc doanh nghiệp mình.

### 500 khi upload file Firebase

Kiểm tra:

- File service account Firebase có đúng đường dẫn trong `.env`.
- Firebase Storage bucket đã bật.
- API upload dùng `form-data`, key phải đúng là `file`.

### Proposal không nộp được

Kiểm tra:

- Job đã `OPEN`.
- Expert đã `Approved`.
- Expert đã có portfolio.
- `domainId` và `skillId` thuộc cả job và portfolio.
- Expert chưa từng nộp proposal cho job đó.

### Job public không hiện proposal count

Gọi:

```http
GET {{baseUrl}}/api/v1/jobs
```

Kiểm tra response từng job có field:

```json
{
  "proposalsCount": 1
}
```

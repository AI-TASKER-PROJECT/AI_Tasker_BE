# Hướng Dẫn Test API Back-end Bằng Postman

Tài liệu này liệt kê các API back-end đang dùng được ở hiện tại và hướng dẫn test bằng Postman theo flow thực tế của dự án.

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

Header cho API cần đăng nhập:

```text
Authorization: Bearer {{token}}
Content-Type: application/json
```

Với API upload file, không tự set `Content-Type`; để Postman tự tạo `multipart/form-data`.

Environment khuyến nghị:

| Key | Value |
| --- | --- |
| `baseUrl` | `http://localhost:8080` |
| `businessToken` | Token BUSINESS |
| `expertToken` | Token EXPERT |
| `adminToken` | Token ADMIN |
| `staffToken` | Token STAFF |

## 2. Tài Khoản Seed

Mật khẩu seed thường dùng:

```text
12345678
```

| Role | Email | Ghi chú |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | Có business profile đã duyệt |
| EXPERT | `expert@aitasker.local` | Có expert profile đã duyệt |
| ADMIN | `admin@aitasker.local` | Quản trị account, setting, audit log |
| STAFF | `staff@aitasker.local` | Duyệt KYC/KYB |

ID seed thường dùng:

| Loại dữ liệu | ID mẫu |
| --- | --- |
| `businessId` | `1` |
| `expertId` | `1` |
| `jobId` | `1`, `2`, `3` |
| `proposalId` | `1`, `2`, `3` |
| `contractId` | `1`, `2` |
| `milestoneId` | `1`, `2`, `3`, `4` |
| `domainId` | `2`, `3`, `4`, `8` |
| `skillId` | `2`, `3`, `6`, `8`, `9`, `14`, `15` |
| `technologyId` | Lấy từ `GET /api/v1/technologies?activeOnly=true` |
| `criteriaId` | Lấy từ `GET /api/v1/acceptance-criteria?activeOnly=true` |

## 3. Auth API

### Đăng nhập

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

Copy `accessToken` từ response vào biến môi trường tương ứng.

### Kiểm tra phiên hiện tại

```http
GET {{baseUrl}}/api/auth/me
```

Authorization:

```text
Bearer {{businessToken}}
```

### Đăng ký

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

### Kiểm tra email tồn tại

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

## 4. Profile, Portfolio Và Firebase File API

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
  "address": "Quận 1, TP. Hồ Chí Minh",
  "businessLicenseUrl": "business-licenses/accounts/1/license-demo.pdf"
}
```

### Upload giấy phép kinh doanh

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/profiles/business/license-file
```

Body `form-data`:

| Key | Type | Value |
| --- | --- | --- |
| `file` | File | Chọn PDF/JPG/PNG |

Response trả về path Firebase. Dùng path đó gán vào `businessLicenseUrl`.

### Lấy hồ sơ business hiện tại

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

### Lấy hồ sơ expert hiện tại

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
POST {{baseUrl}}/api/v1/profiles/approve/EXPERT/1?status=Rejected
```

Giá trị `status`: `Approved`, `Rejected`.

### Expert tạo hoặc cập nhật portfolio

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/profiles/portfolio
```

Raw body:

```json
{
  "domainIds": "2,3,5",
  "skillIds": "2,3,5,8",
  "technologyIds": "1,2,3,6,8",
  "yearsExperience": 5,
  "certificates": "expert-certificates/accounts/2/certificate-demo.pdf",
  "selfDescription": "Chuyên gia AI có kinh nghiệm xây dựng RAG, xử lý dữ liệu, thiết kế API và triển khai giải pháp AI cho doanh nghiệp."
}
```

Lưu ý: `technologyIds` là bắt buộc trong logic hiện tại. Lấy id công nghệ từ `GET /api/v1/technologies?activeOnly=true`.

### Upload chứng chỉ expert

Token: EXPERT.

```http
POST {{baseUrl}}/api/v1/profiles/portfolio/certificate-file
```

Body `form-data`:

| Key | Type | Value |
| --- | --- | --- |
| `file` | File | Chọn PDF/JPG/PNG |

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

## 5. Catalog API

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

### Xem technologies

Token: BUSINESS, EXPERT, STAFF hoặc ADMIN.

```http
GET {{baseUrl}}/api/v1/technologies?activeOnly=true
```

### Admin tạo technology

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/technologies
```

Raw body:

```json
{
  "technologyCode": "NEXTJS",
  "technologyName": "Next.js",
  "description": "Framework React dùng để xây dựng ứng dụng web full-stack.",
  "isActive": true,
  "sortOrder": 30
}
```

### Admin cập nhật technology

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/technologies/1
```

Raw body:

```json
{
  "technologyName": "Python",
  "description": "Ngôn ngữ lập trình phổ biến cho AI, dữ liệu và backend.",
  "isActive": true,
  "sortOrder": 1
}
```

### Xem tiêu chí nghiệm thu nền tảng

Public.

```http
GET {{baseUrl}}/api/v1/acceptance-criteria?activeOnly=true
```

Dùng các `criteriaId` trả về để gán vào `milestones[].criteriaIds` khi tạo job hoặc tạo milestone.

### Admin tạo tiêu chí nghiệm thu nền tảng

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/criteria
```

Raw body:

```json
{
  "criteriaCode": "DEMO_ACCEPTED",
  "description": "Demo nghiệm thu được thực hiện và doanh nghiệp xác nhận kết quả phù hợp.",
  "isActive": true,
  "sortOrder": 300
}
```

Lưu ý: API này tạo dữ liệu danh mục trong bảng `acceptance_criteria`, không còn tạo criteria riêng trực tiếp theo `milestoneId`.

### Xem domain của job

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

### Xem technology của job

```http
GET {{baseUrl}}/api/v1/jobs/1/technologies
```

### Business hoặc Admin thay technology của job

Token: BUSINESS sở hữu job hoặc ADMIN.

```http
PUT {{baseUrl}}/api/v1/jobs/1/technologies
```

Raw body:

```json
[1, 2, 3, 6, 8]
```

## 6. Job, SoW Và Proposal API

### AI generate SoW

Public theo controller hiện tại.

```http
POST {{baseUrl}}/api/jobs/generate-sow
```

Raw body:

```json
{
  "projectTitle": "Tích hợp RAG chatbot cho chăm sóc khách hàng",
  "rawRequirement": "Cần chatbot trả lời câu hỏi sản phẩm, lấy dữ liệu từ FAQ và chuyển lead cho nhân viên.",
  "budget": 90000000,
  "duration": 6,
  "durationUnit": "WEEK",
  "supportFields": ["E-commerce", "Customer Support"],
  "requiredSkills": ["RAG Architecture", "React TypeScript", "Java Spring Boot"]
}
```

Response có thể dùng để điền lại vào `sow` và `milestones` khi tạo job. Nếu chưa dùng AI, có thể tự nhập thủ công như body bên dưới.

### Business tạo job nháp kèm SoW và milestone

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/jobs
```

Raw body mới:

```json
{
  "title": "Tích hợp RAG chatbot cho chăm sóc khách hàng",
  "rawRequirements": "Cần chatbot trả lời câu hỏi sản phẩm, lấy dữ liệu từ FAQ và chuyển lead cho nhân viên.",
  "structuredSow": "Scope: ingestion FAQ, RAG chatbot, handoff CRM, analytics dashboard.",
  "budget": 90000000,
  "plannedDurationValue": 6,
  "plannedDurationUnit": "WEEK",
  "isHot": false,
  "domainIds": [2, 3],
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
  "technologyIds": [1, 2, 3, 6, 8],
  "sow": {
    "title": "SoW RAG chatbot chăm sóc khách hàng",
    "overview": "Xây dựng chatbot RAG trả lời câu hỏi sản phẩm từ dữ liệu FAQ và tài liệu nội bộ.",
    "objectives": "[\"Tự động trả lời câu hỏi phổ biến\", \"Giảm tải cho nhân viên chăm sóc khách hàng\", \"Ghi nhận lead cần tư vấn\"]",
    "scopeOfWork": "[\"Chuẩn hóa dữ liệu FAQ\", \"Xây dựng retrieval\", \"Tích hợp chatbot\", \"Báo cáo chất lượng câu trả lời\"]",
    "deliverable": "[\"API chatbot\", \"Giao diện demo\", \"Tài liệu triển khai\", \"Báo cáo nghiệm thu\"]",
    "assumptions": "[\"Doanh nghiệp cung cấp dữ liệu FAQ\", \"Có môi trường test để kiểm thử tích hợp\"]",
    "outOfScope": "[\"Không tích hợp tổng đài thoại\", \"Không huấn luyện mô hình nền từ đầu\"]"
  },
  "milestones": [
    {
      "milestoneName": "Khảo sát dữ liệu và thiết kế kiến trúc",
      "description": "Phân tích FAQ, xác định schema dữ liệu và thiết kế kiến trúc RAG.",
      "fundsAllocated": 25000000,
      "orderIndex": 1,
      "criteriaIds": [1, 2, 3]
    },
    {
      "milestoneName": "Triển khai chatbot RAG MVP",
      "description": "Xây dựng retrieval, API chatbot và giao diện demo.",
      "fundsAllocated": 40000000,
      "orderIndex": 2,
      "criteriaIds": [4, 5, 6]
    },
    {
      "milestoneName": "Kiểm thử, tài liệu và bàn giao",
      "description": "Chạy test nghiệm thu, hoàn thiện tài liệu kỹ thuật và hướng dẫn vận hành.",
      "fundsAllocated": 25000000,
      "orderIndex": 3,
      "criteriaIds": [7, 8, 9]
    }
  ]
}
```

Lưu ý:

- Service luôn set `status` thành `DRAFT`.
- Không gửi `businessId` hoặc `status`.
- `sow` và `milestones` là tùy chọn, nhưng nên gửi để test flow mới.
- `criteriaIds` phải là id đang active trong `GET /api/v1/acceptance-criteria?activeOnly=true`.
- `domainIds`, `skills`, `technologyIds` có thể gửi ngay khi tạo job.
- Sau khi tạo job, vẫn có thể dùng API riêng bên dưới để thay lại domain, skill hoặc technology.

### Business gán domain cho job

Token: BUSINESS.

```http
PUT {{baseUrl}}/api/v1/jobs/{{jobId}}/domains
```

Raw body:

```json
[2, 3]
```

### Business gán skill cho job

Token: BUSINESS.

```http
PUT {{baseUrl}}/api/v1/jobs/{{jobId}}/skills
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

### Business gán technology cho job

Token: BUSINESS.

```http
PUT {{baseUrl}}/api/v1/jobs/{{jobId}}/technologies
```

Raw body:

```json
[1, 2, 3, 6, 8]
```

### Business tạo milestone riêng cho job đã có

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/milestones
```

Raw body:

```json
{
  "jobId": 1,
  "contractId": null,
  "milestoneName": "Bổ sung bộ kiểm thử hội thoại",
  "description": "Thiết kế thêm kịch bản test fallback, hallucination và kiểm tra chất lượng câu trả lời.",
  "fundsAllocated": 15000000,
  "orderIndex": 4,
  "status": "Pending",
  "criteriaIds": [10, 11, 12]
}
```

Lưu ý: `orderIndex` không được trùng trong cùng một job.

### Xem job public

Public. API chỉ trả job `OPEN`.

```http
GET {{baseUrl}}/api/v1/jobs
```

Response job có các field quan trọng:

```json
{
  "proposalsCount": 0,
  "domainIds": [2, 3],
  "domains": [],
  "skillIds": [2, 3],
  "skills": [
    {
      "skillId": 2,
      "isMandatory": true
    }
  ],
  "skillDetails": [],
  "technologyIds": [1, 2, 3],
  "technologies": [],
  "sow": {},
  "milestones": [
    {
      "criteriaIds": [1, 2, 3],
      "criteria": []
    }
  ]
}
```

### Business xem job của mình

Token: BUSINESS.

```http
GET {{baseUrl}}/api/v1/jobs/my
```

Trả cả `DRAFT`, `OPEN`, `CLOSED`, `CANCELLED`.

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

Nếu có file proposal PDF/DOCX, upload trước để lấy path Firebase:

```http
POST {{baseUrl}}/api/v1/proposals/file
```

Body `form-data`:

| Key | Type | Value |
| --- | --- | --- |
| `file` | File | Chọn PDF/DOCX |

Copy path trả về vào `proposalFileUrl`.

```http
POST {{baseUrl}}/api/v1/proposals
```

Raw body:

```json
{
  "jobId": 1,
  "technicalSolution": "Triển khai RAG chatbot bằng PostgreSQL metadata, vector retrieval, reranking và CRM handoff.",
  "proposalDescription": "Chuyên gia sẽ xây dựng pipeline dữ liệu FAQ, API chatbot, dashboard theo dõi chất lượng câu trả lời và tài liệu vận hành.",
  "proposalFileUrl": "proposal-files/experts/1/rag-chatbot-proposal.pdf",
  "bidAmount": 110000000,
  "proposalMilestone": [
    {
      "milestoneId": 1,
      "proposedBudget": 30000000
    },
    {
      "milestoneId": 2,
      "proposedBudget": 55000000
    },
    {
      "milestoneId": 3,
      "proposedBudget": 25000000
    }
  ]
}
```

Điều kiện:

- Job phải `OPEN`.
- Expert phải `Approved`.
- Expert phải có portfolio.
- Không còn chọn `domainId` hoặc `skillId` khi gửi proposal.
- Nếu không đề xuất ngân sách milestone mới, bỏ `proposalMilestone` hoặc gửi `null`.
- `proposalMilestone` chỉ được sửa ngân sách của milestone đã có, không được thêm milestone mới.
- Nếu gửi `proposalMilestone`, phải gửi đủ toàn bộ milestone của job, không được thiếu hoặc trùng `milestoneId`.
- Tổng `proposedBudget` trong `proposalMilestone` phải bằng `bidAmount`.
- Expert có thể gửi lại proposal nếu proposal cũ của job đó đã bị business `Rejected`.
- Expert không thể gửi thêm nếu đã có proposal cùng job đang khác `Rejected`.

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

## 7. Contract Và Execution API

### Tạo contract draft từ proposal

Token: BUSINESS.

```http
POST {{baseUrl}}/api/v1/contracts/from-proposals/2
```

Raw body:

```json
{
  "contractTitle": "Hợp đồng triển khai RAG chatbot cho chăm sóc khách hàng",
  "timelineDays": 42
}
```

Lưu ý:

- Chỉ tạo được từ proposal đã `Accepted`.
- Không gửi `technologyUsed` vì cột này đã được bỏ khỏi `contracts`.
- Không cần gửi `totalBudget`; backend tự tính từ milestone của job và `proposalMilestone` nếu proposal có đề xuất ngân sách.
- Khi tạo draft, backend sinh `contractMilestones` gồm `originalBudget`, `finalBudget` và `difference`.

### Xem chi tiết contract

Token: BUSINESS hoặc EXPERT thuộc contract, STAFF/ADMIN theo quyền hiện có.

```http
GET {{baseUrl}}/api/v1/contracts/1
```

Response quan trọng:

```json
{
  "contractId": 1,
  "proposalId": 2,
  "contractTitle": "Hợp đồng triển khai RAG chatbot cho chăm sóc khách hàng",
  "totalBudget": 110000000,
  "status": "Draft",
  "businessAcceptedAt": null,
  "expertAcceptedAt": null,
  "businessNdaSignedAt": null,
  "expertNdaSignedAt": null,
  "activatedAt": null,
  "contractMilestones": [
    {
      "jobMilestoneId": 1,
      "milestoneName": "Khảo sát dữ liệu và thiết kế kiến trúc",
      "originalBudget": 25000000,
      "finalBudget": 30000000,
      "difference": 5000000
    }
  ]
}
```

### Tạo yêu cầu thay đổi contract

Token: BUSINESS hoặc EXPERT thuộc contract.

```http
POST {{baseUrl}}/api/v1/contracts/change-requests
```

Raw body:

```json
{
  "contractId": 1,
  "changeType": "ScopeAdjustment",
  "changeSummary": "Bổ sung thêm bộ test tiếng Việt và báo cáo nghiệm thu.",
  "proposedBudget": 120000000,
  "proposedTimelineDays": 60
}
```

### Ký xác nhận hợp đồng

Token: BUSINESS hoặc EXPERT thuộc contract.

```http
POST {{baseUrl}}/api/v1/contracts/1/sign
```

Lưu ý:

- Business gọi một lần sẽ set `businessAcceptedAt`.
- Expert gọi một lần sẽ set `expertAcceptedAt`.
- API này chưa chắc làm contract `Active` ngay. Contract chỉ active khi đủ chữ ký hợp đồng và NDA của cả hai bên.

### Ký NDA

Token: BUSINESS hoặc EXPERT thuộc contract.

```http
POST {{baseUrl}}/api/v1/contracts/1/nda-sign
```

Lưu ý:

- Business gọi một lần sẽ set `businessNdaSignedAt`.
- Expert gọi một lần sẽ set `expertNdaSignedAt`.
- Khi đủ `businessAcceptedAt`, `expertAcceptedAt`, `businessNdaSignedAt`, `expertNdaSignedAt`, backend chuyển contract sang `Active`, job sang `CLOSED`, và cập nhật ngân sách milestone theo `contract_milestones.finalBudget`.

### Chấm dứt contract

Token: BUSINESS sở hữu contract hoặc ADMIN.

```http
POST {{baseUrl}}/api/v1/contracts/1/terminate?reason=Không tiếp tục triển khai
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

Token: BUSINESS thuộc contract hoặc ADMIN.

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

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/transactions/2/status?status=Success
```

### Payment webhook giả lập

Token: ADMIN.

```http
POST {{baseUrl}}/api/v1/transactions/2/webhook?paymentStatus=Success&bankTxCode=VNPAY-DEMO-001&receiptImgUrl=https://storage.aitasker.local/receipts/demo.png
```

### Tạo dispute

Token: BUSINESS hoặc EXPERT thuộc contract.

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

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/disputes/1/assign?staffId=1
```

### Resolve dispute

Token: ADMIN.

```http
PATCH {{baseUrl}}/api/v1/disputes/1/resolve?proposedAction=Release payout after correction accepted
```

### Staff ghi kết quả demo testing

Token: STAFF được gán dispute.

```http
POST {{baseUrl}}/api/v1/disputes/1/demo-testing?testResult=Passed 18/20 scenarios, cần bổ sung 2 case fallback
```

### Staff tạo technical report

Token: STAFF được gán dispute.

```http
POST {{baseUrl}}/api/v1/disputes/1/technical-report?reportContent=Kết quả kiểm thử đạt yêu cầu sau chỉnh sửa&proposedAction=Release escrow
```

### Chạy SLA auto approve

Token: ADMIN.

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

API này trả dữ liệu từ bảng `contract_milestones`, tức là milestone đã chốt cho hợp đồng, có `originalBudget`, `finalBudget` và `difference`.

### Milestone theo job

```http
GET {{baseUrl}}/api/v1/jobs/1/milestones
```

### Criteria đã chọn theo milestone

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

## 8. Admin API

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

## 9. Wallet API

### Xem ví của tài khoản hiện tại

Token: BUSINESS, EXPERT, ADMIN hoặc STAFF.

```http
GET {{baseUrl}}/api/v1/wallet/me
```

## 10. Notification Và WebSocket API

Notification có 2 phần:

- REST API dùng để lấy danh sách, đếm số chưa đọc và đánh dấu đã đọc.
- WebSocket/STOMP dùng để nhận thông báo realtime khi có hành động mới.

Các trường `title` và `message` trong notification trả về bằng tiếng Việt để hiển thị trực tiếp trên giao diện.

### Lấy danh sách thông báo của tài khoản hiện tại

Token: BUSINESS, EXPERT, STAFF hoặc ADMIN.

```http
GET {{baseUrl}}/api/v1/notifications
```

Response mẫu:

```json
{
  "success": true,
  "message": "LAY DANH SACH THONG BAO THANH CONG",
  "data": [
    {
      "notificationId": 1,
      "type": "PROPOSAL_CREATED",
      "title": "Có proposal mới",
      "message": "Một chuyên gia vừa gửi proposal cho dự án \"OCR hóa đơn và phiếu bảo hành tiếng Việt\".",
      "targetUrl": "/business/jobs/3/proposals",
      "isRead": false,
      "createdAt": "2026-06-13T16:05:00",
      "readAt": null
    }
  ]
}
```

### Đếm thông báo chưa đọc

Token: BUSINESS, EXPERT, STAFF hoặc ADMIN.

```http
GET {{baseUrl}}/api/v1/notifications/unread-count
```

Response mẫu:

```json
{
  "success": true,
  "message": "DEM THONG BAO CHUA DOC THANH CONG",
  "data": {
    "unreadCount": 2
  }
}
```

### Đánh dấu một thông báo đã đọc

Token: tài khoản nhận thông báo.

```http
PATCH {{baseUrl}}/api/v1/notifications/1/read
```

### Đánh dấu tất cả thông báo đã đọc

Token: BUSINESS, EXPERT, STAFF hoặc ADMIN.

```http
PATCH {{baseUrl}}/api/v1/notifications/read-all
```

### Test realtime WebSocket bằng Postman

Postman REST request không test được realtime trực tiếp; cần tạo WebSocket request.

URL WebSocket:

```text
ws://localhost:8080/ws/000/postman/websocket
```

Lưu ý: `/ws` là endpoint SockJS. Khi test bằng Postman WebSocket, URL transport cần có dạng `/ws/{serverId}/{sessionId}/websocket`; ví dụ `000/postman` ở trên chỉ là giá trị test. Không gửi raw STOMP trực tiếp như client STOMP thuần; cần bọc STOMP frame trong mảng JSON của SockJS.

Sau khi connect, Postman có thể nhận message mở kết nối là:

```text
o
```

Gửi CONNECT bằng dạng text sau. Ví dụ dùng token business để nghe thông báo của doanh nghiệp:

```text
["CONNECT\naccept-version:1.2\nheart-beat:10000,10000\nAuthorization:Bearer {{businessToken}}\n\n\u0000"]
```

Sau khi server trả message có `CONNECTED`, gửi SUBSCRIBE:

```text
["SUBSCRIBE\nid:sub-notifications\ndestination:/user/queue/notifications\n\n\u0000"]
```

Giữ tab WebSocket này mở, sau đó dùng tab REST khác để tạo hành động phát sinh thông báo.

### Hành động tạo notification để test

Thông báo cho BUSINESS khi EXPERT nộp proposal:

```http
POST {{baseUrl}}/api/v1/proposals
```

Token: EXPERT.

Thông báo cho EXPERT khi BUSINESS duyệt proposal:

```http
PATCH {{baseUrl}}/api/v1/proposals/{proposalId}/status?status=Accepted
```

Token: BUSINESS.

Thông báo cho BUSINESS/EXPERT khi STAFF duyệt hồ sơ:

```http
POST {{baseUrl}}/api/v1/profiles/approve/BUSINESS/{businessProfileId}?status=Approved
POST {{baseUrl}}/api/v1/profiles/approve/EXPERT/{expertProfileId}?status=Rejected
```

Token: STAFF.

Thông báo cho BUSINESS khi EXPERT nộp deliverable:

```http
POST {{baseUrl}}/api/v1/deliverables
```

Token: EXPERT.

Thông báo cho STAFF khi tranh chấp được tạo hoặc được admin gán:

```http
POST {{baseUrl}}/api/v1/disputes
PATCH {{baseUrl}}/api/v1/disputes/{disputeId}/assign?staffId=1
```

Token tương ứng: BUSINESS/EXPERT tạo dispute, ADMIN gán dispute.

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

## 12. Luồng Test Khuyến Nghị

### Luồng 1: Auth và profile

1. Login BUSINESS, EXPERT, STAFF, ADMIN.
2. Gọi `GET /api/auth/me` với từng token.
3. Business gọi `POST /api/v1/profiles/business`.
4. Expert gọi `POST /api/v1/profiles/expert`.
5. Staff duyệt BUSINESS/EXPERT qua `POST /api/v1/profiles/approve/...`.

### Luồng 2: Job posting, SoW, milestone criteria và proposal

1. Gọi `GET /api/v1/domains?activeOnly=true`.
2. Gọi `GET /api/v1/skills?activeOnly=true`.
3. Gọi `GET /api/v1/technologies?activeOnly=true`.
4. Gọi `GET /api/v1/acceptance-criteria?activeOnly=true`.
5. Business gọi `POST /api/v1/jobs` với `domainIds`, `skills`, `technologyIds`, `sow` và `milestones[].criteriaIds`.
6. Nếu cần đổi domain sau khi tạo job, gọi `PUT /api/v1/jobs/{jobId}/domains`.
7. Nếu cần đổi skill sau khi tạo job, gọi `PUT /api/v1/jobs/{jobId}/skills`.
8. Nếu cần đổi technology sau khi tạo job, gọi `PUT /api/v1/jobs/{jobId}/technologies`.
9. Business gọi `GET /api/v1/jobs/my` để kiểm tra job nháp có `sow`, `milestones`, `criteria`, `technologyIds`.
10. Business gọi `PATCH /api/v1/jobs/{jobId}/status?status=OPEN`.
11. Expert gọi `GET /api/v1/jobs`.
12. Expert gọi `POST /api/v1/proposals`, có thể gửi `proposalMilestone` nếu muốn đề xuất ngân sách mới.
13. Business gọi `GET /api/v1/jobs/{jobId}/proposals`.
14. Business gọi `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted`.

### Luồng 2.1: Contract draft và ký hợp đồng

1. Business accept proposal qua `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted`.
2. Business gọi `POST /api/v1/contracts/from-proposals/{proposalId}` để tạo contract draft.
3. Business gọi `GET /api/v1/contracts/{contractId}` để kiểm tra `contractMilestones`.
4. Business gọi `POST /api/v1/contracts/{contractId}/sign` để ký hợp đồng phía business.
5. Business gọi `POST /api/v1/contracts/{contractId}/nda-sign` để đồng ý NDA phía business.
6. Expert gọi `GET /api/v1/contracts/{contractId}` để xem draft.
7. Expert gọi `POST /api/v1/contracts/{contractId}/sign` để ký hợp đồng phía expert.
8. Expert gọi `POST /api/v1/contracts/{contractId}/nda-sign` để đồng ý NDA phía expert.
9. Gọi lại `GET /api/v1/contracts/{contractId}` để kiểm tra `status = Active`.
10. Gọi `GET /api/v1/jobs/{jobId}` hoặc `GET /api/v1/jobs/{jobId}/milestones` để kiểm tra job đã `CLOSED` và milestone đã cập nhật ngân sách chốt.

### Luồng 3: Portfolio và file Firebase

1. Expert upload file qua `POST /api/v1/profiles/portfolio/certificate-file`.
2. Copy path trả về vào `certificates`.
3. Gọi `GET /api/v1/technologies?activeOnly=true` để lấy `technologyIds`.
4. Expert gọi `POST /api/v1/profiles/portfolio` với `domainIds`, `skillIds`, `technologyIds`.
5. Expert gọi `GET /api/v1/profiles/portfolio/me`.
6. Business hoặc Staff gọi `GET /api/v1/profiles/files/view-url?path=...`.

### Luồng 4: Admin audit log

1. Login admin.
2. Gọi `GET /api/v1/admin/audit-logs`.
3. Test `actorGroup=INTERNAL`.
4. Test `actorGroup=EXTERNAL`.
5. Thực hiện hành động như business tạo job hoặc expert nộp proposal.
6. Gọi lại audit log để kiểm tra log mới.

### Luồng 5: Notification realtime

1. Login BUSINESS và EXPERT để lấy `businessToken`, `expertToken`.
2. Mở WebSocket request tới `ws://localhost:8080/ws/000/postman/websocket`.
3. Gửi frame `CONNECT` bằng `businessToken`.
4. Gửi frame `SUBSCRIBE` tới `/user/queue/notifications`.
5. Ở tab REST khác, dùng EXPERT nộp proposal cho job của BUSINESS.
6. Kiểm tra tab WebSocket nhận message có `title`, `message` tiếng Việt.
7. Gọi `GET /api/v1/notifications` bằng `businessToken` để kiểm tra notification đã được lưu database.
8. Gọi `PATCH /api/v1/notifications/{notificationId}/read` rồi kiểm tra `GET /api/v1/notifications/unread-count`.

## 13. Lỗi Thường Gặp Khi Test

### 401 Unauthorized

- Chưa thêm `Authorization`.
- Token hết hạn.
- Dán sai token hoặc dùng sai biến môi trường.

### 403 Forbidden hoặc lỗi quyền

- Dùng token EXPERT để tạo job.
- Dùng token BUSINESS để tạo domain/skill/criteria admin.
- Dùng token không phải STAFF để duyệt profile.
- Business xem proposal của job không thuộc doanh nghiệp mình.

### 500 khi upload file Firebase

- File service account Firebase sai path trong `.env`.
- Firebase Storage bucket chưa bật.
- Body không phải `form-data`.
- Key file không đúng là `file`.

### Tạo job không lưu milestone criteria

- Chưa gọi `GET /api/v1/acceptance-criteria?activeOnly=true` để lấy `criteriaId` đúng.
- Gửi nhầm `criteria` thay vì `criteriaIds`.
- `criteriaIds` chứa id không tồn tại hoặc inactive.
- `orderIndex` milestone bị trùng.

### Proposal không nộp được

- Job chưa `OPEN`.
- Expert chưa `Approved`.
- Expert chưa có portfolio.
- Expert đã có proposal cùng job đang khác `Rejected`.
- `proposalMilestone` không phải JSON array hợp lệ.
- `proposalMilestone[].milestoneId` không thuộc job đang nộp.
- Tổng `proposalMilestone[].proposedBudget` không khớp `bidAmount`.

### Contract draft không tạo được

- Proposal chưa được business duyệt `Accepted`.
- Proposal đã có contract draft trước đó.
- Job chưa có milestone.
- `proposalMilestone` trong proposal chứa milestone không thuộc job.

### Job public không hiện proposal count

Gọi:

```http
GET {{baseUrl}}/api/v1/jobs
```

Kiểm tra response từng job có:

```json
{
  "proposalsCount": 1
}
```

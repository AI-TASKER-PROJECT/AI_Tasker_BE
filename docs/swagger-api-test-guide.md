# Hướng Dẫn Test API Back-end Bằng Swagger

Tài liệu này hướng dẫn test API trực tiếp trên Swagger UI của back-end AITASKER.

Thứ tự API trong tài liệu được sắp theo đúng cách Swagger UI đang hiển thị:

- Nhóm controller/tag theo alphabet, tương ứng cấu hình `springdoc.swagger-ui.tags-sorter=alpha`.
- API trong từng controller theo HTTP method, tương ứng cấu hình `springdoc.swagger-ui.operations-sorter=method`.
- Các API cùng method giữ theo thứ tự runtime của `GET /v3/api-docs`.

Lưu ý: WebSocket/STOMP không xuất hiện trong Swagger vì không phải REST API.

## 1. Chuẩn Bị

Chạy Docker database và Redis:

```powershell
docker compose up -d
```

Chạy back-end:

```powershell
.\mvnw.cmd spring-boot:run
```

Mở Swagger:

```text
http://localhost:8080/swagger-ui.html
```

Kiểm tra OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## 2. Cách Dùng Swagger Để Test

### 2.1. Gọi API không cần đăng nhập

1. Mở controller cần test.
2. Bấm API muốn test.
3. Bấm `Try it out`.
4. Nhập path/query/body nếu có.
5. Bấm `Execute`.
6. Xem kết quả ở `Server response` và `Response body`.

### 2.2. Gọi API cần đăng nhập

1. Gọi `POST /api/auth/login`.
2. Copy `accessToken` trong response.
3. Bấm nút `Authorize` ở góc phải Swagger.
4. Nhập token theo dạng:

```text
Bearer <accessToken>
```

Ví dụ:

```text
Bearer eyJhbGciOiJIUzI1NiJ9...
```

5. Bấm `Authorize`, sau đó bấm `Close`.
6. Gọi các API cần quyền bằng `Try it out` và `Execute`.

Nếu đổi role test, bấm `Authorize`, `Logout`, rồi nhập token role mới.

### 2.3. Test API upload file

Với API upload file, Swagger sẽ hiện nút chọn file trong phần request body. Chỉ cần chọn file rồi bấm `Execute`.

Không cần tự nhập `Content-Type`; Swagger sẽ tự gửi `multipart/form-data`.

## 3. Tài Khoản Seed Thường Dùng

Mật khẩu chung:

```text
12345678
```

| Role | Email | Dùng để test |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | Tạo job, xem proposal, tạo contract |
| EXPERT | `expert@aitasker.local` | Tạo portfolio, xem job, gửi proposal, ký contract |
| STAFF | `staff@aitasker.local` | Duyệt hồ sơ business/expert |
| ADMIN | `admin@aitasker.local` | Quản lý account, catalog, audit log, system |

ID seed thường dùng:

| Dữ liệu | ID mẫu |
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

## 4. Danh Sách API Theo Thứ Tự Swagger

## admin-controller

### 1. DELETE `/api/v1/admin/accounts/{accountId}`

- Token: ADMIN.
- Cách test: nhập `accountId`, bấm `Execute`.
- Dùng để khóa account bằng logic admin.

### 2. GET `/api/v1/admin/staffs`

- Token: ADMIN.
- Cách test: bấm `Try it out` rồi `Execute`.
- Dùng để lấy danh sách staff.

### 3. GET `/api/v1/admin/accounts`

- Token: ADMIN.
- Query thường dùng: lọc theo role/status nếu Swagger hiển thị field query.
- Dùng để lấy danh sách account.

### 4. GET `/api/v1/admin/wallet`

- Token: ADMIN.
- Dùng để xem ví hệ thống.

### 5. GET `/api/v1/admin/settings`

- Token: ADMIN.
- Dùng để xem cấu hình hệ thống.

### 6. GET `/api/v1/admin/reviews/contracts/{contractId}`

- Token: ADMIN.
- Nhập `contractId`.
- Dùng để xem review theo contract.

### 7. GET `/api/v1/admin/audit-logs`

- Token: ADMIN.
- Dùng để xem audit log.

### 8. GET `/api/v1/admin/analytics/overview`

- Token: ADMIN.
- Dùng để xem tổng quan dashboard admin.

### 9. PATCH `/api/v1/admin/staffs/{staffId}`

- Token: ADMIN.
- Nhập `staffId`.
- Body:

```json
{
  "staffId": 1,
  "accountId": 3,
  "specialization": "Duyệt hồ sơ và xử lý tranh chấp"
}
```

### 10. PATCH `/api/v1/admin/settings/{key}`

- Token: ADMIN.
- Nhập `key`.
- Query/body: nhập value theo field Swagger đang hiện.
- Dùng để cập nhật system setting.

### 11. PATCH `/api/v1/admin/accounts/{accountId}`

- Token: ADMIN.
- Nhập `accountId`.
- Body:

```json
{
  "email": "updated.user@aitasker.local",
  "phone": "0909000002",
  "fullName": "Updated User",
  "role": "EXPERT",
  "status": "Approved",
  "specialization": "AI"
}
```

### 12. PATCH `/api/v1/admin/accounts/{accountId}/status`

- Token: ADMIN.
- Nhập `accountId`.
- Query `status`: `Pending`, `Approved`, `Rejected`, `Lock`.
- Dùng để đổi trạng thái account.

### 13. PATCH `/api/v1/admin/accounts/{accountId}/active`

- Token: ADMIN.
- Nhập `accountId`.
- Query `active`: `true` hoặc `false`.
- Dùng để bật hoặc khóa nhanh account.

### 14. POST `/api/v1/admin/wallet/sync`

- Token: ADMIN.
- Không cần body.
- Dùng để đồng bộ ví hệ thống.

### 15. POST `/api/v1/admin/staffs`

- Token: ADMIN.
- Body:

```json
{
  "accountId": 3,
  "specialization": "Duyệt hồ sơ KYC/KYB"
}
```

### 16. POST `/api/v1/admin/reviews`

- Token: ADMIN hoặc role được backend cho phép.
- Body:

```json
{
  "contractId": 1,
  "reviewerId": 1,
  "revieweeId": 2,
  "rating": 5,
  "comment": "Hoàn thành đúng yêu cầu."
}
```

### 17. POST `/api/v1/admin/accounts`

- Token: ADMIN.
- Body:

```json
{
  "email": "new.staff@aitasker.local",
  "password": "12345678",
  "phone": "0909000003",
  "fullName": "New Staff",
  "role": "STAFF",
  "status": "Approved",
  "specialization": "KYC/KYB"
}
```

## auth-controller

### 18. GET `/api/auth/me`

- Token: bất kỳ role đã đăng nhập.
- Dùng để kiểm tra access token đang thuộc account nào.

### 19. GET `/api/auth/check-email`

- Không cần token.
- Query `email`: ví dụ `expert@aitasker.local`.
- Dùng để kiểm tra email đã tồn tại chưa.

### 20. POST `/api/auth/register`

- Không cần token.
- Body:

```json
{
  "email": "new.business@aitasker.local",
  "password": "12345678",
  "fullName": "New Business",
  "phone": "0909000001",
  "role": "BUSINESS"
}
```

- Lưu ý: email phải xác thực OTP trước khi đăng ký.

### 21. POST `/api/auth/login`

- Không cần token.
- Body:

```json
{
  "email": "business@aitasker.local",
  "password": "12345678"
}
```

- Sau khi execute, copy `accessToken` để Authorize.

## catalog-controller

### 22. GET `/api/v1/jobs/{jobId}/technologies`

- Token: tùy quyền xem job.
- Nhập `jobId`.
- Dùng để xem technology đã gán cho job.

### 23. GET `/api/v1/jobs/{jobId}/skills`

- Token: tùy quyền xem job.
- Nhập `jobId`.
- Dùng để xem skill đã gán cho job.

### 24. GET `/api/v1/jobs/{jobId}/domains`

- Token: tùy quyền xem job.
- Nhập `jobId`.
- Dùng để xem domain đã gán cho job.

### 25. GET `/api/v1/technologies`

- Token: thường không bắt buộc hoặc dùng token bất kỳ nếu bị chặn.
- Query `activeOnly=true` để lấy công nghệ đang hoạt động.

### 26. GET `/api/v1/skills`

- Token: thường không bắt buộc hoặc dùng token bất kỳ nếu bị chặn.
- Query `activeOnly=true` để lấy skill đang hoạt động.

### 27. GET `/api/v1/domains`

- Token: thường không bắt buộc hoặc dùng token bất kỳ nếu bị chặn.
- Query `activeOnly=true` để lấy domain đang hoạt động.

### 28. GET `/api/v1/acceptance-criteria`

- Token: thường không bắt buộc hoặc dùng token bất kỳ nếu bị chặn.
- Query `activeOnly=true` để lấy tiêu chí nghiệm thu đang hoạt động.

### 29. PATCH `/api/v1/technologies/{technologyId}`

- Token: ADMIN.
- Nhập `technologyId`.
- Body:

```json
{
  "technologyCode": "RAG",
  "technologyName": "RAG Architecture",
  "description": "Thiết kế truy xuất dữ liệu và sinh câu trả lời bằng RAG.",
  "isActive": true,
  "sortOrder": 1
}
```

### 30. PATCH `/api/v1/skills/{skillId}`

- Token: ADMIN.
- Nhập `skillId`.
- Body:

```json
{
  "skillCode": "JAVA_SPRING_BOOT",
  "skillName": "Java Spring Boot",
  "description": "Xây dựng API backend bằng Spring Boot.",
  "isActive": true
}
```

### 31. PATCH `/api/v1/domains/{domainId}`

- Token: ADMIN.
- Nhập `domainId`.
- Body:

```json
{
  "domainCode": "ECOMMERCE",
  "domainName": "E-commerce & Retail Tech",
  "description": "Giải pháp AI cho thương mại điện tử và bán lẻ.",
  "isActive": true,
  "sortOrder": 1
}
```

### 32. POST `/api/v1/technologies`

- Token: ADMIN.
- Body:

```json
{
  "technologyCode": "VECTOR_DB",
  "technologyName": "Vector Database",
  "description": "Lưu trữ embedding và hỗ trợ truy xuất ngữ nghĩa cho RAG.",
  "isActive": true,
  "sortOrder": 10
}
```

- Khi test nhiều lần, đổi `technologyCode` hoặc `technologyName` để tránh trùng dữ liệu.

### 33. POST `/api/v1/skills`

- Token: ADMIN.
- Body:

```json
{
  "skillCode": "PROMPT_ENGINEERING",
  "skillName": "Prompt Engineering",
  "description": "Thiết kế prompt và kiểm soát đầu ra của mô hình AI.",
  "isActive": true
}
```

- Khi test nhiều lần, đổi `skillCode` hoặc `skillName` để tránh trùng dữ liệu.

### 34. POST `/api/v1/domains`

- Token: ADMIN.
- Body:

```json
{
  "domainCode": "AI_CUSTOMER_SERVICE",
  "domainName": "AI Customer Service",
  "description": "Ứng dụng AI cho chăm sóc khách hàng và tự động hóa hỗ trợ.",
  "isActive": true,
  "sortOrder": 10
}
```

- Khi test nhiều lần, đổi `domainCode` hoặc `domainName` để tránh trùng dữ liệu.

### 35. PUT `/api/v1/jobs/{jobId}/technologies`

- Token: BUSINESS sở hữu job hoặc ADMIN.
- Nhập `jobId`.
- Body:

```json
[1, 2, 3]
```

### 36. PUT `/api/v1/jobs/{jobId}/skills`

- Token: BUSINESS sở hữu job hoặc ADMIN.
- Nhập `jobId`.
- Body:

```json
[2, 6, 9]
```

### 37. PUT `/api/v1/jobs/{jobId}/domains`

- Token: BUSINESS sở hữu job hoặc ADMIN.
- Nhập `jobId`.
- Body:

```json
[2, 3]
```

## chatbot-controller

### 38. POST `/api/chatbot/ask`

- Token: tùy cấu hình bảo mật hiện tại.
- Body:

```json
{
  "question": "AITASKER hỗ trợ những chức năng nào?"
}
```

## contract-execution-controller

### 39. GET `/api/v1/milestones/{milestoneId}/transactions`

- Token: BUSINESS/EXPERT thuộc contract hoặc ADMIN/STAFF theo quyền.
- Nhập `milestoneId`.

### 40. GET `/api/v1/milestones/{milestoneId}/deliverables`

- Token: BUSINESS/EXPERT thuộc contract hoặc STAFF/ADMIN theo quyền.
- Nhập `milestoneId`.

### 41. GET `/api/v1/milestones/{milestoneId}/criteria`

- Token: role được xem milestone.
- Nhập `milestoneId`.

### 42. GET `/api/v1/jobs/{jobId}/milestones`

- Token: role được xem job.
- Nhập `jobId`.

### 43. GET `/api/v1/jobs/{jobId}/matching`

- Token: BUSINESS sở hữu job hoặc STAFF/ADMIN theo quyền.
- Nhập `jobId`.
- Dùng để xem danh sách expert matching mức MVP.

### 44. GET `/api/v1/disputes/{disputeId}`

- Token: role liên quan tranh chấp hoặc nội bộ.
- Nhập `disputeId`.

### 45. GET `/api/v1/contracts`

- Token: BUSINESS, EXPERT, STAFF hoặc ADMIN.
- Dùng để xem danh sách contract theo quyền.

### 46. GET `/api/v1/contracts/{contractId}`

- Token: BUSINESS/EXPERT thuộc contract hoặc STAFF/ADMIN.
- Nhập `contractId`.
- Dùng để xem chi tiết contract.

### 47. GET `/api/v1/contracts/{contractId}/milestones`

- Token: role được xem contract.
- Nhập `contractId`.
- Dùng để xem contract milestone và ngân sách final.

### 48. GET `/api/v1/contracts/{contractId}/disputes`

- Token: role được xem contract.
- Nhập `contractId`.

### 49. PATCH `/api/v1/transactions/{transactionId}/status`

- Token: role finance/admin theo quyền.
- Nhập `transactionId`.
- Query `status`: ví dụ `Success`, `Failed`, `Pending`.

### 50. PATCH `/api/v1/disputes/{disputeId}/resolve`

- Token: ADMIN.
- Nhập `disputeId`.
- Query nhập theo field Swagger: thường gồm kết luận/trạng thái xử lý.

### 51. PATCH `/api/v1/disputes/{disputeId}/assign`

- Token: ADMIN.
- Nhập `disputeId`.
- Query `staffId`: ID staff được gán.

### 52. POST `/api/v1/transactions`

- Token: role được phép tạo giao dịch.
- Body:

```json
{
  "milestoneId": 1,
  "amount": 30000000,
  "commissionFee": 3000000,
  "transactionType": "Deposit",
  "status": "Pending"
}
```

### 53. POST `/api/v1/transactions/{transactionId}/webhook`

- Token: tùy cấu hình hiện tại.
- Nhập `transactionId`.
- Query `status`: ví dụ `Success`.

### 54. POST `/api/v1/milestones`

- Token: BUSINESS hoặc role nội bộ theo quyền.
- Body:

```json
{
  "jobId": 1,
  "milestoneName": "Hoàn thiện API và kiểm thử",
  "description": "Bàn giao API, tài liệu và kết quả kiểm thử.",
  "fundsAllocated": 30000000,
  "orderIndex": 1,
  "status": "Pending",
  "criteriaIds": [1, 2]
}
```

### 55. POST `/api/v1/milestones/sla-auto-approve`

- Token: STAFF hoặc ADMIN.
- Không cần body.
- Dùng để mô phỏng tự động duyệt milestone quá hạn SLA.

### 56. POST `/api/v1/disputes`

- Token: BUSINESS hoặc EXPERT thuộc contract.
- Body:

```json
{
  "contractId": 1,
  "milestoneId": 1,
  "evidenceReport": "Deliverable chưa đáp ứng tiêu chí nghiệm thu.",
  "proposedAction": "Yêu cầu chỉnh sửa và kiểm thử lại.",
  "status": "Open"
}
```

### 57. POST `/api/v1/disputes/{disputeId}/technical-report`

- Token: STAFF hoặc ADMIN.
- Nhập `disputeId`.
- Query/body nhập theo field Swagger hiển thị.

### 58. POST `/api/v1/disputes/{disputeId}/demo-testing`

- Token: STAFF hoặc ADMIN.
- Nhập `disputeId`.
- Query/body nhập theo field Swagger hiển thị.

### 59. POST `/api/v1/deliverables`

- Token: EXPERT thuộc contract.
- Body:

```json
{
  "milestoneId": 1,
  "sourceCodeUrl": "https://github.com/demo/project",
  "demoLink": "https://demo.aitasker.local",
  "submissionNotes": "Đã hoàn thành milestone và gửi tài liệu kiểm thử."
}
```

### 60. POST `/api/v1/criteria`

- Token: ADMIN hoặc role được phép.
- Body:

```json
{
  "criteriaCode": "API_TEST_PASSED",
  "description": "API hoạt động đúng theo tài liệu và test case đã thống nhất.",
  "isActive": true,
  "sortOrder": 1
}
```

### 61. POST `/api/v1/contracts/{contractId}/terminate`

- Token: BUSINESS/EXPERT thuộc contract hoặc ADMIN theo quyền.
- Nhập `contractId`.
- Query `reason`: lý do chấm dứt.

### 62. POST `/api/v1/contracts/{contractId}/sign`

- Token: BUSINESS hoặc EXPERT thuộc contract.
- Nhập `contractId`.
- Không cần body.
- Mỗi bên gọi một lần để ký xác nhận hợp đồng.

### 63. POST `/api/v1/contracts/{contractId}/nda-sign`

- Token: BUSINESS hoặc EXPERT thuộc contract.
- Nhập `contractId`.
- Không cần body.
- Mỗi bên gọi một lần để đồng ý thỏa thuận bảo mật.

### 64. POST `/api/v1/contracts/from-proposals/{proposalId}`

- Token: BUSINESS sở hữu job của proposal.
- Nhập `proposalId` đã được `Accepted`.
- Body:

```json
{
  "contractTitle": "Hợp đồng triển khai RAG chatbot",
  "timelineDays": 42
}
```

Có thể dùng body rỗng nếu backend cho phép:

```json
{}
```

### 65. POST `/api/v1/contracts/change-requests`

- Token: BUSINESS hoặc EXPERT thuộc contract.
- Body:

```json
{
  "contractId": 1,
  "changeType": "Budget",
  "changeSummary": "Đề xuất điều chỉnh ngân sách do thay đổi phạm vi.",
  "proposedBudget": 95000000,
  "proposedTimelineDays": 45,
  "status": "Pending"
}
```

## email-otp-controller

### 66. POST `/api/auth/email/verify-otp`

- Không cần token.
- Body:

```json
{
  "email": "expert@aitasker.local",
  "otp": "123456"
}
```

### 67. POST `/api/auth/email/send-otp`

- Không cần token.
- Body:

```json
{
  "email": "expert@aitasker.local"
}
```

## health-controller

### 68. GET `/api/health`

- Không cần token.
- Dùng để kiểm tra backend còn chạy hay không.

## marketplace-controller

### 69. GET `/api/v1/jobs`

- Token: thường dùng EXPERT hoặc public theo cấu hình.
- Dùng để xem job public đang OPEN.

### 70. GET `/api/v1/proposals/my`

- Token: EXPERT.
- Dùng để xem proposal chuyên gia đã gửi.

### 71. GET `/api/v1/jobs/{jobId}`

- Token: role được xem job.
- Nhập `jobId`.
- Dùng để xem chi tiết job.

### 72. GET `/api/v1/jobs/{jobId}/proposals`

- Token: BUSINESS sở hữu job.
- Nhập `jobId`.
- Dùng để xem proposal của job.

### 73. GET `/api/v1/jobs/my`

- Token: BUSINESS.
- Dùng để xem job của doanh nghiệp, gồm draft/open/closed.

### 74. PATCH `/api/v1/proposals/{proposalId}/status`

- Token: BUSINESS sở hữu job của proposal.
- Nhập `proposalId`.
- Query `status`: `Accepted` hoặc `Rejected`.

### 75. PATCH `/api/v1/jobs/{jobId}/status`

- Token: BUSINESS sở hữu job.
- Nhập `jobId`.
- Query `status`: ví dụ `OPEN`, `CLOSED`, `DRAFT`.

### 76. POST `/api/v1/proposals`

- Token: EXPERT.
- Body:

```json
{
  "jobId": 1,
  "technicalSolution": "Giải pháp triển khai hệ thống RAG chatbot kết hợp vector database và API backend.",
  "proposalDescription": "Chuyên gia sẽ phân tích dữ liệu, xây dựng pipeline RAG, kiểm thử và bàn giao tài liệu.",
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

- Nếu không đề xuất ngân sách milestone mới, để:

```json
{
  "jobId": 1,
  "technicalSolution": "Giải pháp triển khai hệ thống RAG chatbot kết hợp vector database và API backend.",
  "proposalDescription": "Chuyên gia sẽ phân tích dữ liệu, xây dựng pipeline RAG, kiểm thử và bàn giao tài liệu.",
  "proposalFileUrl": "proposal-files/experts/1/example.pdf",
  "bidAmount": 90000000,
  "proposalMilestone": null
}
```

### 77. POST `/api/v1/proposals/file`

- Token: EXPERT.
- Body: chọn file trong Swagger.
- Field file thường là `file`.
- Response trả path Firebase để gán vào `proposalFileUrl`.

### 78. POST `/api/v1/jobs`

- Token: BUSINESS.
- Body:

```json
{
  "title": "Tích hợp RAG chatbot cho chăm sóc khách hàng",
  "rawRequirements": "Cần chatbot trả lời câu hỏi sản phẩm, lấy dữ liệu từ FAQ và chuyển lead cho nhân viên.",
  "structuredSow": "Chatbot RAG phục vụ chăm sóc khách hàng.",
  "budget": 90000000,
  "plannedDurationValue": 6,
  "plannedDurationUnit": "WEEK",
  "domainIds": [2, 3],
  "skillIds": [2, 6, 9],
  "technologyIds": [1, 2],
  "milestones": [
    {
      "milestoneName": "Phân tích yêu cầu và thiết kế giải pháp",
      "description": "Khảo sát dữ liệu FAQ, thiết kế kiến trúc RAG.",
      "fundsAllocated": 30000000,
      "orderIndex": 1,
      "criteriaIds": [1, 2]
    },
    {
      "milestoneName": "Triển khai chatbot và kiểm thử",
      "description": "Xây dựng API, tích hợp model và kiểm thử nghiệp vụ.",
      "fundsAllocated": 60000000,
      "orderIndex": 2,
      "criteriaIds": [3, 4]
    }
  ]
}
```

- Job mới tạo sẽ ở trạng thái draft theo logic hiện tại.

## notification-controller

### 79. GET `/api/v1/notifications`

- Token: tài khoản nhận thông báo.
- Dùng để xem danh sách thông báo.

### 80. GET `/api/v1/notifications/unread-count`

- Token: tài khoản nhận thông báo.
- Dùng để đếm thông báo chưa đọc.

### 81. PATCH `/api/v1/notifications/{notificationId}/read`

- Token: tài khoản sở hữu thông báo.
- Nhập `notificationId`.
- Dùng để đánh dấu một thông báo đã đọc.

### 82. PATCH `/api/v1/notifications/read-all`

- Token: tài khoản đang đăng nhập.
- Dùng để đánh dấu tất cả thông báo đã đọc.

## profile-controller

### 83. GET `/api/v1/profiles/portfolio`

- Token: STAFF hoặc BUSINESS theo quyền.
- Query nếu Swagger hiển thị: nhập `expertId` hoặc filter tương ứng.
- Dùng để xem portfolio chuyên gia.

### 84. GET `/api/v1/profiles/expert`

- Token: STAFF hoặc BUSINESS.
- Dùng để xem danh sách hồ sơ expert.

### 85. GET `/api/v1/profiles/business`

- Token: STAFF hoặc ADMIN.
- Dùng để xem danh sách hồ sơ business.

### 86. GET `/api/v1/profiles/portfolio/me`

- Token: EXPERT.
- Dùng để xem portfolio của chính chuyên gia.

### 87. GET `/api/v1/profiles/files/view-url`

- Token: role được phép xem file.
- Query `path`: path Firebase đã lưu trong database.
- Ví dụ:

```text
business-licenses/accounts/1/license-demo.pdf
```

### 88. GET `/api/v1/profiles/expert/me`

- Token: EXPERT.
- Dùng để xem hồ sơ KYC của chính chuyên gia.

### 89. GET `/api/v1/profiles/business/me`

- Token: BUSINESS.
- Dùng để xem hồ sơ KYB của chính doanh nghiệp.

### 90. GET `/api/v1/profiles/business/by-job/{jobId}`

- Token: EXPERT hoặc role được xem job.
- Nhập `jobId`.
- Dùng để xem thông tin doanh nghiệp theo job.

### 91. POST `/api/v1/profiles/portfolio`

- Token: EXPERT.
- Body:

```json
{
  "domainIds": [2, 3],
  "skillIds": [2, 6, 9],
  "technologyIds": [1, 2],
  "yearsExperience": 3,
  "certificates": "expert-portfolios/accounts/2/certificate-demo.pdf",
  "selfDescription": "Chuyên gia AI có kinh nghiệm triển khai RAG, chatbot và hệ thống backend."
}
```

### 92. POST `/api/v1/profiles/portfolio/certificate-file`

- Token: EXPERT.
- Body: chọn file trong Swagger.
- Field file thường là `file`.
- Response trả path Firebase để gán vào `certificates`.

### 93. POST `/api/v1/profiles/expert`

- Token: EXPERT.
- Body:

```json
{
  "nationalId": "079203009999",
  "portfolioUrl": "https://portfolio.aitasker.local/expert-demo",
  "yearsOfExperience": 5,
  "title": "AI Engineer"
}
```

### 94. POST `/api/v1/profiles/business`

- Token: BUSINESS.
- Body:

```json
{
  "taxCode": "0312345678",
  "companyName": "Nova Retail",
  "address": "Quận 1, TP. Hồ Chí Minh",
  "businessLicenseUrl": "business-licenses/accounts/1/license-demo.pdf"
}
```

### 95. POST `/api/v1/profiles/business/license-file`

- Token: BUSINESS.
- Body: chọn file trong Swagger.
- Field file thường là `file`.
- Response trả path Firebase để gán vào `businessLicenseUrl`.

### 96. POST `/api/v1/profiles/approve/{type}/{id}`

- Token: STAFF.
- Path `type`: `BUSINESS` hoặc `EXPERT`.
- Path `id`: ID profile.
- Query `status`: `Approved` hoặc `Rejected`.

## SoW Generation

### 97. POST `/api/jobs/generate-sow`

- Token: BUSINESS nếu Swagger yêu cầu bảo mật.
- Body:

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

- Lưu ý endpoint này là `/api/jobs/generate-sow`, không có `/api/v1`.

## tax-check-controller

### 98. GET `/api/auth/tax-check/{mst}`

- Không cần token hoặc dùng BUSINESS nếu bị chặn.
- Nhập `mst`, ví dụ `0312345678`.
- Dùng để kiểm tra mã số thuế.

## test-controller

### 99. GET `/api/test/secure`

- Token: bất kỳ role đã đăng nhập.
- Dùng để kiểm tra JWT/security.

## wallet-controller

### 100. GET `/api/v1/wallet/me`

- Token: BUSINESS, EXPERT hoặc role có ví.
- Dùng để xem ví của tài khoản hiện tại.

## 6. Lỗi Thường Gặp Khi Test Swagger

### 401 Unauthorized

Nguyên nhân thường gặp:

- Chưa bấm `Authorize`.
- Token hết hạn.
- Thiếu chữ `Bearer ` trước token.
- Đang dùng token role khác hoặc token copy thiếu ký tự.

### 403 Forbidden

Nguyên nhân thường gặp:

- Token đúng nhưng role không có quyền.
- Ví dụ dùng token EXPERT gọi API chỉ dành cho BUSINESS hoặc ADMIN.

### 400 Bad Request

Nguyên nhân thường gặp:

- Thiếu field bắt buộc trong JSON.
- Sai enum, ví dụ nhập `open` thay vì `OPEN`.
- ID không tồn tại.
- Email chưa xác thực OTP khi gọi register.

### 409 Conflict

Nguyên nhân thường gặp:

- Tạo trùng domain/skill/technology code hoặc name.
- Expert gửi proposal khi đã có proposal cùng job chưa bị `Rejected`.

### 500 Internal Server Error

Nguyên nhân thường gặp:

- Dữ liệu trong database chưa đúng trạng thái.
- Migration/Flyway bị lệch checksum.
- Cấu hình Firebase/OpenAI/Mail thiếu hoặc sai.
- Nên xem log terminal đang chạy `spring-boot:run` để biết lỗi gốc.

## 7. Luồng Test Đề Xuất

1. `POST /api/auth/login` bằng BUSINESS, EXPERT, STAFF, ADMIN để lấy token.
2. `GET /api/auth/me` để kiểm tra token.
3. BUSINESS upload giấy phép và tạo profile.
4. EXPERT upload chứng chỉ, tạo expert profile và portfolio.
5. STAFF approve hồ sơ BUSINESS/EXPERT.
6. BUSINESS tạo job draft bằng `POST /api/v1/jobs`.
7. BUSINESS mở job bằng `PATCH /api/v1/jobs/{jobId}/status?status=OPEN`.
8. EXPERT xem job public bằng `GET /api/v1/jobs`.
9. EXPERT gửi proposal bằng `POST /api/v1/proposals`.
10. BUSINESS xem proposal bằng `GET /api/v1/jobs/{jobId}/proposals`.
11. BUSINESS accept proposal bằng `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted`.
12. BUSINESS tạo contract draft bằng `POST /api/v1/contracts/from-proposals/{proposalId}`.
13. BUSINESS và EXPERT lần lượt gọi `/sign` và `/nda-sign`.
14. Kiểm tra contract active bằng `GET /api/v1/contracts/{contractId}`.

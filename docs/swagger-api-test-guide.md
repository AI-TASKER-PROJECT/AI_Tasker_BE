# Huong dan test API Back-end bang Swagger

Tai lieu nay huong dan test API truc tiep tren Swagger UI theo dung thu tu tag/endpoint dang hien thi trong `docs/swagger-api-overview.md`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Route `Public`: test duoc khi khong co token.
- Route `Bearer JWT`: login bang `POST /api/auth/login`, copy `accessToken`, bam `Authorize`, dan token vao Swagger.
- Moi API ben duoi co muc rieng. Test toi API nao thi dung dung `Body raw` hoac huong dan input trong muc cua API do.
- API khong nhan request body van ghi ro `Body raw: Khong co body raw` de tranh dung nham payload cua API khac.

## Tai khoan seed thuong dung

| Role | Email | Mat khau |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | `12345678` |
| EXPERT | `expert@aitasker.local` | `12345678` |
| ADMIN | `admin@aitasker.local` | `12345678` |
| STAFF | `staff@aitasker.local` | `12345678` |

## admin-controller

### DELETE `/api/v1/admin/accounts/{accountId}`
- Giai thich: Vo hieu hoa account theo `accountId`.
- Huong dan test: Dung token ADMIN/STAFF, nhap `accountId` hop le, kiem tra account bi deactivate va response `DEACTIVATE ACCOUNT SUCCESS`.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/accounts`
- Giai thich: Lay danh sach account cho man hinh quan tri.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra response co danh sach account va role/status.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/analytics/overview`
- Giai thich: Lay so lieu tong quan cho dashboard admin.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra cac chi so tong hop tra ve trong `data`.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/audit-logs`
- Giai thich: Lay audit log, co the loc theo nhom actor.
- Huong dan test: Dung token ADMIN/STAFF; co the dien query `actorGroup=INTERNAL` hoac de trong; kiem tra log tra ve dung nhom.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/reviews/contracts/{contractId}`
- Giai thich: Lay review theo contract.
- Huong dan test: Dung token ADMIN/STAFF, nhap `contractId`, kiem tra danh sach review cua contract.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/settings`
- Giai thich: Lay danh sach system settings.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra key/value/isActive.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/staffs`
- Giai thich: Lay danh sach staff.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra staffId, accountId, specialization.
- Body raw: Khong co body raw.

### GET `/api/v1/admin/wallet`
- Giai thich: Lay system wallet cho admin.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra so du system wallet.
- Body raw: Khong co body raw.

### PATCH `/api/v1/admin/accounts/{accountId}`
- Giai thich: Cap nhat thong tin account.
- Huong dan test: Dung token ADMIN/STAFF, nhap `accountId`, dan body raw rieng ben duoi, kiem tra account duoc cap nhat.
- Body raw:
```json
{
  "email": "staff.updated@aitasker.local",
  "password": "12345678",
  "phone": "0909000111",
  "fullName": "Updated Staff Account",
  "role": "STAFF",
  "status": "ACTIVE",
  "specialization": "Dispute Operations"
}
```

### PATCH `/api/v1/admin/accounts/{accountId}/active`
- Giai thich: Bat/tat trang thai active cua account.
- Huong dan test: Dung token ADMIN/STAFF, nhap `accountId`, query `active=true` hoac `false`, kiem tra status active thay doi.
- Body raw: Khong co body raw.

### PATCH `/api/v1/admin/accounts/{accountId}/status`
- Giai thich: Cap nhat status account bang query param.
- Huong dan test: Dung token ADMIN/STAFF, nhap `accountId`, query `status=ACTIVE`, kiem tra response account co status moi.
- Body raw: Khong co body raw.

### PATCH `/api/v1/admin/settings/{key}`
- Giai thich: Cap nhat value hoac trang thai active cua system setting.
- Huong dan test: Dung token ADMIN/STAFF, nhap `key`, query `value` va/hoac `isActive`, kiem tra setting thay doi.
- Body raw: Khong co body raw.

### PATCH `/api/v1/admin/staffs/{staffId}`
- Giai thich: Cap nhat ho so staff.
- Huong dan test: Dung token ADMIN/STAFF, nhap `staffId`, dan body raw rieng ben duoi, kiem tra specialization duoc cap nhat.
- Body raw:
```json
{
  "accountId": 4,
  "specialization": "Technical Dispute Review"
}
```

### POST `/api/v1/admin/accounts`
- Giai thich: Tao account moi tu trang quan tri.
- Huong dan test: Dung token ADMIN/STAFF, dan body raw rieng ben duoi, kiem tra account moi duoc tao.
- Body raw:
```json
{
  "email": "staff.new@aitasker.local",
  "password": "12345678",
  "phone": "0909000123",
  "fullName": "New Staff Account",
  "role": "STAFF",
  "status": "ACTIVE",
  "specialization": "Operations"
}
```

### POST `/api/v1/admin/reviews`
- Giai thich: Tao review cho contract.
- Huong dan test: Dung token ADMIN/STAFF, dan body raw rieng ben duoi, kiem tra review moi co rating/comment dung.
- Body raw:
```json
{
  "contractId": 1,
  "reviewerId": 2,
  "revieweeId": 3,
  "rating": 4.5,
  "comment": "Tien do tot, giao tiep ro rang."
}
```

### POST `/api/v1/admin/staffs`
- Giai thich: Tao staff profile gan voi account.
- Huong dan test: Dung token ADMIN/STAFF, dan body raw rieng ben duoi, kiem tra staff moi duoc tao.
- Body raw:
```json
{
  "accountId": 4,
  "specialization": "Dispute Resolution"
}
```

### POST `/api/v1/admin/wallet/sync`
- Giai thich: Dong bo/lazy-create system wallet.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra response tra system wallet hien tai.
- Body raw: Khong co body raw.

## auth-controller

### GET `/api/auth/check-email`
- Giai thich: Kiem tra email da ton tai hay chua.
- Huong dan test: Khong can token, dien query `email=business@aitasker.local`, kiem tra response `true/false`.
- Body raw: Khong co body raw.

### GET `/api/auth/me`
- Giai thich: Lay current session theo token hien tai.
- Huong dan test: Dang nhap truoc, Authorize token, bam Execute, kiem tra thong tin account trong `data`.
- Body raw: Khong co body raw.

### POST `/api/auth/google/login`
- Giai thich: Dang nhap bang Google credential.
- Huong dan test: Khong can token, dan body raw rieng ben duoi; credential demo co the fail neu khong phai token Google that, nhung Swagger schema phai dung.
- Body raw:
```json
{
  "credential": "google-id-token-demo",
  "role": "EXPERT",
  "fullName": "Google Expert",
  "phone": "0908111222"
}
```

### POST `/api/auth/google/register`
- Giai thich: Dang ky/dang nhap Google cho user moi.
- Huong dan test: Khong can token, dan body raw rieng ben duoi; kiem tra validation credential va role.
- Body raw:
```json
{
  "credential": "google-id-token-demo",
  "role": "BUSINESS",
  "fullName": "Google Business",
  "phone": "0908333444"
}
```

### POST `/api/auth/login`
- Giai thich: Dang nhap bang email/password de lay JWT.
- Huong dan test: Khong can token, dan body raw rieng ben duoi, copy `accessToken` trong response de Authorize cac API private.
- Body raw:
```json
{
  "email": "business@aitasker.local",
  "password": "12345678"
}
```

### POST `/api/auth/register`
- Giai thich: Dang ky account moi.
- Huong dan test: Khong can token, doi email moi moi lan test, dan body raw rieng ben duoi, kiem tra response co token/account.
- Body raw:
```json
{
  "email": "new.business@aitasker.local",
  "password": "12345678",
  "fullName": "New Business User",
  "phone": "0908555666",
  "role": "BUSINESS"
}
```

## catalog-controller

### GET `/api/v1/acceptance-criteria`
- Giai thich: Lay danh muc acceptance criteria.
- Huong dan test: Khong can token, query `activeOnly=true`, kiem tra chi tra criteria active.
- Body raw: Khong co body raw.

### GET `/api/v1/domains`
- Giai thich: Lay danh muc domain.
- Huong dan test: Khong can token, query `activeOnly=false` hoac `true`, kiem tra danh sach domain.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}/domains`
- Giai thich: Lay domain gan voi job.
- Huong dan test: Dung token hop le, nhap `jobId`, kiem tra danh sach domain cua job.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}/skills`
- Giai thich: Lay skill gan voi job.
- Huong dan test: Dung token hop le, nhap `jobId`, kiem tra skill va isMandatory neu co.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}/technologies`
- Giai thich: Lay technology gan voi job.
- Huong dan test: Dung token hop le, nhap `jobId`, kiem tra danh sach technology.
- Body raw: Khong co body raw.

### GET `/api/v1/skills`
- Giai thich: Lay danh muc skill.
- Huong dan test: Khong can token, query `activeOnly=true` hoac `false`, kiem tra danh sach skill.
- Body raw: Khong co body raw.

### GET `/api/v1/technologies`
- Giai thich: Lay danh muc technology.
- Huong dan test: Dung token hop le, query `activeOnly=false`, kiem tra danh sach technology.
- Body raw: Khong co body raw.

### PATCH `/api/v1/domains/{domainId}`
- Giai thich: Cap nhat domain.
- Huong dan test: Dung token ADMIN/STAFF, nhap `domainId`, dan body raw rieng ben duoi, kiem tra domain thay doi.
- Body raw:
```json
{
  "domainCode": "AI",
  "domainName": "AI & Automation",
  "description": "Updated domain for AI delivery projects",
  "isActive": true,
  "sortOrder": 1
}
```

### PATCH `/api/v1/skills/{skillId}`
- Giai thich: Cap nhat skill.
- Huong dan test: Dung token ADMIN/STAFF, nhap `skillId`, dan body raw rieng ben duoi, kiem tra skill thay doi.
- Body raw:
```json
{
  "skillCode": "SPRING_BOOT",
  "skillName": "Spring Boot",
  "description": "Updated backend API skill",
  "isActive": true
}
```

### PATCH `/api/v1/technologies/{technologyId}`
- Giai thich: Cap nhat technology.
- Huong dan test: Dung token ADMIN/STAFF, nhap `technologyId`, dan body raw rieng ben duoi, kiem tra technology thay doi.
- Body raw:
```json
{
  "technologyCode": "POSTGRESQL",
  "technologyName": "PostgreSQL",
  "description": "Updated relational database technology",
  "isActive": true,
  "sortOrder": 3
}
```

### POST `/api/v1/domains`
- Giai thich: Tao domain moi.
- Huong dan test: Dung token ADMIN/STAFF, doi `domainCode` moi moi lan test, dan body raw rieng ben duoi.
- Body raw:
```json
{
  "domainCode": "WEB_APP",
  "domainName": "Web Application",
  "description": "Projects for building web-based systems",
  "isActive": true,
  "sortOrder": 2
}
```

### POST `/api/v1/skills`
- Giai thich: Tao skill moi.
- Huong dan test: Dung token ADMIN/STAFF, doi `skillCode` moi neu bi trung, dan body raw rieng ben duoi.
- Body raw:
```json
{
  "skillCode": "REACTJS",
  "skillName": "ReactJS",
  "description": "Frontend SPA development",
  "isActive": true
}
```

### POST `/api/v1/technologies`
- Giai thich: Tao technology moi.
- Huong dan test: Dung token ADMIN/STAFF, doi `technologyCode` moi neu bi trung, dan body raw rieng ben duoi.
- Body raw:
```json
{
  "technologyCode": "REDIS",
  "technologyName": "Redis",
  "description": "Caching and queue support",
  "isActive": true,
  "sortOrder": 4
}
```

### PUT `/api/v1/jobs/{jobId}/domains`
- Giai thich: Thay the toan bo domain cua job.
- Huong dan test: Dung token owner Business hoac ADMIN/STAFF, nhap `jobId`, dan body raw array rieng ben duoi.
- Body raw:
```json
[1, 2]
```

### PUT `/api/v1/jobs/{jobId}/skills`
- Giai thich: Thay the toan bo skill assignment cua job.
- Huong dan test: Dung token owner Business hoac ADMIN/STAFF, nhap `jobId`, dan body raw array rieng ben duoi.
- Body raw:
```json
[
  {
    "skillId": 1,
    "isMandatory": true
  },
  {
    "skillId": 2,
    "isMandatory": false
  }
]
```

### PUT `/api/v1/jobs/{jobId}/technologies`
- Giai thich: Thay the toan bo technology cua job.
- Huong dan test: Dung token owner Business hoac ADMIN/STAFF, nhap `jobId`, dan body raw array rieng ben duoi.
- Body raw:
```json
[1, 3, 5]
```

## chatbot-controller

### POST `/api/chatbot/ask`
- Giai thich: Gui cau hoi vao chatbot/RAG.
- Huong dan test: Khong can token, dan body raw rieng ben duoi, kiem tra cau tra loi trong response.
- Body raw:
```json
{
  "question": "Quy trinh ky hop dong va nap deposit tren AITASKER dien ra nhu the nao?"
}
```

## contract-execution-controller

### GET `/api/v1/contracts`
- Giai thich: Lay danh sach contract ma user hien tai duoc phep xem.
- Huong dan test: Dung token BUSINESS/EXPERT/ADMIN/STAFF, bam Execute, kiem tra service chi tra contract dung quyen.
- Body raw: Khong co body raw.

### GET `/api/v1/contracts/{contractId}`
- Giai thich: Lay chi tiet contract.
- Huong dan test: Dung token participant hoac operator, nhap `contractId`, kiem tra thong tin contract.
- Body raw: Khong co body raw.

### GET `/api/v1/contracts/{contractId}/disputes`
- Giai thich: Lay dispute cua contract.
- Huong dan test: Dung token co quyen tren contract, nhap `contractId`, kiem tra danh sach dispute.
- Body raw: Khong co body raw.

### GET `/api/v1/contracts/{contractId}/milestones`
- Giai thich: Lay milestone snapshot/live view cua contract.
- Huong dan test: Dung token co quyen tren contract, nhap `contractId`, kiem tra milestone va status hien tai.
- Body raw: Khong co body raw.

### GET `/api/v1/disputes/{disputeId}`
- Giai thich: Lay chi tiet dispute.
- Huong dan test: Dung token participant/operator, nhap `disputeId`, kiem tra evidence/status/proposedAction.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}/matching`
- Giai thich: Chay matching heuristic/legacy cho job.
- Huong dan test: Dung token hop le, nhap `jobId`, kiem tra response matching; danh dau la manual simulation.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}/milestones`
- Giai thich: Lay milestone public cua job OPEN.
- Huong dan test: Khong can token voi job OPEN, nhap numeric `jobId`, kiem tra job non-OPEN bi chan theo service rule.
- Body raw: Khong co body raw.

### GET `/api/v1/milestones/{milestoneId}/criteria`
- Giai thich: Lay acceptance criteria cua milestone.
- Huong dan test: Dung token co quyen, nhap `milestoneId`, kiem tra criteria gan dung milestone.
- Body raw: Khong co body raw.

### GET `/api/v1/milestones/{milestoneId}/deliverables`
- Giai thich: Lay deliverable cua milestone.
- Huong dan test: Dung token co quyen, nhap `milestoneId`, kiem tra sourceCodeUrl/demoLink/submissionNotes.
- Body raw: Khong co body raw.

### GET `/api/v1/milestones/{milestoneId}/transactions`
- Giai thich: Lay transaction legacy cua milestone.
- Huong dan test: Dung token co quyen, nhap `milestoneId`, kiem tra amount/status/type.
- Body raw: Khong co body raw.

### PATCH `/api/v1/disputes/{disputeId}/assign`
- Giai thich: Gan dispute cho staff.
- Huong dan test: Dung token ADMIN/STAFF, nhap `disputeId`, query `staffId=4`, kiem tra assignedStaffId thay doi.
- Body raw: Khong co body raw.

### PATCH `/api/v1/disputes/{disputeId}/resolve`
- Giai thich: Resolve dispute bang proposed action.
- Huong dan test: Dung token ADMIN/STAFF, nhap `disputeId`, query `proposedAction=REQUEST_REWORK`, kiem tra status/action.
- Body raw: Khong co body raw.

### PATCH `/api/v1/milestones/{milestoneId}`
- Giai thich: Cap nhat milestone.
- Huong dan test: Dung token co quyen tren milestone, nhap `milestoneId`, dan body raw rieng ben duoi.
- Body raw:
```json
{
  "jobId": 1,
  "contractId": 1,
  "milestoneName": "Backend API delivery phase 1",
  "description": "Cap nhat scope milestone sau khi chot SoW",
  "fundsAllocated": 15000000,
  "orderIndex": 1,
  "status": "IN_PROGRESS",
  "duration": 10,
  "durationUnit": "DAY",
  "criteriaIds": [1, 2]
}
```

### PATCH `/api/v1/transactions/{transactionId}/status`
- Giai thich: Cap nhat status transaction legacy.
- Huong dan test: Dung token ADMIN/STAFF, nhap `transactionId`, query `status=SUCCESS`, kiem tra status moi.
- Body raw: Khong co body raw.

### POST `/api/v1/admin/contracts/{contractId}/deposit/refund`
- Giai thich: Admin refund contract deposit.
- Huong dan test: Dung token ADMIN/STAFF, nhap `contractId`, dan body raw rieng ben duoi, kiem tra refund amount/note.
- Body raw:
```json
{
  "refundAmount": 2000000,
  "adminNote": "Refund phan deposit con lai sau khi dong contract."
}
```

### POST `/api/v1/contracts/{contractId}/deposit/pay`
- Giai thich: Business thanh toan deposit cho contract.
- Huong dan test: Dung token BUSINESS owner, nhap `contractId`, kiem tra wallet available/escrow va contract status.
- Body raw: Khong co body raw.

### POST `/api/v1/contracts/{contractId}/nda-sign`
- Giai thich: Ky NDA cho contract.
- Huong dan test: Dung token BUSINESS/EXPERT participant, nhap `contractId`, kiem tra timestamp NDA tuong ung.
- Body raw: Khong co body raw.

### POST `/api/v1/contracts/{contractId}/reject`
- Giai thich: Expert reject contract draft/pending.
- Huong dan test: Dung token EXPERT participant, nhap `contractId`, kiem tra contract cancelled va job quay ve OPEN neu hop le.
- Body raw: Khong co body raw.

### POST `/api/v1/contracts/{contractId}/sign`
- Giai thich: Ky hop dong cho business hoac expert.
- Huong dan test: Dung token participant, nhap `contractId`, kiem tra accepted timestamp va status sau khi du chu ky.
- Body raw: Khong co body raw.

### POST `/api/v1/contracts/{contractId}/terminate`
- Giai thich: Ket thuc contract co ly do.
- Huong dan test: Dung token co quyen, nhap `contractId`, query `reason=Scope cancelled by agreement`, kiem tra status termination.
- Body raw: Khong co body raw.

### POST `/api/v1/contracts/from-proposals/{proposalId}`
- Giai thich: Tao contract draft tu proposal da accepted.
- Huong dan test: Dung token BUSINESS owner/Admin, nhap `proposalId`, dan body raw rieng ben duoi, kiem tra contract DRAFT.
- Body raw:
```json
{
  "jobId": 1,
  "businessId": 2,
  "expertId": 3,
  "proposalId": 1,
  "contractTitle": "AI Hiring Platform MVP Contract",
  "totalBudget": 45000000,
  "timelineDays": 30,
  "status": "DRAFT"
}
```

### POST `/api/v1/criteria`
- Giai thich: Tao acceptance criteria.
- Huong dan test: Dung token ADMIN/STAFF hoac role duoc service cho phep, doi `criteriaCode` moi, dan body raw rieng ben duoi.
- Body raw:
```json
{
  "criteriaCode": "API_DOC_COMPLETE",
  "description": "Swagger docs da day du va endpoint test pass",
  "isActive": true,
  "sortOrder": 10
}
```

### POST `/api/v1/deliverables`
- Giai thich: Expert submit deliverable cho milestone.
- Huong dan test: Dung token EXPERT participant, dan body raw rieng ben duoi, kiem tra deliverable duoc tao va milestone vao review neu service xu ly.
- Body raw:
```json
{
  "milestoneId": 1,
  "sourceCodeUrl": "https://github.com/aitasker/demo-backend",
  "demoLink": "https://staging.aitasker.local/swagger-ui.html",
  "submissionNotes": "Da cap nhat Swagger va hoan tat regression."
}
```

### POST `/api/v1/disputes`
- Giai thich: Tao dispute cho contract/milestone.
- Huong dan test: Dung token co quyen, dan body raw rieng ben duoi, kiem tra dispute OPEN.
- Body raw:
```json
{
  "contractId": 1,
  "milestoneId": 1,
  "assignedStaffId": 4,
  "evidenceReport": "Business cho rang deliverable chua dung acceptance criteria.",
  "proposedAction": "REQUEST_REWORK",
  "status": "OPEN"
}
```

### POST `/api/v1/disputes/{disputeId}/demo-testing`
- Giai thich: Ghi nhan ket qua demo testing cho dispute.
- Huong dan test: Dung token STAFF/ADMIN, nhap `disputeId`, query `testResult=FAILED_ACCEPTANCE`, kiem tra evidence/status duoc cap nhat.
- Body raw: Khong co body raw.

### POST `/api/v1/disputes/{disputeId}/technical-report`
- Giai thich: Staff ghi technical report cho dispute.
- Huong dan test: Dung token STAFF/ADMIN, nhap `disputeId`, query `reportContent=...` va `proposedAction=REQUEST_REWORK`, kiem tra report duoc luu.
- Body raw: Khong co body raw.

### POST `/api/v1/milestones`
- Giai thich: Tao milestone.
- Huong dan test: Dung token co quyen tren job/contract, dan body raw rieng ben duoi, kiem tra milestone duoc tao.
- Body raw:
```json
{
  "jobId": 1,
  "contractId": 1,
  "milestoneName": "Swagger alignment",
  "description": "Cap nhat swagger overview va test guide",
  "fundsAllocated": 10000000,
  "orderIndex": 1,
  "status": "PENDING",
  "duration": 7,
  "durationUnit": "DAY",
  "criteriaIds": [1, 2]
}
```

### POST `/api/v1/milestones/{milestoneId}/complete`
- Giai thich: Business complete milestone.
- Huong dan test: Dung token BUSINESS owner, nhap `milestoneId`, kiem tra milestone completed va contract/job status neu milestone cuoi.
- Body raw: Khong co body raw.

### POST `/api/v1/milestones/sla-auto-approve`
- Giai thich: Chay SLA auto approve dang manual simulation.
- Huong dan test: Dung token STAFF/ADMIN, bam Execute, kiem tra cac milestone du dieu kien duoc approve.
- Body raw: Khong co body raw.

### POST `/api/v1/transactions`
- Giai thich: Tao transaction legacy cho milestone.
- Huong dan test: Dung token co quyen, dan body raw rieng ben duoi, kiem tra transaction duoc tao.
- Body raw:
```json
{
  "milestoneId": 1,
  "amount": 10000000,
  "commissionFee": 500000,
  "transactionType": "MILESTONE_PAYMENT",
  "status": "PENDING"
}
```

### POST `/api/v1/transactions/{transactionId}/webhook`
- Giai thich: Simulation webhook transaction legacy.
- Huong dan test: Dung token STAFF/ADMIN, nhap `transactionId`, query `paymentStatus=SUCCESS`, `bankTxCode=BANK123`, `receiptImgUrl=https://example.com/receipt.png`.
- Body raw: Khong co body raw.

## credit-controller

### POST `/api/credits/job-post/purchase`
- Giai thich: Mua job-post credits bang wallet.
- Huong dan test: Dung token BUSINESS, dan body raw rieng ben duoi, kiem tra quota job-post tang va wallet tru tien.
- Body raw:
```json
{
  "quantity": 3
}
```

### POST `/api/credits/proposal/purchase`
- Giai thich: Mua proposal credits bang wallet.
- Huong dan test: Dung token EXPERT, dan body raw rieng ben duoi, kiem tra quota proposal tang va wallet tru tien.
- Body raw:
```json
{
  "quantity": 5
}
```

## email-otp-controller

### POST `/api/auth/email/send-otp`
- Giai thich: Gui OTP den email.
- Huong dan test: Khong can token, dan body raw rieng ben duoi, kiem tra response gui OTP thanh cong.
- Body raw:
```json
{
  "email": "new.business@aitasker.local"
}
```

### POST `/api/auth/email/verify-otp`
- Giai thich: Xac minh OTP email.
- Huong dan test: Khong can token, thay `otp` bang ma that nhan duoc, kiem tra verify thanh cong.
- Body raw:
```json
{
  "email": "new.business@aitasker.local",
  "otp": "123456"
}
```

## Expert Candidates

### GET `/api/jobs/{jobPostingId}/expert-candidates`
- Giai thich: Lay/rank candidate expert cho job.
- Huong dan test: Dung token BUSINESS/Admin co quyen, nhap `jobPostingId`, kiem tra danh sach candidate va score.
- Body raw: Khong co body raw.

## Expert Recommendations

### GET `/api/jobs/{jobPostingId}/expert-recommendations`
- Giai thich: Lay recommendation da luu cho job.
- Huong dan test: Dung token BUSINESS co Premium/Admin, nhap `jobPostingId`, kiem tra danh sach recommendation da persist.
- Body raw: Khong co body raw.

### POST `/api/jobs/{jobPostingId}/expert-recommendations`
- Giai thich: Generate va luu Top expert recommendations.
- Huong dan test: Dung token BUSINESS co Premium, nhap `jobPostingId`, bam Execute, kiem tra recommendation duoc tao/luu.
- Body raw: Khong co body raw.

### POST `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select`
- Giai thich: Business chon expert tu recommendation.
- Huong dan test: Dung token BUSINESS owner, nhap `jobPostingId` va `expertId`, kiem tra recommendation selected va notification gui cho expert.
- Body raw: Khong co body raw.

## health-controller

### GET `/api/health`
- Giai thich: Health check backend.
- Huong dan test: Khong can token, bam Execute, kiem tra response 200.
- Body raw: Khong co body raw.

## marketplace-controller

### GET `/api/v1/jobs`
- Giai thich: Public list job OPEN tren marketplace.
- Huong dan test: Khong can token, bam Execute, kiem tra khong tra job DRAFT/CLOSED.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/my`
- Giai thich: Lay job cua Business dang dang nhap.
- Huong dan test: Dung token BUSINESS, bam Execute, kiem tra route nay bat buoc can JWT.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}`
- Giai thich: Lay chi tiet job.
- Huong dan test: Khong can token voi job OPEN; voi job DRAFT phai test owner/token theo service rule.
- Body raw: Khong co body raw.

### GET `/api/v1/jobs/{jobId}/proposals`
- Giai thich: Lay proposal cua job cho Business owner/operator.
- Huong dan test: Dung token BUSINESS owner/Admin, nhap `jobId`, kiem tra danh sach proposal.
- Body raw: Khong co body raw.

### GET `/api/v1/proposals/my`
- Giai thich: Lay proposal cua Expert dang dang nhap.
- Huong dan test: Dung token EXPERT, bam Execute, kiem tra chi tra proposal cua expert hien tai.
- Body raw: Khong co body raw.

### PATCH `/api/v1/jobs/{jobId}/status`
- Giai thich: Cap nhat status job.
- Huong dan test: Dung token BUSINESS owner/Admin, nhap `jobId`, query `status=OPEN`, kiem tra quota/SoW rule khi publish.
- Body raw: Khong co body raw.

### PATCH `/api/v1/proposals/{proposalId}/status`
- Giai thich: Review proposal Accepted/Rejected.
- Huong dan test: Dung token BUSINESS owner, nhap `proposalId`, query `status=Accepted`, kiem tra proposal status.
- Body raw: Khong co body raw.

### POST `/api/v1/jobs`
- Giai thich: Business tao job draft.
- Huong dan test: Dung token BUSINESS approved KYB, dan body raw rieng ben duoi, kiem tra job DRAFT va milestone/SoW neu co.
- Body raw:
```json
{
  "businessId": 2,
  "title": "Cap nhat Swagger va tai lieu API",
  "rawRequirements": "Can ra soat database, swagger docs, overview, test guide va tai lieu DB.",
  "structuredSow": "Milestone 1: audit API. Milestone 2: cap nhat docs. Milestone 3: regression.",
  "budget": 30000000,
  "status": "DRAFT",
  "plannedDurationValue": 14,
  "plannedDurationUnit": "DAY",
  "domainIds": [1],
  "skills": [
    {
      "skillId": 1,
      "isMandatory": true
    },
    {
      "skillId": 2,
      "isMandatory": false
    }
  ],
  "technologyIds": [1, 2],
  "milestones": [
    {
      "milestoneName": "Swagger audit",
      "description": "Kiem tra API va auth",
      "fundsAllocated": 10000000,
      "orderIndex": 1,
      "status": "PENDING",
      "duration": 3,
      "durationUnit": "DAY",
      "criteriaIds": [1]
    }
  ]
}
```

### POST `/api/v1/jobs/{jobId}/publish`
- Giai thich: Publish job sang OPEN.
- Huong dan test: Dung token BUSINESS owner, nhap `jobId`, kiem tra job co SoW va con quota truoc khi publish.
- Body raw: Khong co body raw.

### POST `/api/v1/proposals`
- Giai thich: Expert submit proposal vao job OPEN.
- Huong dan test: Dung token EXPERT approved KYC, dan body raw rieng ben duoi, kiem tra proposal duoc tao va quota proposal bi tru.
- Body raw:
```json
{
  "jobId": 1,
  "technicalSolution": "Dung Spring Boot, PostgreSQL va springdoc de dong bo API inventory.",
  "proposalDescription": "Se audit toan bo endpoint, chinh SecurityConfig, cap nhat Swagger docs va test guide.",
  "proposalFileUrl": "proposal-files/proposal-demo.pdf",
  "bidAmount": 28000000,
  "proposalMilestone": "Milestone 1 audit, milestone 2 update docs, milestone 3 regression"
}
```

### POST `/api/v1/proposals/file`
- Giai thich: Upload file proposal.
- Huong dan test: Dung token EXPERT, chon `multipart/form-data`, upload file vao field `file`, kiem tra response tra storage path.
- Body raw: Khong dung body raw JSON. Dung multipart field `file`.

### PUT `/api/v1/jobs/{jobId}`
- Giai thich: Cap nhat draft job, SoW va milestone.
- Huong dan test: Dung token BUSINESS owner, job phai DRAFT va chua co contract, nhap `jobId`, dan body raw rieng ben duoi.
- Body raw:
```json
{
  "businessId": 2,
  "title": "Cap nhat Swagger va tai lieu API - revised",
  "rawRequirements": "Bo sung yeu cau sap xep guide theo dung thu tu Swagger va them body mau tung API.",
  "structuredSow": "Milestone 1: audit route. Milestone 2: cap nhat docs. Milestone 3: QA.",
  "budget": 35000000,
  "status": "DRAFT",
  "plannedDurationValue": 16,
  "plannedDurationUnit": "DAY",
  "domainIds": [1, 2],
  "skills": [
    {
      "skillId": 1,
      "isMandatory": true
    },
    {
      "skillId": 3,
      "isMandatory": true
    }
  ],
  "technologyIds": [1, 3],
  "milestones": [
    {
      "milestoneName": "Swagger re-order",
      "description": "Sap lai overview va test guide theo Swagger",
      "fundsAllocated": 15000000,
      "orderIndex": 1,
      "status": "PENDING",
      "duration": 5,
      "durationUnit": "DAY",
      "criteriaIds": [1, 2]
    }
  ]
}
```

## membership-controller

### GET `/api/membership/packages`
- Giai thich: Lay package membership theo role hien tai.
- Huong dan test: Dung token BUSINESS/EXPERT, bam Execute, kiem tra package phu hop role.
- Body raw: Khong co body raw.

### POST `/api/membership/packages/{packageId}/purchase`
- Giai thich: Mua membership bang wallet.
- Huong dan test: Dung token co du wallet, nhap `packageId`, kiem tra membership/quota/premium va wallet ledger.
- Body raw: Khong co body raw.

## notification-controller

### GET `/api/v1/notifications`
- Giai thich: Lay danh sach notification cua user hien tai.
- Huong dan test: Dung token bat ky role, bam Execute, kiem tra notification dung user.
- Body raw: Khong co body raw.

### GET `/api/v1/notifications/unread-count`
- Giai thich: Dem notification chua doc.
- Huong dan test: Dung token hop le, bam Execute, kiem tra count giam sau khi mark read.
- Body raw: Khong co body raw.

### PATCH `/api/v1/notifications/{notificationId}/read`
- Giai thich: Danh dau mot notification da doc.
- Huong dan test: Dung token owner notification, nhap `notificationId`, kiem tra unread count giam.
- Body raw: Khong co body raw.

### PATCH `/api/v1/notifications/read-all`
- Giai thich: Danh dau tat ca notification da doc.
- Huong dan test: Dung token hop le, bam Execute, kiem tra unread count bang 0.
- Body raw: Khong co body raw.

## pay-ospayment-controller

### GET `/api/payments/payos/return`
- Giai thich: Public callback-style return URL cua PayOS.
- Huong dan test: Khong can token, dien query toi thieu `orderCode`, kiem tra sync status cua payment order.
- Body raw: Khong co body raw.

### POST `/api/payments/payos/{orderCode}/sync`
- Giai thich: Chu dong sync trang thai PayOS theo order code.
- Huong dan test: Dung token owner/admin, nhap `orderCode`, kiem tra payment order va wallet top-up duoc cap nhat.
- Body raw: Khong co body raw.

### POST `/api/payments/payos/create`
- Giai thich: Tao payment order PayOS de nap vi.
- Huong dan test: Dung token hop le, dan body raw rieng ben duoi, kiem tra response co checkout/payment link.
- Body raw:
```json
{
  "amount": 500000,
  "description": "Nap vi de mua membership va credits"
}
```

## profile-controller

### GET `/api/v1/profiles/business`
- Giai thich: Lay danh sach business profiles cho operator.
- Huong dan test: Dung token STAFF/ADMIN, bam Execute, kiem tra danh sach KYB.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/business/{businessId}`
- Giai thich: Public lay business profile theo id.
- Huong dan test: Khong can token, nhap `businessId`, kiem tra profile va fullName; id khong ton tai tra loi ro.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/business/by-job/{jobId}`
- Giai thich: Public lay business profile theo job OPEN.
- Huong dan test: Khong can token voi job OPEN, nhap `jobId`, kiem tra non-OPEN bi gate theo service rule.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/business/me`
- Giai thich: Lay KYB profile cua business dang dang nhap.
- Huong dan test: Dung token BUSINESS, bam Execute, kiem tra profile hien tai.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/expert`
- Giai thich: Lay danh sach expert profiles cho operator.
- Huong dan test: Dung token STAFF/ADMIN, bam Execute, kiem tra danh sach KYC.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/expert/{expertId}`
- Giai thich: Lay expert profile theo id.
- Huong dan test: Dung token hop le, nhap `expertId`, kiem tra profile/portfolio URL.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/expert/me`
- Giai thich: Lay KYC profile cua expert dang dang nhap.
- Huong dan test: Dung token EXPERT, bam Execute, kiem tra profile hien tai.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/files/view-url`
- Giai thich: Tao signed/view URL cho file Firebase/storage.
- Huong dan test: Dung token hop le, query `path=business-licenses/license-demo.pdf`, kiem tra URL tra ve.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/portfolio`
- Giai thich: Lay danh sach portfolio cho operator.
- Huong dan test: Dung token STAFF/ADMIN, bam Execute, kiem tra danh sach portfolio.
- Body raw: Khong co body raw.

### GET `/api/v1/profiles/portfolio/me`
- Giai thich: Lay portfolio cua expert dang dang nhap.
- Huong dan test: Dung token EXPERT, bam Execute, kiem tra domainIds/skillIds/technologyIds.
- Body raw: Khong co body raw.

### POST `/api/v1/profiles/approve/{type}/{id}`
- Giai thich: Staff/Admin approve/reject KYB/KYC profile.
- Huong dan test: Dung token STAFF/ADMIN, nhap `type=business` hoac `expert`, `id`, query `status=Approved` hoac `Rejected`, neu reject them `reason`.
- Body raw: Khong co body raw.

### POST `/api/v1/profiles/business`
- Giai thich: Business tao/cap nhat KYB profile.
- Huong dan test: Dung token BUSINESS, dan body raw rieng ben duoi, kiem tra `kybStatus=PENDING`.
- Body raw:
```json
{
  "accountId": 2,
  "taxCode": "0312345678",
  "companyName": "AITasker Business Co",
  "address": "123 Nguyen Hue, Quan 1, TP.HCM",
  "businessLicenseUrl": "business-licenses/license-demo.pdf",
  "kybStatus": "PENDING"
}
```

### POST `/api/v1/profiles/business/license-file`
- Giai thich: Upload business license file.
- Huong dan test: Dung token BUSINESS, chon `multipart/form-data`, upload file vao field `file`, kiem tra response tra storage path.
- Body raw: Khong dung body raw JSON. Dung multipart field `file`.

### POST `/api/v1/profiles/expert`
- Giai thich: Expert tao/cap nhat KYC profile.
- Huong dan test: Dung token EXPERT, dan body raw rieng ben duoi, kiem tra `kycStatus=PENDING`.
- Body raw:
```json
{
  "accountId": 3,
  "nationalId": "079203001234",
  "portfolioUrl": "expert-files/portfolio-demo.pdf",
  "yearsOfExperience": 5,
  "kycStatus": "PENDING"
}
```

### POST `/api/v1/profiles/expert/portfolio-file`
- Giai thich: Upload portfolio file cua expert.
- Huong dan test: Dung token EXPERT, chon `multipart/form-data`, upload file vao field `file`, kiem tra storage path.
- Body raw: Khong dung body raw JSON. Dung multipart field `file`.

### POST `/api/v1/profiles/portfolio`
- Giai thich: Expert tao/cap nhat portfolio structured.
- Huong dan test: Dung token EXPERT, dan body raw rieng ben duoi, kiem tra portfolio duoc luu.
- Body raw:
```json
{
  "expertId": 3,
  "domainIds": [1, 2],
  "skillIds": [1, 2, 3],
  "technologyIds": [1, 3],
  "yearsExperience": 5,
  "certificates": "AWS SAA, Google Professional Cloud Developer",
  "selfDescription": "Backend engineer chuyen Spring Boot, payment va AI-integrated workflow."
}
```

### POST `/api/v1/profiles/portfolio/certificate-file`
- Giai thich: Upload certificate file cua expert.
- Huong dan test: Dung token EXPERT, chon `multipart/form-data`, upload file vao field `file`, kiem tra storage path.
- Body raw: Khong dung body raw JSON. Dung multipart field `file`.

## SoW Generation

### POST `/api/jobs/generate-sow`
- Giai thich: Generate SoW, milestone va budget tu requirement tho.
- Huong dan test: Dung token BUSINESS, dan body raw rieng ben duoi, kiem tra response co structured SoW va milestone budgets.
- Body raw:
```json
{
  "projectTitle": "AI Recruitment Marketplace MVP",
  "rawRequirement": "Can xay backend quan ly job, proposal, contract, wallet va swagger docs day du.",
  "budget": 120000000,
  "duration": 8,
  "durationUnit": "WEEK",
  "supportFields": [
    "Can role BUSINESS, EXPERT, ADMIN",
    "Can payment wallet va dispute flow"
  ],
  "requiredSkills": [
    "Java",
    "Spring Boot",
    "PostgreSQL",
    "Swagger/OpenAPI"
  ]
}
```

## tax-check-controller

### GET `/api/auth/tax-check/{mst}`
- Giai thich: Tra cuu ma so thue doanh nghiep.
- Huong dan test: Khong can token, nhap `mst`, kiem tra response tax info hoac loi provider ro rang.
- Body raw: Khong co body raw.

## test-controller

### GET `/api/test/secure`
- Giai thich: Endpoint smoke test security.
- Huong dan test: Dung token hop le, bam Execute, kiem tra route private tra 200; thieu token phai bi chan.
- Body raw: Khong co body raw.

## user-quota-controller

### GET `/api/users/me/quota`
- Giai thich: Lay source of truth cho quota, package active va Premium.
- Huong dan test: Dung token BUSINESS/EXPERT, bam Execute, kiem tra quota va `premiumExpiredAt`.
- Body raw: Khong co body raw.

## wallet-api-controller

### GET `/api/wallet/current`
- Giai thich: Lay wallet hien tai cua account.
- Huong dan test: Dung token hop le, bam Execute, kiem tra available/holding/escrow balance.
- Body raw: Khong co body raw.

### GET `/api/wallet/transactions`
- Giai thich: Lay ledger wallet transactions.
- Huong dan test: Dung token hop le, bam Execute, kiem tra transaction list dung account.
- Body raw: Khong co body raw.

## wallet-controller

### GET `/api/v1/wallet/me`
- Giai thich: Snapshot vi hien tai cho account dang nhap.
- Huong dan test: Dung token hop le, bam Execute, doi chieu voi `GET /api/wallet/current` neu can.
- Body raw: Khong co body raw.

## withdrawal-controller

### GET `/api/v1/admin/withdrawal-requests`
- Giai thich: Admin lay danh sach withdrawal requests.
- Huong dan test: Dung token ADMIN/STAFF, bam Execute, kiem tra danh sach request va status.
- Body raw: Khong co body raw.

### GET `/api/v1/withdrawal-requests`
- Giai thich: User lay withdrawal requests cua minh.
- Huong dan test: Dung token co wallet, bam Execute, kiem tra chi tra request cua current account.
- Body raw: Khong co body raw.

### POST `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve`
- Giai thich: Admin approve withdrawal.
- Huong dan test: Dung token ADMIN/STAFF, nhap `withdrawalId`, dan body raw rieng ben duoi hoac `{}`, kiem tra holding bi tru va status approved.
- Body raw:
```json
{
  "adminNote": "Da doi soat so du va duyet rut tien."
}
```

### POST `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject`
- Giai thich: Admin reject withdrawal.
- Huong dan test: Dung token ADMIN/STAFF, nhap `withdrawalId`, dan body raw rieng ben duoi hoac `{}`, kiem tra holding tra ve available va status rejected.
- Body raw:
```json
{
  "adminNote": "Tu choi do thong tin tai khoan ngan hang chua hop le."
}
```

### POST `/api/v1/withdrawal-requests`
- Giai thich: Tao withdrawal request.
- Huong dan test: Dung token co wallet balance, dan body raw rieng ben duoi, kiem tra available chuyen sang holding.
- Body raw:
```json
{
  "amount": 1500000,
  "bankName": "Vietcombank",
  "bankAccountNumber": "0011002233445",
  "bankAccountHolder": "NGUYEN VAN A"
}
```

## Luu y regression

- `GET /api/v1/jobs/my` phai can JWT, khong duoc public.
- Moi API trong guide nay co muc rieng va body/input rieng ngay tai API do.
- Cac endpoint multipart phai hien field `file` trong Swagger.
- Cac endpoint legacy/manual simulation can danh dau khi test: `GET /api/v1/jobs/{jobId}/matching`, `POST /api/v1/disputes/{disputeId}/demo-testing`, `POST /api/v1/disputes/{disputeId}/technical-report`, `POST /api/v1/milestones/sla-auto-approve`, `POST /api/v1/transactions/{transactionId}/webhook`.

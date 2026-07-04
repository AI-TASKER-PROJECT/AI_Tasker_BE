# HUONG DAN TEST END-TO-END BACKEND AITASKER

## 1) Muc tieu
Tai lieu nay huong dan test nhanh cac luong backend week 1-8 theo role:
- BUSINESS
- EXPERT
- ADMIN
- STAFF

## 2) Chuan bi
1. Chay DB:
```bash
docker compose up -d
```
2. Chay app:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```
3. Cong cu goi API: Postman/Bruno/curl.

## 3) Seed can thiet
- Bang `roles` duoc seed tu migration V1.
- `system_settings` duoc seed tu migration V6.
- Neu can account mau, tao qua API register.

## 4) Kich ban test theo luong

### A. Auth + RBAC
1. Register BUSINESS:
- `POST /api/auth/register`
- Body: `{ "email": "business1@mail.com", "password": "12345678", "fullName": "B1", "phone": "0901", "role": "BUSINESS" }`
2. Register EXPERT tuong tu (`role=EXPERT`).
3. Login 2 tai khoan, lay access token.
4. Dung token EXPERT goi `POST /api/v1/jobs` phai bi chan quyen.

### B. Profile KYC/KYB
1. BUSINESS token goi `POST /api/v1/profiles/business`.
2. EXPERT token goi `POST /api/v1/profiles/expert`.
3. STAFF/ADMIN token goi duyet:
- `POST /api/v1/profiles/approve/BUSINESS/{id}?status=Approved`
- `POST /api/v1/profiles/approve/EXPERT/{id}?status=Approved`
4. Kiem tra list:
- `GET /api/v1/profiles/business`
- `GET /api/v1/profiles/business/{businessId}` — xem trang ca nhan doanh nghiep, response co `fullName`, `email`, `phone`
- `GET /api/v1/profiles/business/by-job/{jobId}` — job OPEN public, response co `fullName`, `email`, `phone`
- `GET /api/v1/profiles/expert`
- `GET /api/v1/profiles/expert/{expertId}` — xem trang ca nhan chuyen gia, response co `fullName`, `email`, `phone`, `title`

### C. Marketplace
1. BUSINESS tao job: `POST /api/v1/jobs`.
2. EXPERT nop proposal: `POST /api/v1/proposals`.
3. Thu nop duplicate proposal cung job phai bao conflict.
4. BUSINESS xem proposal theo job: `GET /api/v1/jobs/{jobId}/proposals`.
5. BUSINESS mo/tat job:
- `PATCH /api/v1/jobs/{jobId}/status?status=OPEN`
- `PATCH /api/v1/jobs/{jobId}/status?status=CLOSED`
6. BUSINESS review proposal:
- `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted`

### D. Contract + Execution
1. BUSINESS tao contract draft tu proposal:
- `POST /api/v1/contracts/from-proposals/{proposalId}`
3. Ky contract, ky NDA, va thanh toan contract deposit:
- `POST /api/v1/contracts/{contractId}/sign`
- `POST /api/v1/contracts/{contractId}/nda-sign`
- `POST /api/v1/contracts/{contractId}/deposit/pay`
4. Tao milestone va criteria:
- `POST /api/v1/milestones`
- `POST /api/v1/milestones/{milestoneId}/criteria`
- `PUT /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- `DELETE /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
5. Milestone Dispute v2 smoke flow:
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`
- `POST /api/v1/milestones/{milestoneId}/start`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- `POST /api/v1/milestones/{milestoneId}/deliverables`
- `POST /api/v1/milestones/{milestoneId}/approve` hoac `POST /api/v1/milestones/{milestoneId}/reject?reason=...`
6. BUSINESS/EXPERT tao termination request:
- `POST /api/v1/contracts/{contractId}/termination-requests`

### E. Finance + Dispute
1. Tao dispute theo milestone v2:
- `POST /api/v1/milestones/{milestoneId}/disputes?contractId={contractId}&initiatedBy=EXPERT&initiationType=EXPERT_SCOPE_CONCERN`
2. Escalate/settle dispute v2:
- `POST /api/v1/disputes/{disputeId}/escalation-request?reason=...&evidenceFile=...`
- `POST /api/v1/disputes/{disputeId}/assign-staff?staffId={staffId}`
- `POST /api/v1/disputes/{disputeId}/reject-intervention?reason=...`
- `POST /api/v1/disputes/{disputeId}/staff-decision?expertPercent=70&note=...&staffReport=...`
- `POST /api/v1/disputes/{disputeId}/execute-settlement`
 
### F. Review + System Settings
1. TAO REVIEW SAU KHI CONTRACT O TRANG THAI KET THUC:
- `POST /api/v1/admin/reviews`
2. LIST REVIEW THEO CONTRACT:
- `GET /api/v1/admin/reviews/contracts/{contractId}`
3. ADMIN XEM SETTINGS:
- `GET /api/v1/admin/settings`
4. ADMIN CAP NHAT 1 SETTING:
- `PATCH /api/v1/admin/settings/{key}?value=...&isActive=true`
5. ADMIN/STAFF XEM TONG QUAN CHI SO:
- `GET /api/v1/admin/analytics/overview`

## 5) Checklist expected result
- API tra ve dung format `ApiResponse`.
- Flow quyen theo role hoat dong.
- Migration len den `v10` thanh cong.
- Docker DB co tong `24` bang (23 nghiep vu + `flyway_schema_history`).
- Job/Proposal/Contract/Milestone/Transaction/Dispute tao duoc ban ghi.
- Khong co loi startup khi chay local docker profile.

## 6) Loi thuong gap
1. Loi ket noi DB:
- Kiem tra docker postgres co dang RUN va dung port `5433`.
2. JWT secret khong hop le:
- Dam bao `APP_JWT_SECRET` la BASE64.
3. Flyway migration:
- DAM BAO DA CO `V8__align_excel_schema.sql`, `V9__seed_demo_account.sql`, `V10__add_skill_domain_tables_and_contract_status.sql`.
- FILE `V7_seed_demo_account.sql` LA FILE LEGACY TEN CU, KHONG THAM GIA CHAY MIGRATION.

## 7) Lenh reset nhanh
```bash
docker compose down -v
docker compose up -d
./mvnw test
```

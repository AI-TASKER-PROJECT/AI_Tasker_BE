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
- `GET /api/v1/profiles/business/{businessId}` — xem trang ca nhan doanh nghiep
- `GET /api/v1/profiles/expert`
- `GET /api/v1/profiles/expert/{expertId}` — xem trang ca nhan chuyen gia

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
3. Kich hoat contract:
- `POST /api/v1/contracts/{contractId}/activate`
4. Tao milestone, criteria, deliverable:
- `POST /api/v1/milestones`
- `POST /api/v1/milestones/{milestoneId}/criteria`
- `PUT /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- `DELETE /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- `POST /api/v1/deliverables`
5. EXPERT ky NDA khi contract active:
- `POST /api/v1/contracts/{contractId}/nda-sign`
6. BUSINESS/ADMIN cham dut contract:
- `POST /api/v1/contracts/{contractId}/terminate?reason=...`

### E. Finance + Dispute
1. Tao transaction:
- `POST /api/v1/transactions`
2. STAFF/ADMIN cap nhat trang thai transaction:
- `PATCH /api/v1/transactions/{transactionId}/status?status=Success`
2b. Mo phong webhook payment:
- `POST /api/v1/transactions/{transactionId}/webhook?paymentStatus=Success&bankTxCode=...&receiptImgUrl=...`
2. Tao invoice:
- `POST /api/v1/invoices`
3. Tao dispute:
- `POST /api/v1/disputes`
4. ADMIN gan dispute cho staff:
- `PATCH /api/v1/disputes/{disputeId}/assign?staffId={staffId}`
5. ADMIN resolve dispute:
- `PATCH /api/v1/disputes/{disputeId}/resolve?proposedAction=...`
6. STAFF ghi ket qua demo testing:
- `POST /api/v1/disputes/{disputeId}/demo-testing?testResult=...`
7. STAFF ban hanh technical report:
- `POST /api/v1/disputes/{disputeId}/technical-report?reportContent=...&proposedAction=...`
8. Chay tac vu SLA auto approve milestone:
- `POST /api/v1/milestones/sla-auto-approve`
 
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

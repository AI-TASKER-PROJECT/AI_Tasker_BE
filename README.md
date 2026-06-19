# AITASKER Backend

## 1) Yeu cau moi truong
- Java 21
- Docker Desktop
- Maven Wrapper (`./mvnw`)

## 2) Chay database local
```bash
docker compose up -d
```
PostgreSQL local map port: `5433:5432`.

## 3) Cau hinh env
Su dung file `.env.docker` (da co san):
- `DB_HOST=127.0.0.1`
- `DB_PORT=5433`
- `DB_NAME=aitasker_db`
- `DB_USER=aitasker`
- `DB_PASSWORD=aitasker123`
- `APP_JWT_SECRET=<BASE64_SECRET>`

## 4) Chay backend
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```
Healthcheck:
- `GET http://localhost:8080/api/health`

Swagger/OpenAPI:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- De goi API duoc bao ve, bam `Authorize` va nhap access token JWT, khong them tien to `Bearer`.
- Dat `SWAGGER_ENABLED=false` neu can tat Swagger UI va OpenAPI docs.

## 5) Flyway migration
- Da bo sung migration `V8__align_excel_schema.sql` de dong bo theo DB/BR moi.
- Da bo sung migration `V10__add_skill_domain_tables_and_contract_status.sql` de them 4 bang moi theo Excel:
  - `domains`
  - `skills`
  - `job_domains`
  - `job_skills`
- KHONG SUA migration cu, chi THEM migration moi.
- Da chuyen seed demo account sang migration dung convention: `V9__seed_demo_account.sql`.
- `V7_seed_demo_account.sql` la FILE LEGACY TEN CU (KHONG DUNG CONVENTION FLYWAY), duoc GIU LAI de tham chieu lich su commit, KHONG tham gia migrate.

## 6) Chay test
```bash
docker compose up -d
./mvnw test
```
Test context da duoc khoa cau hinh local docker, khong phu thuoc Supabase.

## 7) Cac API backend da bo sung (week 4-8)
- Profile/KYC-KYB:
  - `POST /api/v1/profiles/business`
  - `POST /api/v1/profiles/expert`
  - `POST /api/v1/profiles/approve/{type}/{id}?status=...`
- Marketplace:
  - `POST /api/v1/jobs`
  - `GET /api/v1/jobs`
  - `GET /api/v1/jobs/{jobId}`
  - `POST /api/v1/proposals`
  - `GET /api/v1/jobs/{jobId}/proposals`
  - `PATCH /api/v1/jobs/{jobId}/status?status=...`
  - `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted|Rejected`
- Contract/Execution/Finance/Dispute:
  - `POST /api/v1/contracts/from-proposals/{proposalId}`
  - `POST /api/v1/contracts/change-requests`
  - `POST /api/v1/contracts/{contractId}/sign`
  - `POST /api/v1/contracts/{contractId}/nda-sign`
  - `POST /api/v1/contracts/{contractId}/reject`
  - `POST /api/v1/contracts/{contractId}/terminate?reason=...`
  - `POST /api/v1/milestones`
  - `POST /api/v1/milestones/{milestoneId}/complete`
  - `POST /api/v1/criteria`
  - `POST /api/v1/deliverables`
  - `POST /api/v1/transactions`
  - `POST /api/v1/transactions/{transactionId}/webhook?paymentStatus=Success|Failed&bankTxCode=...&receiptImgUrl=...`
  - `POST /api/v1/invoices`
  - `POST /api/v1/disputes`
  - `PATCH /api/v1/transactions/{transactionId}/status?status=Pending|Success|Failed`
  - `PATCH /api/v1/disputes/{disputeId}/assign?staffId=...`
  - `PATCH /api/v1/disputes/{disputeId}/resolve?proposedAction=...`
  - `POST /api/v1/milestones/sla-auto-approve`
  - `POST /api/v1/disputes/{disputeId}/demo-testing?testResult=...`
  - `POST /api/v1/disputes/{disputeId}/technical-report?reportContent=...&proposedAction=...`
- Admin/Review/Settings:
  - `POST /api/v1/admin/reviews`
  - `GET /api/v1/admin/reviews/contracts/{contractId}`
  - `GET /api/v1/admin/settings`
  - `PATCH /api/v1/admin/settings/{key}?value=...&isActive=...`
  - `GET /api/v1/admin/staffs`
  - `POST /api/v1/admin/staffs`
  - `GET /api/v1/admin/analytics/overview`

## 8) Luu y nghiep vu
- JWT da nang cap claim role thuc (`BUSINESS/EXPERT/ADMIN/STAFF`) de phuc vu RBAC.
- API nghiep vu su dung role check trong service (chan goi cheo API sai tham quyen).
- Cac file da chinh sua duoc bo sung comment huong dan theo ngu canh backend.

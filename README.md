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
- `GET /api/users/me/quota` la source of truth cho quota, active package va
  Premium permission. Frontend khong duoc dung localStorage
  `aitasker_active_package` lam business truth.

## 5) Flyway migration
- Da bo sung migration `V8__align_excel_schema.sql` de dong bo theo DB/BR moi.
- Da bo sung migration `V10__add_skill_domain_tables_and_contract_status.sql` de them 4 bang moi theo Excel:
  - `domains`
  - `skills`
  - `job_domains`
  - `job_skills`
- Da bo sung migration `V31__status_enum_constraint_alignment.sql` de chuan hoa
  status uppercase cho contract/job/milestone va gioi han `wallet_transactions`
  ve `POSTED`.
- Da bo sung migration `V32__premium_expiration_entitlement.sql` de them
  `user_quotas.premium_expired_at`, backfill Premium hien huu, va xoa flag cu.
- Da bo sung migration `V34__profile_rejection_reason.sql` de luu ly do
  staff tu choi xac minh KYB/KYC vao `business_profiles.rejection_reason`
  va `expert_profiles.rejection_reason`.
- Da bo sung migration `V37__business_initial_quota_and_recommendation_selection.sql`
  de cap 3 job-post quota mien phi cho Business va luu trang thai Business
  chon expert duoc AI recommend.
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
- Auth/Security:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/forgot-password`
  - `POST /api/auth/reset-password`
  - `POST /api/auth/google/login`
  - `POST /api/auth/google/register`
  - `GET /api/auth/check-email`
  - `GET /api/auth/me`
  - `POST /api/auth/email/send-otp`
  - `POST /api/auth/email/verify-otp`
- Profile/KYC-KYB:
  - `POST /api/v1/profiles/business`
  - `GET /api/v1/profiles/business/{businessId}`
  - `POST /api/v1/profiles/expert`
  - `GET /api/v1/profiles/expert/{expertId}`
  - `POST /api/v1/profiles/expert/portfolio-file`
  - `POST /api/v1/profiles/approve/{type}/{id}?status=...`
- Marketplace:
  - `POST /api/v1/jobs`
  - `PUT /api/v1/jobs/{jobId}` (draft update: persist jobs + sow + milestones, US-022)
  - `GET /api/v1/jobs`
  - `GET /api/v1/jobs/{jobId}`
  - `POST /api/v1/proposals`
  - `GET /api/v1/jobs/{jobId}/proposals`
  - `PATCH /api/v1/jobs/{jobId}/status?status=DRAFT|OPEN|IN_PROGRESS|CLOSED`
  - `PATCH /api/v1/proposals/{proposalId}/status?status=Accepted|Rejected`
- Contract/Execution/Finance/Dispute:
  - `POST /api/v1/contracts/from-proposals/{proposalId}`
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
  - `GET /api/wallet/current`
  - `GET /api/wallet/transactions`
  - `GET /api/membership/packages`
  - `POST /api/membership/packages/{packageId}/purchase`
  - `POST /api/credits/job-post/purchase`
  - `POST /api/credits/proposal/purchase`
  - `GET /api/users/me/quota`
  - `POST /api/v1/jobs/{jobId}/publish`
  - `POST /api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select`
  - `POST /api/v1/contracts/{contractId}/deposit/pay`
  - `POST /api/v1/admin/contracts/{contractId}/deposit/refund`
  - `POST /api/v1/withdrawal-requests`
  - `GET /api/v1/withdrawal-requests`
  - `GET /api/v1/admin/withdrawal-requests`
  - `POST /api/v1/admin/withdrawal-requests/{withdrawalId}/approve`
  - `POST /api/v1/admin/withdrawal-requests/{withdrawalId}/reject`
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

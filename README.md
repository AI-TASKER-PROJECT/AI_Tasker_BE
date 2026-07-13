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
- Da bo sung migration `V48__milestone_owned_acceptance_criteria.sql` de bo
  catalog 26 tieu chi co dinh, chuyen du lieu cu thanh tieu chi thuoc tung
  milestone va cho AI/Business quan ly noi dung rieng theo moc.
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
  - `POST /api/auth/refresh`
  - `POST /api/auth/forgot-password`
  - `POST /api/auth/reset-password`
  - `POST /api/auth/google/login`
  - `POST /api/auth/google/register`
  - `GET /api/auth/check-email`
  - `GET /api/auth/me`
  - `POST /api/auth/email/send-otp`
  - `POST /api/auth/email/verify-otp`
  - JWT access/refresh tokens are bound to `account.active_token_version`;
    a newer successful login invalidates older tokens for the same account.
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
  - `GET /api/v1/contracts`
  - `GET /api/v1/contracts/{contractId}`
  - `POST /api/v1/contracts/from-proposals/{proposalId}`
  - `POST /api/v1/contracts/{contractId}/sign`
  - `POST /api/v1/contracts/{contractId}/nda-sign`
  - `POST /api/v1/contracts/{contractId}/reject`
  - `POST /api/v1/contracts/{contractId}/deposit/pay`
  - `POST /api/v1/contracts/{contractId}/expert-deposit/pay`
  - `POST /api/v1/admin/contracts/{contractId}/deposits/refund`
  - `GET /api/v1/contracts/{contractId}/milestones`
  - `POST /api/v1/milestones`
  - `PATCH /api/v1/milestones/{milestoneId}`
  - `GET /api/v1/jobs/{jobId}/milestones`
  - `GET /api/v1/milestones/{milestoneId}/criteria`
  - `POST /api/v1/milestones/{milestoneId}/criteria`
  - `PUT /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
  - `DELETE /api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`
  - `POST /api/v1/milestones/{milestoneId}/start`
  - `POST /api/v1/milestones/{milestoneId}/deliverables`
  - `GET /api/v1/milestones/{milestoneId}/deliverables`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
  - `GET /api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
  - `POST /api/v1/milestones/{milestoneId}/approve`
  - `POST /api/v1/milestones/{milestoneId}/reject?reason=...`
  - `POST /api/v1/milestones/{milestoneId}/complete`
  - `POST /api/v1/milestones/{milestoneId}/disputes?contractId=...&initiatedBy=...&initiationType=...`
  - `GET /api/v1/contracts/{contractId}/disputes`
  - `GET /api/v1/disputes/{disputeId}`
  - `POST /api/v1/disputes/{disputeId}/escalation-request?reason=...&evidenceFile=...`
  - `POST /api/v1/disputes/{disputeId}/assign-staff?staffId=...`
  - `POST /api/v1/disputes/{disputeId}/reject-intervention?reason=...`
  - `POST /api/v1/disputes/{disputeId}/staff-decision?expertPercent=...&note=...&staffReport=...`
  - `POST /api/v1/disputes/{disputeId}/execute-settlement`
  - `POST /api/v1/disputes/{disputeId}/cancel?reason=...`
  - `POST /api/v1/disputes/staff-sla-escalate`
  - `POST /api/v1/contracts/{contractId}/termination-requests`
  - `POST /api/v1/contracts/{contractId}/immediate-termination`
  - `POST /api/v1/termination-requests/{terminationRequestId}/accept`
  - `POST /api/v1/termination-requests/{terminationRequestId}/dispute`
  - `POST /api/v1/termination-requests/expire-awaiting-expert`
  - `GET /api/v1/contracts/{contractId}/termination-requests`
  - `GET /api/v1/termination-requests/{terminationRequestId}`
  - `POST /api/v1/termination-requests/{terminationRequestId}/assign-staff?staffId=...`
  - `POST /api/v1/termination-requests/{terminationRequestId}/reject?reason=...`
  - `POST /api/v1/termination-requests/{terminationRequestId}/approve`
  - `POST /api/v1/termination-requests/{terminationRequestId}/partial-evidence`
  - `POST /api/v1/termination-requests/{terminationRequestId}/execute-settlement`
  - `POST /api/v1/termination-requests/{terminationRequestId}/withdraw?reason=...`
  - `POST /api/v1/termination-requests/{terminationRequestId}/refund-deposit`
  - `POST /api/v1/case-attachments`
  - `GET /api/v1/case-attachments?ownerType=...&ownerId=...`
  - `POST /api/v1/contracts/{contractId}/reviews`
  - `GET /api/v1/contracts/{contractId}/reviews`
  - `GET /api/wallet/current`
  - `GET /api/wallet/transactions`
  - `GET /api/membership/packages`
  - `POST /api/membership/packages/{packageId}/purchase`
  - `GET /api/v1/admin/membership/packages`
  - `POST /api/v1/admin/membership/packages`
  - `PATCH /api/v1/admin/membership/packages/{packageId}`
  - `DELETE /api/v1/admin/membership/packages/{packageId}`
  - `POST /api/credits/job-post/purchase`
  - `POST /api/credits/proposal/purchase`
  - `GET /api/users/me/quota`
  - `POST /api/v1/jobs/{jobId}/publish`
  - `POST /api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select`
  - `POST /api/v1/contracts/{contractId}/deposit/pay`
  - `POST /api/v1/contracts/{contractId}/expert-deposit/pay`
  - `POST /api/v1/admin/contracts/{contractId}/deposits/refund`
  - `POST /api/v1/withdrawal-requests`
  - `GET /api/v1/withdrawal-requests`
  - `GET /api/v1/admin/withdrawal-requests`
  - `POST /api/v1/admin/withdrawal-requests/{withdrawalId}/approve`
  - `POST /api/v1/admin/withdrawal-requests/{withdrawalId}/reject`
- Admin/Review/Settings:
  - `POST /api/v1/admin/reviews`
  - `GET /api/v1/admin/reviews/contracts/{contractId}`
  - `GET /api/v1/admin/settings`
  - `POST /api/v1/admin/settings`
  - `PATCH /api/v1/admin/settings/{key}?value=...&isActive=...`
  - `PUT /api/v1/admin/settings/{key}`
  - `DELETE /api/v1/admin/settings/{key}`
  - `GET /api/v1/admin/staffs`
  - `POST /api/v1/admin/staffs`
  - `GET /api/v1/admin/analytics/overview`
  - `GET /api/v1/admin/dashboard/summary`
  - `GET /api/v1/admin/dashboard/revenue?from=...&to=...&groupBy=month`
  - `GET /api/v1/admin/dashboard/contracts?from=...&to=...&groupBy=month`
  - `GET /api/v1/admin/dashboard/users?from=...&to=...&groupBy=month`
  - `GET /api/v1/admin/dashboard/jobs-proposals?from=...&to=...&groupBy=month`
  - `GET /api/v1/admin/dashboard/disputes?from=...&to=...&groupBy=month`
  - `GET /api/v1/admin/dashboard/membership?from=...&to=...&groupBy=month`
  - `GET /api/v1/admin/dashboard/finance-breakdown?from=...&to=...`
  - `GET /api/v1/admin/wallet/transactions`
  - `GET /api/v1/admin/disputes`
  - `GET /api/v1/admin/disputes/{disputeId}`
  - `GET /api/v1/staff/disputes`
- Catalog Admin:
  - `POST /api/v1/domains`
  - `PATCH /api/v1/domains/{domainId}`
  - `DELETE /api/v1/domains/{domainId}`
  - `POST /api/v1/skills`
  - `PATCH /api/v1/skills/{skillId}`
  - `DELETE /api/v1/skills/{skillId}`
  - `POST /api/v1/technologies`
  - `PATCH /api/v1/technologies/{technologyId}`
  - `DELETE /api/v1/technologies/{technologyId}`

## 8) Luu y nghiep vu
- JWT da nang cap claim role thuc (`BUSINESS/EXPERT/ADMIN/STAFF`) de phuc vu RBAC.
- API nghiep vu su dung role check trong service (chan goi cheo API sai tham quyen).
- Cac file da chinh sua duoc bo sung comment huong dan theo ngu canh backend.

## 9) Deploy backend cloud

Khuyen nghi deploy backend len Railway/Render va dung Neon PostgreSQL.

Neon can bat pgvector truoc khi backend chay migration:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Build command:
```bash
./mvnw -DskipTests package
```

Start command:
```bash
java -jar target/aitasker-0.0.1-SNAPSHOT.jar
```

Env toi thieu:
```env
PORT=8080

DB_HOST=<neon-direct-host>
DB_PORT=5432
DB_NAME=neondb
DB_USER=neondb_owner
DB_PASSWORD=<neon-password>
DB_SSLMODE=require

REDIS_HOST=<redis-host>
REDIS_PORT=<redis-port>
REDIS_PASSWORD=<redis-password-if-any>

APP_JWT_SECRET=<base64-secret>
APP_FRONTEND_URL=https://<frontend-domain>
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://<frontend-domain>,https://*.vercel.app

OPENAI_API_KEY=<openai-api-key>
MAIL_USERNAME=<smtp-username>
MAIL_PASSWORD=<smtp-app-password>

PAYOS_CLIENT_ID=<payos-client-id>
PAYOS_API_KEY=<payos-api-key>
PAYOS_CHECKSUM_KEY=<payos-checksum-key>
PAYOS_RETURN_URL=https://<backend-domain>/api/payments/payos/return
PAYOS_CANCEL_URL=https://<frontend-domain>

FIREBASE_STORAGE_BUCKET=<firebase-storage-bucket>
FIREBASE_SERVICE_ACCOUNT_JSON=<firebase-service-account-json>

GOOGLE_CLIENT_ID=<google-client-id>
SWAGGER_ENABLED=false
```

Neu van chay local bang file service account thi dung `FIREBASE_SERVICE_ACCOUNT_PATH`.
Khi deploy cloud, uu tien `FIREBASE_SERVICE_ACCOUNT_JSON` de khong phu thuoc file local trong thu muc `secrets`.

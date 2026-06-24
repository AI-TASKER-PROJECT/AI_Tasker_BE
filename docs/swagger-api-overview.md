# Tong quan Swagger API - AITASKER BE

Tai lieu nay liet ke API theo dung thu tu tag hien thi tren Swagger UI, bam theo `springdoc.swagger-ui.tags-sorter=alpha` va nhom tag thuc te cua springdoc.
Khi source, migration va tai lieu mau thuan, uu tien controller source + `SecurityConfig` + Flyway migration.

- Tong so REST endpoint trong source: **130**.
- Public endpoint theo SecurityConfig: **20**.
- Endpoint can Bearer JWT: **110**.
- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- OpenAPI JSON runtime: `http://localhost:8080/v3/api-docs`.
- Thu tu section duoi day la thu tu Swagger UI; overview nay khong con gom lai theo domain nghiep vu.

## Nguon su that

- Route inventory: `src/main/java/com/aitasker/be/controller/**` va `src/main/java/com/aitasker/be/test_demo/HealthController.java`.
- Public/private route: `src/main/java/com/aitasker/be/security/config/SecurityConfig.java`.
- Swagger order: `springdoc.swagger-ui.tags-sorter=alpha`, `springdoc.swagger-ui.operations-sorter=method` trong `src/main/resources/application.properties`.

## admin-controller

- Controller source: `AdminController`
- Giai thich: Tai khoan, staff, settings, audit, analytics va system wallet.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| DELETE | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/accounts` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/analytics/overview` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/audit-logs` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/reviews/contracts/{contractId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/settings` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/staffs` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/admin/wallet` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/admin/accounts/{accountId}/active` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/admin/accounts/{accountId}/status` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/admin/settings/{key}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/admin/staffs/{staffId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/accounts` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/reviews` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/staffs` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/wallet/sync` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## auth-controller

- Controller source: `AuthController`
- Giai thich: Dang ky, dang nhap, Google auth va current session.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/auth/check-email` | Public | Swagger UI phai hien route nay khong can token. |
| GET | `/api/auth/me` | Public | Swagger UI phai hien route nay khong can token. |
| POST | `/api/auth/google/login` | Public | Swagger UI phai hien route nay khong can token. |
| POST | `/api/auth/google/register` | Public | Swagger UI phai hien route nay khong can token. |
| POST | `/api/auth/login` | Public | Swagger UI phai hien route nay khong can token. Tai khoan bi tam khoa sau 5 lan sai mat khau. |
| POST | `/api/auth/register` | Public | Swagger UI phai hien route nay khong can token. |
| POST | `/api/auth/forgot-password` | Public | Gui email reset link, khong tiet lo email co ton tai. |
| POST | `/api/auth/reset-password` | Public | Dat lai mat khau bang token, mo khoa account bi lock do sai mat khau.

## catalog-controller

- Controller source: `CatalogController`
- Giai thich: Domain, skill, technology, acceptance criteria va taxonomy cua job.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/acceptance-criteria` | Public | Swagger UI phai hien route nay khong can token. |
| GET | `/api/v1/domains` | Public | Swagger UI phai hien route nay khong can token. |
| GET | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/skills` | Public | Swagger UI phai hien route nay khong can token. |
| GET | `/api/v1/technologies` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/domains/{domainId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/skills/{skillId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/technologies/{technologyId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/domains` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/skills` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/technologies` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PUT | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PUT | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PUT | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## chatbot-controller

- Controller source: `ChatbotController`
- Giai thich: Q&A ho tro tu knowledge/RAG noi bo.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| POST | `/api/chatbot/ask` | Public | Swagger UI phai hien route nay khong can token. |

## contract-execution-controller

- Controller source: `ContractExecutionController`
- Giai thich: Hop dong, milestone, deliverable, dispute va legacy transaction flow.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/contracts` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/contracts/{contractId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/contracts/{contractId}/disputes` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/contracts/{contractId}/milestones` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/disputes/{disputeId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/jobs/{jobId}/matching` | Bearer JWT | Flow legacy/manual simulation hoac heuristic, khong nen coi la production-complete. |
| GET | `/api/v1/jobs/{jobId}/milestones` | Public | Public chi cho numeric milestone route theo SecurityConfig. |
| GET | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/milestones/{milestoneId}/deliverables` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/milestones/{milestoneId}/transactions` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/disputes/{disputeId}/assign` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/disputes/{disputeId}/resolve` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/milestones/{milestoneId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/transactions/{transactionId}/status` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/contracts/{contractId}/deposit/pay` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/contracts/{contractId}/nda-sign` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/contracts/{contractId}/reject` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/contracts/{contractId}/sign` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/contracts/{contractId}/terminate` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/contracts/from-proposals/{proposalId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/criteria` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/deliverables` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/disputes` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/disputes/{disputeId}/demo-testing` | Bearer JWT | Flow legacy/manual simulation hoac heuristic, khong nen coi la production-complete. |
| POST | `/api/v1/disputes/{disputeId}/technical-report` | Bearer JWT | Flow legacy/manual simulation hoac heuristic, khong nen coi la production-complete. |
| POST | `/api/v1/milestones` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/milestones/{milestoneId}/complete` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/milestones/sla-auto-approve` | Bearer JWT | Flow legacy/manual simulation hoac heuristic, khong nen coi la production-complete. |
| POST | `/api/v1/transactions` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/transactions/{transactionId}/webhook` | Bearer JWT | Flow legacy/manual simulation hoac heuristic, khong nen coi la production-complete. |

## credit-controller

- Controller source: `CreditController`
- Giai thich: Mua credit job-post va proposal.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| POST | `/api/credits/job-post/purchase` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/credits/proposal/purchase` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## email-otp-controller

- Controller source: `EmailOtpController`
- Giai thich: Gui va xac minh OTP email.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| POST | `/api/auth/email/send-otp` | Public | Swagger UI phai hien route nay khong can token. |
| POST | `/api/auth/email/verify-otp` | Public | Swagger UI phai hien route nay khong can token. |

## Expert Candidates

- Controller source: `ExpertCandidateController`
- Giai thich: Ranking candidate expert tu du lieu job/SoW.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/jobs/{jobPostingId}/expert-candidates` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## Expert Recommendations

- Controller source: `ExpertRecommendationController`
- Giai thich: AI recommendation, luu recommendation va chon expert.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## health-controller

- Controller source: `HealthController`
- Giai thich: Health check va smoke endpoint.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/health` | Public | Swagger UI phai hien route nay khong can token. |

## marketplace-controller

- Controller source: `MarketplaceController`
- Giai thich: Job marketplace, draft update, publish, proposal va review proposal.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/jobs` | Public | Swagger UI phai hien route nay khong can token. |
| GET | `/api/v1/jobs/my` | Bearer JWT | Dashboard Business route, phai can JWT. |
| GET | `/api/v1/jobs/{jobId}` | Public | Swagger UI phai hien route nay khong can token. |
| GET | `/api/v1/jobs/{jobId}/proposals` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/proposals/my` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/jobs/{jobId}/status` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/proposals/{proposalId}/status` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/jobs` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/jobs/{jobId}/publish` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/proposals` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/proposals/file` | Bearer JWT | Upload file proposal qua multipart/form-data. |
| PUT | `/api/v1/jobs/{jobId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## membership-controller

- Controller source: `MembershipController`
- Giai thich: Danh sach goi thanh vien va mua goi.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/membership/packages` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/membership/packages/{packageId}/purchase` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## notification-controller

- Controller source: `NotificationController`
- Giai thich: Doc thong bao va cap nhat trang thai da doc.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/notifications` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/notifications/unread-count` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/notifications/{notificationId}/read` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| PATCH | `/api/v1/notifications/read-all` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## pay-ospayment-controller

- Controller source: `PayOSPaymentController`
- Giai thich: Tao order nap vi, return callback va dong bo trang thai PayOS.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/payments/payos/return` | Public | Swagger UI phai hien route nay khong can token. |
| POST | `/api/payments/payos/{orderCode}/sync` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/payments/payos/create` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## profile-controller

- Controller source: `ProfileController`
- Giai thich: Business/Expert profile, approval, portfolio va file view/upload.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/profiles/business` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/business/{businessId}` | Public | Public route tren Swagger; service van giu state/role gate cho case khong thuoc OPEN flow. |
| GET | `/api/v1/profiles/business/by-job/{jobId}` | Public | Public route tren Swagger; service van giu state/role gate cho case khong thuoc OPEN flow. |
| GET | `/api/v1/profiles/business/me` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/expert` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/expert/{expertId}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/expert/me` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/files/view-url` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/portfolio` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/profiles/portfolio/me` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/approve/{type}/{id}` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/business` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/business/license-file` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/expert` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/expert/portfolio-file` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/portfolio` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/profiles/portfolio/certificate-file` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## SoW Generation

- Controller source: `SowGenerationController`
- Giai thich: Sinh SoW tu raw requirements.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| POST | `/api/jobs/generate-sow` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## tax-check-controller

- Controller source: `TaxCheckController`
- Giai thich: Tra cuu ma so thue doanh nghiep.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/auth/tax-check/{mst}` | Public | Swagger UI phai hien route nay khong can token. |

## test-controller

- Controller source: `TestController`
- Giai thich: Endpoint kiem thu ky thuat noi bo.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/test/secure` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## user-quota-controller

- Controller source: `UserQuotaController`
- Giai thich: Nguon chuan quota, active package va Premium.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/users/me/quota` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## wallet-api-controller

- Controller source: `WalletApiController`
- Giai thich: Vi hien tai va ledger wallet_transactions.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/wallet/current` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/wallet/transactions` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## wallet-controller

- Controller source: `WalletController`
- Giai thich: Snapshot vi hien tai cho account dang nhap.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/wallet/me` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## withdrawal-controller

- Controller source: `WithdrawalController`
- Giai thich: Tao/ra soat yeu cau rut tien.

| Method | Path | Auth | Ghi chu |
| --- | --- | --- | --- |
| GET | `/api/v1/admin/withdrawal-requests` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| GET | `/api/v1/withdrawal-requests` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |
| POST | `/api/v1/withdrawal-requests` | Bearer JWT | Can goi dung role/ownership/state rule trong service. |

## Luu y nghiep vu

- `GET /api/users/me/quota` la nguon chuan cho quota, active package va Premium entitlement.
- `GET /api/v1/jobs/my` la route rieng cho dashboard Business va phai co JWT.
- `GET /api/payments/payos/return` la public callback-style route; sync top-up chu dong van qua `POST /api/payments/payos/{orderCode}/sync`.
- `POST /api/v1/transactions/{transactionId}/webhook`, dispute demo/technical-report, `POST /api/v1/milestones/sla-auto-approve` va `GET /api/v1/jobs/{jobId}/matching` van la nhom legacy/manual simulation.

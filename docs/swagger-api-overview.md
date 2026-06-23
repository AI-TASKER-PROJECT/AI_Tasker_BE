# Tong quan Swagger API - AITASKER BE

Tai lieu nay duoc dong bo tu controller source hien tai va quy tac auth trong `SecurityConfig`. Khi source, migration va tai lieu mau thuan, uu tien source + Flyway + `SecurityConfig`.

- Tong so REST endpoint trong source: **130**.
- Public endpoint theo SecurityConfig: **20**.
- Endpoint can Bearer JWT: **110**.
- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- OpenAPI JSON runtime: `http://localhost:8080/v3/api-docs`.
- Regression da sua trong dot nay: `GET /api/v1/jobs/my` khong con bi match nham boi public wildcard; chi `GET /api/v1/jobs/{jobId}` va `GET /api/v1/jobs/{jobId}/milestones` moi la public numeric route.

## Nguon su that

- Route inventory: `src/main/java/com/aitasker/be/controller/**` va `src/main/java/com/aitasker/be/test_demo/HealthController.java`.
- Public/private route: `src/main/java/com/aitasker/be/security/config/SecurityConfig.java`.
- Database/runtime contract: `src/main/resources/db/migration/*`.

## Health

- Controller: `HealthController`
- Nhom nghiep vu: Hạ tầng
- Ghi chu: Health check và smoke endpoint.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/health` | Public |

## Auth

- Controller: `AuthController`
- Nhom nghiep vu: Xác thực
- Ghi chu: Đăng ký, đăng nhập, Google auth và current session.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/auth/check-email` | Public |
| POST | `/api/auth/google/login` | Public |
| POST | `/api/auth/google/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Public |
| POST | `/api/auth/register` | Public |

## Email OTP

- Controller: `EmailOtpController`
- Nhom nghiep vu: Xác thực
- Ghi chu: Gửi và xác minh OTP email.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/auth/email/send-otp` | Public |
| POST | `/api/auth/email/verify-otp` | Public |

## Tax Check

- Controller: `TaxCheckController`
- Nhom nghiep vu: Xác thực
- Ghi chu: Tra cứu mã số thuế doanh nghiệp.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/auth/tax-check/{mst}` | Public |

## Catalog

- Controller: `CatalogController`
- Nhom nghiep vu: Catalog
- Ghi chu: Domain, skill, technology, acceptance criteria và taxonomy của job.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/v1/acceptance-criteria` | Public |
| GET | `/api/v1/domains` | Public |
| POST | `/api/v1/domains` | Bearer JWT |
| PATCH | `/api/v1/domains/{domainId}` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}/domains` | Bearer JWT |
| PUT | `/api/v1/jobs/{jobId}/domains` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}/skills` | Bearer JWT |
| PUT | `/api/v1/jobs/{jobId}/skills` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT |
| PUT | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT |
| GET | `/api/v1/skills` | Public |
| POST | `/api/v1/skills` | Bearer JWT |
| PATCH | `/api/v1/skills/{skillId}` | Bearer JWT |
| GET | `/api/v1/technologies` | Bearer JWT |
| POST | `/api/v1/technologies` | Bearer JWT |
| PATCH | `/api/v1/technologies/{technologyId}` | Bearer JWT |

## Marketplace

- Controller: `MarketplaceController`
- Nhom nghiep vu: Marketplace
- Ghi chu: Job, draft update, publish, proposal và review proposal.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/v1/jobs` | Public |
| POST | `/api/v1/jobs` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}` | Public |
| PUT | `/api/v1/jobs/{jobId}` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}/proposals` | Bearer JWT |
| POST | `/api/v1/jobs/{jobId}/publish` | Bearer JWT |
| PATCH | `/api/v1/jobs/{jobId}/status` | Bearer JWT |
| GET | `/api/v1/jobs/my` | Bearer JWT |
| POST | `/api/v1/proposals` | Bearer JWT |
| PATCH | `/api/v1/proposals/{proposalId}/status` | Bearer JWT |
| POST | `/api/v1/proposals/file` | Bearer JWT |
| GET | `/api/v1/proposals/my` | Bearer JWT |

## Expert Candidates

- Controller: `ExpertCandidateController`
- Nhom nghiep vu: AI
- Ghi chu: Ranking candidate expert từ dữ liệu job/SoW.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/jobs/{jobPostingId}/expert-candidates` | Bearer JWT |

## Expert Recommendations

- Controller: `ExpertRecommendationController`
- Nhom nghiep vu: AI
- Ghi chu: AI recommendation, lưu recommendation và chọn expert.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT |
| POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT |
| POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | Bearer JWT |

## SoW Generation

- Controller: `SowGenerationController`
- Nhom nghiep vu: AI
- Ghi chu: Sinh SoW từ raw requirements.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/jobs/generate-sow` | Bearer JWT |

## Contract Execution

- Controller: `ContractExecutionController`
- Nhom nghiep vu: Hợp đồng
- Ghi chu: Hợp đồng, milestone, deliverable, dispute và legacy transaction flow.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Bearer JWT |
| GET | `/api/v1/contracts` | Bearer JWT |
| GET | `/api/v1/contracts/{contractId}` | Bearer JWT |
| POST | `/api/v1/contracts/{contractId}/deposit/pay` | Bearer JWT |
| GET | `/api/v1/contracts/{contractId}/disputes` | Bearer JWT |
| GET | `/api/v1/contracts/{contractId}/milestones` | Bearer JWT |
| POST | `/api/v1/contracts/{contractId}/nda-sign` | Bearer JWT |
| POST | `/api/v1/contracts/{contractId}/reject` | Bearer JWT |
| POST | `/api/v1/contracts/{contractId}/sign` | Bearer JWT |
| POST | `/api/v1/contracts/{contractId}/terminate` | Bearer JWT |
| POST | `/api/v1/contracts/from-proposals/{proposalId}` | Bearer JWT |
| POST | `/api/v1/criteria` | Bearer JWT |
| POST | `/api/v1/deliverables` | Bearer JWT |
| POST | `/api/v1/disputes` | Bearer JWT |
| GET | `/api/v1/disputes/{disputeId}` | Bearer JWT |
| PATCH | `/api/v1/disputes/{disputeId}/assign` | Bearer JWT |
| POST | `/api/v1/disputes/{disputeId}/demo-testing` | Bearer JWT |
| PATCH | `/api/v1/disputes/{disputeId}/resolve` | Bearer JWT |
| POST | `/api/v1/disputes/{disputeId}/technical-report` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}/matching` | Bearer JWT |
| GET | `/api/v1/jobs/{jobId}/milestones` | Public |
| POST | `/api/v1/milestones` | Bearer JWT |
| PATCH | `/api/v1/milestones/{milestoneId}` | Bearer JWT |
| POST | `/api/v1/milestones/{milestoneId}/complete` | Bearer JWT |
| GET | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT |
| GET | `/api/v1/milestones/{milestoneId}/deliverables` | Bearer JWT |
| GET | `/api/v1/milestones/{milestoneId}/transactions` | Bearer JWT |
| POST | `/api/v1/milestones/sla-auto-approve` | Bearer JWT |
| POST | `/api/v1/transactions` | Bearer JWT |
| PATCH | `/api/v1/transactions/{transactionId}/status` | Bearer JWT |
| POST | `/api/v1/transactions/{transactionId}/webhook` | Bearer JWT |

## Membership

- Controller: `MembershipController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Danh sách gói thành viên và mua gói.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/membership/packages` | Bearer JWT |
| POST | `/api/membership/packages/{packageId}/purchase` | Bearer JWT |

## Credits

- Controller: `CreditController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Mua credit job-post và proposal.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/credits/job-post/purchase` | Bearer JWT |
| POST | `/api/credits/proposal/purchase` | Bearer JWT |

## PayOS Payment

- Controller: `PayOSPaymentController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Tạo order nạp ví, return callback và đồng bộ trạng thái PayOS.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/payments/payos/{orderCode}/sync` | Bearer JWT |
| POST | `/api/payments/payos/create` | Bearer JWT |
| GET | `/api/payments/payos/return` | Public |

## Wallet API

- Controller: `WalletApiController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Ví hiện tại và ledger wallet_transactions.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/wallet/current` | Bearer JWT |
| GET | `/api/wallet/transactions` | Bearer JWT |

## Wallet

- Controller: `WalletController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Snapshot ví hiện tại cho account đăng nhập.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/v1/wallet/me` | Bearer JWT |

## User Quota

- Controller: `UserQuotaController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Nguồn chuẩn quota, active package và Premium.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/users/me/quota` | Bearer JWT |

## Withdrawal

- Controller: `WithdrawalController`
- Nhom nghiep vu: Ví và quota
- Ghi chu: Tạo/rà soát yêu cầu rút tiền.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/v1/admin/withdrawal-requests` | Bearer JWT |
| POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Bearer JWT |
| POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Bearer JWT |
| GET | `/api/v1/withdrawal-requests` | Bearer JWT |
| POST | `/api/v1/withdrawal-requests` | Bearer JWT |

## Profiles

- Controller: `ProfileController`
- Nhom nghiep vu: Hồ sơ
- Ghi chu: Business/Expert profile, approval, portfolio và file view/upload.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/v1/profiles/approve/{type}/{id}` | Bearer JWT |
| GET | `/api/v1/profiles/business` | Bearer JWT |
| POST | `/api/v1/profiles/business` | Bearer JWT |
| GET | `/api/v1/profiles/business/{businessId}` | Public |
| GET | `/api/v1/profiles/business/by-job/{jobId}` | Public |
| POST | `/api/v1/profiles/business/license-file` | Bearer JWT |
| GET | `/api/v1/profiles/business/me` | Bearer JWT |
| GET | `/api/v1/profiles/expert` | Bearer JWT |
| POST | `/api/v1/profiles/expert` | Bearer JWT |
| GET | `/api/v1/profiles/expert/{expertId}` | Bearer JWT |
| GET | `/api/v1/profiles/expert/me` | Bearer JWT |
| POST | `/api/v1/profiles/expert/portfolio-file` | Bearer JWT |
| GET | `/api/v1/profiles/files/view-url` | Bearer JWT |
| GET | `/api/v1/profiles/portfolio` | Bearer JWT |
| POST | `/api/v1/profiles/portfolio` | Bearer JWT |
| POST | `/api/v1/profiles/portfolio/certificate-file` | Bearer JWT |
| GET | `/api/v1/profiles/portfolio/me` | Bearer JWT |

## Notifications

- Controller: `NotificationController`
- Nhom nghiep vu: Thông báo
- Ghi chu: Đọc thông báo và cập nhật trạng thái đã đọc.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/v1/notifications` | Bearer JWT |
| PATCH | `/api/v1/notifications/{notificationId}/read` | Bearer JWT |
| PATCH | `/api/v1/notifications/read-all` | Bearer JWT |
| GET | `/api/v1/notifications/unread-count` | Bearer JWT |

## Chatbot

- Controller: `ChatbotController`
- Nhom nghiep vu: Chatbot
- Ghi chu: Q&A hỗ trợ từ knowledge/RAG nội bộ.

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/chatbot/ask` | Public |

## Admin

- Controller: `AdminController`
- Nhom nghiep vu: Quản trị
- Ghi chu: Tài khoản, staff, settings, audit, analytics và system wallet.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/v1/admin/accounts` | Bearer JWT |
| POST | `/api/v1/admin/accounts` | Bearer JWT |
| DELETE | `/api/v1/admin/accounts/{accountId}` | Bearer JWT |
| PATCH | `/api/v1/admin/accounts/{accountId}` | Bearer JWT |
| PATCH | `/api/v1/admin/accounts/{accountId}/active` | Bearer JWT |
| PATCH | `/api/v1/admin/accounts/{accountId}/status` | Bearer JWT |
| GET | `/api/v1/admin/analytics/overview` | Bearer JWT |
| GET | `/api/v1/admin/audit-logs` | Bearer JWT |
| POST | `/api/v1/admin/reviews` | Bearer JWT |
| GET | `/api/v1/admin/reviews/contracts/{contractId}` | Bearer JWT |
| GET | `/api/v1/admin/settings` | Bearer JWT |
| PATCH | `/api/v1/admin/settings/{key}` | Bearer JWT |
| GET | `/api/v1/admin/staffs` | Bearer JWT |
| POST | `/api/v1/admin/staffs` | Bearer JWT |
| PATCH | `/api/v1/admin/staffs/{staffId}` | Bearer JWT |
| GET | `/api/v1/admin/wallet` | Bearer JWT |
| POST | `/api/v1/admin/wallet/sync` | Bearer JWT |

## Test

- Controller: `TestController`
- Nhom nghiep vu: Hạ tầng
- Ghi chu: Endpoint kiểm thử kỹ thuật nội bộ.

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/api/test/secure` | Bearer JWT |

## Luu y nghiep vu

- `GET /api/users/me/quota` la nguon chuan cho quota, active package va Premium entitlement.
- `GET /api/v1/jobs/my` la route rieng cho dashboard Business va phai co JWT.
- `GET /api/v1/profiles/business/{businessId}` va `GET /api/v1/profiles/business/by-job/{jobId}` la public route, nhung service van giu role/state guard cho truong hop ngoai OPEN-job flow.
- `POST /api/v1/transactions/{transactionId}/webhook`, dispute demo/technical-report va `POST /api/v1/milestones/sla-auto-approve` van la flow legacy/manual simulation, can danh dau ro khi test.
- `GET /api/payments/payos/return` la public callback-style route; dong bo trang thai top-up chu dong van qua `POST /api/payments/payos/{orderCode}/sync`.
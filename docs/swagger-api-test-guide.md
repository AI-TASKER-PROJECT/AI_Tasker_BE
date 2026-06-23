# Huong dan test API Back-end bang Swagger

Tai lieu nay di kem `docs/swagger-api-overview.md`. Overview la inventory day du; guide nay tap trung vao cach test va cac diem can xac nhan khi thao tac tren Swagger UI.

## 1. Chuan bi
 
```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`.
- Route `Public` trong bang ben duoi khong can token.
- Route `Bearer JWT` can login qua `POST /api/auth/login`, copy `accessToken`, bam `Authorize`, dan token vao o token, khong them chu `Bearer `.

## 2. Tai khoan seed thuong dung

| Role | Email | Mat khau |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | `12345678` |
| EXPERT | `expert@aitasker.local` | `12345678` |
| ADMIN | `admin@aitasker.local` | `12345678` |
| STAFF | `staff@aitasker.local` | `12345678` |

## 3. Checklist chung khi test

- Kiem tra response envelope `success`, `message`, `data` cho cac route dung `ApiResponse`.
- Kiem tra route `Public` truy cap duoc khi khong co token; route `Bearer JWT` phai tra `401/403` khi thieu hoac sai quyen.
- Kiem tra query param, multipart va body schema trong Swagger co day du va dung ten field.
- Kiem tra state/business rule quan trong: publish job, submit proposal, sign/NDA/deposit, withdrawal approve/reject, quota va Premium.
- Danh dau ro endpoint legacy/manual simulation khi chay test regression.

## 4. Public route regression uu tien

- `GET /api/health`: tra chuoi health check, khong can token.
- `GET /api/v1/jobs`: public list chi nen tra job OPEN.
- `GET /api/v1/jobs/{jobId}`: public voi job OPEN, nhung `GET /api/v1/jobs/my` phai van can JWT.
- `GET /api/v1/jobs/{jobId}/milestones`: public cho OPEN-job flow.
- `GET /api/v1/domains`, `GET /api/v1/skills`, `GET /api/v1/acceptance-criteria`: public taxonomy lookup.
- `GET /api/v1/profiles/business/{businessId}` va `/business/by-job/{jobId}`: public route theo rule da mo.
- Toan bo `/api/auth/**`, `POST /api/chatbot/ask`, `GET /api/payments/payos/return`: Swagger phai hien la public route.

## Health

- Pham vi: Health check và smoke endpoint.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/health` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |

## Auth

- Pham vi: Đăng ký, đăng nhập, Google auth và current session.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/auth/check-email` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/auth/google/login` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/auth/google/register` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/auth/login` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| GET | `/api/auth/me` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/auth/register` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |

## Email OTP

- Pham vi: Gửi và xác minh OTP email.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/auth/email/send-otp` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/auth/email/verify-otp` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |

## Tax Check

- Pham vi: Tra cứu mã số thuế doanh nghiệp.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/auth/tax-check/{mst}` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |

## Catalog

- Pham vi: Domain, skill, technology, acceptance criteria và taxonomy của job.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/v1/acceptance-criteria` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| GET | `/api/v1/domains` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/v1/domains` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/domains/{domainId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PUT | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PUT | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PUT | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/skills` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/v1/skills` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/skills/{skillId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/technologies` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/technologies` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/technologies/{technologyId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Marketplace

- Pham vi: Job, draft update, publish, proposal và review proposal.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/v1/jobs` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/v1/jobs` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/{jobId}` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| PUT | `/api/v1/jobs/{jobId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/{jobId}/proposals` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/jobs/{jobId}/publish` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/jobs/{jobId}/status` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/my` | Bearer JWT | Phai can JWT; regression cho wildcard public route da duoc khoa lai. |
| POST | `/api/v1/proposals` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/proposals/{proposalId}/status` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/proposals/file` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/proposals/my` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Expert Candidates

- Pham vi: Ranking candidate expert từ dữ liệu job/SoW.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/jobs/{jobPostingId}/expert-candidates` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Expert Recommendations

- Pham vi: AI recommendation, lưu recommendation và chọn expert.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## SoW Generation

- Pham vi: Sinh SoW từ raw requirements.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/jobs/generate-sow` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Contract Execution

- Pham vi: Hợp đồng, milestone, deliverable, dispute và legacy transaction flow.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/contracts` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/contracts/{contractId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/contracts/{contractId}/deposit/pay` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/contracts/{contractId}/disputes` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/contracts/{contractId}/milestones` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/contracts/{contractId}/nda-sign` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/contracts/{contractId}/reject` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/contracts/{contractId}/sign` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/contracts/{contractId}/terminate` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/contracts/from-proposals/{proposalId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/criteria` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/deliverables` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/disputes` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/disputes/{disputeId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/disputes/{disputeId}/assign` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/disputes/{disputeId}/demo-testing` | Bearer JWT | Endpoint legacy/manual simulation; chi test khi can regression backend noi bo. |
| PATCH | `/api/v1/disputes/{disputeId}/resolve` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/disputes/{disputeId}/technical-report` | Bearer JWT | Endpoint legacy/manual simulation; chi test khi can regression backend noi bo. |
| GET | `/api/v1/jobs/{jobId}/matching` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/jobs/{jobId}/milestones` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/v1/milestones` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/milestones/{milestoneId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/milestones/{milestoneId}/complete` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/milestones/{milestoneId}/deliverables` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/milestones/{milestoneId}/transactions` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/milestones/sla-auto-approve` | Bearer JWT | Endpoint legacy/manual simulation; chi test khi can regression backend noi bo. |
| POST | `/api/v1/transactions` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/transactions/{transactionId}/status` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/transactions/{transactionId}/webhook` | Bearer JWT | Endpoint legacy/manual simulation; chi test khi can regression backend noi bo. |

## Membership

- Pham vi: Danh sách gói thành viên và mua gói.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/membership/packages` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/membership/packages/{packageId}/purchase` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Credits

- Pham vi: Mua credit job-post và proposal.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/credits/job-post/purchase` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/credits/proposal/purchase` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## PayOS Payment

- Pham vi: Tạo order nạp ví, return callback và đồng bộ trạng thái PayOS.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/payments/payos/{orderCode}/sync` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/payments/payos/create` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/payments/payos/return` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |

## Wallet API

- Pham vi: Ví hiện tại và ledger wallet_transactions.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/wallet/current` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/wallet/transactions` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Wallet

- Pham vi: Snapshot ví hiện tại cho account đăng nhập.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/v1/wallet/me` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## User Quota

- Pham vi: Nguồn chuẩn quota, active package và Premium.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/users/me/quota` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Withdrawal

- Pham vi: Tạo/rà soát yêu cầu rút tiền.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/v1/admin/withdrawal-requests` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/withdrawal-requests` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/withdrawal-requests` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Profiles

- Pham vi: Business/Expert profile, approval, portfolio và file view/upload.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/v1/profiles/approve/{type}/{id}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/business` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/profiles/business` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/business/{businessId}` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| GET | `/api/v1/profiles/business/by-job/{jobId}` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |
| POST | `/api/v1/profiles/business/license-file` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/business/me` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/expert` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/profiles/expert` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/expert/{expertId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/expert/me` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/profiles/expert/portfolio-file` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/files/view-url` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/portfolio` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/profiles/portfolio` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/profiles/portfolio/certificate-file` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/profiles/portfolio/me` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Notifications

- Pham vi: Đọc thông báo và cập nhật trạng thái đã đọc.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/v1/notifications` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/notifications/{notificationId}/read` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/notifications/read-all` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/notifications/unread-count` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Chatbot

- Pham vi: Q&A hỗ trợ từ knowledge/RAG nội bộ.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| POST | `/api/chatbot/ask` | Public | Goi duoc khi khong co token; Swagger khong khoa auth route nay. |

## Admin

- Pham vi: Tài khoản, staff, settings, audit, analytics và system wallet.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/v1/admin/accounts` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/admin/accounts` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| DELETE | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/admin/accounts/{accountId}/active` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/admin/accounts/{accountId}/status` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/admin/analytics/overview` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/admin/audit-logs` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/admin/reviews` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/admin/reviews/contracts/{contractId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/admin/settings` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/admin/settings/{key}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/admin/staffs` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/admin/staffs` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| PATCH | `/api/v1/admin/staffs/{staffId}` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| GET | `/api/v1/admin/wallet` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |
| POST | `/api/v1/admin/wallet/sync` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

## Test

- Pham vi: Endpoint kiểm thử kỹ thuật nội bộ.
- Kiem tra toi thieu: auth dung theo bang, schema request/response hien du trong Swagger, va loi nghiep vu tra ve ro rang neu du lieu khong hop le.

| Method | Path | Auth | Can kiem tra |
| --- | --- | --- | --- |
| GET | `/api/test/secure` | Bearer JWT | Can JWT hop le; thu them case thieu token va sai role/ownership neu phu hop. |

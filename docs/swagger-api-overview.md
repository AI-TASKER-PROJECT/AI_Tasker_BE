# Tong quan Swagger API - AITASKER BE

Tai lieu nay liet ke API theo dung thu tu tag hien thi tren Swagger UI sau khi da sap xep lai theo luong nghiep vu.
Thu tu hien tai duoc khoa trong `OpenApiConfig` va khong con phu thuoc `tags-sorter=alpha`.

- Tong so REST endpoint trong Swagger runtime: **134**.
- Public endpoint: **23**.
- Endpoint can Bearer JWT: **111**.
- Swagger UI mac dinh: `http://localhost:8080/swagger-ui.html`.
- OpenAPI JSON runtime: `http://localhost:8080/v3/api-docs`.

## Nguon su that

- Flow order va tag order: `src/main/java/com/aitasker/be/config/OpenApiConfig.java`.
- Route inventory: `src/main/java/com/aitasker/be/controller/**` va `src/main/java/com/aitasker/be/test_demo/HealthController.java`.
- Public/private route: `src/main/java/com/aitasker/be/security/config/SecurityConfig.java`.

## Auth Flow

- Giai thich flow: Dang ky, dang nhap, OTP email, current session va tra cuu ma so thue.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/auth/check-email` | Public | Kiem tra email da ton tai hay chua. |
| 2 | GET | `/api/auth/me` | Public | Lay current session theo token hien tai. |
| 3 | GET | `/api/auth/tax-check/{mst}` | Public | Tra cuu ma so thue doanh nghiep. |
| 4 | POST | `/api/auth/email/send-otp` | Public | Gui OTP den email. |
| 5 | POST | `/api/auth/email/verify-otp` | Public | Xac minh OTP email. |
| 6 | POST | `/api/auth/google/login` | Public | Dang nhap bang Google credential. |
| 7 | POST | `/api/auth/google/register` | Public | Dang ky/dang nhap Google cho user moi. |
| 8 | POST | `/api/auth/login` | Public | Dang nhap bang email/password de lay JWT. |
| 9 | POST | `/api/auth/refresh` | Public | Dung refresh token con han de cap access token moi khi access token het han. |
| 10 | POST | `/api/auth/register` | Public | Dang ky account moi. |
| 11 | POST | `/api/auth/forgot-password` | Public | Gui email reset link, khong tiet lo email co ton tai. |
| 12 | POST | `/api/auth/reset-password` | Public | Dat lai mat khau bang token, mo khoa account bi lock do sai mat khau. |

## Profile Verification Flow

- Giai thich flow: Business/Expert profile, portfolio, file profile va luong duyet KYC/KYB.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/profiles/business` | Bearer JWT | STAFF lay danh sach business profiles, tra kem `fullName`, `email`, `phone`. |
| 2 | GET | `/api/v1/profiles/business/by-job/{jobId}` | Public | Public lay business profile theo job OPEN, tra kem `fullName`, `email`, `phone`. |
| 3 | GET | `/api/v1/profiles/business/me` | Bearer JWT | BUSINESS lay KYB profile cua minh, tra kem `fullName`, `email`, `phone`. |
| 4 | GET | `/api/v1/profiles/business/{businessId}` | Public | Public lay business profile theo id, tra kem `fullName`, `email`, `phone`. |
| 5 | GET | `/api/v1/profiles/expert` | Bearer JWT | STAFF/BUSINESS lay danh sach expert profiles, tra kem `fullName`, `email`, `phone`, `title`. |
| 6 | GET | `/api/v1/profiles/expert/me` | Bearer JWT | EXPERT lay KYC profile cua minh, tra kem `fullName`, `email`, `phone`, `title`. |
| 7 | GET | `/api/v1/profiles/expert/{expertId}` | Bearer JWT | EXPERT/BUSINESS/STAFF/ADMIN lay expert profile theo id, tra kem `fullName`, `email`, `phone`, `title`. |
| 8 | GET | `/api/v1/profiles/files/view-url` | Bearer JWT | Tao signed/view URL cho file Firebase/storage. |
| 9 | GET | `/api/v1/profiles/portfolio` | Bearer JWT | Lay danh sach portfolio cho operator. |
| 10 | GET | `/api/v1/profiles/portfolio/me` | Bearer JWT | Lay portfolio cua expert dang dang nhap. |
| 11 | POST | `/api/v1/profiles/approve/{type}/{id}` | Bearer JWT | Staff/Admin approve/reject KYB/KYC profile. |
| 12 | POST | `/api/v1/profiles/business` | Bearer JWT | Business tao/cap nhat KYB profile. |
| 13 | POST | `/api/v1/profiles/business/license-file` | Bearer JWT | Upload business license file. |
| 14 | POST | `/api/v1/profiles/expert` | Bearer JWT | Expert tao/cap nhat KYC profile. |
| 15 | POST | `/api/v1/profiles/expert/portfolio-file` | Bearer JWT | Upload portfolio file cua expert. |
| 16 | POST | `/api/v1/profiles/portfolio` | Bearer JWT | Expert tao/cap nhat portfolio structured. |
| 17 | POST | `/api/v1/profiles/portfolio/certificate-file` | Bearer JWT | Upload certificate file cua expert. |

## Job Draft & Publish Flow

- Giai thich flow: Tao draft job, cap nhat SoW, gan taxonomy, xem chi tiet va publish job.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/jobs` | Public | Public list job OPEN tren marketplace. |
| 2 | GET | `/api/v1/jobs/my` | Bearer JWT | Lay job cua Business dang dang nhap. |
| 3 | GET | `/api/v1/jobs/{jobId}` | Public | Lay chi tiet job. |
| 4 | GET | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Lay domain gan voi job. |
| 5 | GET | `/api/v1/jobs/{jobId}/milestones` | Public | Lay milestone public cua job OPEN. |
| 6 | GET | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Lay skill gan voi job. |
| 7 | GET | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Lay technology gan voi job. |
| 8 | PUT | `/api/v1/jobs/{jobId}` | Bearer JWT | Cap nhat draft job, SoW va milestone. |
| 9 | PUT | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Thay the toan bo domain cua job. |
| 10 | PUT | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Thay the toan bo skill assignment cua job. |
| 11 | PUT | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Thay the toan bo technology cua job. |
| 12 | POST | `/api/jobs/generate-sow` | Bearer JWT | Generate SoW, milestone va budget tu requirement tho. |
| 13 | POST | `/api/v1/jobs` | Bearer JWT | Business tao job draft. |
| 14 | POST | `/api/v1/jobs/{jobId}/publish` | Bearer JWT | Publish job sang OPEN. |
| 15 | PATCH | `/api/v1/jobs/{jobId}/status` | Bearer JWT | Cap nhat status job. |

## Proposal Flow

- Giai thich flow: Submit proposal, review proposal, matching, expert candidates va expert recommendations.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | Bearer JWT | Lay/rank candidate expert cho job. |
| 2 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Lay recommendation da luu cho job. |
| 3 | GET | `/api/v1/jobs/{jobId}/matching` | Bearer JWT | Chay matching heuristic/legacy cho job. |
| 4 | GET | `/api/v1/jobs/{jobId}/proposals` | Bearer JWT | Lay proposal cua job cho Business owner/operator. |
| 5 | GET | `/api/v1/proposals/my` | Bearer JWT | Lay proposal cua Expert dang dang nhap. |
| 6 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Generate va luu Top expert recommendations. |
| 7 | POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | Bearer JWT | Business chon expert tu recommendation. |
| 8 | POST | `/api/v1/proposals` | Bearer JWT | Expert submit proposal vao job OPEN. |
| 9 | POST | `/api/v1/proposals/file` | Bearer JWT | Upload file proposal. |
| 10 | PATCH | `/api/v1/proposals/{proposalId}/status` | Bearer JWT | Review proposal Accepted/Rejected. |

## Wallet & Payment Flow

- Giai thich flow: Wallet, quota, membership, credit, PayOS top-up va withdrawal.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/membership/packages` | Bearer JWT | Lay package membership theo role hien tai. |
| 2 | GET | `/api/payments/payos/return` | Public | Public callback-style return URL cua PayOS. |
| 3 | GET | `/api/users/me/quota` | Bearer JWT | Lay source of truth cho quota, package active va Premium. |
| 4 | GET | `/api/v1/admin/withdrawal-requests` | Bearer JWT | Admin lay danh sach withdrawal requests. |
| 5 | GET | `/api/v1/wallet/me` | Bearer JWT | Snapshot vi hien tai cho account dang nhap. |
| 6 | GET | `/api/v1/withdrawal-requests` | Bearer JWT | User lay withdrawal requests cua minh. |
| 7 | GET | `/api/wallet/current` | Bearer JWT | Lay wallet hien tai cua account. |
| 8 | GET | `/api/wallet/transactions` | Bearer JWT | Lấy lịch sử giao dịch ví dạng minh bạch, gồm mã ledger thô và tiêu đề/mô tả tiếng Việt có ngữ cảnh nghiệp vụ. |
| 9 | POST | `/api/credits/job-post/purchase` | Bearer JWT | Mua job-post credits bang wallet. |
| 10 | POST | `/api/credits/proposal/purchase` | Bearer JWT | Mua proposal credits bang wallet. |
| 11 | POST | `/api/membership/packages/{packageId}/purchase` | Bearer JWT | Mua membership bang wallet. |
| 12 | POST | `/api/payments/payos/create` | Bearer JWT | Tao payment order PayOS de nap vi. |
| 13 | POST | `/api/payments/payos/{orderCode}/sync` | Bearer JWT | Chu dong sync trang thai PayOS theo order code. |
| 14 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Bearer JWT | Admin approve withdrawal. |
| 15 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Bearer JWT | Admin reject withdrawal. |
| 16 | POST | `/api/v1/withdrawal-requests` | Bearer JWT | Tao withdrawal request. |

## Contract Execution Flow

- Giai thich flow: Contract, milestone, deliverable, dispute, deposit va transaction.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/contracts` | Bearer JWT | Lay danh sach contract ma user hien tai duoc phep xem. |
| 2 | GET | `/api/v1/contracts/{contractId}` | Bearer JWT | Lay chi tiet contract. |
| 3 | GET | `/api/v1/contracts/{contractId}/disputes` | Bearer JWT | Lay dispute cua contract. |
| 4 | GET | `/api/v1/contracts/{contractId}/milestones` | Bearer JWT | Lay milestone snapshot/live view cua contract. |
| 5 | GET | `/api/v1/disputes/{disputeId}` | Bearer JWT | Lay chi tiet dispute. |
| 6 | GET | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT | Lay acceptance criteria cua milestone. |
| 7 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | Bearer JWT | Lay deliverable cua milestone. |
| 8 | GET | `/api/v1/milestones/{milestoneId}/transactions` | Bearer JWT | Lay transaction legacy cua milestone. |
| 9 | POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Bearer JWT | Admin refund contract deposit. |
| 10 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | Bearer JWT | Tao contract draft tu proposal da accepted. |
| 11 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | Bearer JWT | Business thanh toan deposit cho contract. |
| 12 | POST | `/api/v1/contracts/{contractId}/nda-sign` | Bearer JWT | Ky NDA cho contract. |
| 13 | POST | `/api/v1/contracts/{contractId}/reject` | Bearer JWT | Expert reject contract draft/pending. |
| 14 | POST | `/api/v1/contracts/{contractId}/sign` | Bearer JWT | Ky hop dong cho business hoac expert. |
| 15 | POST | `/api/v1/contracts/{contractId}/terminate` | Bearer JWT | Ket thuc contract co ly do. |
| 16 | POST | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT | Business them acceptance criteria cho milestone. |
| 17 | POST | `/api/v1/deliverables` | Bearer JWT | Expert submit deliverable cho milestone. |
| 18 | POST | `/api/v1/disputes` | Bearer JWT | Tao dispute cho contract/milestone. |
| 19 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | Bearer JWT | Ghi nhan ket qua demo testing cho dispute. |
| 20 | POST | `/api/v1/disputes/{disputeId}/technical-report` | Bearer JWT | Staff ghi technical report cho dispute. |
| 21 | POST | `/api/v1/milestones` | Bearer JWT | Tao milestone. |
| 22 | POST | `/api/v1/milestones/sla-auto-approve` | Bearer JWT | Chay SLA auto approve dang manual simulation. |
| 23 | POST | `/api/v1/milestones/{milestoneId}/complete` | Bearer JWT | Business complete milestone. |
| 24 | POST | `/api/v1/transactions` | Bearer JWT | Tao transaction legacy cho milestone. |
| 25 | POST | `/api/v1/transactions/{transactionId}/webhook` | Bearer JWT | Simulation webhook transaction legacy. |
| 26 | PATCH | `/api/v1/disputes/{disputeId}/assign` | Bearer JWT | Gan dispute cho staff. |
| 27 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | Bearer JWT | Resolve dispute bang proposed action. |
| 28 | PATCH | `/api/v1/milestones/{milestoneId}` | Bearer JWT | Cap nhat milestone. |
| 29 | PATCH | `/api/v1/transactions/{transactionId}/status` | Bearer JWT | Cap nhat status transaction legacy. |
| 30 | PUT | `/api/v1/milestones/{milestoneId}/criteria/{criteriaId}` | Bearer JWT | Business sua acceptance criteria cua milestone. |
| 31 | DELETE | `/api/v1/milestones/{milestoneId}/criteria/{criteriaId}` | Bearer JWT | Business xoa acceptance criteria cua milestone. |

## Notification Flow

- Giai thich flow: Thong bao va trang thai da doc.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/notifications` | Bearer JWT | Lay danh sach notification cua user hien tai. |
| 2 | GET | `/api/v1/notifications/unread-count` | Bearer JWT | Dem notification chua doc. |
| 3 | PATCH | `/api/v1/notifications/read-all` | Bearer JWT | Danh dau tat ca notification da doc. |
| 4 | PATCH | `/api/v1/notifications/{notificationId}/read` | Bearer JWT | Danh dau mot notification da doc. |

## Catalog & Reference Flow

- Giai thich flow: Domain, skill va technology.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/domains` | Public | Lay danh muc domain. |
| 2 | GET | `/api/v1/skills` | Public | Lay danh muc skill. |
| 3 | GET | `/api/v1/technologies` | Bearer JWT | Lay danh muc technology. |
| 4 | POST | `/api/v1/domains` | Bearer JWT | Tao domain moi. |
| 5 | POST | `/api/v1/skills` | Bearer JWT | Tao skill moi. |
| 6 | POST | `/api/v1/technologies` | Bearer JWT | Tao technology moi. |
| 7 | PATCH | `/api/v1/domains/{domainId}` | Bearer JWT | Cap nhat domain. |
| 8 | PATCH | `/api/v1/skills/{skillId}` | Bearer JWT | Cap nhat skill. |
| 9 | PATCH | `/api/v1/technologies/{technologyId}` | Bearer JWT | Cap nhat technology. |

## AI & Matching Flow

- Giai thich flow: Chatbot va cac endpoint AI/phu tro.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/chatbot/ask` | Public | Gui cau hoi vao chatbot/RAG. |

## Admin & Governance Flow

- Giai thich flow: Quan tri account, staff, settings, analytics, audit, review va wallet he thong.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/admin/accounts` | Bearer JWT | Lay danh sach account cho man hinh quan tri. |
| 2 | GET | `/api/v1/admin/analytics/overview` | Bearer JWT | Lay so lieu tong quan cho dashboard admin. |
| 3 | GET | `/api/v1/admin/audit-logs` | Bearer JWT | Lay audit log, co the loc theo nhom actor. |
| 4 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | Bearer JWT | Lay review theo contract. |
| 5 | GET | `/api/v1/admin/settings` | Bearer JWT | Lay danh sach system settings. |
| 6 | GET | `/api/v1/admin/staffs` | Bearer JWT | Lay danh sach staff. |
| 7 | GET | `/api/v1/admin/wallet` | Bearer JWT | Lay system wallet cho admin. |
| 8 | GET | `/api/v1/admin/wallet/transactions` | Bearer JWT | Admin lay lich su giao dich vi nen tang minh bach bang tieng Viet co dau, gom thong tin goi, credit, ky quy, rut tien, nap vi va ID doi soat. |
| 9 | POST | `/api/v1/admin/accounts` | Bearer JWT | Tao account moi tu trang quan tri. |
| 10 | POST | `/api/v1/admin/reviews` | Bearer JWT | Tao review cho contract. |
| 11 | POST | `/api/v1/admin/staffs` | Bearer JWT | Tao staff profile gan voi account. |
| 12 | POST | `/api/v1/admin/wallet/sync` | Bearer JWT | Dong bo/lazy-create system wallet. |
| 13 | DELETE | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Vo hieu hoa account theo `accountId`. |
| 14 | PATCH | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Cap nhat thong tin account. |
| 15 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | Bearer JWT | Bat/tat trang thai active cua account. |
| 16 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | Bearer JWT | Cap nhat status account bang query param. |
| 17 | PATCH | `/api/v1/admin/settings/{key}` | Bearer JWT | Cap nhat value hoac trang thai active cua system setting. |
| 18 | PATCH | `/api/v1/admin/staffs/{staffId}` | Bearer JWT | Cap nhat ho so staff. |

## System & Test Flow

- Giai thich flow: Health check va endpoint test ky thuat.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/health` | Public | Health check backend. |
| 2 | GET | `/api/test/secure` | Bearer JWT | Endpoint smoke test security. |

## Luu y nghiep vu

- `GET /api/auth/me` dang nam trong Auth Flow; du SecurityConfig permitAll theo pattern `/api/auth/**`, khi test current session van nen gan token de co du lieu user.
- `GET /api/v1/jobs/{jobId}/milestones` duoc dat trong Job Draft & Publish Flow vi no phuc vu public job detail/open job testing, khong phai luong milestone contract private.
- `GET /api/v1/jobs/{jobId}/matching`, `GET /api/jobs/{jobPostingId}/expert-candidates` va `POST/GET /api/jobs/{jobPostingId}/expert-recommendations` duoc dua vao Proposal Flow vi chung phuc vu viec tim/chon expert va xu ly proposal.
- Notification, catalog, AI, admin va system/test duoc day xuong cuoi Swagger de khong pha vo 6 flow nghiep vu chinh.

# Swagger API Overview - AITASKER BE

File này liệt kê các REST API đang xuất hiện trong Swagger hiện tại của backend.

Thứ tự bên dưới được sắp theo cách Swagger UI đang hiển thị:

- Nhóm controller/tag theo alphabet, do cấu hình `springdoc.swagger-ui.tags-sorter=alpha`.
- API trong từng controller được sắp theo HTTP method, do cấu hình `springdoc.swagger-ui.operations-sorter=method`.
- Với các API cùng method, giữ theo thứ tự Swagger runtime trả về trong `GET /v3/api-docs`.

Lưu ý: WebSocket/STOMP không xuất hiện trong Swagger vì không phải REST API.

## admin-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 1 | DELETE | `/api/v1/admin/accounts/{accountId}` | Admin khóa account bằng cách đưa account về trạng thái Lock. | Quản lý tài khoản hệ thống. |
| 2 | GET | `/api/v1/admin/staffs` | Admin lấy danh sách staff. | Quản lý nhân sự nội bộ. |
| 3 | GET | `/api/v1/admin/accounts` | Admin lấy danh sách account. | Quản lý tài khoản hệ thống. |
| 4 | GET | `/api/v1/admin/wallet` | Admin xem ví hệ thống. | Finance admin, system wallet. |
| 5 | GET | `/api/v1/admin/settings` | Admin xem cấu hình hệ thống. | System settings. |
| 6 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | Xem review theo contract. | Đánh giá sau hợp đồng. |
| 7 | GET | `/api/v1/admin/audit-logs` | Admin xem audit log. | Audit log, kiểm tra thao tác hệ thống. |
| 8 | GET | `/api/v1/admin/analytics/overview` | Admin xem số liệu tổng quan. | Dashboard admin. |
| 9 | PATCH | `/api/v1/admin/staffs/{staffId}` | Admin cập nhật thông tin staff. | Quản lý nhân sự nội bộ. |
| 10 | PATCH | `/api/v1/admin/settings/{key}` | Admin cập nhật system setting theo key. | Cấu hình hệ thống. |
| 11 | PATCH | `/api/v1/admin/accounts/{accountId}` | Admin cập nhật thông tin account. | Quản lý tài khoản hệ thống. |
| 12 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | Admin đổi status account: Pending, Approved, Rejected, Lock. | Duyệt, từ chối hoặc khóa account. |
| 13 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | Admin bật hoặc khóa nhanh account. | Quản lý trạng thái account. |
| 14 | POST | `/api/v1/admin/wallet/sync` | Admin đồng bộ ví hệ thống. | Finance admin. |
| 15 | POST | `/api/v1/admin/staffs` | Admin tạo staff mới. | Quản lý nhân sự nội bộ. |
| 16 | POST | `/api/v1/admin/reviews` | Tạo review sau contract. | Đánh giá sau dự án. |
| 17 | POST | `/api/v1/admin/accounts` | Admin tạo account. | Quản lý tài khoản hệ thống. |

## auth-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 18 | GET | `/api/auth/me` | Lấy thông tin session hiện tại từ access token. | Auth session, reload trạng thái user. |
| 19 | GET | `/api/auth/check-email` | Kiểm tra email đã tồn tại chưa. | Register validation. |
| 20 | POST | `/api/auth/register` | Đăng ký tài khoản mới sau khi xác thực OTP. | Authentication, registration. |
| 21 | POST | `/api/auth/login` | Đăng nhập và nhận accessToken/refreshToken. | Authentication, login. |

## catalog-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 22 | GET | `/api/v1/jobs/{jobId}/technologies` | Xem danh sách công nghệ đã gán cho job. | Job posting, phân loại công nghệ. |
| 23 | GET | `/api/v1/jobs/{jobId}/skills` | Xem danh sách kỹ năng yêu cầu của job. | Job posting, matching chuyên gia. |
| 24 | GET | `/api/v1/jobs/{jobId}/domains` | Xem danh sách lĩnh vực của job. | Job posting, phân loại lĩnh vực. |
| 25 | GET | `/api/v1/technologies` | Lấy danh mục công nghệ của hệ thống. | Catalog technology cho job/portfolio. |
| 26 | GET | `/api/v1/skills` | Lấy danh mục kỹ năng của hệ thống. | Catalog skill cho job/portfolio. |
| 27 | GET | `/api/v1/domains` | Lấy danh mục lĩnh vực của hệ thống. | Catalog domain cho job/portfolio. |
| 28 | GET | `/api/v1/acceptance-criteria` | Lấy danh mục tiêu chí nghiệm thu nền tảng. | Milestone acceptance criteria. |
| 29 | PATCH | `/api/v1/technologies/{technologyId}` | Admin cập nhật công nghệ. | Quản trị catalog technology. |
| 30 | PATCH | `/api/v1/skills/{skillId}` | Admin cập nhật kỹ năng. | Quản trị catalog skill. |
| 31 | PATCH | `/api/v1/domains/{domainId}` | Admin cập nhật lĩnh vực. | Quản trị catalog domain. |
| 32 | POST | `/api/v1/technologies` | Admin tạo công nghệ mới. | Quản trị catalog technology. |
| 33 | POST | `/api/v1/skills` | Admin tạo kỹ năng mới. | Quản trị catalog skill. |
| 34 | POST | `/api/v1/domains` | Admin tạo lĩnh vực mới. | Quản trị catalog domain. |
| 35 | PUT | `/api/v1/jobs/{jobId}/technologies` | Thay danh sách công nghệ của job. | Business/Admin chỉnh metadata job. |
| 36 | PUT | `/api/v1/jobs/{jobId}/skills` | Thay danh sách kỹ năng yêu cầu của job. | Business/Admin chỉnh yêu cầu kỹ năng. |
| 37 | PUT | `/api/v1/jobs/{jobId}/domains` | Thay danh sách lĩnh vực của job. | Business/Admin chỉnh lĩnh vực job. |

## chatbot-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 38 | POST | `/api/chatbot/ask` | Gửi câu hỏi cho chatbot. | Chatbot hỗ trợ người dùng. |

## contract-execution-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 39 | GET | `/api/v1/milestones/{milestoneId}/transactions` | Xem giao dịch theo milestone. | Finance theo milestone. |
| 40 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | Xem deliverable theo milestone. | Theo dõi bàn giao. |
| 41 | GET | `/api/v1/milestones/{milestoneId}/criteria` | Xem tiêu chí nghiệm thu của milestone. | Acceptance review. |
| 42 | GET | `/api/v1/jobs/{jobId}/milestones` | Xem milestone của job. | Job detail, contract draft. |
| 43 | GET | `/api/v1/jobs/{jobId}/matching` | Lấy danh sách matching theo keyword. | AI/matching chuyên gia mức MVP. |
| 44 | GET | `/api/v1/disputes/{disputeId}` | Xem chi tiết dispute. | Dispute detail. |
| 45 | GET | `/api/v1/contracts` | Xem danh sách contract theo quyền. | Contract management. |
| 46 | GET | `/api/v1/contracts/{contractId}` | Xem chi tiết contract kèm contract milestones. | Contract detail, ký hợp đồng. |
| 47 | GET | `/api/v1/contracts/{contractId}/milestones` | Xem milestone đã chốt của contract. | Contract milestone, ngân sách final. |
| 48 | GET | `/api/v1/contracts/{contractId}/disputes` | Xem dispute của contract. | Theo dõi tranh chấp hợp đồng. |
| 49 | PATCH | `/api/v1/transactions/{transactionId}/status` | Cập nhật trạng thái giao dịch. | Finance transaction update. |
| 50 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | Admin xử lý/kết luận dispute. | Dispute resolution. |
| 51 | PATCH | `/api/v1/disputes/{disputeId}/assign` | Admin gán dispute cho staff. | Điều phối xử lý tranh chấp. |
| 52 | POST | `/api/v1/transactions` | Tạo giao dịch deposit, payout hoặc refund theo quyền. | Finance, escrow, thanh toán milestone. |
| 53 | POST | `/api/v1/transactions/{transactionId}/webhook` | Mô phỏng webhook cập nhật trạng thái giao dịch. | Payment webhook, cập nhật thanh toán. |
| 54 | POST | `/api/v1/milestones` | Tạo milestone thủ công cho job/contract. | Thiết lập giai đoạn dự án. |
| 55 | POST | `/api/v1/milestones/sla-auto-approve` | Chạy mô phỏng tự động duyệt milestone quá hạn SLA. | SLA, nghiệm thu tự động. |
| 56 | POST | `/api/v1/disputes` | Tạo tranh chấp cho contract/milestone. | Dispute management. |
| 57 | POST | `/api/v1/disputes/{disputeId}/technical-report` | Staff/Admin ghi báo cáo kỹ thuật cho dispute. | Xử lý tranh chấp. |
| 58 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | Ghi kết quả test demo trong dispute. | Kiểm tra nghiệm thu khi dispute. |
| 59 | POST | `/api/v1/deliverables` | Expert nộp sản phẩm bàn giao cho milestone. | Contract execution, deliverable. |
| 60 | POST | `/api/v1/criteria` | Tạo tiêu chí nghiệm thu cho milestone. | Acceptance criteria. |
| 61 | POST | `/api/v1/contracts/{contractId}/terminate` | Chấm dứt contract theo quyền. | Contract termination. |
| 62 | POST | `/api/v1/contracts/{contractId}/sign` | Business/Expert ký xác nhận hợp đồng. | Ký hợp đồng, kích hoạt khi đủ điều kiện. |
| 63 | POST | `/api/v1/contracts/{contractId}/nda-sign` | Business/Expert đồng ý NDA. | Ký thỏa thuận bảo mật. |
| 64 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | Business tạo contract draft từ proposal đã accepted. | Contract draft từ job và proposal. |
| 65 | POST | `/api/v1/contracts/{contractId}/reject` | Expert tu choi contract DRAFT/PENDING. | Huy contract va dua job ve OPEN. |
| 67 | POST | `/api/v1/milestones/{milestoneId}/complete` | Business hoan tat milestone dang review. | Nghiem thu milestone, tu hoan tat contract khi du dieu kien. |
| 68 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | Business tra 20% ky quy hop dong tu wallet. | Contract deposit, bat dau execution. |
| 69 | POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Admin xu ly hoan/resolution ky quy hop dong. | Contract deposit refund, close contract. |

## credit-controller

| STT | Method | API | API dung de lam gi | Phuc vu chuc nang |
| --- | --- | --- | --- | --- |
| 70 | POST | `/api/credits/job-post/purchase` | Business mua job-post credits bang wallet. | Job publishing quota. |
| 71 | POST | `/api/credits/proposal/purchase` | Expert mua proposal credits bang wallet. | Proposal quota. |

## membership-controller

| STT | Method | API | API dung de lam gi | Phuc vu chuc nang |
| --- | --- | --- | --- | --- |
| 72 | GET | `/api/membership/packages` | Business/Expert xem package phu hop role. | Membership package. |
| 73 | POST | `/api/membership/packages/{packageId}/purchase` | Business/Expert mua package bang wallet. | Membership, badge, quota. |

## user-quota-controller

| STT | Method | API | API dung de lam gi | Phuc vu chuc nang |
| --- | --- | --- | --- | --- |
| 74 | GET | `/api/users/me/quota` | Business/Expert xem quota hien tai. | Job/proposal quota. |

## withdrawal-controller

| STT | Method | API | API dung de lam gi | Phuc vu chuc nang |
| --- | --- | --- | --- | --- |
| 75 | GET | `/api/v1/withdrawal-requests` | User xem withdrawal requests cua minh. | Withdrawal. |
| 76 | GET | `/api/v1/admin/withdrawal-requests` | Admin xem withdrawal requests. | Withdrawal review. |
| 77 | POST | `/api/v1/withdrawal-requests` | User tao withdrawal request va move available sang holding. | Withdrawal. |
| 78 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Admin approve sau khi chuyen khoan thu cong. | Withdrawal review. |
| 79 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Admin reject va tra holding ve available. | Withdrawal review. |

## email-otp-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 66 | POST | `/api/auth/email/verify-otp` | Xác thực OTP email. | Email OTP verification. |
| 67 | POST | `/api/auth/email/send-otp` | Gửi OTP về email. | Email OTP registration flow. |

## health-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 68 | GET | `/api/health` | Kiểm tra backend còn sống. | Health check. |

## marketplace-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 69 | GET | `/api/v1/jobs` | Lấy danh sách job public đang OPEN. | Marketplace cho expert xem job. |
| 70 | GET | `/api/v1/proposals/my` | Expert xem proposal đã gửi. | Proposal của chuyên gia. |
| 71 | GET | `/api/v1/jobs/{jobId}` | Xem chi tiết job. | Job detail. |
| 72 | GET | `/api/v1/jobs/{jobId}/proposals` | Business xem proposal của một job. | Proposal management theo job. |
| 73 | GET | `/api/v1/jobs/my` | Business xem job của mình, gồm draft/open/closed. | Quản lý job doanh nghiệp. |
| 74 | PATCH | `/api/v1/proposals/{proposalId}/status` | Business duyệt hoặc từ chối proposal. | Proposal review. |
| 75 | PATCH | `/api/v1/jobs/{jobId}/status` | Business đổi trạng thái job, ví dụ DRAFT sang OPEN. | Job lifecycle. |
| 76 | POST | `/api/v1/proposals` | Expert gửi proposal cho job đang mở. | Proposal, ứng tuyển dự án. |
| 77 | POST | `/api/v1/proposals/file` | Upload file proposal lên Firebase Storage. | Lưu file proposal PDF/DOCX của expert. |
| 78 | POST | `/api/v1/jobs` | Business tạo job draft, kèm SoW, domain, skill, technology, milestone. | Job posting. |

| 78a | POST | `/api/v1/jobs/{jobId}/publish` | Business publish job co SoW va consume 1 job-post credit. | Job publishing quota. |

## notification-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 79 | GET | `/api/v1/notifications` | Lấy danh sách thông báo của tài khoản hiện tại. | Notification center. |
| 80 | GET | `/api/v1/notifications/unread-count` | Đếm thông báo chưa đọc. | Notification badge. |
| 81 | PATCH | `/api/v1/notifications/{notificationId}/read` | Đánh dấu một thông báo đã đọc. | Notification. |
| 82 | PATCH | `/api/v1/notifications/read-all` | Đánh dấu tất cả thông báo đã đọc. | Notification. |

## profile-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 83 | GET | `/api/v1/profiles/portfolio` | Staff/Business xem portfolio expert theo quyền. | Review năng lực chuyên gia. |
| 84 | GET | `/api/v1/profiles/expert` | Lấy danh sách hồ sơ expert theo quyền. | Staff/Business xem hồ sơ chuyên gia. |
| 85 | GET | `/api/v1/profiles/business` | Lấy danh sách hồ sơ business theo quyền. | Staff/Admin xem hồ sơ doanh nghiệp. |
| 86 | GET | `/api/v1/profiles/portfolio/me` | Expert xem portfolio của mình. | Hồ sơ năng lực cá nhân. |
| 87 | GET | `/api/v1/profiles/files/view-url` | Lấy signed URL để xem file Firebase. | Xem chứng chỉ, giấy phép, proposal file. |
| 88 | GET | `/api/v1/profiles/expert/me` | Expert xem hồ sơ KYC của mình. | Hồ sơ chuyên gia cá nhân. |
| 89 | GET | `/api/v1/profiles/business/me` | Business xem hồ sơ KYB của mình. | Hồ sơ doanh nghiệp cá nhân. |
| 90 | GET | `/api/v1/profiles/business/by-job/{jobId}` | Xem thông tin business theo job. | Expert xem chi tiết doanh nghiệp của job. |
| 91 | POST | `/api/v1/profiles/portfolio` | Expert tạo hoặc cập nhật portfolio. | Hồ sơ năng lực AI của chuyên gia. |
| 92 | POST | `/api/v1/profiles/portfolio/certificate-file` | Upload file chứng chỉ portfolio lên Firebase Storage. | Lưu file chứng chỉ chuyên gia. |
| 93 | POST | `/api/v1/profiles/expert` | Expert tạo hoặc cập nhật hồ sơ KYC. | Hồ sơ xác minh chuyên gia. |
| 94 | POST | `/api/v1/profiles/business` | Business tạo hoặc cập nhật hồ sơ KYB. | Hồ sơ xác minh doanh nghiệp. |
| 95 | POST | `/api/v1/profiles/business/license-file` | Upload giấy phép kinh doanh lên Firebase Storage. | Lưu file giấy phép doanh nghiệp. |
| 96 | POST | `/api/v1/profiles/approve/{type}/{id}` | Staff duyệt hoặc từ chối hồ sơ BUSINESS/EXPERT. | KYB/KYC approval. |

## SoW Generation

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 97 | POST | `/api/jobs/generate-sow` | AI generate cấu trúc SoW từ yêu cầu dự án. | AI hỗ trợ tạo job/SoW. |

## tax-check-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 98 | GET | `/api/auth/tax-check/{mst}` | Kiểm tra mã số thuế doanh nghiệp. | Register/KYB business. |

## test-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 99 | GET | `/api/test/secure` | Test endpoint cần xác thực. | Kiểm tra bảo mật/JWT. |

## wallet-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 100 | GET | `/api/v1/wallet/me` | Người dùng xem ví của mình. | Finance, user wallet. |
| 101 | GET | `/api/wallet/current` | User xem wallet hien tai theo spec payment. | Finance, user wallet. |
| 102 | GET | `/api/wallet/transactions` | User xem wallet transaction history. | Wallet ledger. |

## Nhận xét kiểm tra Swagger

- Các REST API trong các flow chính hiện đều có trong Swagger vì Springdoc tự quét controller.
- API upload file Firebase có trong Swagger: `business/license-file`, `portfolio/certificate-file`, `proposals/file`.
- API contract mới `/api/v1/contracts/{contractId}/sign` đã có trong Swagger; endpoint cũ `/activate` không còn trong Swagger runtime.
- Khi du chu ky Contract va NDA, backend chuyen contract sang `PENDING`; business tra 20% deposit thi contract moi `ACTIVE` va job sang `IN_PROGRESS`.
- WebSocket realtime notification không nằm trong Swagger, cần test bằng WebSocket/STOMP riêng.

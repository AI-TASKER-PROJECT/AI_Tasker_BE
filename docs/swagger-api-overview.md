# Swagger API Overview - AITASKER BE

File này liệt kê các REST API đang xuất hiện trong Swagger hiện tại của backend.
Thứ tự bên dưới bám theo Swagger UI: nhóm controller/tag theo alphabet, API trong từng nhóm theo HTTP method, các API cùng method giữ theo thứ tự runtime của `GET /v3/api-docs`.
Lưu ý: WebSocket/STOMP không xuất hiện trong Swagger vì không phải REST API.

## admin-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 1 | DELETE | `/api/v1/admin/accounts/{accountId}` | Xóa, khóa hoặc vô hiệu hóa dữ liệu theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 2 | GET | `/api/v1/admin/staffs` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 3 | GET | `/api/v1/admin/accounts` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 4 | GET | `/api/v1/admin/wallet` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 5 | GET | `/api/v1/admin/settings` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 6 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 7 | GET | `/api/v1/admin/audit-logs` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 8 | GET | `/api/v1/admin/analytics/overview` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 9 | PATCH | `/api/v1/admin/staffs/{staffId}` | Cập nhật một phần dữ liệu theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 10 | PATCH | `/api/v1/admin/settings/{key}` | Cập nhật một phần dữ liệu theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 11 | PATCH | `/api/v1/admin/accounts/{accountId}` | Cập nhật một phần dữ liệu theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 12 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | Cập nhật một phần dữ liệu theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 13 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | Cập nhật một phần dữ liệu theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 14 | POST | `/api/v1/admin/wallet/sync` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 15 | POST | `/api/v1/admin/staffs` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 16 | POST | `/api/v1/admin/reviews` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |
| 17 | POST | `/api/v1/admin/accounts` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Quản lý admin, account, staff, setting, audit log và ví hệ thống. |

## auth-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 18 | GET | `/api/auth/me` | Lấy thông tin của account đang đăng nhập. | Xác thực, đăng nhập, đăng ký và session. |
| 19 | GET | `/api/auth/check-email` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Xác thực, đăng nhập, đăng ký và session. |
| 20 | POST | `/api/auth/register` | Đăng ký tài khoản sau khi email đã xác thực OTP. | Xác thực, đăng nhập, đăng ký và session. |
| 21 | POST | `/api/auth/login` | Đăng nhập và nhận accessToken/refreshToken. | Xác thực, đăng nhập, đăng ký và session. |

## catalog-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 22 | GET | `/api/v1/jobs/{jobId}/technologies` | Xem, tạo hoặc cập nhật dữ liệu job. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 23 | GET | `/api/v1/jobs/{jobId}/skills` | Xem, tạo hoặc cập nhật dữ liệu job. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 24 | GET | `/api/v1/jobs/{jobId}/domains` | Xem, tạo hoặc cập nhật dữ liệu job. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 25 | GET | `/api/v1/technologies` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 26 | GET | `/api/v1/skills` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 27 | GET | `/api/v1/domains` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 28 | GET | `/api/v1/acceptance-criteria` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 29 | PATCH | `/api/v1/technologies/{technologyId}` | Cập nhật một phần dữ liệu theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 30 | PATCH | `/api/v1/skills/{skillId}` | Cập nhật một phần dữ liệu theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 31 | PATCH | `/api/v1/domains/{domainId}` | Cập nhật một phần dữ liệu theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 32 | POST | `/api/v1/technologies` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 33 | POST | `/api/v1/skills` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 34 | POST | `/api/v1/domains` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 35 | PUT | `/api/v1/jobs/{jobId}/technologies` | Xem, tạo hoặc cập nhật dữ liệu job. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 36 | PUT | `/api/v1/jobs/{jobId}/skills` | Xem, tạo hoặc cập nhật dữ liệu job. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |
| 37 | PUT | `/api/v1/jobs/{jobId}/domains` | Xem, tạo hoặc cập nhật dữ liệu job. | Danh mục lĩnh vực, kỹ năng, công nghệ và tiêu chí nghiệm thu. |

## chatbot-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 38 | POST | `/api/chatbot/ask` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Chatbot hỗ trợ người dùng. |

## contract-execution-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 39 | GET | `/api/v1/milestones/{milestoneId}/transactions` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 40 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 41 | GET | `/api/v1/milestones/{milestoneId}/criteria` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 42 | GET | `/api/v1/jobs/{jobId}/milestones` | Xem, tạo hoặc cập nhật dữ liệu job. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 43 | GET | `/api/v1/jobs/{jobId}/matching` | Xem, tạo hoặc cập nhật dữ liệu job. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 44 | GET | `/api/v1/disputes/{disputeId}` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 45 | GET | `/api/v1/contracts` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 46 | GET | `/api/v1/contracts/{contractId}` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 47 | GET | `/api/v1/contracts/{contractId}/milestones` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 48 | GET | `/api/v1/contracts/{contractId}/disputes` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 49 | PATCH | `/api/v1/transactions/{transactionId}/status` | Cập nhật một phần dữ liệu theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 50 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | Cập nhật một phần dữ liệu theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 51 | PATCH | `/api/v1/disputes/{disputeId}/assign` | Cập nhật một phần dữ liệu theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 52 | POST | `/api/v1/transactions` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 53 | POST | `/api/v1/transactions/{transactionId}/webhook` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 54 | POST | `/api/v1/milestones` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 55 | POST | `/api/v1/milestones/sla-auto-approve` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 56 | POST | `/api/v1/disputes` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 57 | POST | `/api/v1/disputes/{disputeId}/technical-report` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 58 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 59 | POST | `/api/v1/deliverables` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 60 | POST | `/api/v1/criteria` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 61 | POST | `/api/v1/contracts/{contractId}/terminate` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 62 | POST | `/api/v1/contracts/{contractId}/sign` | Ký xác nhận hợp đồng hoặc NDA. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 63 | POST | `/api/v1/contracts/{contractId}/nda-sign` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 64 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | Tạo hợp đồng nháp từ proposal đã được chấp nhận. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |
| 65 | POST | `/api/v1/contracts/change-requests` | Tạo mới dữ liệu hoặc thực hiện hành động theo endpoint này. | Hợp đồng, milestone, deliverable, tranh chấp và giao dịch. |

| 66 | POST | `/api/v1/contracts/{contractId}/reject` | Expert tu choi contract Draft/Negotiating. | Huy contract va dua job ve proposal review. |
| 67 | POST | `/api/v1/milestones/{milestoneId}/complete` | Business hoan tat milestone dang review. | Nghiem thu milestone, tu hoan tat contract khi du dieu kien. |

## email-otp-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 66 | POST | `/api/auth/email/verify-otp` | Xác thực OTP email. | Gửi và xác thực OTP email. |
| 67 | POST | `/api/auth/email/send-otp` | Gửi OTP xác thực email. | Gửi và xác thực OTP email. |

## Expert Candidates

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 68 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | Lấy danh sách ứng viên chuyên gia phù hợp với job. | AI đề xuất và lọc chuyên gia phù hợp với job. |

## Expert Recommendations

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 69 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Sinh hoặc xem danh sách chuyên gia AI đề xuất cho job. | AI đề xuất và lọc chuyên gia phù hợp với job. |
| 70 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Sinh hoặc xem danh sách chuyên gia AI đề xuất cho job. | AI đề xuất và lọc chuyên gia phù hợp với job. |

## health-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 71 | GET | `/api/health` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Kiểm tra trạng thái backend. |

## marketplace-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 72 | GET | `/api/v1/jobs` | Xem, tạo hoặc cập nhật dữ liệu job. | Luồng job posting, job public, proposal và review proposal. |
| 73 | GET | `/api/v1/proposals/my` | Gửi, xem hoặc duyệt proposal. | Luồng job posting, job public, proposal và review proposal. |
| 74 | GET | `/api/v1/jobs/{jobId}` | Xem, tạo hoặc cập nhật dữ liệu job. | Luồng job posting, job public, proposal và review proposal. |
| 75 | GET | `/api/v1/jobs/{jobId}/proposals` | Gửi, xem hoặc duyệt proposal. | Luồng job posting, job public, proposal và review proposal. |
| 76 | GET | `/api/v1/jobs/my` | Xem, tạo hoặc cập nhật dữ liệu job. | Luồng job posting, job public, proposal và review proposal. |
| 77 | PATCH | `/api/v1/proposals/{proposalId}/status` | Gửi, xem hoặc duyệt proposal. | Luồng job posting, job public, proposal và review proposal. |
| 78 | PATCH | `/api/v1/jobs/{jobId}/status` | Xem, tạo hoặc cập nhật dữ liệu job. | Luồng job posting, job public, proposal và review proposal. |
| 79 | POST | `/api/v1/proposals` | Gửi, xem hoặc duyệt proposal. | Luồng job posting, job public, proposal và review proposal. |
| 80 | POST | `/api/v1/proposals/file` | Upload file hoặc lấy URL xem file. | Luồng job posting, job public, proposal và review proposal. |
| 81 | POST | `/api/v1/jobs` | Xem, tạo hoặc cập nhật dữ liệu job. | Luồng job posting, job public, proposal và review proposal. |

## notification-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 82 | GET | `/api/v1/notifications` | Lấy hoặc cập nhật trạng thái thông báo. | Thông báo realtime và trạng thái đã đọc. |
| 83 | GET | `/api/v1/notifications/unread-count` | Lấy hoặc cập nhật trạng thái thông báo. | Thông báo realtime và trạng thái đã đọc. |
| 84 | PATCH | `/api/v1/notifications/{notificationId}/read` | Lấy hoặc cập nhật trạng thái thông báo. | Thông báo realtime và trạng thái đã đọc. |
| 85 | PATCH | `/api/v1/notifications/read-all` | Lấy hoặc cập nhật trạng thái thông báo. | Thông báo realtime và trạng thái đã đọc. |

## pay-os-payment-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 86 | GET | `/api/payments/payos/return` | Tạo, đồng bộ hoặc nhận callback thanh toán. | Thanh toán PayOS và nạp ví. |
| 87 | POST | `/api/payments/payos/{orderCode}/sync` | Tạo, đồng bộ hoặc nhận callback thanh toán. | Thanh toán PayOS và nạp ví. |
| 88 | POST | `/api/payments/payos/webhook` | Tạo, đồng bộ hoặc nhận callback thanh toán. | Thanh toán PayOS và nạp ví. |
| 89 | POST | `/api/payments/payos/create` | Tạo, đồng bộ hoặc nhận callback thanh toán. | Thanh toán PayOS và nạp ví. |

## profile-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 90 | GET | `/api/v1/profiles/portfolio` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 91 | GET | `/api/v1/profiles/expert` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 92 | GET | `/api/v1/profiles/business` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 93 | GET | `/api/v1/profiles/portfolio/me` | Lấy thông tin của account đang đăng nhập. | Hồ sơ business, expert, portfolio và file Firebase. |
| 94 | GET | `/api/v1/profiles/files/view-url` | Upload file hoặc lấy URL xem file. | Hồ sơ business, expert, portfolio và file Firebase. |
| 95 | GET | `/api/v1/profiles/expert/me` | Lấy thông tin của account đang đăng nhập. | Hồ sơ business, expert, portfolio và file Firebase. |
| 96 | GET | `/api/v1/profiles/business/me` | Lấy thông tin của account đang đăng nhập. | Hồ sơ business, expert, portfolio và file Firebase. |
| 97 | GET | `/api/v1/profiles/business/by-job/{jobId}` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 98 | POST | `/api/v1/profiles/portfolio` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 99 | POST | `/api/v1/profiles/portfolio/certificate-file` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 100 | POST | `/api/v1/profiles/expert` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 101 | POST | `/api/v1/profiles/business` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 102 | POST | `/api/v1/profiles/business/license-file` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |
| 103 | POST | `/api/v1/profiles/approve/{type}/{id}` | Xem, tạo hoặc cập nhật hồ sơ người dùng. | Hồ sơ business, expert, portfolio và file Firebase. |

## SoW Generation

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 104 | POST | `/api/jobs/generate-sow` | AI sinh SoW, milestone gợi ý và cấu trúc dự án. | AI generate SoW, milestone gợi ý và cấu trúc dự án. |

## tax-check-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 105 | GET | `/api/auth/tax-check/{mst}` | Lấy dữ liệu hoặc danh sách theo endpoint này. | Kiểm tra mã số thuế. |

## test-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 106 | GET | `/api/test/secure` | Lấy dữ liệu hoặc danh sách theo endpoint này. | API kiểm thử bảo mật trong môi trường dev. |

## wallet-controller

| STT | Method | API | API dùng để làm gì | Phục vụ chức năng |
| --- | --- | --- | --- | --- |
| 107 | GET | `/api/v1/wallet/me` | Lấy thông tin của account đang đăng nhập. | Ví của người dùng. |

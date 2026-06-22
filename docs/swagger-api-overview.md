# Swagger API Overview - AITASKER BE

Tài liệu này liệt kê đầy đủ REST API đang có trong source controller hiện tại và được đồng bộ với `docs/openapi/openapi-v1.json`. Swagger UI runtime: `http://localhost:8080/swagger-ui.html`.

- Tổng số REST endpoint: **126**.
- WebSocket/STOMP không nằm trong Swagger vì không phải REST API.
- `POST /api/payments/payos/webhook` không còn trong controller hiện tại; PayOS wallet top-up dùng return/sync theo order code.

## admin-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/admin/accounts` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý account hệ thống. |
| 2 | GET | `/api/v1/admin/analytics/overview` | Cần Bearer JWT | Admin vận hành hệ thống | Xem dashboard analytics. |
| 3 | GET | `/api/v1/admin/audit-logs` | Cần Bearer JWT | Admin vận hành hệ thống | Xem audit logs. |
| 4 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo hoặc xem review sau contract. |
| 5 | GET | `/api/v1/admin/settings` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý system settings. |
| 6 | GET | `/api/v1/admin/staffs` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý staff. |
| 7 | GET | `/api/v1/admin/wallet` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 8 | POST | `/api/v1/admin/accounts` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý account hệ thống. |
| 9 | POST | `/api/v1/admin/reviews` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo hoặc xem review sau contract. |
| 10 | POST | `/api/v1/admin/staffs` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý staff. |
| 11 | POST | `/api/v1/admin/wallet/sync` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 12 | PATCH | `/api/v1/admin/accounts/{accountId}` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý account hệ thống. |
| 13 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý account hệ thống. |
| 14 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý account hệ thống. |
| 15 | PATCH | `/api/v1/admin/settings/{key}` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý system settings. |
| 16 | PATCH | `/api/v1/admin/staffs/{staffId}` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý staff. |
| 17 | DELETE | `/api/v1/admin/accounts/{accountId}` | Cần Bearer JWT | Admin vận hành hệ thống | Quản lý account hệ thống. |

## auth-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 18 | GET | `/api/auth/check-email` | Không cần token | Auth, OTP và session | Lấy dữ liệu hoặc danh sách theo quyền. |
| 19 | GET | `/api/auth/me` | Cần Bearer JWT | Auth, OTP và session | Lấy thông tin session hiện tại. |
| 20 | POST | `/api/auth/google/login` | Không cần token | Auth, OTP và session | Đăng nhập bằng Google token. |
| 21 | POST | `/api/auth/google/register` | Không cần token | Auth, OTP và session | Đăng ký/khởi tạo bằng Google token. |
| 22 | POST | `/api/auth/login` | Không cần token | Auth, OTP và session | Đăng nhập và nhận accessToken/refreshToken. |
| 23 | POST | `/api/auth/register` | Không cần token | Auth, OTP và session | Đăng ký tài khoản mới. |

## catalog-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 24 | GET | `/api/v1/acceptance-criteria` | Không cần token | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 25 | GET | `/api/v1/domains` | Không cần token | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 26 | GET | `/api/v1/jobs/{jobId}/domains` | Cần Bearer JWT | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 27 | GET | `/api/v1/jobs/{jobId}/skills` | Cần Bearer JWT | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 28 | GET | `/api/v1/jobs/{jobId}/technologies` | Cần Bearer JWT | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 29 | GET | `/api/v1/skills` | Không cần token | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 30 | GET | `/api/v1/technologies` | Cần Bearer JWT | Catalog job metadata | Lấy dữ liệu hoặc danh sách theo quyền. |
| 31 | POST | `/api/v1/domains` | Cần Bearer JWT | Catalog job metadata | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 32 | POST | `/api/v1/skills` | Cần Bearer JWT | Catalog job metadata | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 33 | POST | `/api/v1/technologies` | Cần Bearer JWT | Catalog job metadata | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 34 | PUT | `/api/v1/jobs/{jobId}/domains` | Cần Bearer JWT | Catalog job metadata | Thay thế danh sách/dữ liệu liên quan. |
| 35 | PUT | `/api/v1/jobs/{jobId}/skills` | Cần Bearer JWT | Catalog job metadata | Thay thế danh sách/dữ liệu liên quan. |
| 36 | PUT | `/api/v1/jobs/{jobId}/technologies` | Cần Bearer JWT | Catalog job metadata | Thay thế danh sách/dữ liệu liên quan. |
| 37 | PATCH | `/api/v1/domains/{domainId}` | Cần Bearer JWT | Catalog job metadata | Cập nhật một phần dữ liệu hoặc trạng thái. |
| 38 | PATCH | `/api/v1/skills/{skillId}` | Cần Bearer JWT | Catalog job metadata | Cập nhật một phần dữ liệu hoặc trạng thái. |
| 39 | PATCH | `/api/v1/technologies/{technologyId}` | Cần Bearer JWT | Catalog job metadata | Cập nhật một phần dữ liệu hoặc trạng thái. |

## chatbot-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 40 | POST | `/api/chatbot/ask` | Không cần token | AI hỗ trợ job và matching | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## contract-execution-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 41 | GET | `/api/v1/contracts` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 42 | GET | `/api/v1/contracts/{contractId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 43 | GET | `/api/v1/contracts/{contractId}/disputes` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 44 | GET | `/api/v1/contracts/{contractId}/milestones` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 45 | GET | `/api/v1/disputes/{disputeId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 46 | GET | `/api/v1/jobs/{jobId}/matching` | Cần Bearer JWT | AI hỗ trợ job và matching | Lấy dữ liệu hoặc danh sách theo quyền. |
| 47 | GET | `/api/v1/jobs/{jobId}/milestones` | Không cần token với job `OPEN`; cần Bearer JWT với job chưa public | Marketplace job và proposal | Xem milestone của job. Public với job `OPEN`, job chưa public vẫn kiểm tra quyền tham gia/sở hữu. |
| 48 | GET | `/api/v1/milestones/{milestoneId}/criteria` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 49 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 50 | GET | `/api/v1/milestones/{milestoneId}/transactions` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 51 | POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Admin xử lý hoàn/resolution ký quỹ. |
| 52 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo contract draft từ proposal accepted. |
| 53 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Business trả ký quỹ hợp đồng từ wallet. |
| 54 | POST | `/api/v1/contracts/{contractId}/nda-sign` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Ký NDA. |
| 55 | POST | `/api/v1/contracts/{contractId}/reject` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Từ chối contract theo flow. |
| 56 | POST | `/api/v1/contracts/{contractId}/sign` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Ký xác nhận contract. |
| 57 | POST | `/api/v1/contracts/{contractId}/terminate` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 58 | POST | `/api/v1/criteria` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 59 | POST | `/api/v1/deliverables` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 60 | POST | `/api/v1/disputes` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 61 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Ghi kết quả demo testing trong dispute. |
| 62 | POST | `/api/v1/disputes/{disputeId}/technical-report` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Ghi báo cáo kỹ thuật cho dispute. |
| 63 | POST | `/api/v1/milestones` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 64 | POST | `/api/v1/milestones/sla-auto-approve` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Mô phỏng SLA auto approve milestone. |
| 65 | POST | `/api/v1/milestones/{milestoneId}/complete` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Business nghiệm thu milestone. |
| 66 | POST | `/api/v1/transactions` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 67 | POST | `/api/v1/transactions/{transactionId}/webhook` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 68 | PATCH | `/api/v1/disputes/{disputeId}/assign` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Cập nhật một phần dữ liệu hoặc trạng thái. |
| 69 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Cập nhật một phần dữ liệu hoặc trạng thái. |
| 70 | PATCH | `/api/v1/transactions/{transactionId}/status` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Cập nhật một phần dữ liệu hoặc trạng thái. |

## credit-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 71 | POST | `/api/credits/job-post/purchase` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 72 | POST | `/api/credits/proposal/purchase` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## email-otp-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 73 | POST | `/api/auth/email/send-otp` | Không cần token | Auth, OTP và session | Gửi OTP xác thực email. |
| 74 | POST | `/api/auth/email/verify-otp` | Không cần token | Auth, OTP và session | Xác thực OTP email. |

## Expert Candidates

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 75 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | Cần Bearer JWT | AI hỗ trợ job và matching | Lấy danh sách expert candidate phù hợp job. |

## Expert Recommendations

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 76 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Cần Bearer JWT | AI hỗ trợ job và matching | Sinh hoặc xem expert recommendations bằng AI. |
| 77 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Cần Bearer JWT | AI hỗ trợ job và matching | Sinh hoặc xem expert recommendations bằng AI. |

## health-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 78 | GET | `/api/health` | Không cần token | Dev/Test và health check | Kiểm tra backend còn sống. |

## marketplace-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 79 | GET | `/api/v1/jobs` | Không cần token | Marketplace job và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 80 | GET | `/api/v1/jobs/my` | Không cần token | Marketplace job và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 81 | GET | `/api/v1/jobs/{jobId}` | Không cần token | Marketplace job và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 82 | GET | `/api/v1/jobs/{jobId}/proposals` | Cần Bearer JWT | Marketplace job và proposal | Xem proposal của job. |
| 83 | GET | `/api/v1/proposals/my` | Cần Bearer JWT | Marketplace job và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 84 | POST | `/api/v1/jobs` | Cần Bearer JWT | Marketplace job và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 85 | POST | `/api/v1/jobs/{jobId}/publish` | Cần Bearer JWT | Marketplace job và proposal | Publish job và tiêu thụ job-post credit. |
| 86 | POST | `/api/v1/proposals` | Cần Bearer JWT | Marketplace job và proposal | Expert gửi proposal. |
| 87 | POST | `/api/v1/proposals/file` | Cần Bearer JWT | Marketplace job và proposal | Upload file proposal. |
| 88 | PATCH | `/api/v1/jobs/{jobId}/status` | Cần Bearer JWT | Marketplace job và proposal | Cập nhật một phần dữ liệu hoặc trạng thái. |
| 89 | PATCH | `/api/v1/proposals/{proposalId}/status` | Cần Bearer JWT | Marketplace job và proposal | Duyệt hoặc từ chối proposal. |

## membership-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 90 | GET | `/api/membership/packages` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Xem membership package theo role. |
| 91 | POST | `/api/membership/packages/{packageId}/purchase` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## notification-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 92 | GET | `/api/v1/notifications` | Cần Bearer JWT | Notification | Lấy danh sách thông báo. |
| 93 | GET | `/api/v1/notifications/unread-count` | Cần Bearer JWT | Notification | Lấy dữ liệu hoặc danh sách theo quyền. |
| 94 | PATCH | `/api/v1/notifications/read-all` | Cần Bearer JWT | Notification | Cập nhật một phần dữ liệu hoặc trạng thái. |
| 95 | PATCH | `/api/v1/notifications/{notificationId}/read` | Cần Bearer JWT | Notification | Cập nhật một phần dữ liệu hoặc trạng thái. |

## pay-o-s-payment-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 96 | GET | `/api/payments/payos/return` | Không cần token | Payment, wallet, membership, quota và withdrawal | Nhận return từ PayOS và sync trạng thái. |
| 97 | POST | `/api/payments/payos/create` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo payment order PayOS để nạp ví. |
| 98 | POST | `/api/payments/payos/{orderCode}/sync` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Chủ động sync trạng thái PayOS theo order code. |

## profile-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 99 | GET | `/api/v1/profiles/business` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 100 | GET | `/api/v1/profiles/business/by-job/{jobId}` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 101 | GET | `/api/v1/profiles/business/me` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 102 | GET | `/api/v1/profiles/business/{businessId}` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 103 | GET | `/api/v1/profiles/expert` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 104 | GET | `/api/v1/profiles/expert/me` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 105 | GET | `/api/v1/profiles/expert/{expertId}` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 106 | GET | `/api/v1/profiles/files/view-url` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy signed URL để xem file. |
| 107 | GET | `/api/v1/profiles/portfolio` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 108 | GET | `/api/v1/profiles/portfolio/me` | Cần Bearer JWT | KYB/KYC, profile và file | Lấy dữ liệu hoặc danh sách theo quyền. |
| 109 | POST | `/api/v1/profiles/approve/{type}/{id}` | Cần Bearer JWT | KYB/KYC, profile và file | Staff duyệt hoặc từ chối hồ sơ BUSINESS/EXPERT; khi `status=Rejected` phải gửi query `reason` để lưu lý do từ chối. |
| 110 | POST | `/api/v1/profiles/business` | Cần Bearer JWT | KYB/KYC, profile và file | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 111 | POST | `/api/v1/profiles/business/license-file` | Cần Bearer JWT | KYB/KYC, profile và file | Upload giấy phép kinh doanh. |
| 112 | POST | `/api/v1/profiles/expert` | Cần Bearer JWT | KYB/KYC, profile và file | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 113 | POST | `/api/v1/profiles/portfolio` | Cần Bearer JWT | KYB/KYC, profile và file | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 114 | POST | `/api/v1/profiles/portfolio/certificate-file` | Cần Bearer JWT | KYB/KYC, profile và file | Upload chứng chỉ portfolio. |

## SoW Generation

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 115 | POST | `/api/jobs/generate-sow` | Cần Bearer JWT | AI hỗ trợ job và matching | AI sinh SoW và milestone gợi ý. |

## tax-check-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 116 | GET | `/api/auth/tax-check/{mst}` | Không cần token | Auth, OTP và session | Kiểm tra mã số thuế doanh nghiệp. |

## test-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 117 | GET | `/api/test/secure` | Cần Bearer JWT | Dev/Test và health check | Lấy dữ liệu hoặc danh sách theo quyền. |

## user-quota-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 118 | GET | `/api/users/me/quota` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Xem quota, package active và premium entitlement. |

## wallet-api-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 119 | GET | `/api/wallet/current` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Xem wallet hiện tại. |
| 120 | GET | `/api/wallet/transactions` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Xem lịch sử wallet transaction. |

## wallet-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 121 | GET | `/api/v1/wallet/me` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |

## withdrawal-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 122 | GET | `/api/v1/admin/withdrawal-requests` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Xem danh sách withdrawal request. |
| 123 | GET | `/api/v1/withdrawal-requests` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Xem danh sách withdrawal request. |
| 124 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Admin approve withdrawal sau khi chuyển khoản thủ công. |
| 125 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Admin reject withdrawal và trả holding về available. |
| 126 | POST | `/api/v1/withdrawal-requests` | Cần Bearer JWT | Payment, wallet, membership, quota và withdrawal | Tạo withdrawal request. |

## Ghi chú kiểm tra

- Danh sách trên được tạo lại từ các class `@RestController` trong `src/main/java`.
- Nếu thêm/xóa API trong controller, cần cập nhật lại OpenAPI JSON, overview và test guide.
- Schema request/response chi tiết xem trong Swagger UI hoặc OpenAPI JSON.

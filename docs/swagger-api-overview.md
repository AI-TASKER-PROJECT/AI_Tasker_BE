# Tổng quan Swagger API - AITASKER BE

Tài liệu này liệt kê REST API đang có trong controller source hiện tại sau khi xử lý merge conflict.

- Tổng số REST endpoint: **128**.
- WebSocket/STOMP không nằm trong Swagger vì không phải REST API.

## admin-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/admin/accounts` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 2 | POST | `/api/v1/admin/accounts` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 3 | DELETE | `/api/v1/admin/accounts/{accountId}` | Cần Bearer JWT | Admin vận hành hệ thống | Xóa, khóa hoặc vô hiệu hóa dữ liệu theo quyền. |
| 4 | PATCH | `/api/v1/admin/accounts/{accountId}` | Cần Bearer JWT | Admin vận hành hệ thống | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 5 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | Cần Bearer JWT | Admin vận hành hệ thống | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 6 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | Cần Bearer JWT | Admin vận hành hệ thống | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 7 | GET | `/api/v1/admin/analytics/overview` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 8 | GET | `/api/v1/admin/audit-logs` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 9 | POST | `/api/v1/admin/reviews` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 10 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 11 | GET | `/api/v1/admin/settings` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 12 | PATCH | `/api/v1/admin/settings/{key}` | Cần Bearer JWT | Admin vận hành hệ thống | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 13 | GET | `/api/v1/admin/staffs` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 14 | POST | `/api/v1/admin/staffs` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 15 | PATCH | `/api/v1/admin/staffs/{staffId}` | Cần Bearer JWT | Admin vận hành hệ thống | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 16 | GET | `/api/v1/admin/wallet` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 17 | POST | `/api/v1/admin/wallet/sync` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## auth-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 18 | GET | `/api/auth/check-email` | Không cần token | Auth, OTP và session | Lấy dữ liệu hoặc danh sách theo quyền. |
| 19 | POST | `/api/auth/google/login` | Không cần token | Auth, OTP và session | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 20 | POST | `/api/auth/google/register` | Không cần token | Auth, OTP và session | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 21 | POST | `/api/auth/login` | Không cần token | Auth, OTP và session | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 22 | GET | `/api/auth/me` | Cần Bearer JWT | Auth, OTP và session | Lấy dữ liệu hoặc danh sách theo quyền. |
| 23 | POST | `/api/auth/register` | Không cần token | Auth, OTP và session | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## catalog-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 24 | GET | `/api/v1/acceptance-criteria` | Cần Bearer JWT | Catalog, skill, technology | Lấy dữ liệu hoặc danh sách theo quyền. |
| 25 | GET | `/api/v1/domains` | Cần Bearer JWT | Catalog, skill, technology | Lấy dữ liệu hoặc danh sách theo quyền. |
| 26 | POST | `/api/v1/domains` | Cần Bearer JWT | Catalog, skill, technology | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 27 | PATCH | `/api/v1/domains/{domainId}` | Cần Bearer JWT | Catalog, skill, technology | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 28 | GET | `/api/v1/jobs/{jobId}/domains` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 29 | PUT | `/api/v1/jobs/{jobId}/domains` | Cần Bearer JWT | Job marketplace và proposal | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 30 | GET | `/api/v1/jobs/{jobId}/skills` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 31 | PUT | `/api/v1/jobs/{jobId}/skills` | Cần Bearer JWT | Job marketplace và proposal | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 32 | GET | `/api/v1/jobs/{jobId}/technologies` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 33 | PUT | `/api/v1/jobs/{jobId}/technologies` | Cần Bearer JWT | Job marketplace và proposal | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 34 | GET | `/api/v1/skills` | Cần Bearer JWT | Catalog, skill, technology | Lấy dữ liệu hoặc danh sách theo quyền. |
| 35 | POST | `/api/v1/skills` | Cần Bearer JWT | Catalog, skill, technology | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 36 | PATCH | `/api/v1/skills/{skillId}` | Cần Bearer JWT | Catalog, skill, technology | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 37 | GET | `/api/v1/technologies` | Cần Bearer JWT | Catalog, skill, technology | Lấy dữ liệu hoặc danh sách theo quyền. |
| 38 | POST | `/api/v1/technologies` | Cần Bearer JWT | Catalog, skill, technology | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 39 | PATCH | `/api/v1/technologies/{technologyId}` | Cần Bearer JWT | Catalog, skill, technology | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |

## chatbot-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 40 | POST | `/api/chatbot/ask` | Cần Bearer JWT | Chat và messaging | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## contract-execution-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 41 | POST | `/api/v1/admin/contracts/{contractId}/deposit/refund` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 42 | GET | `/api/v1/contracts` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 43 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 44 | GET | `/api/v1/contracts/{contractId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 45 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 46 | GET | `/api/v1/contracts/{contractId}/disputes` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 47 | GET | `/api/v1/contracts/{contractId}/milestones` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 48 | POST | `/api/v1/contracts/{contractId}/nda-sign` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 49 | POST | `/api/v1/contracts/{contractId}/reject` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 50 | POST | `/api/v1/contracts/{contractId}/sign` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 51 | POST | `/api/v1/contracts/{contractId}/terminate` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 52 | POST | `/api/v1/criteria` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 53 | POST | `/api/v1/deliverables` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 54 | POST | `/api/v1/disputes` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 55 | GET | `/api/v1/disputes/{disputeId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 56 | PATCH | `/api/v1/disputes/{disputeId}/assign` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 57 | POST | `/api/v1/disputes/{disputeId}/demo-testing` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 58 | PATCH | `/api/v1/disputes/{disputeId}/resolve` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 59 | POST | `/api/v1/disputes/{disputeId}/technical-report` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 60 | GET | `/api/v1/jobs/{jobId}/matching` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 61 | GET | `/api/v1/jobs/{jobId}/milestones` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 62 | POST | `/api/v1/milestones` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 63 | POST | `/api/v1/milestones/sla-auto-approve` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 64 | PATCH | `/api/v1/milestones/{milestoneId}` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 65 | POST | `/api/v1/milestones/{milestoneId}/complete` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 66 | GET | `/api/v1/milestones/{milestoneId}/criteria` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 67 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 68 | GET | `/api/v1/milestones/{milestoneId}/transactions` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Lấy dữ liệu hoặc danh sách theo quyền. |
| 69 | POST | `/api/v1/transactions` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 70 | PATCH | `/api/v1/transactions/{transactionId}/status` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 71 | POST | `/api/v1/transactions/{transactionId}/webhook` | Cần Bearer JWT | Contract, milestone, deliverable và dispute | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## credit-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 72 | POST | `/api/credits/job-post/purchase` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 73 | POST | `/api/credits/proposal/purchase` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## email-otp-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 74 | POST | `/api/auth/email/send-otp` | Cần Bearer JWT | Auth, OTP và session | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 75 | POST | `/api/auth/email/verify-otp` | Không cần token | Auth, OTP và session | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## expert-candidate-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 76 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |

## expert-recommendation-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 77 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 78 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 79 | POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | Cần Bearer JWT | Job marketplace và proposal | Business chọn expert ưng ý từ danh sách AI recommend và gửi thông báo cho expert. |

## marketplace-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 80 | GET | `/api/v1/jobs` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 81 | POST | `/api/v1/jobs` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 81a | PUT | `/api/v1/jobs/{jobId}` | Cần Bearer JWT (BUSINESS) | Job marketplace và proposal | Cập nhật draft job: persist cùng `jobs` + `sow` + `milestones` (US-022). Chỉ cho DRAFT, upsert sow theo jobId, thay milestone nháp. Không tiêu quota publish. |
| 82 | GET | `/api/v1/jobs/my` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 83 | GET | `/api/v1/jobs/{jobId}` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 84 | GET | `/api/v1/jobs/{jobId}/proposals` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 85 | POST | `/api/v1/jobs/{jobId}/publish` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 86 | PATCH | `/api/v1/jobs/{jobId}/status` | Cần Bearer JWT | Job marketplace và proposal | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 87 | POST | `/api/v1/proposals` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 88 | POST | `/api/v1/proposals/file` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 89 | GET | `/api/v1/proposals/my` | Cần Bearer JWT | Job marketplace và proposal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 90 | PATCH | `/api/v1/proposals/{proposalId}/status` | Cần Bearer JWT | Job marketplace và proposal | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |

## membership-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 91 | GET | `/api/membership/packages` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 92 | POST | `/api/membership/packages/{packageId}/purchase` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## notification-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 93 | GET | `/api/v1/notifications` | Cần Bearer JWT | Notification | Lấy dữ liệu hoặc danh sách theo quyền. |
| 94 | PATCH | `/api/v1/notifications/read-all` | Cần Bearer JWT | Notification | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |
| 95 | GET | `/api/v1/notifications/unread-count` | Cần Bearer JWT | Notification | Lấy dữ liệu hoặc danh sách theo quyền. |
| 96 | PATCH | `/api/v1/notifications/{notificationId}/read` | Cần Bearer JWT | Notification | Cập nhật trạng thái hoặc dữ liệu nghiệp vụ. |

## pay-ospayment-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 97 | POST | `/api/payments/payos/create` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 98 | GET | `/api/payments/payos/return` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 99 | POST | `/api/payments/payos/{orderCode}/sync` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## profile-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 100 | POST | `/api/v1/profiles/approve/{type}/{id}` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 101 | GET | `/api/v1/profiles/business` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 102 | POST | `/api/v1/profiles/business` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 103 | GET | `/api/v1/profiles/business/by-job/{jobId}` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 104 | POST | `/api/v1/profiles/business/license-file` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 105 | GET | `/api/v1/profiles/business/me` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 106 | GET | `/api/v1/profiles/business/{businessId}` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 107 | GET | `/api/v1/profiles/expert` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 108 | POST | `/api/v1/profiles/expert` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 109 | GET | `/api/v1/profiles/expert/me` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 110 | POST | `/api/v1/profiles/expert/portfolio-file` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Expert upload file portfolio lên storage và lưu link vào hồ sơ. |
| 111 | GET | `/api/v1/profiles/expert/{expertId}` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 112 | GET | `/api/v1/profiles/files/view-url` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 113 | GET | `/api/v1/profiles/portfolio` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |
| 114 | POST | `/api/v1/profiles/portfolio` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 115 | POST | `/api/v1/profiles/portfolio/certificate-file` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 116 | GET | `/api/v1/profiles/portfolio/me` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |

## sow-generation-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 117 | POST | `/api/jobs/generate-sow` | Cần Bearer JWT | Job marketplace và proposal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

## tax-check-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 118 | GET | `/api/auth/tax-check/{mst}` | Cần Bearer JWT | Auth, OTP và session | Lấy dữ liệu hoặc danh sách theo quyền. |

## test-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 119 | GET | `/api/test/secure` | Cần Bearer JWT | Khác / hạ tầng | Lấy dữ liệu hoặc danh sách theo quyền. |

## user-quota-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 120 | GET | `/api/users/me/quota` | Cần Bearer JWT | Tài khoản, hồ sơ và quota | Lấy dữ liệu hoặc danh sách theo quyền. |

## wallet-api-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 121 | GET | `/api/wallet/current` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 122 | GET | `/api/wallet/transactions` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |

## wallet-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 123 | GET | `/api/v1/wallet/me` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |

## withdrawal-controller

| STT | Method | API | Auth | Luồng | Mục đích |
| --- | --- | --- | --- | --- | --- |
| 124 | GET | `/api/v1/admin/withdrawal-requests` | Cần Bearer JWT | Admin vận hành hệ thống | Lấy dữ liệu hoặc danh sách theo quyền. |
| 125 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 126 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Cần Bearer JWT | Admin vận hành hệ thống | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |
| 127 | GET | `/api/v1/withdrawal-requests` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Lấy dữ liệu hoặc danh sách theo quyền. |
| 128 | POST | `/api/v1/withdrawal-requests` | Cần Bearer JWT | Payment, wallet, quota và withdrawal | Tạo mới dữ liệu hoặc thực hiện hành động nghiệp vụ. |

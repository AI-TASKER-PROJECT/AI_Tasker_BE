# Tong quan Swagger API - AITASKER BE

Tai lieu nay duoc dong bo tu runtime OpenAPI `/v3/api-docs` va sap xep theo tag flow trong `OpenApiConfig`.

- Tong so REST endpoint trong Swagger runtime: **186**.
- Public endpoint: **21**.
- Endpoint can Bearer JWT: **165**.
- Swagger UI mac dinh: `http://localhost:8080/swagger-ui.html`.
- OpenAPI JSON runtime: `http://localhost:8080/v3/api-docs`.

## Nguon su that

- Flow order va tag order: `src/main/java/com/aitasker/be/config/OpenApiConfig.java`.
- Route inventory: runtime `/v3/api-docs`, regenerated into `docs/openapi/openapi-v1.json`.
- Public/private route: method-level OpenAPI security plus `SecurityConfig`.

## Auth Flow

- Giai thich flow: Dang ky, dang nhap, OTP email va tra cuu ma so thue.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/auth/reset-password` | Public | Operation resetPassword. |
| 2 | POST | `/api/auth/register` | Public | Operation register. |
| 3 | POST | `/api/auth/refresh` | Public | Operation refresh. |
| 4 | POST | `/api/auth/login` | Public | Operation login. |
| 5 | POST | `/api/auth/google/register` | Public | Operation googleRegister. |
| 6 | POST | `/api/auth/google/login` | Public | Operation googleLogin. |
| 7 | POST | `/api/auth/forgot-password` | Public | Operation forgotPassword. |
| 8 | POST | `/api/auth/email/verify-otp` | Public | Operation verifyOtp. |
| 9 | POST | `/api/auth/email/send-otp` | Public | Operation sendOtp. |
| 10 | GET | `/api/auth/tax-check/{mst}` | Public | Operation checkTaxCode. |
| 11 | GET | `/api/auth/me` | Bearer JWT | Operation me. |
| 12 | GET | `/api/auth/check-email` | Public | Operation checkEmail. |

## Profile Verification Flow

- Giai thich flow: Tao, xem va duyet ho so Business/Expert va portfolio.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/profiles/portfolio` | Bearer JWT | Operation listPortfolio. |
| 2 | POST | `/api/v1/profiles/portfolio` | Bearer JWT | Operation upsertPortfolio. |
| 3 | POST | `/api/v1/profiles/portfolio/certificate-file` | Bearer JWT | Operation uploadExpertCertificate. |
| 4 | GET | `/api/v1/profiles/expert` | Bearer JWT | Operation listExpert. |
| 5 | POST | `/api/v1/profiles/expert` | Bearer JWT | Operation upsertExpert. |
| 6 | POST | `/api/v1/profiles/expert/portfolio-file` | Bearer JWT | Operation uploadExpertPortfolio. |
| 7 | GET | `/api/v1/profiles/business` | Bearer JWT | Operation listBusiness. |
| 8 | POST | `/api/v1/profiles/business` | Bearer JWT | Operation upsertBusiness. |
| 9 | POST | `/api/v1/profiles/business/license-file` | Bearer JWT | Operation uploadBusinessLicense. |
| 10 | POST | `/api/v1/profiles/approve/{type}/{id}` | Bearer JWT | Operation approve. |
| 11 | GET | `/api/v1/profiles/portfolio/me` | Bearer JWT | Operation myPortfolio. |
| 12 | GET | `/api/v1/profiles/files/view-url` | Bearer JWT | Operation fileViewUrl. |
| 13 | GET | `/api/v1/profiles/expert/{expertId}` | Bearer JWT | Operation getExpertById. |
| 14 | GET | `/api/v1/profiles/expert/me` | Bearer JWT | Operation myExpert. |
| 15 | GET | `/api/v1/profiles/business/{businessId}` | Public | Operation getBusinessById. |
| 16 | GET | `/api/v1/profiles/business/me` | Bearer JWT | Operation myBusiness. |
| 17 | GET | `/api/v1/profiles/business/by-job/{jobId}` | Public | Operation businessByJob. |

## Job Draft & Publish Flow

- Giai thich flow: Tao draft, cap nhat, gan taxonomy, sinh SoW va publish job.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/jobs/{jobId}` | Public | Operation jobDetail. |
| 2 | PUT | `/api/v1/jobs/{jobId}` | Bearer JWT | Operation updateDraftJob. |
| 3 | GET | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Operation listJobTechnologies. |
| 4 | PUT | `/api/v1/jobs/{jobId}/technologies` | Bearer JWT | Operation replaceJobTechnologies. |
| 5 | GET | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Operation listJobSkills. |
| 6 | PUT | `/api/v1/jobs/{jobId}/skills` | Bearer JWT | Operation replaceJobSkills. |
| 7 | GET | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Operation listJobDomains. |
| 8 | PUT | `/api/v1/jobs/{jobId}/domains` | Bearer JWT | Operation replaceJobDomains. |
| 9 | GET | `/api/v1/jobs` | Public | Operation listJobs. |
| 10 | POST | `/api/v1/jobs` | Bearer JWT | Operation createJob. |
| 11 | POST | `/api/v1/jobs/{jobId}/publish` | Bearer JWT | Operation publishJob. |
| 12 | POST | `/api/jobs/generate-sow` | Bearer JWT | Generate SoW plus advisory budget range and Business/recommended milestone allocations; Business keeps final price authority. |
| 13 | POST | `/api/jobs/reallocate-sow-budget` | Bearer JWT | Reallocate a Business-selected custom whole-VND budget across generated milestones without AI or persistence. |
| 14 | PATCH | `/api/v1/jobs/{jobId}/status` | Bearer JWT | Operation updateJobStatus. |
| 15 | GET | `/api/v1/jobs/{jobId}/milestones` | Public | Operation listJobMilestones. |
| 16 | GET | `/api/v1/jobs/my` | Bearer JWT | Operation listMyJobs. |

## Proposal Flow

- Giai thich flow: Submit proposal, review proposal, matching va de xuat expert.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/v1/proposals` | Bearer JWT | Operation submitProposal. |
| 2 | POST | `/api/v1/proposals/file` | Bearer JWT | Operation uploadProposalFile. |
| 3 | GET | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Operation getRecommendations. |
| 4 | POST | `/api/jobs/{jobPostingId}/expert-recommendations` | Bearer JWT | Operation generateRecommendations. |
| 5 | POST | `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select` | Bearer JWT | Operation selectRecommendedExpert. |
| 6 | PATCH | `/api/v1/proposals/{proposalId}/status` | Bearer JWT | Operation reviewProposal. |
| 7 | GET | `/api/v1/proposals/my` | Bearer JWT | Operation listMyProposals. |
| 8 | GET | `/api/v1/jobs/{jobId}/proposals` | Bearer JWT | Operation listProposals. |
| 9 | GET | `/api/v1/jobs/{jobId}/matching` | Bearer JWT | Operation matching. |
| 10 | GET | `/api/jobs/{jobPostingId}/expert-candidates` | Bearer JWT | Operation findTopCandidates. |

## Wallet & Payment Flow

- Giai thich flow: Wallet, top-up, membership, credits, quota va withdrawal.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/withdrawal-requests` | Bearer JWT | Operation listMyWithdrawalRequests. |
| 2 | POST | `/api/v1/withdrawal-requests` | Bearer JWT | Operation createWithdrawalRequest. |
| 3 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject` | Bearer JWT | Operation rejectWithdrawal. |
| 4 | POST | `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve` | Bearer JWT | Operation approveWithdrawal. |
| 5 | POST | `/api/payments/payos/{orderCode}/sync` | Bearer JWT | Operation syncPaymentStatus. |
| 6 | POST | `/api/payments/payos/create` | Bearer JWT | Operation createPayment. |
| 7 | POST | `/api/membership/packages/{packageId}/purchase` | Bearer JWT | Operation purchasePackage. |
| 8 | POST | `/api/credits/proposal/purchase` | Bearer JWT | Operation purchaseProposalCredits. |
| 9 | POST | `/api/credits/job-post/purchase` | Bearer JWT | Operation purchaseJobPostCredits. |
| 10 | GET | `/api/wallet/transactions` | Bearer JWT | Operation walletTransactions. |
| 11 | GET | `/api/wallet/current` | Bearer JWT | Operation currentWallet. |
| 12 | GET | `/api/v1/wallet/me` | Bearer JWT | Operation currentWallet_1. |
| 13 | GET | `/api/v1/admin/withdrawal-requests` | Bearer JWT | Operation listWithdrawalRequestsForAdmin. |
| 14 | GET | `/api/users/me/quota` | Bearer JWT | Operation currentQuota. |
| 15 | GET | `/api/payments/payos/return` | Public | Operation handleReturn. |
| 16 | GET | `/api/membership/packages` | Bearer JWT | Operation listPackages. |

## Contract Execution Flow

- Giai thich flow: Contract, milestone, deliverable, dispute, termination va review.
- Progress-report feedback endpoint restored: Business can store feedback and acknowledge a pending report without creating dispute/revision semantics.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | PUT | `/api/v1/milestones/{milestoneId}/criteria/{criteriaId}` | Bearer JWT | Operation updateCriteria. |
| 2 | DELETE | `/api/v1/milestones/{milestoneId}/criteria/{criteriaId}` | Bearer JWT | Operation deleteCriteria. |
| 3 | POST | `/api/v1/termination-requests/{terminationRequestId}/withdraw` | Bearer JWT | Operation withdrawTermination. |
| 4 | POST | `/api/v1/termination-requests/{terminationRequestId}/reject` | Bearer JWT | Operation rejectTermination. |
| 5 | POST | `/api/v1/termination-requests/{terminationRequestId}/refund-deposit` | Bearer JWT | Operation refundTerminationDeposit. |
| 6 | POST | `/api/v1/termination-requests/{terminationRequestId}/partial-evidence` | Bearer JWT | Operation submitPartialEvidence. |
| 7 | POST | `/api/v1/termination-requests/{terminationRequestId}/execute-settlement` | Bearer JWT | Operation executeTerminationSettlement. |
| 8 | POST | `/api/v1/termination-requests/{terminationRequestId}/dispute` | Bearer JWT | Operation disputeTermination. |
| 9 | POST | `/api/v1/termination-requests/{terminationRequestId}/assign-staff` | Bearer JWT | Operation assignTerminationStaff. |
| 10 | POST | `/api/v1/termination-requests/{terminationRequestId}/approve` | Bearer JWT | Operation approveTermination. |
| 11 | POST | `/api/v1/termination-requests/{terminationRequestId}/accept` | Bearer JWT | Operation acceptTermination. |
| 12 | POST | `/api/v1/termination-requests/expire-awaiting-expert` | Bearer JWT | Operation expireTerminationResponses. |
| 13 | POST | `/api/v1/milestones` | Bearer JWT | Operation createMilestone. |
| 14 | POST | `/api/v1/milestones/{milestoneId}/start` | Bearer JWT | Compatibility start endpoint; deposit now auto-starts milestones and this route is idempotent for `IN_PROGRESS`. |
| 15 | POST | `/api/v1/milestones/{milestoneId}/reject` | Bearer JWT | Operation rejectMilestone. |
| 16 | POST | `/api/v1/milestones/{milestoneId}/disputes` | Bearer JWT | Operation initiateMilestoneDispute. |
| 17 | GET | `/api/v1/milestones/{milestoneId}/deliverables` | Bearer JWT | Operation listDeliverables. |
| 18 | POST | `/api/v1/milestones/{milestoneId}/deliverables` | Bearer JWT | Assigned Expert submits a first or corrected deliverable before the contract milestone deadline. |
| 19 | GET | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT | Operation listCriteria. |
| 20 | POST | `/api/v1/milestones/{milestoneId}/criteria` | Bearer JWT | Operation createCriteria. |
| 21 | POST | `/api/v1/milestones/{milestoneId}/complete` | Bearer JWT | Operation completeMilestone. |
| 22 | POST | `/api/v1/milestones/{milestoneId}/approve` | Bearer JWT | Operation approveMilestone. |
| 23 | POST | `/api/v1/disputes/{disputeId}/staff-decision` | Bearer JWT | Operation staffDecideAlias. |
| 24 | POST | `/api/v1/disputes/{disputeId}/route-staff` | Bearer JWT | Operation routeDisputeStaff. |
| 25 | POST | `/api/v1/disputes/{disputeId}/execute-settlement` | Bearer JWT | Operation executeDisputeSettlementAlias. |
| 26 | POST | `/api/v1/disputes/{disputeId}/escalation-request` | Bearer JWT | Operation requestEscalation. |
| 27 | POST | `/api/v1/disputes/{disputeId}/cancel` | Bearer JWT | Operation cancelDispute. |
| 28 | POST | `/api/v1/disputes/staff-sla-escalate` | Bearer JWT | Operation escalateOverdueStaffDisputes. |
| 29 | GET | `/api/v1/contracts/{contractId}/termination-requests` | Bearer JWT | Operation listTerminationRequests. |
| 30 | POST | `/api/v1/contracts/{contractId}/termination-requests` | Bearer JWT | Operation requestTermination. |
| 31 | POST | `/api/v1/contracts/{contractId}/sign` | Bearer JWT | Operation signContract. |
| 32 | GET | `/api/v1/contracts/{contractId}/reviews` | Bearer JWT | Operation listContractReviews. |
| 33 | POST | `/api/v1/contracts/{contractId}/reviews` | Bearer JWT | Operation createContractReview. |
| 34 | POST | `/api/v1/contracts/{contractId}/reject` | Bearer JWT | Operation rejectContract. |
| 35 | POST | `/api/v1/contracts/{contractId}/nda-sign` | Bearer JWT | Operation signNda. |
| 36 | GET | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports` | Bearer JWT | Operation listProgressReports. |
| 37 | POST | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports` | Bearer JWT | Expert submits scheduled, requested, or voluntary progress report. |
| 38 | POST | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback` | Bearer JWT | Business records feedback for a progress report; feedback also acknowledges it when pending. |
| 39 | POST | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/acknowledge` | Bearer JWT | Business acknowledges the latest progress report to unlock the next submission. |
| 40 | POST | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-report-request` | Bearer JWT | Business requests report with 24h/12h SLA. |
| 41 | POST | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit` | Bearer JWT | Operation depositMilestone. |
| 42 | POST | `/api/v1/contracts/{contractId}/milestones/sla-auto-approve` | Bearer JWT | Operation autoApproveReviewSla. |
| 43 | POST | `/api/v1/contracts/{contractId}/milestones/check-overdue` | Bearer JWT | Operation checkOverdue. |
| 44 | POST | `/api/v1/contracts/{contractId}/immediate-termination` | Bearer JWT | Operation immediateTermination. |
| 45 | POST | `/api/v1/contracts/{contractId}/expert-deposit/pay` | Bearer JWT | Operation payExpertContractDeposit. |
| 46 | POST | `/api/v1/contracts/{contractId}/deposit/pay` | Bearer JWT | Operation payContractDeposit. |
| 47 | POST | `/api/v1/contracts/{contractId}/cancel-draft` | Bearer JWT | Operation cancelDraftContract. |
| 48 | POST | `/api/v1/contracts/from-proposals/{proposalId}` | Bearer JWT | Operation createDraft. |
| 49 | GET | `/api/v1/case-attachments` | Bearer JWT | Operation listCaseAttachments. |
| 50 | POST | `/api/v1/case-attachments` | Bearer JWT | Operation createCaseAttachment. |
| 51 | POST | `/api/v1/admin/contracts/{contractId}/deposits/refund` | Bearer JWT | Operation refundContractDeposits. |
| 52 | PATCH | `/api/v1/milestones/{milestoneId}` | Bearer JWT | Operation updateMilestone. |
| 53 | GET | `/api/v1/termination-requests/{terminationRequestId}` | Bearer JWT | Operation getTerminationRequest. |
| 54 | GET | `/api/v1/disputes/{disputeId}` | Bearer JWT | Operation getDispute. |
| 55 | GET | `/api/v1/disputes/{disputeId}/staff-candidates` | Bearer JWT | Operation listStaffCandidates. |
| 56 | GET | `/api/v1/contracts` | Bearer JWT | Operation listContracts. |
| 57 | GET | `/api/v1/contracts/{contractId}` | Bearer JWT | Operation getContract. |
| 58 | GET | `/api/v1/contracts/{contractId}/milestones` | Bearer JWT | Operation listMilestones. |
| 59 | GET | `/api/v1/contracts/{contractId}/disputes` | Bearer JWT | Operation listDisputes. |
| 60 | POST | `/api/v1/milestones/{milestoneId}/source-code-file` | Bearer JWT | Approved assigned Expert uploads one ZIP source archive before the milestone deadline, maximum 50 MB. |

## Notification Flow

- Giai thich flow: Thong bao trong he thong.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | PATCH | `/api/v1/notifications/{notificationId}/read` | Bearer JWT | Operation markAsRead. |
| 2 | PATCH | `/api/v1/notifications/read-all` | Bearer JWT | Operation markAllAsRead. |
| 3 | GET | `/api/v1/notifications` | Bearer JWT | Operation listMine. |
| 4 | GET | `/api/v1/notifications/unread-count` | Bearer JWT | Operation countUnreadMine. |

## Catalog & Reference Flow

- Giai thich flow: Danh muc domain, skill va technology.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/technologies` | Bearer JWT | Operation listTechnologies. |
| 2 | POST | `/api/v1/technologies` | Bearer JWT | Operation createTechnology. |
| 3 | GET | `/api/v1/skills` | Public | Operation listSkills. |
| 4 | POST | `/api/v1/skills` | Bearer JWT | Operation createSkill. |
| 5 | GET | `/api/v1/domains` | Public | Operation listDomains. |
| 6 | POST | `/api/v1/domains` | Bearer JWT | Operation createDomain. |
| 7 | PATCH | `/api/v1/technologies/{technologyId}` | Bearer JWT | Operation updateTechnology. |
| 8 | DELETE | `/api/v1/technologies/{technologyId}` | Bearer JWT | Admin soft-deletes/deactivates a technology. |
| 9 | PATCH | `/api/v1/skills/{skillId}` | Bearer JWT | Operation updateSkill. |
| 10 | DELETE | `/api/v1/skills/{skillId}` | Bearer JWT | Admin soft-deletes/deactivates a skill. |
| 11 | PATCH | `/api/v1/domains/{domainId}` | Bearer JWT | Operation updateDomain. |
| 12 | DELETE | `/api/v1/domains/{domainId}` | Bearer JWT | Admin soft-deletes/deactivates a domain. |

## AI & Matching Flow

- Giai thich flow: Chatbot va cac endpoint AI ho tro nghiep vu.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/chatbot/ask` | Public | Operation ask. |

## Admin & Governance Flow

- Giai thich flow: Quan tri account, settings, analytics, reviews va wallet system.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/v1/admin/wallet/sync` | Bearer JWT | Operation syncSystemWallet. |
| 2 | GET | `/api/v1/admin/staffs` | Bearer JWT | Operation listStaffs. |
| 3 | POST | `/api/v1/admin/staffs` | Bearer JWT | Operation createStaff. |
| 4 | POST | `/api/v1/admin/reviews` | Bearer JWT | Operation createReview. |
| 5 | GET | `/api/v1/admin/accounts` | Bearer JWT | Operation listAccounts. |
| 6 | POST | `/api/v1/admin/accounts` | Bearer JWT | Operation createAccount. |
| 7 | PATCH | `/api/v1/admin/staffs/{staffId}` | Bearer JWT | Operation updateStaff. |
| 8 | PATCH | `/api/v1/admin/settings/{key}` | Bearer JWT | Operation updateSetting. |
| 9 | POST | `/api/v1/admin/settings` | Bearer JWT | Admin creates a system setting. |
| 10 | PUT | `/api/v1/admin/settings/{key}` | Bearer JWT | Admin updates system setting metadata by request body. |
| 11 | DELETE | `/api/v1/admin/settings/{key}` | Bearer JWT | Admin soft-deletes/deactivates a system setting. |
| 12 | GET | `/api/v1/admin/membership/packages` | Bearer JWT | Admin lists membership packages, optionally active-only. |
| 13 | POST | `/api/v1/admin/membership/packages` | Bearer JWT | Admin creates a membership package. |
| 14 | PATCH | `/api/v1/admin/membership/packages/{packageId}` | Bearer JWT | Admin updates a membership package. |
| 15 | DELETE | `/api/v1/admin/membership/packages/{packageId}` | Bearer JWT | Admin soft-deletes/deactivates a membership package. |
| 16 | DELETE | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Operation deactivateAccount. |
| 17 | PATCH | `/api/v1/admin/accounts/{accountId}` | Bearer JWT | Operation updateAccount. |
| 18 | PATCH | `/api/v1/admin/accounts/{accountId}/status` | Bearer JWT | Operation setAccountStatus. |
| 19 | PATCH | `/api/v1/admin/accounts/{accountId}/active` | Bearer JWT | Operation setAccountActive. |
| 20 | GET | `/api/v1/admin/wallet` | Bearer JWT | Operation systemWallet. |
| 21 | GET | `/api/v1/admin/wallet/transactions` | Bearer JWT | Operation platformWalletTransactions. |
| 22 | GET | `/api/v1/admin/settings` | Bearer JWT | Operation listSettings. |
| 23 | GET | `/api/v1/admin/reviews/contracts/{contractId}` | Bearer JWT | Operation listReviewsByContract. |
| 24 | GET | `/api/v1/admin/disputes` | Bearer JWT | Operation listDisputes_1. |
| 25 | GET | `/api/v1/admin/disputes/{disputeId}` | Bearer JWT | Operation getDisputeDetail. |
| 26 | GET | `/api/v1/admin/audit-logs` | Bearer JWT | Operation listAuditLogs. |
| 27 | GET | `/api/v1/admin/analytics/overview` | Bearer JWT | Operation analyticsOverview. |
| 28 | GET | `/api/v1/admin/dashboard/summary` | Bearer JWT | Admin dashboard summary cards. |
| 29 | GET | `/api/v1/admin/dashboard/revenue` | Bearer JWT | Chart-ready revenue series and transaction-type breakdown. |
| 30 | GET | `/api/v1/admin/dashboard/contracts` | Bearer JWT | Contract status breakdown and created trend. |
| 31 | GET | `/api/v1/admin/dashboard/users` | Bearer JWT | User role/status breakdown and new-user trend. |
| 32 | GET | `/api/v1/admin/dashboard/jobs-proposals` | Bearer JWT | Marketplace job/proposal funnel and trends. |
| 33 | GET | `/api/v1/admin/dashboard/disputes` | Bearer JWT | Dispute status, SLA, and created trend. |
| 34 | GET | `/api/v1/admin/dashboard/membership` | Bearer JWT | Membership purchase revenue and package breakdown. |
| 35 | GET | `/api/v1/admin/dashboard/finance-breakdown` | Bearer JWT | Finance balances, gross volume, withdrawals, and transaction breakdown. |

## System & Test Flow

- Giai thich flow: Health check va endpoint test ky thuat.

| # | Method | Path | Auth | Giai thich |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/v1/staff/disputes` | Bearer JWT | Operation listStaffDisputes. |
| 2 | GET | `/api/test/secure` | Bearer JWT | Operation secure. |
| 3 | GET | `/api/health` | Public | Operation health. |

# Data Dictionary - AITASKER

## 1) Mục tiêu
Tài liệu mô tả nhanh các bảng dữ liệu cốt lõi, mục đích sử dụng và quan hệ chính để team FE/BE đồng bộ nghiệp vụ.

## 2) Danh sách bảng (19 bảng)

### Roles
- Mục đích: Danh mục vai trò hệ thống (`BUSINESS`, `EXPERT`, `ADMIN`, `STAFF`).
- Quan hệ chính: `Account.role_id -> Roles.role_id`.

### Account
- Mục đích: Tài khoản đăng nhập trung tâm.
- Cột nổi bật: `email` (unique), `password`, `role_id`, `is_active`.
- Quan hệ chính: FK tới `Roles`; 1-1 tới `BusinessProfiles` hoặc `ExpertProfiles`; 1-1 tới `Staffs` (nếu là staff/admin).

### Staffs
- Mục đích: Hồ sơ nhân sự vận hành nội bộ.
- Quan hệ chính: `Staffs.account_id -> Account.account_id`.

### BusinessProfiles
- Mục đích: Hồ sơ pháp nhân doanh nghiệp (KYB).
- Cột nổi bật: `tax_code`, `company_name`, `kyb_status`.
- Quan hệ chính: `account_id -> Account`; `approved_by -> Staffs`.

### ExpertProfiles
- Mục đích: Hồ sơ chuyên gia (KYC).
- Cột nổi bật: `national_id`, `kyc_status`.
- Quan hệ chính: `account_id -> Account`; `approved_by -> Staffs`.

### Portfolios
- Mục đích: Hồ sơ năng lực AI của chuyên gia.
- Quan hệ chính: `expert_id -> ExpertProfiles.expert_id`.

### Jobs
- Mục đích: Bài toán/tin tuyển dụng của doanh nghiệp.
- Cột nổi bật: `structured_sow`, `ai_tag`, `budget`, `status`.
- Quan hệ chính: `business_id -> BusinessProfiles.business_id`.

### Proposals
- Mục đích: Hồ sơ dự thầu của chuyên gia cho job.
- Cột nổi bật: `bid_amount`, `status`.
- Quan hệ chính: `job_id -> Jobs`; `expert_id -> ExpertProfiles`.
- Ràng buộc nghiệp vụ: unique theo cặp `(job_id, expert_id)`.

### Contracts
- Mục đích: Hợp đồng giữa doanh nghiệp và chuyên gia.
- Cột nổi bật: `total_budget`, `timeline_days`, `business_accepted_at`, `expert_accepted_at`, `business_nda_signed_at`, `expert_nda_signed_at`, `status`.
- Quan hệ chính: `job_id -> Jobs`; `business_id -> BusinessProfiles`; `expert_id -> ExpertProfiles`.

### ContractChangeRequests
- Mục đích: Lịch sử yêu cầu sửa đổi hợp đồng trong quá trình đàm phán.
- Quan hệ chính: `contract_id -> Contracts`; `requested_by_account_id -> Account`; `reviewed_by_account_id -> Account`.

### Milestones
- Mục đích: Các cột mốc thực thi trong hợp đồng.
- Quan hệ chính: `contract_id -> Contracts.contract_id`.

### AcceptanceCriteria
- Mục đích: Tiêu chí nghiệm thu chi tiết theo milestone.
- Quan hệ chính: `milestone_id -> Milestones.milestone_id`.

### Deliverables
- Mục đích: Sản phẩm bàn giao cho milestone.
- Quan hệ chính: `milestone_id -> Milestones.milestone_id`.

### Transactions
- Mục đích: Dòng tiền ký quỹ/giải ngân/hoàn tiền.
- Cột nổi bật: `amount`, `commission_fee`, `transaction_type`, `status`.
- Quan hệ chính: `milestone_id -> Milestones.milestone_id`.

### Invoices
- Mục đích: Hóa đơn/chứng từ giao dịch.
- Quan hệ chính: `transaction_id -> Transactions.transaction_id`.

### Payment Wallet Tables
- `payment_order`: PayOS wallet top-up orders and provider metadata.
- `system_wallet`: wallet balance snapshot per account/role.
- `wallet_transactions`: real wallet ledger movements.
- `membership_packages`: configurable Business/Expert packages.
- `membership_purchases`: membership purchase history and badge range.
- `user_quotas`: job-post/proposal credit balances, initial free quota grants,
  badge expiration, and Premium entitlement expiration.
- `quota_usage_logs`: grant, purchase, consume, and adjust quota history,
  including `INITIAL_BUSINESS_GRANT` and `INITIAL_EXPERT_GRANT`.
- `contract_deposits`: 20% contract security deposit hold/refund lifecycle.
- `withdrawal_requests`: manual withdrawal requests and admin review.

### Reviews
- Mục đích: Đánh giá chéo sau khi hợp đồng hoàn tất.
- Cột nổi bật: `rating` (1-5), `comment`.
- Quan hệ chính: `contract_id -> Contracts`; `reviewer_id -> Account`; `reviewee_id -> Account`.

### Disputes
- Mục đích: Quản lý khiếu nại/tranh chấp.
- Cột nổi bật: `proposed_action`, `status`.
- Quan hệ chính: `contract_id -> Contracts`; `milestone_id -> Milestones`; `assigned_staff_id -> Staffs`; `admin_approved_by -> Account`.

### AuditLogs
- Mục đích: Ghi vết thao tác nhạy cảm (audit trail).
- Cột nổi bật: `action`, `entity_name`, `entity_id`, `old_value_json`, `new_value_json`.
- Quan hệ chính: `actor_account_id -> Account`.

### SystemSettings
- Mục đích: Cấu hình tham số lõi hệ thống.
- Ví dụ: `platform_fee_percent`, `default_sla_days`, `auto_assign_staff_enabled`.
- Quan hệ chính: `updated_by -> Account` (nếu có).

## 3) Quy ước trạng thái (gợi ý)
- KYB/KYC: `Pending`, `Approved`, `Rejected`.
- Proposal: `Submitted`, `Shortlisted`, `Accepted`, `Rejected`, `Withdrawn`.
- Job: `DRAFT`, `OPEN`, `IN_PROGRESS`, `CLOSED`.
- Contract: `DRAFT`, `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
- Milestone: `PENDING`, `DEPOSITED`, `IN_PROGRESS`, `UNDER_REVIEW`, `DISPUTED`, `COMPLETED`.
- Wallet transaction: `POSTED`.
- Transaction: `Pending`, `Processing`, `Success`, `Failed`, `Cancelled`.
- Dispute: `Open`, `UnderReview`, `Resolved`, `Rejected`, `Escalated`.

## 4) Ghi chú làm việc nhóm
- Mọi thay đổi schema phải đi qua Flyway migration mới (`Vn__...sql`), không sửa migration đã chạy.
- Mọi API mới phải cập nhật tài liệu này nếu thay đổi ý nghĩa dữ liệu.

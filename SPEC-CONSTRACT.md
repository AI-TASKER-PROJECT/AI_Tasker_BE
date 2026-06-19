# CONTRACT MANAGEMENT SPECIFICATION

## Module

Contract Management

## Actors

* Business
* Expert
* Admin
* Staff (Dispute Only)

---

# 1. Overview

Sau khi Business chấp nhận Proposal của một Expert, hệ thống cho phép Business tạo hợp đồng nháp (Draft Contract).

Hợp đồng được hình thành dựa trên:

* Job đã đăng
* Milestone của Job
* Thay đổi ngân sách hoặc đề xuất từ Proposal

Contract chỉ trở thành hợp đồng chính thức (Active Contract) khi:

* Business ký hợp đồng
* Expert ký hợp đồng
* Business ký NDA
* Expert ký NDA

Sau khi hợp đồng được kích hoạt:

* Job chuyển sang trạng thái In Progress
* Expert được phép thực hiện công việc
* Các milestone được cập nhật theo ngân sách cuối cùng đã thống nhất

---

# 2. Contract Lifecycle

```text
Accepted Proposal
    ↓
Draft Contract
    ↓
Negotiating (optional)
    ↓
Active
    ↓
Completed

or

Active
    ↓
Terminated

or

Draft/Negotiating
    ↓
Cancelled
```

---

# 3. Contract Status

| Status      | Description                      |
| ----------- | -------------------------------- |
| DRAFT       | Contract vừa được tạo            |
| NEGOTIATING | Có yêu cầu chỉnh sửa             |
| ACTIVE      | Hai bên đã ký Contract + NDA     |
| COMPLETED   | Toàn bộ milestone hoàn thành     |
| TERMINATED  | Hợp đồng bị chấm dứt             |
| CANCELLED   | Hợp đồng bị từ chối hoặc hết hạn |

---

# 4. Create Draft Contract

## User Story

As a Business

I want to create a draft contract from an accepted proposal

So that I can formalize cooperation with an Expert.

---

## Preconditions

* Business account Approved
* Proposal status = Accepted
* Proposal chưa có Contract
* Job thuộc Business hiện tại
* Job có ít nhất 1 Milestone

---

## Flow

1. Business chọn "Create Contract"
2. Hệ thống lấy dữ liệu từ:

    * Job
    * Job Milestones
    * Accepted Proposal
3. Hệ thống tạo Contract Draft
4. Hệ thống copy milestone sang Contract Milestones

---

## Acceptance Criteria

### AC-01

Given Proposal đã được Accept

When Business tạo Contract

Then hệ thống tạo Contract thành công.

---

### AC-02

Contract Status mặc định là:

```text
DRAFT
```

---

### AC-03

Contract phải liên kết:

* Job
* Proposal
* Business
* Expert

---

### AC-04

Contract Milestones được tạo từ Job Milestones.

---

# 5. Review Contract

## User Story

As an Expert

I want to review the draft contract

So that I can decide whether to accept or negotiate.

---

## Available Actions

### Accept Contract

### Request Change

### Reject Contract

---

# 6. Change Request

## User Story

As a Business or Expert

I want to request changes to a draft contract

So that both parties can negotiate before signing.

---

## Flow

1. User gửi Change Request
2. Contract chuyển trạng thái:

```text
NEGOTIATING
```

3. Toàn bộ chữ ký bị reset

---

## Acceptance Criteria

### AC-05

Change Request chỉ được tạo khi:

```text
DRAFT
NEGOTIATING
```

---

### AC-06

Sau Change Request:

```text
business_accepted_at = null
expert_accepted_at = null

business_nda_signed_at = null
expert_nda_signed_at = null

activated_at = null
```

---

### AC-07

Contract chuyển sang:

```text
NEGOTIATING
```

---

# 7. Reject Contract

## User Story

As an Expert

I want to reject a draft contract

So that I am not forced to participate.

---

## Acceptance Criteria

### AC-08

Contract chỉ được reject khi:

```text
DRAFT
NEGOTIATING
```

---

### AC-09

Sau reject:

```text
Contract Status = CANCELLED
```

---

### AC-10

Job quay về trạng thái:

```text
PROPOSAL_REVIEW
```

để Business có thể chọn Expert khác.

---

# 8. Sign Contract

## User Story

As a Business/Expert

I want to sign the contract

So that the agreement can proceed.

---

## Flow

Business ký:

```text
business_accepted_at
```

Expert ký:

```text
expert_accepted_at
```

---

## Acceptance Criteria

### AC-11

Chỉ Business hoặc Expert thuộc Contract mới được ký.

---

### AC-12

Chỉ được ký khi:

```text
DRAFT
NEGOTIATING
```

---

# 9. Sign NDA

## User Story

As a Business/Expert

I want to sign NDA

So that confidential information is protected.

---

## Flow

Business:

```text
business_nda_signed_at
```

Expert:

```text
expert_nda_signed_at
```

---

## Acceptance Criteria

### AC-13

NDA chỉ được ký khi:

```text
DRAFT
NEGOTIATING
```

---

# 10. Activate Contract

## User Story

As the System

I want to automatically activate the contract

So that project execution can begin.

---

## Activation Conditions

```text
business_accepted_at != null

expert_accepted_at != null

business_nda_signed_at != null

expert_nda_signed_at != null
```

---

## System Actions

### Update Contract

```text
status = ACTIVE

activated_at = now()
```

### Update Job

```text
status = IN_PROGRESS
```

### Update Milestones

* Apply budget from Contract Milestones
* Attach Contract ID

---

## Acceptance Criteria

### AC-14

Contract tự động Active khi đủ 4 chữ ký.

---

### AC-15

Không cần API Activate riêng.

---

### AC-16

Job chuyển thành:

```text
IN_PROGRESS
```

---

# 11. Submit Deliverable

## User Story

As an Expert

I want to submit milestone deliverables

So that Business can review my work.

---

## Preconditions

* Contract ACTIVE
* Expert thuộc Contract

---

## Flow

1. Expert upload Deliverable
2. Milestone chuyển:

```text
UNDER_REVIEW
```

3. Notification gửi cho Business

---

## Acceptance Criteria

### AC-17

Không được submit nếu Contract chưa ACTIVE.

---

### AC-18

Milestone chuyển:

```text
UNDER_REVIEW
```

---

# 12. Complete Contract

## User Story

As the System

I want to complete the contract

When all milestones are approved.

---

## Completion Conditions

Tất cả milestone:

```text
COMPLETED
```

---

## System Actions

```text
Contract Status = COMPLETED

Job Status = CLOSED
```

---

## Acceptance Criteria

### AC-19

Contract tự động Completed khi toàn bộ milestone hoàn tất.

---

### AC-20

Job chuyển:

```text
CLOSED
```

---

# 13. Terminate Contract

## User Story

As a Business or Admin

I want to terminate a contract

When cooperation can no longer continue.

---

## Acceptance Criteria

### AC-21

Business phải là owner Contract.

---

### AC-22

Không được terminate nếu:

```text
COMPLETED
TERMINATED
CANCELLED
```

---

### AC-23

Sau terminate:

```text
Contract Status = TERMINATED
```

---

# 14. Notifications

## System Notification Events

### Contract Created

Gửi cho Expert.

---

### Contract Accepted

Gửi cho bên còn lại.

---

### NDA Signed

Gửi cho bên còn lại.

---

### Contract Activated

Gửi cho Business và Expert.

---

### Deliverable Submitted

Gửi cho Business.

---

### Contract Completed

Gửi cho Business và Expert.

---

### Contract Terminated

Gửi cho Business và Expert.

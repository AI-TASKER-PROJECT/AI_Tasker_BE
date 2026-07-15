# Flow Tranh Chấp (Dispute)

**Phiên bản:** v2.4
**Spec:** `SPEC-MILESTONE-DISPUTER.md` mục 10
**Implement:** US-045 + US-048 + US-050 + US-062

---

## Tổng quan

Tranh chấp là bất đồng chính thức về milestone hiện tại, deliverable, phạm vi
công việc, tiêu chí chấp nhận, hành vi review, hoặc bằng chứng công việc.

Cả Business và Expert đều có thể khởi tạo tranh chấp. **Reject deliverable không
bao giờ tự động tạo tranh chấp** — phải gọi API dispute một cách rõ ràng.

v2.3 thay đổi so với v2.2:
- Admin **không** tham gia routing/cancel milestone-dispute — chỉ Staff
- Staff **không thể** reject intervention (bỏ `/reject-intervention`)
- Escalation **tự động route** tới Staff thay vì Admin assign thủ công
- Staff **có thể quyết định trước** evidence deadline 48h
- Staff decision **tự động trigger settlement** (không cần Admin execute riêng)
- `INTERVENTION_REJECTED` không còn là valid status cho dispute mới

v2.4 bổ sung cân bằng tải Staff:
- Chỉ Staff có account `Approved`, khớp domain và không trùng tài khoản participant mới đủ điều kiện
- Tạo nhóm đủ chuyên môn từ domain coverage 60% + skill coverage 40%
- Trong nhóm đủ chuyên môn, ưu tiên workload thấp nhất
- Giới hạn tải bằng `dispute_staff_max_active_cases`, mặc định `5`
- Hết capacity thì giữ dispute ở `ESCALATION_REQUESTED`, không gán quá tải
- Khóa Staff pool trong transaction để hai request đồng thời không đọc cùng workload cũ
- Seed bảo đảm mỗi business domain có tối thiểu 2 Staff

---

## Sơ đồ trạng thái Dispute

```text
                    PENDING_SELF_RESOLVE
                    /        |          \
                   /         |           \
        ESCALATION_REQUESTED |            CANCELLED (initiator withdraw)
              |\             | RESOLVED (Business approve sau self-resolve)
              | \-- no eligible/capacity --> giữ ESCALATION_REQUESTED
              |              |
        STAFF_REVIEWING       |
              |               |
        STAFF_DECIDED ──auto──┤
              |               |
        RESOLVED (STAFF_DECISION_SETTLEMENT)
```

**Active statuses:** `PENDING_SELF_RESOLVE`, `ESCALATION_REQUESTED`, `STAFF_REVIEWING`, `STAFF_DECIDED`  
**Không hợp lệ cho dispute mới:** `INTERVENTION_REJECTED`

---

## Sơ đồ flow

```text
 milestone IN_PROGRESS / OVERDUE / UNDER_REVIEW
    │
    ├── 1. Khởi tạo dispute (POST /milestones/{milestoneId}/disputes)
    │   │
    │   ├── Actor: Business hoặc Expert
    │   ├── [Gate] milestone không PENDING / DEPOSITED / COMPLETED / CANCELLED
    │   ├── [Gate] không có active dispute nào trên milestone
    │   ├── [Gate] không có active termination request trên contract
    │   │
    │   ├── Business dispute:
    │   │   └── initiation_type = BUSINESS_REJECTED_DELIVERABLE (kèm deliverable bị reject)
    │   │
    │   ├── Expert dispute:
    │   │   ├── EXPERT_SCOPE_CONCERN        (Business yêu cầu ngoài scope)
    │   │   ├── EXPERT_NO_REVIEW_RESPONSE    (Business không review)
    │   │   └── EXPERT_BAD_FAITH_REJECTION   (Business reject không công bằng)
    │   │
    │   ├── Lưu previous_milestone_status
    │   ├── status = PENDING_SELF_RESOLVE
    │   ├── milestone → DISPUTED
    │   └── 1 active dispute / milestone (unique index partial)
    │
    ├── 2. Self-resolve phase
    │   │
    │   ├── Expert sửa & nộp lại deliverable → Business approve
    │   │   ├── resolveByBusinessApproval
    │   │   ├── dispute → RESOLVED
    │   │   ├── resolution_type = BUSINESS_APPROVED_AFTER_SELF_RESOLVE
    │   │   └── milestone → COMPLETED (escrow release)
    │   │
    │   └── Initiator withdraw (POST /disputes/{disputeId}/cancel)
    │       ├── [Gate] initiator mới được cancel (không phải Admin)
    │       ├── [Gate] status = PENDING_SELF_RESOLVE / ESCALATION_REQUESTED
    │       ├── [Gate] assignedStaffId = null (chưa route tới Staff)
    │       ├── status → CANCELLED
    │       ├── resolution_type = CANCELLED_BY_INITIATOR
    │       └── milestone → khôi phục previous_milestone_status
    │
    ├── 3. Yêu cầu Staff can thiệp (POST /disputes/{disputeId}/escalation-request)
    │   │
    │   ├── Actor: Business hoặc Expert
    │   ├── [Gate] dispute status = PENDING_SELF_RESOLVE
    │   ├── [Gate] cần reason không được rỗng; evidenceFile là tùy chọn
    │   │
    │   ├── Lưu escalationReason, escalationEvidenceFile
    │   ├── escalationRequestedByAccountId + escalationRequestedAt
    │   ├── status = ESCALATION_REQUESTED
    │   │
    │   ├── Auto-route tới Staff (v2.3)
    │   │   ├── selectStaffForDispute() — chọn Staff theo:
    │   │   │   ├── Bắt buộc khớp ít nhất 1 Job domain + account Approved
    │   │   │   ├── Nhóm đủ chuyên môn: domain coverage 60% + skill coverage 40%
    │   │   │   ├── Chưa đạt dispute_staff_max_active_cases
    │   │   │   ├── Active workload ASC, specialization score DESC
    │   │   │   └── Last assigned ASC, staffId ASC
    │   │   │
    │   │   └── routeDisputeToStaff:
    │   │       ├── assignedStaffId
    │   │       ├── status = STAFF_REVIEWING
    │   │       ├── staffReviewStartedAt = now
    │   │       ├── evidenceCollectionDueAt = now + 48h
    │   │       ├── staffAccessScope = READ_EXECUTE
    │   │       ├── staffAccessExpiresAt = due + 3 days
    │   │       └── staffSlaDueAt = due + 3 days
    │   │
    │   └── Notify Staff của escalation + Staff được assign
    │
    ├── 4. Route Staff thủ công (POST /disputes/{disputeId}/route-staff)
    │   │
    │   ├── Actor: Staff (không phải Admin)
    │   ├── [Gate] dispute status = ESCALATION_REQUESTED
    │   │
    │   ├── staffId = null → auto-pick (selectStaffForDispute)
    │   ├── staffId != null → route chỉ định
    │   └── Audit: DISPUTE_STAFF_ROUTED
    │
    ├── 5. Staff review (STAFF_REVIEWING)
    │   │
    │   ├── Business / Expert thêm evidence trong 48h window
    │   │   └── POST /case-attachments (owner_type = DISPUTE)
    │   │
    │   ├── Staff inspect: dispute files, deliverables, SoW, criteria, timeline
    │   │
    │   └── Staff KHÔNG thể reject intervention (v2.3)
    │       └── Đã bỏ: POST /disputes/{disputeId}/reject-intervention
    │
    ├── 6. Staff ra quyết định (POST /disputes/{disputeId}/staff-decision)
    │   │
    │   ├── Actor: Staff (được assign)
    │   ├── [Gate] dispute status = STAFF_REVIEWING
    │   │
    │   ├── Có thể quyết định trước evidenceCollectionDueAt
    │   │   └── v2.3 bỏ guard EVIDENCE_WINDOW_STILL_OPEN
    │   │
    │   ├── expertPayoutPercentage (0% - 100%)
    │   │   ├── expertPayoutAmount = escrow × percentage / 100
    │   │   └── businessRefundAmount = escrow - expertPayout (trừ để tránh rounding)
    │   │
    │   ├── Lưu: staffDecisionPercentage, staffDecisionNote, staffReport,
    │   │        expertPayoutAmount, businessRefundAmount
    │   │
    │   ├── status = STAFF_DECIDED → saved
    │   ├── staffDecidedAt = now
    │   │
    │   └── **Auto-trigger settlement (v2.3 inline)**
    │       └── executeDisputeSettlementInternal (xem bước 7)
    │
    └── 7. Settlement execution
        │
        ├── [Gate] status = STAFF_DECIDED
        ├── [Gate] escrow_released_at IS NULL (chưa release trước đó)
        │
        ├── Lock: milestone + wallet rows (Business + Expert)
        │
        ├── Wallet movements:
        │   ├── Debit: Business escrow → full milestone amount
        │   ├── Credit: Expert available → expertPayoutAmount
        │   └── Credit: Business available → businessRefundAmount
        │
        ├── milestone:
        │   ├── escrow_released_at = now
        │   ├── settlement_source_type = DISPUTE
        │   ├── settlement_source_id = disputeId
        │   ├── resolved_by_dispute_id = disputeId
        │   └── status = COMPLETED
        │
        ├── dispute:
        │   ├── settlement_executed_at = now
        │   ├── status = RESOLVED
        │   └── resolution_type = STAFF_DECISION_SETTLEMENT
        │
        ├── Audit: DISPUTE_SETTLEMENT_EXECUTED
        ├── Notify Business + Expert
        │
        └── Kiểm tra contract completion:
            └── Nếu tất cả milestones COMPLETED → contract COMPLETED
                └── Auto refund participant deposits → contract CLOSED
```

---

## Luồng chọn và cân bằng Staff (US-062)

### 1. Dữ liệu đầu vào

```text
dispute
  -> contract
      -> job_domains
      -> job_skills

system_settings
  -> dispute_staff_max_active_cases (default = 5)

staffs
  -> account.status
  -> staff_domains
  -> staff_skills
  -> active disputes hiện tại
  -> staffReviewStartedAt gần nhất
```

### 2. Khóa và tạo danh sách ứng viên

Khi auto-route hoặc `route-staff` thủ công, backend chạy trong một transaction:

```text
1. Lock toàn bộ Staff rows theo staffId ASC (PESSIMISTIC_WRITE)
2. Đọc lại workload sau khi lấy lock
3. Tính ứng viên và chọn Staff
4. Gán assignedStaffId + lưu dispute
5. Commit -> giải phóng lock
```

Việc lock theo cùng một thứ tự giúp hai request routing đồng thời không cùng chọn
một Staff dựa trên workload cũ và giảm nguy cơ deadlock.

### 3. Hard eligibility gates

Một Staff chỉ đi tiếp khi thỏa toàn bộ điều kiện:

```text
account.status = Approved
AND matchedJobDomains >= 1
AND staff.accountId không thuộc Business/Expert participant của contract
```

Staff không khớp domain, account chưa được duyệt hoặc xung đột participant sẽ bị
loại trước khi tính điểm. `PROFILE_REVIEW` vẫn tách biệt và không được dùng làm
fallback cho tranh chấp hợp đồng.

### 4. Tính điểm chuyên môn

Khi job có cả domain và skill:

```text
domainCoverage = matchedDomainCount / totalJobDomainCount
skillCoverage  = matchedSkillCount  / totalJobSkillCount

specializationScore = domainCoverage * 60% + skillCoverage * 40%
```

Nếu job không có skill, điểm chuyên môn bằng `domainCoverage`.

Ngưỡng nhóm đủ chuyên môn:

```text
bestScore = điểm cao nhất trong danh sách eligible

Nếu bestScore >= 70%:
  threshold = max(70%, bestScore - 20%)
Nếu bestScore < 70%:
  threshold = max(0%, bestScore - 20%)

qualified = specializationScore >= threshold
```

Cơ chế này giữ Staff đủ gần với ứng viên tốt nhất, nhưng không để một Staff hơn
nhẹ về chuyên môn nhận toàn bộ đơn.

### 5. Capacity và workload

```text
active workload = số dispute được gán có status thuộc:
  - PENDING_SELF_RESOLVE
  - ESCALATION_REQUESTED
  - STAFF_REVIEWING
  - STAFF_DECIDED

capacity = dispute_staff_max_active_cases (default 5)
available = active workload < capacity
```

Giá trị `availability` trả về trong candidate response:

| Giá trị | Điều kiện |
|---|---|
| `IDLE` | workload = 0 |
| `BUSY` | 0 < workload < capacity |
| `AT_CAPACITY` | workload >= capacity |

### 6. Thứ tự auto-route

Auto-route chỉ chọn Staff vừa `qualified` vừa `available`, sau đó sắp xếp:

```text
1. active workload ASC
2. specializationScore DESC
3. lastAssignedAt ASC (chưa từng nhận được ưu tiên trước)
4. staffId ASC
```

Ví dụ hai Staff ngang chuyên môn và cùng capacity:

```text
6 dispute liên tiếp
  -> Staff A: 3 dispute
  -> Staff B: 3 dispute
```

### 7. Kết quả auto-route

```text
Có qualified Staff còn capacity
  -> assignedStaffId = selectedStaffId
  -> status = STAFF_REVIEWING
  -> tạo evidence/SLA deadlines
  -> notify assigned Staff + contract participants

Không có Staff khớp domain/Approved/conflict-free
  -> status giữ ESCALATION_REQUESTED
  -> assignedStaffId giữ null

Có Staff đủ chuyên môn nhưng tất cả đạt capacity
  -> status giữ ESCALATION_REQUESTED
  -> assignedStaffId giữ null
```

Không có fallback sang Staff ngoài domain và không gán vượt capacity.

### 8. Route thủ công

`POST /api/v1/disputes/{disputeId}/route-staff?staffId=...`

```text
staffId = null
  -> chạy đúng auto-route algorithm

staffId có giá trị
  -> bắt buộc account Approved
  -> bắt buộc khớp ít nhất 1 domain
  -> bắt buộc không xung đột participant
  -> bắt buộc workload < capacity
  -> Staff có thể override thứ tự specialization trong nhóm eligible
```

Các kết quả lỗi khi gọi routing trực tiếp:

| Mã lỗi | Khi nào |
|---|---|
| `NO_MATCHING_STAFF_FOR_JOB_DOMAIN` | Auto-route không có Staff eligible |
| `NO_AVAILABLE_STAFF_CAPACITY` | Auto-route có ứng viên nhưng không còn capacity |
| `STAFF KHONG HOAT DONG HOAC KHONG CO DOMAIN TUONG UNG VOI JOB` | Staff chỉ định không eligible |
| `STAFF_DA_DAT_GIOI_HAN_DISPUTE_DANG_XU_LY` | Staff chỉ định đã đạt capacity |

### 9. PostgreSQL support

| Thành phần | Vai trò |
|---|---|
| `idx_disputes_active_staff_workload` | Hỗ trợ đếm active workload theo Staff |
| `idx_disputes_staff_last_assigned` | Hỗ trợ tie-break theo lần nhận gần nhất |
| V60 | Capacity setting + mở rộng độ phủ domain |
| V61 | Index lịch sử assignment |

---

## Initiation Types

| Type | Actor | Mô tả |
|---|---|---|
| `BUSINESS_REJECTED_DELIVERABLE` | Business | Explicit dispute về deliverable đã bị reject |
| `EXPERT_SCOPE_CONCERN` | Expert | Business yêu cầu công việc ngoài phạm vi |
| `EXPERT_NO_REVIEW_RESPONSE` | Expert | Business không review hoặc phản hồi |
| `EXPERT_BAD_FAITH_REJECTION` | Expert | Business reject không phù hợp tiêu chí |
| `OTHER` | Cả hai | Lý do khác cần evidence |

---

## Resolution Types

| Type | Mô tả |
|---|---|
| `BUSINESS_APPROVED_AFTER_SELF_RESOLVE` | Business approve deliverable sau self-resolve |
| `STAFF_DECISION_SETTLEMENT` | Staff quyết định + settlement executed |
| `CANCELLED_BY_INITIATOR` | Initiator rút dispute trước khi Staff routing |

---

## Authorization Matrix

| Hành động | Business | Expert | Admin | Staff | System |
|---|---|---|---|---|---|
| Khởi tạo dispute | Có (owner) | Có (assigned) | Không | Không | Không |
| Yêu cầu Staff can thiệp | Có | Có | Không | Không | Không |
| Route Staff | Không | Không | **Không (v2.3)** | Có | Auto-pick |
| Ra quyết định dispute | Không | Không | Không | Assigned Staff | Không |
| Thực thi settlement | Không | Không | Retry only | Không | Auto (v2.3) |
| Cancel dispute | Primary initiator only | Primary initiator only | **Không (v2.3)** | Không | Không |

---

## DB Tables

| Table | Vai trò |
|---|---|
| `disputes` | Core dispute record. Status, initiator, initiation_type, assigned_staff, staff decision fields, SLA fields |
| `milestones` | `resolved_by_dispute_id`, `settlement_source_type = DISPUTE` |
| `contract_milestones` | Mirrors milestone status transition |
| `case_attachments` | Evidence files (owner_type = DISPUTE) |
| `wallet_transactions` | Ledger entries (reference_type = DISPUTE) |

---

## Unique Guards

| Guard | Cơ chế |
|---|---|
| 1 active dispute / milestone | `uq_disputes_one_active_per_milestone` partial unique index |
| Escrow release once | `milestones.escrow_released_at IS NULL` check trước settlement |
| Staff assigned only | `requireAssignedStaff` — `staffId == dispute.assignedStaffId` |
| Staff routing capacity | `active workload < dispute_staff_max_active_cases` |
| Concurrent routing | Pessimistic lock Staff pool trong transaction chọn và gán |
| Settlement idempotent | `DISPUTE_NOT_STAFF_DECIDED` nếu dispute không ở đúng status |

---

## API

| Method | Path | Actor | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/milestones/{milestoneId}/disputes` | Business/Expert | Khởi tạo dispute |
| `POST` | `/api/v1/disputes/{disputeId}/escalation-request` | Business/Expert | Yêu cầu Staff can thiệp (auto-route) |
| `POST` | `/api/v1/disputes/{disputeId}/route-staff` | Staff | Route thủ công tới Staff |
| `GET` | `/api/v1/disputes/{disputeId}/staff-candidates` | Staff | Xem danh sách Staff ứng viên |
| `POST` | `/api/v1/disputes/{disputeId}/staff-decision` | Staff | Ra quyết định + auto settlement |
| `POST` | `/api/v1/disputes/{disputeId}/execute-settlement` | Admin | Retry settlement (idempotent) |
| `POST` | `/api/v1/disputes/{disputeId}/cancel` | Initiator | Rút dispute trước Staff routing |
| `GET` | `/api/v1/contracts/{contractId}/disputes` | Participant/Operator | Danh sách dispute của contract |
| `GET` | `/api/v1/disputes/{disputeId}` | Participant/Operator/Staff | Chi tiết dispute |
| `GET` | `/api/v1/admin/disputes` | Admin | Dashboard danh sách dispute có phân trang/bộ lọc |
| `GET` | `/api/v1/admin/disputes/{disputeId}` | Admin | Dashboard chi tiết dispute kèm ledger + attachments |
| `GET` | `/api/v1/staff/disputes` | Staff | Inbox dispute được gán có phân trang/lọc theo status |

---

## Notification Types

| Type | Gửi tới | Khi nào |
|---|---|---|
| `DISPUTE_CREATED` | Counterparty | Khi dispute được khởi tạo |
| `DISPUTE_ESCALATION_REQUESTED` | Staff | Khi escalation được gửi (tự động route) |
| `DISPUTE_ASSIGNED` | Assigned Staff | Khi Staff được route/gán |
| `DISPUTE_UNDER_REVIEW` | Business + Expert | Khi dispute đang được Staff xem xét |
| `DISPUTE_STAFF_DECIDED` | Business + Expert | Khi Staff ra quyết định |
| `DISPUTE_RESOLVED` | Business + Expert | Khi settlement hoàn tất |
| `DISPUTE_CANCELLED` | Business + Expert | Khi initiator rút dispute |
| `DISPUTE_SETTLEMENT_REPORTED` | Admin | Sau khi settlement commit thành công (báo cáo kiểm toán) |

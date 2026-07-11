# Flow Tranh Chấp (Dispute)

**Phiên bản:** v2.3  
**Spec:** `SPEC-MILESTONE-DISPUTER.md` mục 10  
**Implement:** US-045 + US-048

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

---

## Sơ đồ trạng thái Dispute

```text
                    PENDING_SELF_RESOLVE
                    /        |          \
                   /         |           \
        ESCALATION_REQUESTED |            CANCELLED (initiator withdraw)
              |              | RESOLVED (Business approve sau self-resolve)
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
    │   ├── [Gate] cần reason + evidenceFile (không được rỗng)
    │   │
    │   ├── Lưu escalationReason, escalationEvidenceFile
    │   ├── escalationRequestedByAccountId + escalationRequestedAt
    │   ├── status = ESCALATION_REQUESTED
    │   │
    │   ├── Auto-route tới Staff (v2.3)
    │   │   ├── selectStaffForDispute() — chọn Staff tốt nhất theo:
    │   │   │   ├── Job domain + skills
    │   │   │   ├── Staff availability (IDLE / BUSY)
    │   │   │   ├── Active dispute workload
    │   │   │   └── Conflict-of-interest
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

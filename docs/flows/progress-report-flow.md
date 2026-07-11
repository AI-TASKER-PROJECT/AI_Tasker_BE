# Flow Báo Cáo Tiến Độ (Progress Report)

**Phiên bản:** v2.3  
**Spec:** `SPEC-MILESTONE-DISPUTER.md` mục 9.2A — 9.2C  
**Implement:** US-048

---

## Tổng quan

Trong khi milestone đang `IN_PROGRESS` hoặc `OVERDUE`, Expert gửi báo cáo tiến
độ để Business theo dõi tình trạng dự án. Báo cáo có thể đáp ứng một checkpoint
định kỳ, trả lời một yêu cầu on-demand của Business, hoặc tự nguyện.

Business phải xác nhận (acknowledge) báo cáo mới nhất trước khi Expert được
phép gửi báo cáo tiếp theo hoặc Business được phép gửi yêu cầu on-demand mới.

Ack chỉ là gate kiểm soát luồng — **không** phải feedback chất lượng, không có
đánh giá category/severity, không có yêu cầu chỉnh sửa.

---

## Sơ đồ

```text
Business deposit milestone escrow
    │
    │  → set in_progress_started_at (bắt đầu timeline)
    │
Expert start milestone
    │  → milestone = IN_PROGRESS
    │  → không reset in_progress_started_at
    │
    ├── Expert gửi progress report (POST /progress-reports)
    │   │
    │   ├── [Gate] Không có report nào đang PENDING_BUSINESS_ACK?
    │   │   └── Nếu có → throw PROGRESS_REPORT_ACK_PENDING
    │   │
    │   ├── Xác định checkpoint tiếp theo chưa fulfill
    │   │   ├── MIDPOINT (50% timeline)
    │   │   ├── PRE_DEADLINE (80% timeline)
    │   │   └── NULL (sau khi cả 2 checkpoint đã có report)
    │   │
    │   ├── Nếu có on-demand request PENDING → link report, request = SUBMITTED
    │   │
    │   ├── Tạo milestone_progress_reports:
    │   │   ├── acknowledgement_state = PENDING_BUSINESS_ACK
    │   │   ├── checkpoint_type (MIDPOINT / PRE_DEADLINE / NULL)
    │   │   ├── content, percent_complete, attachment_url, source_code_url,
    │   │   │   demo_link, submission_notes
    │   │   └── is_late (true nếu quá hạn checkpoint hoặc request deadline)
    │   │
    │   ├── Audit: PROGRESS_REPORT_SUBMITTED
    │   └── Notify Business
    │
    ├── Business gửi on-demand request (POST /progress-report-request)
    │   │
    │   ├── [Gate] Không có report nào đang PENDING_BUSINESS_ACK?
    │   │   └── Nếu có → throw PROGRESS_REPORT_ACK_PENDING
    │   │
    │   ├── [Gate] Không có request PENDING còn hạn?
    │   │   └── Nếu có → throw PROGRESS_REPORT_REQUEST_ALREADY_PENDING
    │   │
    │   ├── SLA:
    │   │   ├── Request #1 → due_at = now + 24h
    │   │   └── Request #2+ → due_at = now + 12h
    │   │
    │   ├── Tạo :
    │   │   ├── status = PENDING
    │   │   ├── request_number (tăng dần, không reset)
    │   │   └── due_at
    │   │
    │   ├── Notify Expert với type = PROGRESS_REPORT_REQUESTED
    │   └── Audit: PROGRESS_REPORT_REQUESTED
    │
    └── Business acknowledge report (POST /progress-reports/{id}/acknowledge)
        │
        ├── [Gate] Milestone không phải COMPLETED / CANCELLED?
        │   └── Nếu phải → throw PROGRESS_REPORT_ACK_NOT_ALLOWED
        │
        ├── [Gate] Report đang PENDING_BUSINESS_ACK?
        │   └── Nếu không → throw PROGRESS_REPORT_ACK_NOT_ALLOWED
        │
        ├── Set acknowledgement_state = ACKNOWLEDGED
        ├── Set acknowledged_by_account_id + acknowledged_at
        ├── Audit: PROGRESS_REPORT_ACKNOWLEDGED
        └── Notify Expert (có thể gửi report tiếp theo khi phù hợp)
```

---

## Các rule quan trọng

| Rule | Chi tiết |
|---|---|
| **Gate chặn report thứ 2** | `requireLatestProgressReportAcknowledged` — ném `PROGRESS_REPORT_ACK_PENDING` nếu report gần nhất đang chờ ack |
| **Gate chặn on-demand request** | Cùng gate trên — Business phải ack report trước khi gửi yêu cầu mới |
| **Ack = flow control** | Không có category, severity, dod_items, requires_adjustment, revision semantics |
| **Checkpoint là optional** | Thiếu checkpoint không tạo dispute hay phạt — chỉ là tín hiệu theo dõi |
| **Request hết hạn** | `due_at < now` nhưng status vẫn PENDING → derived flag `progressReportRequestOverdue` |
| **Request ID** | `request_number` tăng dần, không reset — dùng để tính SLA 24h/12h |

---

## DB Tables

| Table | Vai trò |
|---|---|
| `milestone_progress_reports` | Lịch sử báo cáo. `acknowledgement_state`, `checkpoint_type`, `is_late`, `content` |
| `milestone_progress_report_requests` | Lịch sử yêu cầu on-demand. `request_number`, `status`, `due_at`, `submitted_at` |

---

## API

| Method | Path | Actor | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports` | Expert | Gửi báo cáo tiến độ |
| `POST` | `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-report-request` | Business | Yêu cầu báo cáo on-demand |
| `POST` | `/api/v1/.../progress-reports/{progressReportId}/acknowledge` | Business | Xác nhận báo cáo mới nhất |
| `GET` | `/api/v1/.../progress-reports` | Business/Expert/Staff/Admin | Xem danh sách báo cáo |

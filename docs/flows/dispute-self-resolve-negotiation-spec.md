# Dispute Self-Resolve Negotiation Spec

**Phiên bản:** v1.0  
**Phạm vi:** Backend + Frontend  
**Mục tiêu:** Giữ bước "Mở tranh chấp" nhưng bổ sung cơ chế phản hồi/thỏa hiệp giữa Business và Expert trước khi yêu cầu Staff can thiệp.

---

## 1. Mục tiêu nghiệp vụ

Khi một bên mở tranh chấp, hệ thống không chuyển ngay cho Staff. Tranh chấp đi vào trạng thái `PENDING_SELF_RESOLVE` để hai bên tự thương lượng.

Trong giai đoạn này:

- Bên mở tranh chấp nêu lý do, yêu cầu xử lý và bằng chứng.
- Bên còn lại có thể phản hồi lại yêu cầu tranh chấp.
- Hai bên có thể đề xuất phương án thỏa hiệp.
- Nếu một bên chấp nhận phương án của bên kia, tranh chấp được đóng và dự án tiếp tục.
- Nếu thương lượng thất bại, một trong hai bên có thể yêu cầu Staff can thiệp.

---

## 2. Trạng thái dispute

Giữ các trạng thái hiện có:

```text
PENDING_SELF_RESOLVE
ESCALATION_REQUESTED
STAFF_REVIEWING
STAFF_DECIDED
RESOLVED
CANCELLED
```

Ý nghĩa trong flow mới:

| Status | Ý nghĩa |
| --- | --- |
| `PENDING_SELF_RESOLVE` | Hai bên đang tự thương lượng sau khi mở tranh chấp. |
| `ESCALATION_REQUESTED` | Một bên đã yêu cầu Staff can thiệp, chờ hệ thống route Staff. |
| `STAFF_REVIEWING` | Staff đã được gán và đang xử lý. |
| `STAFF_DECIDED` | Staff đã ra quyết định. |
| `RESOLVED` | Tranh chấp đã được xử lý xong. |
| `CANCELLED` | Bên mở tranh chấp đã rút hồ sơ trước khi Staff tiếp nhận. |

Không cần thêm status DB mới cho từng bước thương lượng. Frontend suy ra trạng thái hiển thị từ danh sách phản hồi self-resolve.

---

## 3. Trạng thái hiển thị trên frontend

Khi dispute status là `PENDING_SELF_RESOLVE`, frontend hiển thị sub-status:

| UI sub-status | Điều kiện |
| --- | --- |
| `Chờ đối phương phản hồi` | Chưa có phản hồi nào từ bên còn lại. |
| `Đối phương đã phản hồi` | Có phản hồi mới nhất từ bên còn lại. |
| `Chờ bạn xác nhận thỏa hiệp` | Phản hồi mới nhất là đề xuất/chấp nhận từ bên còn lại. |
| `Chờ đối phương xác nhận` | Người xem là người gửi phản hồi mới nhất. |
| `Sẵn sàng yêu cầu Staff` | Một bên chọn không đồng ý hoặc thương lượng kéo dài. |

---

## 4. Data model backend đề xuất

### 4.1. Bảng `dispute_self_resolve_replies`

Tạo bảng mới để lưu phản hồi thương lượng.

```sql
CREATE TABLE dispute_self_resolve_replies (
  reply_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dispute_id INT NOT NULL,
  actor_account_id INT NOT NULL,
  actor_role VARCHAR(30) NOT NULL,
  reply_type VARCHAR(50) NOT NULL,
  proposed_action VARCHAR(50),
  message TEXT NOT NULL,
  proposed_due_at DATETIME,
  accepted_reply_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_self_resolve_dispute
    FOREIGN KEY (dispute_id) REFERENCES disputes(dispute_id)
);
```

### 4.2. `reply_type`

```text
ACCEPT_REQUEST
COUNTER_PROPOSAL
REQUEST_ADJUSTMENT
REQUEST_STAFF
ACCEPT_PROPOSAL
```

Ý nghĩa:

| Type | Ý nghĩa |
| --- | --- |
| `ACCEPT_REQUEST` | Bên phản hồi chấp nhận yêu cầu ban đầu của đối phương. |
| `COUNTER_PROPOSAL` | Bên phản hồi đề xuất phương án khác. |
| `REQUEST_ADJUSTMENT` | Bên mở tranh chấp yêu cầu đối phương chỉnh lại phương án. |
| `REQUEST_STAFF` | Một bên không đồng ý và muốn Staff can thiệp. |
| `ACCEPT_PROPOSAL` | Một bên chấp nhận phương án thỏa hiệp gần nhất. |

### 4.3. `proposed_action`

```text
CONTINUE_REVISION
ACCEPT_DELIVERABLE
CONTINUE_NEXT_MILESTONE
PARTIAL_REFUND
OTHER
```

Ý nghĩa:

| Action | Kết quả mong muốn |
| --- | --- |
| `CONTINUE_REVISION` | Mở lại mốc hiện tại để Expert chỉnh sửa. |
| `ACCEPT_DELIVERABLE` | Business đồng ý nghiệm thu deliverable hiện tại. |
| `CONTINUE_NEXT_MILESTONE` | Hai bên thống nhất đóng tranh chấp và đi tiếp mốc sau. |
| `PARTIAL_REFUND` | Hai bên đề xuất hoàn tiền một phần, nếu backend hỗ trợ sau. |
| `OTHER` | Phương án khác, bắt buộc nhập mô tả rõ. |

---

## 5. API backend đề xuất

### 5.1. Lấy phản hồi thương lượng

```http
GET /api/v1/disputes/{disputeId}/self-resolve-replies
```

Role được phép:

- Business thuộc contract.
- Expert thuộc contract.
- Staff/Admin chỉ đọc nếu dispute đã escalate hoặc cần audit.

Response:

```json
[
  {
    "replyId": 1,
    "disputeId": 10,
    "actorRole": "EXPERT",
    "actorDisplayName": "Chuyên gia",
    "replyType": "COUNTER_PROPOSAL",
    "proposedAction": "CONTINUE_REVISION",
    "message": "Tôi đồng ý chỉnh sửa phần export Excel trong 2 ngày.",
    "proposedDueAt": "2026-07-15T18:00:00",
    "createdAt": "2026-07-13T09:30:00"
  }
]
```

Không trả về `actorAccountId` cho frontend để tránh lộ ID nhạy cảm.

### 5.2. Gửi phản hồi thương lượng

```http
POST /api/v1/disputes/{disputeId}/self-resolve-replies
```

Request:

```json
{
  "replyType": "COUNTER_PROPOSAL",
  "proposedAction": "CONTINUE_REVISION",
  "message": "Tôi đồng ý chỉnh sửa phần export Excel trong 2 ngày.",
  "proposedDueAt": "2026-07-15T18:00:00"
}
```

Backend validation:

- Dispute phải tồn tại.
- Dispute status phải là `PENDING_SELF_RESOLVE`.
- Actor phải là Business hoặc Expert thuộc contract.
- `message` bắt buộc, trim không rỗng.
- `replyType` phải hợp lệ.
- `proposedAction` bắt buộc nếu `replyType` là `ACCEPT_REQUEST`, `COUNTER_PROPOSAL`, hoặc `ACCEPT_PROPOSAL`.
- `proposedDueAt` không được ở quá khứ nếu có.
- Không cho Staff/Admin tạo reply trong self-resolve.

Nếu `replyType = REQUEST_STAFF`, backend có thể gọi logic escalation hiện có hoặc yêu cầu frontend gọi endpoint escalation riêng.

Khuyến nghị: dùng endpoint riêng `POST /disputes/{disputeId}/escalation-request` để giữ audit rõ ràng.

### 5.3. Chấp nhận thỏa hiệp và đóng dispute

```http
POST /api/v1/disputes/{disputeId}/self-resolve-agreement
```

Request:

```json
{
  "acceptedReplyId": 1,
  "finalAction": "CONTINUE_REVISION",
  "message": "Tôi đồng ý với phương án này."
}
```

Backend validation:

- Dispute status phải là `PENDING_SELF_RESOLVE`.
- `acceptedReplyId` phải thuộc dispute hiện tại.
- Người accept không được là người tạo `acceptedReplyId`.
- Actor phải là một trong hai bên contract.
- `finalAction` phải khớp hoặc tương thích với `proposedAction` của reply được accept.
- Nếu `finalAction = OTHER`, `message` bắt buộc.

Kết quả theo `finalAction`:

| finalAction | Dispute | Milestone | Thanh toán |
| --- | --- | --- | --- |
| `CONTINUE_REVISION` | `RESOLVED` | restore về `IN_PROGRESS` hoặc `previous_milestone_status` phù hợp | Không release escrow. |
| `ACCEPT_DELIVERABLE` | `RESOLVED` | `COMPLETED` | Release escrow 100% cho Expert như approve milestone. |
| `CONTINUE_NEXT_MILESTONE` | `RESOLVED` | `COMPLETED` | Release escrow 100% cho Expert nếu mốc hiện tại được xem là nghiệm thu. |
| `PARTIAL_REFUND` | Chưa khuyến nghị triển khai ngay | Cần settlement riêng | Cần thiết kế wallet riêng. |
| `OTHER` | Tùy nghiệp vụ | Tùy nghiệp vụ | Không tự xử lý nếu chưa có rule. |

Với v1.0, nên chỉ hỗ trợ chắc chắn:

- `CONTINUE_REVISION`
- `ACCEPT_DELIVERABLE`
- `CONTINUE_NEXT_MILESTONE`

### 5.4. DTO response dispute detail nên bổ sung

Khi gọi:

```http
GET /api/v1/disputes/{disputeId}
```

Có thể bổ sung các field không nhạy cảm:

```json
{
  "status": "PENDING_SELF_RESOLVE",
  "selfResolveSummary": {
    "replyCount": 2,
    "latestReplyBy": "EXPERT",
    "latestReplyType": "COUNTER_PROPOSAL",
    "latestProposedAction": "CONTINUE_REVISION",
    "latestReplyAt": "2026-07-13T09:30:00",
    "canCurrentUserReply": true,
    "canCurrentUserAcceptLatestProposal": false,
    "canCurrentUserEscalate": true
  }
}
```

Không bắt buộc nếu frontend gọi riêng replies endpoint.

---

## 6. Backend service logic

### 6.1. Khi mở tranh chấp

Giữ logic hiện tại:

```text
POST /api/v1/milestones/{milestoneId}/disputes
→ status = PENDING_SELF_RESOLVE
→ milestone = DISPUTED
```

Frontend cần hiển thị đây là hồ sơ tự thương lượng, chưa phải Staff review.

### 6.2. Khi gửi phản hồi

```text
validate participant
validate status PENDING_SELF_RESOLVE
save reply
notify đối phương
return reply
```

Notification:

```text
DISPUTE_SELF_RESOLVE_REPLY_CREATED
```

Nội dung:

```text
Đối phương đã phản hồi hồ sơ tranh chấp. Vui lòng xem và xác nhận phương án xử lý.
```

### 6.3. Khi chấp nhận thỏa hiệp

```text
validate participant
validate status PENDING_SELF_RESOLVE
validate accepted reply thuộc đối phương
apply finalAction
set dispute RESOLVED
set resolution_type = SELF_RESOLVE_AGREEMENT
set resolved_at
notify hai bên
```

Cần bổ sung resolution type:

```java
SELF_RESOLVE_AGREEMENT_CONTINUE_REVISION
SELF_RESOLVE_AGREEMENT_ACCEPT_DELIVERABLE
SELF_RESOLVE_AGREEMENT_CONTINUE_NEXT_MILESTONE
```

### 6.4. Khi yêu cầu Staff can thiệp

Giữ endpoint hiện có:

```http
POST /api/v1/disputes/{disputeId}/escalation-request
```

Backend hiện yêu cầu:

- status = `PENDING_SELF_RESOLVE`
- có `reason`
- có `evidenceFile`

Nếu business muốn escalation dễ hơn sau khi đã có replies, có thể cho phép:

- `evidenceFile` optional nếu dispute đã có attachment hoặc self-resolve reply.
- `reason` vẫn bắt buộc.

---

## 7. Frontend spec

### 7.1. Màn `DisputeDetailPage`

Khi `status = PENDING_SELF_RESOLVE`, hiển thị mode:

```text
Tự thương lượng
```

Không hiển thị như Staff đang xử lý.

Bố cục:

1. Header hồ sơ tranh chấp.
2. Stepper trạng thái.
3. Notice giải thích:

```text
Hồ sơ đang ở giai đoạn tự thương lượng. Hai bên có thể phản hồi và thống nhất phương án xử lý trước khi yêu cầu Staff can thiệp.
```

4. Card "Yêu cầu tranh chấp ban đầu".
5. Card "Trao đổi thỏa hiệp".
6. Card hành động theo role.

### 7.2. Card yêu cầu tranh chấp ban đầu

Hiển thị:

- Bên mở tranh chấp: Doanh nghiệp hoặc Chuyên gia.
- Loại tranh chấp đã Việt hóa.
- Mốc đang tranh chấp.
- Lý do tranh chấp.
- Bằng chứng/tệp đính kèm nếu có.

Không hiển thị:

- `disputeId`
- `contractId`
- `milestoneId`
- `accountId`
- `staffId`
- wallet transaction id

### 7.3. Card trao đổi thỏa hiệp

Hiển thị dạng timeline:

```text
Chuyên gia phản hồi
Đề xuất: Tiếp tục chỉnh sửa
Nội dung: Tôi đồng ý chỉnh sửa phần export Excel trong 2 ngày.
Thời hạn đề xuất: 15/07/2026 18:00
```

Mỗi item có badge:

- `Chấp nhận yêu cầu`
- `Đề xuất phương án`
- `Yêu cầu điều chỉnh`
- `Muốn Staff can thiệp`
- `Đã chấp nhận thỏa hiệp`

### 7.4. Hành động cho bên bị tranh chấp

Nếu người xem không phải người mở tranh chấp và chưa có phản hồi mới nhất từ họ:

Form:

```text
Phản hồi yêu cầu tranh chấp

Phương án phản hồi:
- Chấp nhận yêu cầu của đối phương
- Đề xuất phương án thỏa hiệp
- Không đồng ý, yêu cầu Staff can thiệp

Hành động đề xuất:
- Tiếp tục chỉnh sửa mốc này
- Chấp nhận nghiệm thu deliverable
- Tiếp tục sang mốc tiếp theo
- Khác

Nội dung phản hồi *
Thời hạn thực hiện nếu có

[Gửi phản hồi]
```

Validation frontend:

- Nội dung phản hồi bắt buộc.
- Nếu chọn "Khác", bắt buộc mô tả rõ trong nội dung.
- Nếu chọn thời hạn, không được nhỏ hơn thời điểm hiện tại.
- Nếu chọn "Không đồng ý, yêu cầu Staff can thiệp", đổi CTA thành `Yêu cầu Staff can thiệp`.

### 7.5. Hành động cho bên mở tranh chấp

Nếu đối phương đã phản hồi bằng `ACCEPT_REQUEST` hoặc `COUNTER_PROPOSAL`, hiển thị:

```text
Đối phương đã phản hồi yêu cầu tranh chấp.

[Chấp nhận thỏa hiệp và tiếp tục dự án]
[Yêu cầu điều chỉnh lại phản hồi]
[Yêu cầu Staff can thiệp]
```

Khi bấm `Chấp nhận thỏa hiệp và tiếp tục dự án`:

- Mở modal xác nhận.
- Hiển thị rõ hệ quả:
  - Nếu `CONTINUE_REVISION`: mốc hiện tại được mở lại để tiếp tục chỉnh sửa.
  - Nếu `ACCEPT_DELIVERABLE` hoặc `CONTINUE_NEXT_MILESTONE`: mốc hiện tại được xem là nghiệm thu và có thể phát sinh thanh toán.
- Gọi `POST /self-resolve-agreement`.

### 7.6. Sau khi chấp nhận thỏa hiệp

Hiển thị notice trong `DisputeDetailPage`:

```text
Tranh chấp đã được hai bên thống nhất xử lý.
```

Nếu `CONTINUE_REVISION`:

```text
Mốc này đã được mở lại để Chuyên gia tiếp tục chỉnh sửa theo thỏa thuận.
```

Nếu `CONTINUE_NEXT_MILESTONE`:

```text
Mốc tranh chấp đã được thống nhất hoàn tất. Hãy tiếp tục các mốc tiếp theo của dự án.
```

Hiển thị nút:

```text
Tiếp tục dự án
```

Nút này điều hướng về Workspace tương ứng.

### 7.7. Workspace notice

Khi quay lại Workspace từ dispute resolved:

```text
Tranh chấp của mốc {số mốc} đã được giải quyết. Hãy tiếp tục tiến hành các mốc tiếp theo của dự án.
```

Nếu có thanh toán:

```text
Khoản thanh toán theo thỏa thuận đã được ghi nhận. Vui lòng kiểm tra ở lịch sử Ví & Thanh toán.
```

Không hiển thị số tiền nếu backend không trả dữ liệu chắc chắn.

---

## 8. Frontend service đề xuất

Thêm vào `disputeService.ts`:

```ts
listSelfResolveReplies(disputeId: number) {
  return call<DisputeSelfResolveReply[]>({
    method: "GET",
    url: `/api/v1/disputes/${disputeId}/self-resolve-replies`,
  });
}

createSelfResolveReply(
  disputeId: number,
  payload: CreateDisputeSelfResolveReplyRequest,
) {
  return call<DisputeSelfResolveReply>({
    method: "POST",
    url: `/api/v1/disputes/${disputeId}/self-resolve-replies`,
    data: payload,
  });
}

acceptSelfResolveAgreement(
  disputeId: number,
  payload: AcceptDisputeSelfResolveAgreementRequest,
) {
  return call<Dispute>({
    method: "POST",
    url: `/api/v1/disputes/${disputeId}/self-resolve-agreement`,
    data: payload,
  });
}
```

Types:

```ts
export type DisputeSelfResolveReplyType =
  | "ACCEPT_REQUEST"
  | "COUNTER_PROPOSAL"
  | "REQUEST_ADJUSTMENT"
  | "REQUEST_STAFF"
  | "ACCEPT_PROPOSAL";

export type DisputeSelfResolveProposedAction =
  | "CONTINUE_REVISION"
  | "ACCEPT_DELIVERABLE"
  | "CONTINUE_NEXT_MILESTONE"
  | "PARTIAL_REFUND"
  | "OTHER";

export interface DisputeSelfResolveReply {
  replyId: number;
  disputeId: number;
  actorRole: "BUSINESS" | "EXPERT" | string;
  actorDisplayName?: string;
  replyType: DisputeSelfResolveReplyType;
  proposedAction?: DisputeSelfResolveProposedAction;
  message: string;
  proposedDueAt?: string;
  createdAt?: string;
}
```

---

## 9. Quy tắc bảo mật và hiển thị

Frontend tuyệt đối không hiển thị:

- ID kỹ thuật: dispute id, contract id, milestone id, account id, staff id.
- Wallet transaction id.
- Internal audit/action code.

Frontend được hiển thị:

- Tên vai trò: Doanh nghiệp, Chuyên gia, Staff.
- Tên dự án/hợp đồng nếu backend trả.
- Mốc số mấy, không dùng milestone id.
- Trạng thái đã Việt hóa.
- Thời gian định dạng theo locale Việt Nam.

Tất cả text tiếng Việt phải là UTF-8 sạch, không dùng chuỗi mojibake.

---

## 10. Acceptance criteria

### Backend

- Mở tranh chấp vẫn tạo status `PENDING_SELF_RESOLVE`.
- Business/Expert thuộc contract có thể gửi phản hồi self-resolve.
- Người ngoài contract không được xem/gửi phản hồi.
- Staff/Admin không được gửi phản hồi thương lượng thay hai bên.
- Một bên có thể chấp nhận phản hồi của bên còn lại.
- Khi accept agreement, dispute chuyển `RESOLVED`.
- Với `CONTINUE_REVISION`, milestone quay lại trạng thái tiếp tục làm việc.
- Với `ACCEPT_DELIVERABLE` hoặc `CONTINUE_NEXT_MILESTONE`, milestone hoàn tất theo rule thanh toán hiện có.
- Có notification cho đối phương khi có phản hồi mới.
- Không trả ID nhạy cảm trong DTO frontend-facing nếu không cần thiết.

### Frontend

- Khi status `PENDING_SELF_RESOLVE`, màn detail hiển thị phase "Tự thương lượng".
- Bên bị tranh chấp thấy form phản hồi.
- Bên mở tranh chấp thấy phản hồi của đối phương và nút chấp nhận/yêu cầu chỉnh/yêu cầu Staff.
- Timeline phản hồi hiển thị đúng tiếng Việt.
- Sau khi chấp nhận thỏa hiệp, có notice và nút "Tiếp tục dự án".
- Không hiển thị ID kỹ thuật.
- Không có lỗi tiếng Việt/mojibake.


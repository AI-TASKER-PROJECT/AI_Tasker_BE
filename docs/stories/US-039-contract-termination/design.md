# Design — US-039 Contract Termination & Closure

## 1. Flow & Business Rules
- Cần có cơ chế chấm dứt hợp đồng. Trạng thái Contract: `TERMINATION_PENDING` và `TERMINATED`.
- Khi chấm dứt hợp đồng, mọi milestone đang cọc (`DEPOSITED`, `IN_PROGRESS`, `UNDER_REVIEW`, `DISPUTED`) phải được hoàn trả lại cho Business.
- Expert không nhận được tiền từ những milestone chưa hoàn tất này.
- Tiền cọc được release từ `BALANCE_ESCROW` của Business về `BALANCE_AVAILABLE` của Business.
- Trạng thái các milestone này chuyển sang `CANCELLED`.
- Trạng thái Contract chuyển sang `TERMINATED`.

## 2. API / Service Methods (`ContractExecutionService`)
### 2.1 requestTermination(contractId, reason)
- Guard: Contract must be in ACTIVE or TERMINATION_PENDING status.
- Only Business or Expert of the contract can call.
- Sets contract status to `TERMINATION_PENDING`.
- Saves termination reason.

### 2.2 executeTermination(contractId, adminNote)
- Guard: Role ADMIN.
- Contract must be in `TERMINATION_PENDING` or `ACTIVE` (admin override).
- Process refunds for all active/undone milestones having escrow:
  - For each milestone:
    - If status in (`DEPOSITED`, `IN_PROGRESS`, `UNDER_REVIEW`, `DISPUTED`):
      - Refund milestone budget from Business escrow to Business available:
        `walletLedgerService.releaseEscrowToAvailable(businessAccountId, milestone.getFinalBudget(), "MILESTONE_ESCROW_REFUND", "CONTRACT_MILESTONE", milestoneId, "Refund escrow on termination")`
      - Set milestone status to `CANCELLED`.
- Set contract status to `TERMINATED`.
- Log audit event.

## 3. DB Changes
- Đã bổ sung status `TERMINATION_PENDING` và `TERMINATED` cho contract ở migration V45.
- Nếu cần lưu `termination_reason` hay `termination_note`, có thể bổ sung cột vào bảng `contracts`.
- Kiểm tra cấu trúc hiện tại của bảng `contracts` xem đã có cột `termination_reason` chưa.

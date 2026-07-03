# US-040 Withdrawal History Dedup

## Status

implemented

## Lane

normal

## Product Contract

Khi user xem lịch sử giao dịch ví (`GET /api/wallet/transactions`), các yêu cầu rút tiền (withdrawal) chỉ hiển thị **1 bản ghi đại diện duy nhất** theo trạng thái hiện tại của withdrawal, thay vì hiển thị raw ledger nội bộ (DEBIT, HOLD, RELEASE, CREDIT) gây trùng lặp và gây hiểu nhầm cho người dùng.

## Relevant Product Docs

- `SPEC.md`

## Intake

- **Input type:** change_request (bug fix)
- **Risk lane:** normal
- **Risk flags:** public_contracts, existing_behavior

## Acceptance Criteria

- Với withdrawal PENDING, API history chỉ trả 1 bản ghi: "Yêu cầu rút tiền đang chờ duyệt"
- Không trả raw ledger nội bộ DEBIT + HOLD cho FE
- Khi admin APPROVE thì hiển thị "Rút tiền thành công"
- Khi admin REJECT thì hiển thị "Yêu cầu rút tiền bị từ chối"
- Group theo withdrawalId để tránh trùng giao dịch
- Các transaction không phải withdrawal vẫn hiển thị bình thường
- Admin view không bị ảnh hưởng

## Design Notes

- Commands: Sửa `listCurrentWalletTransactions()` trong `PaymentWalletService.java`
- Queries: Thêm `buildWithdrawalHistoryResponse()` để sinh bản ghi đại diện
- API: `GET /api/wallet/transactions` — response shape giữ nguyên DTO `WalletTransactionHistoryResponse`, chỉ thay đổi số lượng và nội dung bản ghi withdrawal
- Tables: Không thay đổi schema
- Domain rules: Group withdrawal ledger entries theo `withdrawalId`, mỗi withdrawal chỉ trả 1 bản ghi với title/description dựa trên trạng thái (PENDING/APPROVED/REJECTED/CANCELLED)

## Validation

| Layer | Expected proof | Status |
| --- | --- | --- |
| Unit | `sh mvnw -Dtest=PaymentWalletServiceTest test` | 1 |
| Integration | - | 0 |
| E2E | - | 0 |
| Platform | - | 0 |
| Release | `sh mvnw test` | 1 |

`PaymentWalletServiceTest` kiểm tra PENDING, APPROVED và REJECTED đều chỉ trả
một bản ghi đại diện; giao dịch không phải withdrawal và admin history tiếp tục
được bao phủ bởi các regression test hiện hữu.

## Harness Delta

- Harness CLI Linux `v0.1.10` hiện có tại `scripts/bin/harness-cli`.
- Story, proof và trace được cập nhật bằng CLI.

## Evidence

### File changed
- `src/main/java/com/aitasker/be/service/core/PaymentWalletService.java`
  - Thêm imports: `ArrayList`, `LinkedHashMap`, `Set`
  - Viết lại `listCurrentWalletTransactions()`: group withdrawal entries theo `withdrawalId`, chỉ giữ 1 bản ghi đại diện
  - Thêm `buildWithdrawalHistoryResponse()`: sinh response dựa trên trạng thái withdrawal
  - Giữ transaction và withdrawal đã resolve trong cùng `WithdrawalHistoryEntry`, không query `findById()` lần hai
- `src/test/java/com/aitasker/be/service/core/PaymentWalletServiceTest.java`
  - Thêm regression test cho PENDING và APPROVED
  - Cập nhật test REJECTED theo response contract mới
  - Xác minh không query lại withdrawal bằng `findById()`

### Focused test
```bash
sh mvnw -Dtest=PaymentWalletServiceTest test
```
Kết quả: `18 tests, 0 failures, 0 errors`.

### Full regression
```bash
sh mvnw test
```
Kết quả: `194 tests, 0 failures, 0 errors`; Flyway validated 48 migrations.

### Static check
```bash
git diff --check
```
Kết quả: pass.

### Trace

- Trace #57 là lần triển khai ban đầu chỉ compile và đã bị supersede vì focused test phát hiện regression.
- Trace #58 ghi lại code fix, tests và proof cập nhật bằng Harness CLI.

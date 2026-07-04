# SPEC - Fix Withdrawal History Bug

## Vấn đề

Khi user tạo yêu cầu rút tiền, `transferFromAvailable()` trong `WalletLedgerService` sinh ra **2 dòng ledger nội bộ** (DEBIT từ AVAILABLE + HOLD vào HOLDING). FE gọi `GET /api/wallet/transactions` → `listCurrentWalletTransactions()` map **từng** dòng ledger thành 1 dòng response. Khi approve/reject lại sinh thêm dòng nữa:

| Trạng thái | # Dòng ledger | FE hiển thị |
|-----------|--------------|-------------|
| PENDING | 2 | 2 dòng (DEBIT + HOLD) |
| APPROVED | 3 (+1) | 3 dòng |
| REJECTED | 4 (+2) | 4 dòng |

## Kế hoạch

**File cần sửa:** `PaymentWalletService.java`

### 1. Sửa `listCurrentWalletTransactions()` (dòng 540)

Sau khi lấy tất cả transaction, tách riêng các dòng liên quan rút tiền (`WITHDRAW_HOLD`, `WITHDRAW_APPROVED`, `WITHDRAW_REJECTED`) và group theo `withdrawalId`. Với mỗi withdrawal, chỉ sinh **1 bản ghi đại diện** dựa trên trạng thái hiện tại của withdrawal (không dựa trên loại ledger entry). Các transaction không liên quan rút tiền vẫn giữ nguyên.

### 2. Thêm method `buildWithdrawalHistoryResponse()`

Sinh 1 response duy nhất cho mỗi withdrawal:

| Trạng thái | `title` | `description` |
|---|---|---|
| PENDING | `Yêu cầu rút tiền đang chờ duyệt` | `Yêu cầu rút {amount} VND về ngân hàng {bankName}, chủ tài khoản {bankAccountHolder}. Đang chờ quản trị viên duyệt.` |
| APPROVED | `Rút tiền thành công` | `Đã rút {amount} VND về ngân hàng {bankName}, chủ tài khoản {bankAccountHolder}. Được duyệt bởi {adminName}.` |
| REJECTED | `Yêu cầu rút tiền bị từ chối` | `Yêu cầu rút {amount} VND bị từ chối bởi {adminName}. Lý do: {adminNote}.` |
| CANCELLED | `Yêu cầu rút tiền đã hủy` | (tương tự) |

- `createdAt` = `requestedAt` với PENDING, `reviewedAt` với APPROVED/REJECTED (để timeline đúng)
- `transactionId` = ID của dòng ledger mới nhất (để truy vết)
- Vẫn enrich đầy đủ: `withdrawalId`, `bankName`, `bankAccountHolder`, `adminId`, `adminName`, `adminNote`

### 3. Không sửa

- `describeWithdrawal()` — giữ nguyên (có thể admin view dùng)
- `listPlatformWalletTransactions()` — admin view đã có filter `isPlatformHistoryEventRow`, admin cần raw ledger để audit
- `WalletLedgerService` — logic ledger nội bộ không đổi

## Required Code Changes

### `listCurrentWalletTransactions()` — Group withdrawal entries by withdrawalId

```java
public List<WalletTransactionHistoryResponse> listCurrentWalletTransactions() {
    AccountEntity actor = accessService.currentAccount();
    List<WalletTransactionEntity> allTx = walletTransactionRepository
        .findByAccountIdOrderByCreatedAtDesc(actor.getAccountId());

    Set<String> withdrawalTypes = Set.of("WITHDRAW_HOLD", "WITHDRAW_APPROVED", "WITHDRAW_REJECTED");

    // Group withdrawal-related entries by withdrawalId, keep latest tx per group
    Map<Long, WalletTransactionEntity> latestByWithdrawalId = new LinkedHashMap<>();
    List<WalletTransactionEntity> nonWithdrawalTx = new ArrayList<>();

    for (WalletTransactionEntity tx : allTx) {
        if (withdrawalTypes.contains(tx.getTransactionType())) {
            Optional<WithdrawalRequestEntity> w = withdrawalForTransaction(tx);
            w.ifPresent(wr -> latestByWithdrawalId.merge(wr.getWithdrawalId(), tx,
                (existing, replacement) ->
                    replacement.getCreatedAt().isAfter(existing.getCreatedAt()) ? replacement : existing));
        } else {
            nonWithdrawalTx.add(tx);
        }
    }

    // Build representative records for each withdrawal
    List<WalletTransactionHistoryResponse> result = new ArrayList<>();
    for (Map.Entry<Long, WalletTransactionEntity> entry : latestByWithdrawalId.entrySet()) {
        WithdrawalRequestEntity wr = withdrawalRequestRepository.findById(entry.getKey()).orElse(null);
        if (wr != null) {
            result.add(buildWithdrawalHistoryResponse(entry.getValue(), wr, actor));
        }
    }

    // Map non-withdrawal transactions normally
    result.addAll(nonWithdrawalTx.stream()
        .map(tx -> toWalletHistory(tx, actor))
        .toList());

    result.sort(Comparator.comparing(WalletTransactionHistoryResponse::getCreatedAt).reversed());
    return result;
}
```

### `buildWithdrawalHistoryResponse()` — Single representative record

Build response based on withdrawal status (PENDING/APPROVED/REJECTED/CANCELLED), not based on ledger transactionType. Include all enrichment fields (withdrawalId, bankName, bankAccountHolder, adminId, adminName, adminNote). Use `requestedAt` for PENDING and `reviewedAt` for APPROVED/REJECTED as `createdAt`.

## Expected Files To Inspect

- `src/main/java/com/aitasker/be/service/core/PaymentWalletService.java`
- `src/main/java/com/aitasker/be/dto/payment/WalletTransactionHistoryResponse.java`
- `src/main/java/com/aitasker/be/repository/WithdrawalRequestRepository.java`

## Validation

```powershell
.\mvnw.cmd -DskipTests compile
```

## Acceptance Criteria

- Với withdrawal PENDING, API history chỉ trả 1 bản ghi: "Yêu cầu rút tiền đang chờ duyệt"
- Không trả raw ledger nội bộ DEBIT + HOLD cho FE
- Khi admin APPROVE thì hiển thị "Rút tiền thành công"
- Khi admin REJECT thì hiển thị "Yêu cầu rút tiền bị từ chối"
- Group theo withdrawalId để tránh trùng giao dịch
- Các transaction không phải withdrawal vẫn hiển thị bình thường
- Admin view không bị ảnh hưởng

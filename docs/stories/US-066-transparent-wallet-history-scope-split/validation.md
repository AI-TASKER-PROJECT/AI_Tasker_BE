# Validation

## Proof Strategy

Proof must show that user history remains readable, new reconciliation fields
are populated for supported flows, platform user activity does not duplicate
internal ledger legs, and platform wallet ledger exposes platform balance
changes.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | User top-up transparency fields; withdrawal masking; platform user activity filtering; platform ledger revenue credit visibility. |
| Integration | No schema change; use existing repository/Flyway coverage if full suite runs. |
| E2E | Not required for this backend slice. |
| Platform | Compile and Swagger-facing docs updated for new routes. |
| Performance | No new unbounded query beyond existing admin/user history style. |
| Logs/Audit | Read-only endpoints; existing write audit rows unchanged. |

## Fixtures

- Business wallet top-up with PayOS payment order.
- Admin platform wallet revenue credit paired with purchaser debit by
  `operationKey`.
- Withdrawal request with bank information.
- Existing membership/credit purchase history fixtures.

## Commands

```text
command: .\mvnw.cmd -Dtest=PaymentWalletServiceTest test
result: PASS, 32 tests, 0 failures, 0 errors.
notes: Focused wallet history projection proof after DTO/service/controller changes.
```

```text
command: .\mvnw.cmd '-Dtest=PaymentWalletServiceTest,WalletLedgerServiceTest,SystemWalletServiceTest' test
result: PASS, 37 tests, 0 failures, 0 errors.
notes: Focused wallet/system-wallet regression suite.
```

```text
command: .\mvnw.cmd -DskipTests compile
result: PASS.
notes: Main source compilation after DTO/controller/service changes.
```

```text
command: runtime GET http://localhost:8081/v3/api-docs with rag.ingest-on-startup=false
result: PASS.
notes: Refreshed docs/openapi/openapi-v1.json from runtime; OpenAPI has 156 paths and 187 operations, including the two new admin wallet routes and the compatibility alias.
```

```text
command: API docs coverage count
result: PASS.
notes: docs/openapi/openapi-v1.json has 187 operations; swagger overview, swagger test guide, and postman test guide each have 187 endpoint entries.
```

```text
command: .\mvnw.cmd test
result: PASS, 375 tests, 0 failures, 0 errors.
notes: Full backend regression suite.
```

```text
command: .\scripts\bin\harness-cli.exe story verify US-066
result: PASS.
notes: Harness story verification ran the configured PaymentWalletServiceTest command.
```

```text
command: git diff --check
result: PASS.
notes: Only Windows LF-to-CRLF working-copy warnings were reported.
```

## Acceptance Evidence

- `PaymentWalletServiceTest.listCurrentWalletTransactions_shouldExposeReconciliationFieldsForTopup`
  proves user history exposes wallet, provider, metadata, scope, category, and
  amount breakdown fields.
- `PaymentWalletServiceTest.listPlatformWalletLedger_shouldReturnOnlyPlatformWalletLedgerRows`
  proves the platform ledger view returns a platform revenue credit row with
  `historyScope=PLATFORM_WALLET` and purchaser counterparty context.
- Existing `listPlatformWalletTransactions_shouldReturnVietnameseBusinessEventsForAdmin`
  continues proving compatibility user-activity behavior.
- Runtime Swagger/OpenAPI, overview guide, Swagger test guide, and Postman test
  guide all expose the split platform routes and enriched DTO fields.

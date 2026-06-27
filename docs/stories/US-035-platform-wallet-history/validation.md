# Validation

## Proof Strategy

Focused unit tests verify the new admin endpoint service behavior and the
Vietnamese projection for requested event families. Compile and focused Maven
tests must pass before the story is marked implemented.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Admin platform history requires ADMIN, filters duplicate transfer legs, renders membership and credit purchase details in Vietnamese. |
| Integration | Not required for this slice; no schema migration. |
| E2E | Not run locally unless a seeded runtime is available. |
| Platform | OpenAPI/docs inventory updated. |
| Performance | Not measured; list is unpaginated to match current wallet history style. |
| Logs/Audit | Existing write-action audit logs remain unchanged. |

## Fixtures

- Mock ADMIN account.
- Mock Business/Expert accounts and wallet transactions.
- Mock membership package/purchase and quota purchase ledger descriptions.

## Commands

```text
command: .\mvnw.cmd -Dtest=PaymentWalletServiceTest test
result: PASS, 16 tests, 0 failures, 0 errors. Story verify US-035 also passed the same command.
notes: Focused proof for platform wallet history projection, duplicate ledger-leg filtering, and existing wallet finance behavior.

command: .\mvnw.cmd -DskipTests compile
result: PASS.
notes: Controller, repository, service, and DTO usage compile.

command: .\mvnw.cmd test
result: PASS, 165 tests, 0 failures, 0 errors.
notes: Full suite also validated 44 Flyway migrations against local PostgreSQL.

command: git diff --check
result: PASS.
notes: No whitespace errors; Git printed Windows LF-to-CRLF warnings only.

command: rg -n "/api/v1/admin/wallet/transactions|PLATFORM WALLET TRANSACTIONS SUCCESS|listPlatformWalletTransactions|findAllByOrderByCreatedAtDesc" src docs README.md
result: PASS.
notes: New route appears in source, Swagger overview/test guide, architecture notes, README, and story docs.
```

## Acceptance Evidence

- `GET /api/v1/admin/wallet/transactions` added to `AdminController` and backed
  by `PaymentWalletService.listPlatformWalletTransactions()`.
- Service requires ADMIN role, reads all wallet transactions newest first, and
  filters duplicate transfer legs for platform-facing event rows.
- `WalletTransactionHistoryResponse` remains the response DTO and preserves raw
  ledger fields plus reconciliation IDs.
- Vietnamese title/description rendering now covers top-up, membership,
  credit, deposit hold/refund/resolution, withdrawal hold/approval/rejection.
- Swagger overview and Swagger test guide include the new endpoint in the
  Admin & Governance wallet section.
- Final trace: #47, outcome `completed`, detailed tier met for high-risk lane.

# Validation

## Proof Strategy

Focused unit tests verify wallet history enrichment for contract deposit and
withdrawal rejection, and verify finance/account notifications use Vietnamese
with diacritics. Existing contract tests cover the notification call sites that
pass generic contract event text.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Contract deposit history includes business/expert/job/contract context; rejected withdrawal history includes requester/bank/admin/reason; websocket finance/account notifications use Vietnamese diacritics. |
| Integration | Not required; no schema change. |
| E2E | Not run in this backend slice. |
| Platform | Compile and full Maven suite. |
| Logs/Audit | Verify credit-purchase audit action writes clean Vietnamese and legacy `Mua credit` maps on read. |

## Fixtures

Mockito repositories with deterministic wallet transactions, contract deposit,
withdrawal request, business/expert profiles, accounts, and job rows.

## Commands

```text
.\mvnw.cmd "-Dtest=PaymentWalletServiceTest,NotificationServiceTest,ContractExecutionServiceTest" test
.\mvnw.cmd test
.\mvnw.cmd -DskipTests compile
.\scripts\bin\harness-cli.exe story verify US-032
```

## Acceptance Evidence

- `.\mvnw.cmd "-Dtest=PaymentWalletServiceTest,NotificationServiceTest,ContractExecutionServiceTest" test`
  passed on 2026-06-26: 49 tests, 0 failures.
- `.\mvnw.cmd test` passed on 2026-06-26: 137 tests, 0 failures.
- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-26.
- `.\scripts\bin\harness-cli.exe story verify US-032` passed on
  2026-06-26.
- `git diff --check` passed on 2026-06-26 with CRLF warnings only.
- Text scan for active core websocket/audit strings found no remaining
  no-diacritic notification text; the remaining `Mua credit` occurrence is
  intentionally kept as a legacy audit translation input.

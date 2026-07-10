# Validation

## Proof Strategy

Use focused service tests for wallet ledger arithmetic, package/credit quota
rules, marketplace quota gates, and contract deposit/withdrawal transitions.
Run the Maven test suite after implementation. Update the durable story proof
matrix with available evidence.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Ledger prevents negative available balance; membership debits wallet and grants quota; credit purchase grants quota; proposal/job publish consumes quota; deposit hold/refund/withdrawal transitions update balances. |
| Integration | Existing integration tests should still load Spring context and Flyway migrations. |
| E2E | Not automated in this slice. |
| Platform | Not automated in this slice. |
| Performance | Not in scope. |
| Logs/Audit | Admin deposit refund and withdrawal review should call audit logging where wired. |

## Fixtures

- Business account with approved profile and wallet balance.
- Expert account with approved profile and wallet balance.
- Admin account.
- Contract with both signatures/NDA timestamps.
- Wallet and quota records with deterministic balances.

## Commands

```text
.\mvnw.cmd test
```

## Acceptance Evidence

- `.\mvnw.cmd -q -DskipTests compile` passed.
- `.\mvnw.cmd -q -DskipTests test-compile` passed.
- `.\mvnw.cmd -q "-Dtest=PaymentWalletServiceTest,WalletLedgerServiceTest,ContractExecutionServiceTest,MarketplaceServiceTest,ExpertRecommendationServiceTest" test` passed.
- `docker compose up -d` brought up the local Postgres/Redis dependencies used by the integration suite.
- `.\mvnw.cmd test` passed on 2026-06-20: 55 tests, 0 failures, 0 errors, 0 skipped.
- `.\scripts\bin\harness-cli.exe story verify US-004` passed on 2026-06-20 using `.\mvnw.cmd test`.
- Test Firebase configuration now disables Firebase Storage by default so the suite does not depend on a developer-local service-account file path.

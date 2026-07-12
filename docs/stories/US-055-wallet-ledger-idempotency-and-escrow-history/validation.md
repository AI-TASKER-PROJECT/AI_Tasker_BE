# Validation

## Proof Strategy

Proof must cover migration validity, idempotent retry safety, same-wallet
multi-leg atomicity, and user-history consolidation without regressing
withdrawal behavior.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | operation key conflict, complete replay, incomplete operation detection |
| Integration | Flyway migration, same-wallet dual-leg retry, dispute/termination settlement regression |
| E2E | none |
| Platform | Docker PostgreSQL migration validation |
| Performance | none |
| Logs/Audit | admin ledger still exposes raw legs with operation identity |

## Fixtures

- deterministic business/expert/admin wallets
- contract deposit and milestone escrow fixtures
- legacy history rows without operation keys

## Commands

```text
command:
sh mvnw -DskipTests compile
result:
pass
notes:
compile succeeded after wallet ledger/entity/repository refactor

command:
sh mvnw -DargLine=-javaagent:/home/hieunt1504/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar -Dtest=WalletLedgerServiceTest,PaymentWalletServiceTest,ContractExecutionServiceTest test
result:
pass
notes:
97 tests, 0 failures, 0 errors

command:
sh mvnw -DargLine=-javaagent:/home/hieunt1504/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar test
result:
pass
notes:
289 tests, 0 failures, 0 errors; Flyway validated 56 migrations and applied V56 on local PostgreSQL

command:
git diff --check
result:
pass
notes:
no whitespace or patch-format issues
```

## Acceptance Evidence

- `WalletLedgerServiceTest.holdEscrowFromAvailable_shouldReturnExistingOperationWithoutMutatingWallet`
  proves same operation replay returns existing persisted leg set.
- `PaymentWalletServiceTest.listCurrentWalletTransactions_shouldCollapseOperationKeyEscrowDepositIntoSingleHistoryRow`
  proves user history collapses escrow multi-leg rows by logical operation.
- `ContractExecutionServiceTest` and `PaymentWalletServiceTest` regressions pass
  after migrating milestone escrow, dispute/termination settlement, contract
  deposit, and withdrawal flows to `WalletOperationContext`.
- Full Maven suite passed with local PostgreSQL/Flyway at migration `V56`.

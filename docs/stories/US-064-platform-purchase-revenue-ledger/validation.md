# Validation

## Proof Strategy

Prove both balance mutation and reporting consistency. Successful purchase tests
must verify purchaser debit, platform credit, shared operation identity, and
entitlement grant. Ledger tests must prove replay does not increase platform
revenue twice. Synchronization tests must include commission and both purchase
types without counting platform credit counterparts.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Membership, job-post credit, proposal credit, platform revenue credit, replay, sync aggregation, history filtering |
| Integration | Flyway V62 and repository aggregate against PostgreSQL |
| E2E | Existing purchase routes through service transaction where environment permits |
| Platform | No API contract change; existing Admin wallet fields show corrected values |
| Performance | Revenue query matches the partial V62 index predicate |
| Logs/Audit | Existing membership and credit audit records remain present |

## Fixtures

- Approved Business with available balance.
- Approved Expert with available balance.
- First Admin account with platform wallet.
- Active membership package.
- Job-post and proposal credit settings.
- Historical posted purchase debit rows plus a platform credit counterpart.

## Commands

```bash
sh mvnw -DskipTests compile
```

- Result: `BUILD SUCCESS`; 249 main source files compiled.

```bash
sh mvnw \
  -DargLine=-javaagent:/home/hieunt1504/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar \
  -Dtest=PaymentWalletServiceTest,WalletLedgerServiceTest,SystemWalletServiceTest,PlatformPurchaseRevenueRepositoryIntegrationTest \
  test
```

- Result: 36 tests, 0 failures, 0 errors, 0 skipped.
- The PostgreSQL integration fixture proved that posted membership and credit
  purchaser debits are included while the platform credit counterpart and an
  escrow debit are excluded.

```bash
sh mvnw \
  -DargLine=-javaagent:/home/hieunt1504/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar \
  test
```

- Result: 365 tests, 0 failures, 0 errors, 0 skipped.
- PostgreSQL 16.14 connection succeeded.
- Flyway validated all 62 migrations and applied V62 successfully.

```bash
scripts/bin/harness-cli story verify US-064
```

- Result: `Story US-064 verification: pass`; focused suite remained 36/36.

```bash
git diff --check
```

- Result: pass with no whitespace errors.

## Acceptance Evidence

- Membership, job-post credit, and proposal-credit success tests verify the
  purchaser debit and platform credit share one operation key with distinct
  operation legs.
- Ledger replay tests verify that reusing the platform operation leg returns
  the existing row without increasing Admin balances a second time.
- Synchronization test verifies `100` commission revenue plus `600` purchase
  revenue produces `700` total/available revenue while preserving holding.
- Platform history regression test verifies the internal platform-credit leg is
  not exposed as a duplicate purchase event.
- The repository integration test validated the aggregate semantics against
  PostgreSQL, and the full Spring run applied the partial V62 index.
- No HTTP route or response contract changed; Swagger/OpenAPI regeneration is
  not required.
- Final Harness trace: `#126` (detailed tier; meets the high-risk lane
  requirement).

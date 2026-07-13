# Validation

## Proof Strategy

The agreement transaction locks the dispute and contract milestone before changing the dispute state or writing escrow ledger legs. Only Business can accept actions that pay the Expert.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Reply persistence/redaction, revision agreement, payout agreement, authorization. |
| Integration | Flyway V57 applies and foreign keys/indexes are valid on PostgreSQL. |
| Platform | Runtime OpenAPI exposes all three routes. |
| Logs/Audit | Reply creation and agreement acceptance are audited. |

## Commands

```text
command: sh mvnw -DargLine=-javaagent:/home/hieunt1504/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar -Dtest=ContractExecutionServiceTest test
result: pass - 76 tests, 0 failures, 0 errors (2026-07-13)

command: sh mvnw -DargLine=-javaagent:/home/hieunt1504/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar test
result: blocked for integration proof - unit tests passed, but 11 integration/migration tests could not connect to PostgreSQL at 127.0.0.1:5433.
```

## Acceptance Evidence

- Reply creation saves an append-only negotiation item, returns no actor account ID, audits the action, and notifies the counterparty.
- Agreement locks both dispute and contract milestone. Revision restores `IN_PROGRESS` without ledger writes; payout actions release escrow only after the Business gate.
- `git diff --check` passed.
- Runtime OpenAPI snapshot and V57 Flyway proof are pending PostgreSQL availability. Harness story `US-056` stays `in_progress` until those checks pass.

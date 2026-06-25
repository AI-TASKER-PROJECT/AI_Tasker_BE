# US-030 Add Missing Realtime Business Finance And Account Notifications

## Status

implemented

## Lane

normal

## Product Contract

Backend realtime notifications must cover the missing user-facing events in
contract rejection, quota consumption, wallet top-up, withdrawal review, admin
withdrawal review queue, and new account registration. Each event must be
persisted in `notifications` and pushed to the receiver over
`/user/queue/notifications`.

## Relevant Product Docs

- `docs/ARCHITECTURE.md`
- `docs/swagger-api-overview.md`

## Acceptance Criteria

- Business cancellation/rejection of a contract notifies the Expert.
- Publishing a job and consuming a Business job-post quota notifies the Business.
- Successful wallet top-up notifies the top-up account.
- Withdrawal request creation notifies Admin accounts that review is needed.
- Withdrawal approval notifies the requester that withdrawal succeeded.
- Withdrawal rejection notifies the requester with the admin reason when present.
- New account creation through normal or Google registration notifies Admin accounts.

## Design Notes

- Commands: reuse existing REST commands that already trigger each event.
- API: no new REST endpoint; WebSocket payload remains `NotificationResponse`.
- Tables: no schema change; reuse `notifications`.
- Domain rules: notifications are side effects after the existing business rule
  succeeds.
- UI surfaces: target URLs should point to the likely existing dashboard pages.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id US-030 --unit 1 --integration 0 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | `.\mvnw.cmd -Dtest=NotificationServiceTest,PaymentWalletServiceTest,ContractExecutionServiceTest,AdminServiceTest test` |
| Integration | Not required for this bounded service change. |
| E2E | Manual WebSocket/STOMP subscribe if frontend wants realtime verification. |
| Platform | Not required. |
| Release | Full `.\mvnw.cmd test` when local database migrations are clean. |

## Harness Delta

No harness rule changes.

## Evidence

- Implemented new notification types in `NotificationService`:
  `CONTRACT_REJECTED_BY_BUSINESS`, `JOB_POST_QUOTA_CONSUMED`,
  `WALLET_TOPUP_SUCCEEDED`, `WITHDRAWAL_REVIEW_REQUESTED`,
  `WITHDRAWAL_APPROVED`, `WITHDRAWAL_REJECTED`, and
  `NEW_ACCOUNT_CREATED`.
- Wired notifications into contract termination, job publish quota consumption,
  PayOS top-up sync, withdrawal create/approve/reject, normal registration,
  Google registration, and admin-created accounts.
- `.\mvnw.cmd "-Dtest=NotificationServiceTest,PaymentWalletServiceTest,ContractExecutionServiceTest,AdminServiceTest" test`
  passed on 2026-06-26: 50 tests, 0 failures, 0 errors.
- `.\mvnw.cmd -DskipTests compile` passed.
- `git diff --check` passed with only line-ending conversion warnings.

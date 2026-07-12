# US-054 — Post-commit Idempotent Admin Dispute Settlement Reports

## Goal

Close the remaining US-049 notification proof gaps without changing payout
authority or the read-only Admin dashboard contract.

- Staff remains the payout decision-maker.
- Settlement remains automatic.
- Admin receives an informational report only after settlement commits.
- A replayed settlement event cannot create a duplicate report for an Admin.
- The public evidence field remains `evidenceReport`; no breaking API rename is
  part of this story.

## Contract

`ContractExecutionService` publishes `DisputeSettlementCompletedEvent` inside
the settlement transaction. `DisputeSettlementNotificationListener` handles it
with `TransactionPhase.AFTER_COMMIT`, so a rollback never invokes notification
delivery.

Each Admin notification uses the durable key:

```text
DISPUTE_SETTLEMENT_REPORTED:{disputeId}:{adminAccountId}
```

Migration V55 adds the nullable key and a partial unique index. Existing
notification types are unaffected.

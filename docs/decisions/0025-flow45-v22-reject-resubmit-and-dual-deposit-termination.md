# Flow 4–5 v2.2 Reject, Resubmit, And Dual-Deposit Termination

Date: 2026-07-09

## Status

Accepted

## Context

Team review corrected three v2.1 assumptions: final-product rejection must not
automatically create dispute, immediate termination must carry a 10% contract-
value penalty, and Expert must fund a 10% deposit so that penalty is secured.

## Decision

- Business rejection marks the deliverable rejected, stores feedback, and
  returns the milestone to `IN_PROGRESS`.
- Expert correction and resubmission use the normal deliverable endpoint.
- Dispute begins only from an explicit Business or Expert dispute command.
- Business must hold 20% and Expert must hold 10% of total contract value before
  activation.
- Immediate termination requires no Staff review.
- The initiating party pays 10% of total contract value to the counterparty:
  Business pays from its held 20%; Expert pays from its held 10%.
- Standard evidence-based termination may enter Staff review and does not apply
  the fixed immediate-termination penalty.
- Staff remains final professional decision-maker in disputes; Admin does not
  override Staff.
- Every public route remains under `/api/v1`.

## Alternatives Considered

1. Rejection automatically creates self-resolve dispute. Rejected because normal
   correction/resubmission is not a dispute.
2. Immediate termination without penalty. Superseded by the approved 10% exit
   compensation rule.
3. Charge Expert available wallet on cancellation. Rejected because Expert must
   pre-fund a held 10% deposit.

## Consequences

Positive:

- Ordinary correction cycles remain lightweight.
- Dispute records represent genuine explicit disagreements.
- Immediate exit compensation is fully funded and deterministic.
- Neither party can activate a contract without its required commitment.

Tradeoffs:

- Deposit schema and activation logic must support two participant-owned rows.
- Existing reject/dispute implementation requires a state-machine rewrite.
- Legacy active contracts require an explicit Expert-deposit compatibility
  policy.

## Follow-Up

- Implement through high-risk migration, wallet, contract, milestone, dispute,
  and test stories.
- Synchronize OpenAPI and product documentation after runtime implementation.

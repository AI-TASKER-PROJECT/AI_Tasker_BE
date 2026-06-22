# Recommendation Selection State

Date: 2026-06-22

## Status

Accepted

## Context

Business users need to select an Expert from the AI recommendation list and notify that Expert to submit a proposal. At selection time, the Expert usually has not submitted a proposal yet, so there may be no `proposals` row to update.

## Decision

Add `business_selected` to both `proposals` and `expert_recommendations`. The actionable selection state for the AI recommendation button is stored on `expert_recommendations.business_selected`; `proposals.business_selected` defaults to false for future proposal-level workflows.

## Alternatives Considered

1. Store selection only on `proposals`. Rejected because no proposal row necessarily exists before the Expert is notified.
2. Create a placeholder proposal automatically. Rejected because proposal submission still belongs to the Expert and consumes Expert proposal quota.

## Consequences

Positive:

- Business selection can be persisted before proposal submission.
- Expert notification can be sent without fabricating proposal data.
- Proposal rows still gain the requested boolean field for later workflow extensions.

Tradeoffs:

- Selection state is split by lifecycle stage: recommendation selection first, proposal state later.

## Follow-Up

- Decide later whether a Business can select multiple recommended Experts for the same job or only one.

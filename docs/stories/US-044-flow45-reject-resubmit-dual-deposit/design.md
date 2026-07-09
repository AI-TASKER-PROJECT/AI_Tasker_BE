# Design — US-044

## Domain Model

Participant-owned contract deposits: Business 20%, Expert 10%. Deliverables
retain submission rounds and rejection history independently of disputes.

## Application Flow

Reject moves `UNDER_REVIEW -> IN_PROGRESS`. Resubmit moves
`IN_PROGRESS -> UNDER_REVIEW`. Explicit dispute moves eligible work states to
`DISPUTED`. Immediate termination consumes 10% from the initiator's held deposit.

## Interface Contract

Add `/api/v1/contracts/{contractId}/expert-deposit/pay`; retain versioned
Business deposit and immediate-termination commands.

## Data Model

Generalize `contract_deposits` by owner role/account and persist penalty
resolution fields.

## UI / Platform Impact

Backend-only specification; clients must show both funding requirements and
explicit penalty confirmation.

## Observability

Audit deposit holds, activation, rejection cycles, explicit disputes, penalty,
compensation, refunds, and closure.

## Alternatives Considered

See decision 0025.

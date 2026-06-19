# Contract Management

## Source

This product contract is derived from `SPEC-CONSTRACT.md` and the active backend
implementation. The source of truth after this slice is this document plus the
Spring service/controller code, Flyway migrations, and tests.

## Actors

- Business creates draft contracts, signs contracts, signs NDA, terminates
  eligible contracts, and completes reviewed milestones.
- Expert reviews draft contracts, signs contracts, signs NDA, rejects eligible
  contracts, and submits deliverables.
- Admin can terminate eligible contracts.
- Staff participates only through dispute handling.

## Lifecycle

```text
Accepted Proposal
  -> Draft Contract
  -> Negotiating
  -> PendingDeposit
  -> Active
  -> Completed
  -> Closed
```

Alternate exits:

- `Draft` or `Negotiating` -> `Cancelled` when the expert rejects.
- Non-final contract -> `Terminated` when the owning business or admin
  terminates it.

## Rules

- A draft contract can be created only from an `Accepted` proposal owned by the
  current approved business.
- A proposal can have only one contract.
- The proposal job must have at least one milestone.
- Draft creation snapshots job milestones into `contract_milestones`, using
  proposal milestone budgets when supplied.
- Contract and NDA signatures are limited to the business and expert attached
  to the contract.
- Signing or requesting changes is allowed only while the contract is `Draft`
  or `Negotiating`.
- A change request resets both contract signatures, both NDA signatures, and
  `activated_at`, then moves the contract to `Negotiating`.
- The contract moves to `PendingDeposit` after business signature, expert
  signature, business NDA, and expert NDA are all present.
- The owning business must pay the 20% wallet security deposit before execution
  starts.
- Deposit payment sets the contract to `Active`, applies final contract
  milestone budgets back to job milestones, attaches the contract id to those
  milestones, and moves the job to `IN_PROGRESS`.
- Expert rejection is allowed only from `Draft` or `Negotiating`; it moves the
  contract to `Cancelled` and the job to `PROPOSAL_REVIEW`.
- Deliverables can be submitted only by the contract expert while the contract
  is `Active` and both NDA signatures exist; submission moves the milestone to
  `Under Review`.
- The owning business can complete a milestone only from `Under Review`.
- When every contract milestone is `Completed`, the system moves the contract
  to `Completed` and the job to `CLOSED`.
- Admin deposit refund/resolution closes the contract as `Closed`.
- Completed, terminated, and cancelled contracts cannot be terminated again.

## API

- `POST /api/v1/contracts/from-proposals/{proposalId}`
- `POST /api/v1/contracts/change-requests`
- `POST /api/v1/contracts/{contractId}/sign`
- `POST /api/v1/contracts/{contractId}/nda-sign`
- `POST /api/v1/contracts/{contractId}/deposit/pay`
- `POST /api/v1/admin/contracts/{contractId}/deposit/refund`
- `POST /api/v1/contracts/{contractId}/reject`
- `POST /api/v1/contracts/{contractId}/terminate?reason=...`
- `POST /api/v1/deliverables`
- `POST /api/v1/milestones/{milestoneId}/complete`

## Notifications And Audit

The backend records audit events for draft creation, signing, NDA signing,
activation, rejection, milestone completion, contract completion, deliverable
submission, and termination.

REST/WebSocket notifications are emitted for contract creation, signing, NDA
signing, activation, rejection, deliverable submission, and completion where
participant account ids are available.

# Contract Management

## Source

This product contract is derived from the active backend implementation,
Flyway migrations, and tests.

## Actors

- Business creates draft contracts, signs contracts, signs NDA, pays deposits,
  terminates eligible contracts, and completes reviewed milestones.
- Expert reviews draft contracts, signs contracts, signs NDA, rejects eligible
  contracts, and submits deliverables.
- Admin can terminate eligible contracts and resolve deposit handling.
- Staff participates only through dispute handling.

## Lifecycle

```text
Accepted Proposal
  -> DRAFT
  -> PENDING
  -> ACTIVE
  -> COMPLETED
```

Alternate exits:

- `DRAFT` or `PENDING` -> `CANCELLED` when the expert rejects.
- Non-final contract -> `CANCELLED` when the owning business or admin
  terminates or cancels it.

## Status Values

- Contracts: `DRAFT`, `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
- Jobs: `DRAFT`, `OPEN`, `IN_PROGRESS`, `CLOSED`.
- Milestones: `PENDING`, `DEPOSITED`, `IN_PROGRESS`, `UNDER_REVIEW`,
  `DISPUTED`, `COMPLETED`.
- Wallet transactions: `POSTED`.

## Rules

- A draft contract can be created only from an `Accepted` proposal owned by the
  current approved business.
- A proposal can have only one contract.
- The proposal job must have at least one milestone.
- `DRAFT` creation snapshots job milestones into `contract_milestones`, using
  proposal milestone budgets when supplied.
- Contract and NDA signatures are limited to the business and expert attached
  to the contract.
- Signing is allowed only while the contract is `DRAFT`.
- The negotiation/change-request lifecycle is disabled and must not move
  contracts out of `DRAFT`.
- The contract moves to `PENDING` after business signature, expert signature,
  business NDA, and expert NDA are all present.
- The owning business must pay the 20% wallet security deposit before execution
  starts.
- Deposit payment sets the contract to `ACTIVE`, applies final contract
  milestone budgets back to job milestones, attaches the contract id to those
  milestones, and moves the job to `IN_PROGRESS`.
- Expert rejection is allowed only from `DRAFT` or `PENDING`; it moves the
  contract to `CANCELLED` and the job back to `OPEN`.
- Deliverables can be submitted only by the contract expert while the contract
  is `ACTIVE` and both NDA signatures exist; submission moves the milestone to
  `UNDER_REVIEW`.
- The owning business can complete a milestone only from `UNDER_REVIEW`.
- When every contract milestone is `COMPLETED`, the system moves the contract
  to `COMPLETED` and the job to `CLOSED`.
- SLA auto-approval of an overdue reviewed milestone uses the same finalization
  rule: if the auto-approved milestone completes the last remaining contract
  milestone, the contract becomes `COMPLETED` and the job becomes `CLOSED`.
- Admin deposit refund/resolution leaves completed contracts `COMPLETED`;
  cancelled contracts remain `CANCELLED`.
- `COMPLETED` and `CANCELLED` contracts cannot be terminated again.

## API

- `POST /api/v1/contracts/from-proposals/{proposalId}`
- `POST /api/v1/contracts/change-requests` (disabled)
- `POST /api/v1/contracts/{contractId}/sign`
- `POST /api/v1/contracts/{contractId}/nda-sign`
- `POST /api/v1/contracts/{contractId}/deposit/pay`
- `POST /api/v1/admin/contracts/{contractId}/deposit/refund`
- `POST /api/v1/contracts/{contractId}/reject`
- `POST /api/v1/contracts/{contractId}/terminate?reason=...`
- `POST /api/v1/deliverables`
- `POST /api/v1/milestones/{milestoneId}/complete`
- `POST /api/v1/milestones/sla-auto-approve`

## Notifications And Audit

The backend records audit events for draft creation, signing, NDA signing,
deposit activation, rejection, milestone completion, contract completion,
deliverable submission, and termination.

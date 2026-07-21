# Design

## Domain Model

- `jobs` remains the editable marketplace post source for `DRAFT` and `OPEN`.
- `proposals` remains owned by the submitting Expert.
- `contract_change_requests` stores participant-proposed contract changes.
- `contract_milestones` remains the authoritative contract execution snapshot.

## Application Flow

1. Context-rich route aliases call the same service methods as existing routes,
   adding job/contract ownership checks before delegating.
2. `PUT /api/v1/jobs/{jobId}` accepts `DRAFT` and `OPEN` edits. A job with any
   contract remains locked from direct edit.
3. `PUT /api/v1/proposals/{proposalId}` lets the owning Expert update content
   while the proposal is `Pending` or `Accepted`, unless a contract exists.
4. `POST /api/v1/contracts/{contractId}/change-requests` stores a pending
   request and notifies the counterparty.
5. `POST /api/v1/contracts/{contractId}/change-requests/{requestId}/accept`
   applies proposed fields after verifying the actor is the counterparty.
6. `POST /api/v1/contracts/{contractId}/change-requests/{requestId}/reject`
   records rejection without applying changes.

## Interface Contract

- Preferred aliases:
  - `POST /api/v1/jobs/{jobId}/milestones`
  - `PATCH /api/v1/jobs/{jobId}/milestones/{milestoneId}`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deliverables`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/source-code-file`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/approve`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/reject`
  - `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/disputes`
- Existing routes remain compatibility aliases.
- Contract change request body includes summary, optional budget/timeline/scope,
  optional milestone snapshot list, and optional review note on reject.

## Data Model

Add nullable columns to `contract_change_requests`:

- `proposed_scope`
- `proposed_milestones`
- `review_note`

No new table is added.

## UI / Platform Impact

Frontend can gradually migrate to preferred routes and add edit forms for
public jobs, proposal revisions, and contract change requests.

## Observability

Every edit/request/review emits an audit log. Notifications are sent to affected
counterparties with target URLs that match existing contract/job/proposal pages.

## Alternatives Considered

1. Direct active-contract edits. Rejected because active contract data controls
   escrow, deadlines, acceptance, and disputes.

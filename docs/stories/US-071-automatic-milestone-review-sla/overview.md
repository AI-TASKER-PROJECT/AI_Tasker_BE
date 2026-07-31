# Overview

## Current Behavior

Milestone review SLA is stored as `default_sla_days`. The backend only processes
overdue reviews when an Admin calls a manual endpoint, and that implementation
scans every milestone even though the route contains a contract id. The frontend
exposes that manual action in the contract workspace. No durable review deadline
is returned for a reliable countdown.

## Target Behavior

Submitting a final deliverable snapshots an automatic-review deadline and moves
the milestone to `UNDER_REVIEW`. A backend scheduler processes due milestones.
If the Business has neither approved nor rejected and no dispute or termination
blocks settlement, the system completes the milestone and releases its escrow to
the Expert exactly once.

Admin can configure one positive duration using minutes, hours, or days. Admin
cannot manually run SLA approval or disable the automatic rule. Configuration
changes apply to submissions made after the change; an active review keeps its
snapshotted deadline.

The workspace shows the server-provided deadline and a client-side countdown for
visibility. The frontend never performs settlement based on its own timer.

## Affected Users

- Expert submitting a final deliverable and receiving milestone payout.
- Business reviewing, accepting, or rejecting before the SLA deadline.
- Admin configuring the automatic-review duration.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/product/admin-configuration.md`
- `docs/swagger-api-overview.md`

## Non-Goals

- Change execution-deadline or progress-report SLA behavior.
- Change dispute, termination, deposit-rate, or manual Business review rules.
- Add manual Admin approval or settlement controls.


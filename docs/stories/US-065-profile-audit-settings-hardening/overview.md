# Overview

## Current Behavior

Profile submissions can be repeatedly resubmitted while pending or after approval, which can move an already-approved Business or Expert back to `Pending`. Staff can also re-approve or re-reject profiles after a final review. Audit logs miss explicit wallet top-up success/failure events, route fallback can spam PayOS sync logs, dispute decision/settlement objects fall back to the Staff actor, and some audit actions/entities remain raw technical labels. Admin settings list seed/demo keys that are not active business controls.

## Target Behavior

Pending profiles are immutable for resubmission until rejected. Rejected profiles can be resubmitted and return to `Pending`. Approved profiles can be updated without reopening review. Staff can review only `Pending` profiles. Wallet top-up terminal outcomes are audited once per status transition, PayOS sync fallback spam is suppressed, dispute decision/settlement audit objects describe both participants, and audit display labels are normalized. Admin settings exposes only supported operational keys used by backend flows.

## Affected Users

- Business and Expert profile owners.
- Staff profile reviewers.
- Admin audit/settings users.
- Business and Expert wallet users.

## Affected Product Docs

- `docs/product/profile-review-routing.md`
- `docs/product/admin-configuration.md`
- `docs/product/contract-management.md`

## Non-Goals

- Changing FE code.
- Adding new profile review endpoints.
- Reworking PayOS provider integration or wallet ledger accounting.

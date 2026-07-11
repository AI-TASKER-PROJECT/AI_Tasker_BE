# Overview

## Current Behavior

- Staff can discover assigned work indirectly through `GET /api/v1/contracts`, then list disputes per contract.
- There is no dedicated Staff dispute inbox API.
- `staff.specialization` is a free-text field without structured domain or skill mappings.
- Candidate ranking exposes the specialization text but primarily sorts by active dispute workload; it does not compare job domains or skills.
- Automatic routing can select a Staff member outside the job's professional domain.

## Target Behavior

1. **Structured Staff Specialization**: Add `staff_domains` and `staff_skills` mapping tables. Admin creates/updates Staff with valid `domainIds` and `skillIds`. Migrate demo Staff with explicit domain mappings.
2. **Domain-Mandatory Staff Routing**: Auto-routing only considers Staff sharing at least one job domain. Ranking: matched domain count DESC, matched skill count DESC, workload ASC, staffId ASC. Manual routing applies same domain guard. No-match leaves dispute unchanged.
3. **Staff Dispute Inbox**: `GET /api/v1/staff/disputes` with pagination and status filter. Staff identity derived from JWT.
4. **Assigned-Case Authorization**: Staff can only read disputes assigned to them. Contract-dispute list, dispute detail, and supporting case data are scoped.

## Affected Users

- **Staff**: Gains inbox and domain-relevant routing.
- **Admin**: Gains structured specialization management for Staff profiles.

## Affected Product Docs

- `docs/flows/dispute-flow.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/product/contract-management.md`

## Non-Goals

- No frontend implementation.
- No Staff access to the global Admin dispute dashboard.
- No fallback assignment to Staff outside the job domain.
- No change to payout percentages, escrow invariants, or automatic settlement.
- No Admin approval, override, reassignment, or cancellation workflow for milestone disputes.
- No redesign of termination-request assignment.
- No removal of the legacy `specialization` display field.

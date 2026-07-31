# Decision 0033 — Balanced Staff Dispute Routing

## Status

Accepted

## Context

Decision 0027 made domain matching mandatory but ranked domain and skill counts
before workload. A Staff member with a stronger match therefore kept winning
every assignment even after becoming busier. Most seeded business domains also
had only one eligible Staff, and `BRAND_VISUAL_DESIGN` had none.

## Decision

1. Keep at least one matching job domain as a hard eligibility gate.
2. Only approved Staff accounts participate in automatic or manual routing;
   an account linked to either contract participant profile is excluded as a
   conflict of interest.
3. Compute specialization as normalized domain coverage (60%) plus skill
   coverage (40%). Candidates qualify when they score at least 70% and remain
   within 20 percentage points of the best score; when no candidate reaches
   70%, use the same 20-point tolerance from the best available score.
4. Inside the qualified pool, rank active workload ascending, specialization
   score descending, last assignment ascending, then Staff id ascending.
5. Enforce `dispute_staff_max_active_cases`, default `5`. An escalation with no
   available capacity remains `ESCALATION_REQUESTED`; manual routing returns a
   capacity error.
6. Lock Staff rows in deterministic id order for the short select-and-assign
   transaction so concurrent requests recalculate workload after earlier
   assignments commit.
7. Add a partial index for active assigned-dispute workload counts.
8. Seed at least two dispute-capable Staff mappings for every business domain;
   keep `PROFILE_REVIEW` isolated.

This decision supersedes the ranking order and no-capacity behavior in Decision
0027. Domain eligibility and assigned-case authorization remain unchanged.

## Consequences

- Equally qualified Staff receive repeated assignments with at most one-case
  difference while their cases remain active.
- Better-qualified Staff remain preferred when another candidate falls outside
  the qualification tolerance.
- Routing is serialized briefly across Staff rows to prevent concurrent
  double-selection from stale workload reads.
- Admin can tune capacity through the existing system-settings API.
- Migrations V60-V61 add the setting, workload/history indexes, and domain coverage.

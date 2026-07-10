# Exec Plan — US-043

## Goal

Produce a complete v2.1 backend specification for Flow 4–5 before implementation.

## Scope

In scope:

- Progress-report request SLA and structured feedback.
- `OVERDUE` milestone behavior.
- Staff candidate, evidence-window, access, and SLA rules.
- Staff-final dispute authority.
- Immediate termination without a fixed 10% penalty.
- Mandatory `/api/v1` public API prefix.

Out of scope:

- Backend implementation, migrations, and runtime API changes.
- Admin override of Staff decisions.
- Any fixed termination penalty.

## Risk Classification

Risk flags:

- Public API, authorization, financial settlement, state machines, and schema.

Hard gates:

- Financial behavior and role authority must be unambiguous.

## Work Phases

1. Read current product, decision, and story contracts.
2. Reconcile accepted changes and explicit exclusions.
3. Update the specification.
4. Run static consistency checks.
5. Record validation and Harness trace.

## Stop Conditions

Pause if a rule would reintroduce an Admin dispute override, fixed penalty, or
non-versioned API.

# Exec Plan

## Goal

Align Spring Security with the existing service rule so public job pages can
read milestones for `OPEN` jobs without authentication.

## Scope

In scope:

- Update the GET security matcher for the job milestone route.
- Refresh docs that describe public job behavior.

Out of scope:

- Changing milestone data shape.
- Changing access rules for non-`OPEN` jobs.

## Risk Classification

Risk flags:

- Authorization
- Public contracts
- Existing behavior

Hard gates:

- Authorization

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- Non-`OPEN` milestone visibility needs to change.
- Additional public job subroutes should be opened together.
- Validation requirements need to be weakened.

# Exec Plan

## Goal

Reduce the public Contract Execution API surface to the intended v1 target
routes while preserving the v2 state machine implementation.

## Scope

In scope:

- Remove duplicate/legacy public mappings from `ContractExecutionController`.
- Regenerate OpenAPI from runtime Swagger.
- Rebuild Swagger overview, Swagger test guide, and Postman guide.
- Update README, backend guide, and contract-management product docs.
- Verify API counts and service behavior.

Out of scope:

- Database migration changes.
- Frontend compatibility shims.
- Service logic rewrites beyond what route cleanup requires.

## Risk Classification

Risk flags:

- Public contracts
- Existing behavior
- Weak proof
- Multi-domain

Hard gates:

- Public API shape changed; decision record required.

## Work Phases

1. Snapshot current dirty worktree and OpenAPI counts.
2. Confirm target routes from spec/product docs/controller/service.
3. Remove public controller mappings for duplicate/legacy/support routes.
4. Regenerate OpenAPI and guides.
5. Run compile, focused service tests, full Maven tests, and count checks.
6. Record Harness story, decision, and trace evidence.

## Stop Conditions

Pause for human confirmation if service behavior must be removed, database
schema must change, or the target API count cannot be reached without weakening
the v2 business rules.

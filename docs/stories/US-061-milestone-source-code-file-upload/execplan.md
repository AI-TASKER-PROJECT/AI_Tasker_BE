# Exec Plan

## Goal

Allow milestone source code to be submitted by repository URL, uploaded ZIP, or
both while preserving `demoLink` semantics.

## Scope

In scope:

- Milestone-scoped ZIP upload.
- Additive database fields and request/response serialization.
- URL-or-file final-deliverable validation.
- Unit, migration, and API documentation proof.

Out of scope:

- Virus scanning or archive extraction.
- Frontend implementation.
- Public anonymous file access.

## Risk Classification

Risk flags:

- Public contracts.
- Data model.
- Existing behavior.
- External systems.

Hard gates:

- Firebase Storage integration remains behind `FirebaseStorageService`.
- Schema changes are additive through Flyway.

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- Product behavior is ambiguous.
- Data migration or deletion risk appears.
- Validation requirements need to be weakened.
- Architecture direction changes.

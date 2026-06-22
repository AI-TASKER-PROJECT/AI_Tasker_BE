# Exec Plan

## Goal

Add backend support for expert portfolio file upload with Firebase storage and audit logging.

## Scope

In scope:

- Add audit action constant.
- Add `ProfileService.uploadExpertPortfolio`.
- Add `POST /api/v1/profiles/expert/portfolio-file`.
- Update OpenAPI and Swagger guide docs.
- Add unit proof for upload path, role gate, and audit action.

Out of scope:

- Database migration.
- Frontend changes.
- Firebase integration test with live credentials.

## Risk Classification

Risk flags:

- Authorization.
- Audit/security.
- External systems.
- Public contracts.
- Weak proof.

Hard gates:

- Authorization.
- Audit/security.
- External provider behavior.

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- The frontend requires a different multipart key than `file`.
- A database field must automatically store the returned path.
- Live Firebase behavior differs from the existing upload service contract.

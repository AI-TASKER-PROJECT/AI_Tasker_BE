# Expert Portfolio File Upload Audit

Date: 2026-06-22

## Status

Accepted

## Context

The frontend needs to upload expert portfolio files through `POST /api/v1/profiles/expert/portfolio-file`. The backend already has Firebase-backed upload flows for business license files and expert certificate files, including audit logs, but the portfolio-file route and action are missing.

## Decision

Add a dedicated expert portfolio upload endpoint, service method, Firebase folder, and audit action. Keep the response as `ApiResponse<String>` with the storage path, matching existing file upload behavior.

## Alternatives Considered

1. Reuse `POST /api/v1/profiles/portfolio/certificate-file`. This would avoid a new route but would not match the frontend contract or give audit logs a distinct portfolio-file action.

## Consequences

Positive:

- Frontend can submit portfolio files to the expected backend route.
- Audit logs can distinguish portfolio file uploads from certificate uploads.
- The implementation stays aligned with existing Firebase upload patterns.

Tradeoffs:

- The returned path still needs the existing profile/portfolio save API to persist it where the frontend wants it displayed.

## Follow-Up

- Add integration proof when a local Firebase emulator or test storage adapter is available.

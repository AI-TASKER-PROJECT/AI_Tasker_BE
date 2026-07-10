# Overview

## Current Behavior

Expert certificate upload exists at `POST /api/v1/profiles/portfolio/certificate-file`, but the backend does not expose the frontend-requested `POST /api/v1/profiles/expert/portfolio-file` route for uploading a portfolio file.

## Target Behavior

Authenticated EXPERT users can upload a portfolio file with multipart key `file`. The backend stores the file through `FirebaseStorageService`, returns the storage path in the standard `ApiResponse<String>` envelope, and writes an audit log against the expert profile when one exists.

## Affected Users

- EXPERT uploading portfolio evidence.
- STAFF/ADMIN reviewing audit logs.
- Frontend profile/KYC screens calling the portfolio upload endpoint.

## Affected Product Docs

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/openapi/openapi-v1.json`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Non-Goals

- No database schema change.
- No frontend code change.
- No change to existing certificate upload behavior.

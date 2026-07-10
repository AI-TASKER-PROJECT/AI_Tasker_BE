# Design

## Domain Model

The upload remains tied to the existing expert profile model. No new table or column is introduced; the endpoint returns a Firebase storage path for the frontend to place into the relevant profile or portfolio payload.

## Application Flow

1. `ProfileController.uploadExpertPortfolio` accepts `multipart/form-data` with key `file`.
2. `ProfileService.uploadExpertPortfolio` requires role `EXPERT`.
3. The service resolves the current account id and uploads to `expert-portfolios/accounts/{accountId}`.
4. If an expert profile exists for the account, the service records an audit log action against `expert_profiles`.

## Interface Contract

- `POST /api/v1/profiles/expert/portfolio-file`
- Auth: Bearer JWT for EXPERT.
- Body: multipart/form-data with required `file`.
- Response: `ApiResponse<String>` containing the Firebase storage path.

## Data Model

No migration is required.

## UI / Platform Impact

Frontend can call the backend route directly instead of targeting a missing endpoint.

## Observability

Uploads use `AuditLogService.ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE` and entity `expert_profiles` when the current account has an expert profile.

## Alternatives Considered

1. Reuse the certificate upload endpoint for portfolio files. Rejected because frontend expects a distinct portfolio-file route and audit action.

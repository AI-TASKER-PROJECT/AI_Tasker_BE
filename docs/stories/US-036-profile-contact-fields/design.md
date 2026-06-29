# Design

## Domain Model

No persisted model change. `BusinessProfileEntity` gains transient `email` and
`phone` fields. `ExpertProfileEntity` gains transient `email`; `phone`,
`fullName`, and `title` already existed.

## Application Flow

`ProfileService` enriches profile read responses from `AccountRepository`:

- `attachBusinessAccountInfo` sets `fullName`, `email`, and `phone`.
- `attachExpertPublicAccountInfo` and `attachExpertAccountInfo` set `fullName`,
  `email`, `phone`, and `title`.

Current, by-id, by-job, and list reads call the same enrichment helpers so the
response surface is consistent.

## Interface Contract

Business profile read APIs return `BusinessProfileEntity` with contact fields:

- `GET /api/v1/profiles/business/me`
- `GET /api/v1/profiles/business/{businessId}`
- `GET /api/v1/profiles/business/by-job/{jobId}`
- `GET /api/v1/profiles/business`

Expert profile read APIs return `ExpertProfileEntity` with contact fields:

- `GET /api/v1/profiles/expert/me`
- `GET /api/v1/profiles/expert/{expertId}`
- `GET /api/v1/profiles/expert`

## Data Model

No migration. Email, phone, and full name remain sourced from `account`.

## UI / Platform Impact

Frontend profile pages, user-info views, and contact/expanded panels can render
without an extra account lookup.

## Observability

No new audit event for reads. Existing service role checks remain in place.

## Alternatives Considered

1. Add DTOs immediately. Deferred to keep the change focused and compatible
   with current Swagger entity schemas.
2. Keep public Business routes contact-free. Rejected by the current product
   requirement.

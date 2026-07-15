# Design

## Domain Model

`DeliverableEntity` and `MilestoneProgressReportEntity` gain an optional
`sourceCodeFileUrl`. A final deliverable is valid when either the repository URL
or uploaded archive path is non-blank.

## Application Flow

1. Expert uploads a ZIP through the milestone-scoped upload route.
2. The service verifies role, approval, contract ownership, active contract,
   NDA signatures, and milestone execution state before calling Firebase.
3. Firebase returns a storage path under a milestone/account-specific folder.
4. The client sends that path as `sourceCodeFileUrl` in a progress report or
   deliverable request.
5. Final deliverable submission rejects requests where both source-code fields
   are blank.

## Interface Contract

- `POST /api/v1/milestones/{milestoneId}/source-code-file`
  - multipart key: `file`
  - EXPERT only
  - ZIP only, maximum 50 MB
  - response: `ApiResponse<String>` containing the Firebase storage path
- Progress-report JSON adds optional `sourceCodeFileUrl`.
- Deliverable JSON adds optional `sourceCodeFileUrl`; at least it or
  `sourceCodeUrl` is required.

## Data Model

Add nullable `source_code_file_url TEXT` columns to `deliverables` and
`milestone_progress_reports` through `V59`.

## UI / Platform Impact

Frontend can present repository URL and ZIP upload as alternative source-code
inputs. `demoLink` remains a URL input.

## Observability

Successful source archive uploads write a milestone-scoped audit record.

## Alternatives Considered

1. Replace repository URL with file-only submission. Rejected because Git
   history and code review remain valuable.
2. Add ZIP to the generic upload allowlist. Rejected because it would also
   permit ZIP files in unrelated license/certificate upload flows.

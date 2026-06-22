# Design

## Domain Model

`jobs.planned_duration_value` and `jobs.planned_duration_unit` define the total
planned job duration. `milestones.duration` and `milestones.duration_unit`
define the planned duration of each milestone.

Valid units are `DAY`, `WEEK`, and `MONTH`. For the cross-field total rule, the
backend converts values to days using:

- `DAY = 1`
- `WEEK = 7`
- `MONTH = 30`

## Application Flow

1. Business calls `POST /api/v1/jobs`.
2. `MarketplaceService.createJob` validates required job fields.
3. The service validates and normalizes job duration.
4. The service validates and normalizes all nested milestone durations.
5. If milestone durations are present, the job duration is required.
6. The total converted milestone duration must be less than or equal to the
   converted job duration.
7. Only validated data is saved.

## Interface Contract

The route shape is unchanged:

- `POST /api/v1/jobs`

Validation errors use existing `AppException` behavior and messages:

- `JOB DURATION VA DURATION UNIT PHAI CUNG CO HOAC CUNG KHONG CO`
- `JOB DURATION PHAI LON HON 0`
- `MILESTONE DURATION VA DURATION UNIT PHAI CUNG CO HOAC CUNG KHONG CO`
- `MILESTONE DURATION PHAI LON HON 0`
- `DURATION UNIT KHONG HOP LE. CHAP NHAN: DAY, WEEK, MONTH`
- `JOB DURATION BAT BUOC KHI MILESTONE CO DURATION`
- `TONG DURATION CUA MILESTONE KHONG DUOC VUOT QUA DURATION CUA JOB`

## Data Model

No new migration is needed. The existing columns are used:

- `jobs.planned_duration_value`
- `jobs.planned_duration_unit`
- `milestones.duration`
- `milestones.duration_unit`
- `contract_milestones.duration`
- `contract_milestones.duration_unit`

## UI / Platform Impact

Frontend can continue sending duration fields in the existing job creation
payload. The backend now rejects inconsistent duration input before saving.

## Observability

No new audit action is introduced. Successful job creation continues to record
`ACTION_CREATE_JOB_DRAFT`.

## Alternatives Considered

1. Rely only on database constraints. Rejected because the database cannot check
   aggregate milestone duration against job duration in the incoming request.
2. Require all jobs to have duration. Rejected to preserve nullable legacy data
   and draft flexibility when no milestone durations are supplied.

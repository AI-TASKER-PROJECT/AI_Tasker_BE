# Design

## API

`POST /api/v1/profiles/approve/{type}/{id}` keeps the existing `status` query
parameter and adds:

```text
reason=<text>
```

`reason` is optional for `Approved` and required for `Rejected`.

## Database

Add nullable `VARCHAR(500)` columns:

- `business_profiles.rejection_reason`
- `expert_profiles.rejection_reason`

The migration is `V33__profile_rejection_reason.sql` because week6 already uses
`V32`.

## Service Rules

- `status=Rejected` with blank reason throws `LY DO TU CHOI KHONG DUOC DE TRONG`.
- Reason over 500 characters throws
  `LY DO TU CHOI KHONG DUOC VUOT QUA 500 KY TU`.
- `status=Approved` stores `null` reason.
- Business/expert resubmission sets profile status back to `Pending` and clears
  the old reason.

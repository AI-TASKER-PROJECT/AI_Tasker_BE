# US-012 Profile Rejection Reason

## Goal

Port the week4 staff rejection reason behavior into the week6 synthetic branch.

## Scope

- Add database columns for rejection reason on business and expert profiles.
- Include `rejectionReason` in JPA entities and profile responses.
- Accept query `reason` in profile approval API.
- Require `reason` when staff rejects a profile.
- Clear previous reason when a profile is approved or resubmitted.

## Non-goals

- Do not reuse the old week4 `V19` migration name.
- Do not redesign the KYB/KYC approval workflow.

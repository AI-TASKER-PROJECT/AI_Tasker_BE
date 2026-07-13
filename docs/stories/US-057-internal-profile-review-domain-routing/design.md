# Design

## Data

Reuse existing tables:

- `domains`: add `PROFILE_REVIEW`.
- `staff_domains`: map Staff who can review profiles to the internal domain.

No new table is introduced.

## Domain Visibility

`CatalogService.listDomains(activeOnly)` loads the normal catalog list and filters out `PROFILE_REVIEW` unless `SecurityUtils.hasRole("ADMIN")` is true.

This keeps anonymous and Business/Expert users from seeing the internal review domain while allowing Admin to manage Staff specialization.

## Job Assignment Guard

`CatalogService.replaceJobDomains` rejects `PROFILE_REVIEW` for non-admin callers. This closes the direct API bypass where a Business could submit the hidden `domainId` manually.

## Profile Review Routing

`ProfileService.notifyStaffProfileSubmitted` resolves the `PROFILE_REVIEW` domain and sends notifications only to Staff linked through `staff_domains`.

`ProfileService.approveProfile` resolves the current Staff row and checks the same mapping before modifying KYB/KYC status.

Staff-only profile review list methods also require `PROFILE_REVIEW` so non-review Staff cannot use review screens as a side door.

## Failure Behavior

- Missing `PROFILE_REVIEW` during notification produces no notification rather than blocking profile submission.
- Missing `PROFILE_REVIEW` during Staff review authorization throws an app configuration error.
- Staff without the mapping receives a forbidden-style business exception.

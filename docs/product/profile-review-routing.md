# Profile Review Routing

KYB/KYC profile review is Staff work restricted by an internal Staff domain.

## Internal Domain

`PROFILE_REVIEW` is stored in `domains` so Admin can assign it through existing Staff specialization flows. It is not a marketplace job domain.

Rules:

- Admin domain catalog responses include `PROFILE_REVIEW`.
- Anonymous, Business, Expert, and Staff domain catalog responses hide `PROFILE_REVIEW`.
- Business job-domain assignment cannot include `PROFILE_REVIEW`.

## Staff Eligibility

A Staff account can receive profile-submission notifications and approve/reject Business or Expert profiles only when its `staffs.staff_id` is mapped to `PROFILE_REVIEW` in `staff_domains`.

The default demo account `staff@aitasker.local` is assigned only this domain so it acts as the profile-review Staff account.

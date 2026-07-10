# Overview

## Current Behavior

Business and Expert users can submit verification profiles, and staff can approve or reject them. The submitter receives a notification after review, but staff do not receive a notification when a new KYB/KYC profile is submitted.

The recommendation test dataset is too small: the shared seed has only one verified Expert, so AI recommendation ranking cannot be tested against a realistic candidate pool.

## Target Behavior

When a Business or Expert submits a verification profile, all staff profiles receive a persisted notification and realtime WebSocket push through the existing notification pipeline.

The database migration seeds:

- 20 fully verified Expert accounts with profiles and portfolios.
- 10 fully verified Business accounts with profiles and open jobs.
- 10 recommendation-oriented jobs with SOW, domains, skills, technologies, and milestones.
- 20 proposals that connect the seeded Experts to matching seeded Jobs.
- Quota and wallet support rows for seeded Business and Expert accounts.

## Affected Users

- STAFF reviewing KYB/KYC profile submissions.
- BUSINESS and EXPERT accounts submitting profile verification data.
- Developers and testers validating Expert recommendation quality.

## Non-Goals

- No frontend changes.
- No new profile review endpoint.
- No automatic approval logic.
- No production data migration beyond local/demo seed records.

# Overview

## Current Behavior

The recommendation demo seed migration creates Expert and Business accounts with
PostgreSQL `format('%02s', n)` and similar string formats. PostgreSQL pads those
string formats with spaces instead of zeroes, so seeded account data can contain
embedded whitespace in emails, phones, national IDs, tax codes, and display
labels.

## Target Behavior

The recommendation demo dataset is reset and re-seeded with deterministic,
whitespace-free account identifiers:

- 20 verified Expert accounts and profiles.
- 10 verified Business accounts and profiles.
- 10 OPEN recommendation jobs with SoW, metadata, milestones, and proposals.
- Quota and wallet support rows for the seeded accounts.

The old malformed seed rows are removed before the corrected dataset is
inserted.

## Affected Users

- Developers and testers using recommendation demo Expert accounts.
- STAFF/ADMIN users reviewing seeded profile/account data.

## Affected Product Docs

- `docs/stories/US-021-profile-verification-staff-notifications-and-recommendation-seed-data/*`

## Non-Goals

- No API contract change.
- No production account cleanup outside the deterministic recommendation-demo ID ranges.
- No frontend changes.

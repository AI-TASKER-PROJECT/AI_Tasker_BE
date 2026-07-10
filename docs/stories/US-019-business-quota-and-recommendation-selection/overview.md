# Overview

## Current Behavior

Expert accounts receive 3 initial proposal credits, but Business accounts start with 0 job-post credits. AI expert recommendations can be generated and read, but Business users cannot persist which recommended expert they prefer or notify that Expert to submit a proposal.

## Target Behavior

Business accounts receive 3 initial free job-post credits. Existing Business quota rows are backfilled once with `INITIAL_BUSINESS_GRANT`.

For AI recommendations, saved recommendation rows expose `businessSelected`. Business users can select one recommended Expert for a job through a dedicated endpoint. The backend marks the recommendation selected and sends a notification to the Expert so they can review the job and submit a proposal. Proposal rows also carry `business_selected=false` by default.

## Affected Users

- BUSINESS publishing jobs and choosing recommended experts.
- EXPERT receiving invitation notifications and submitting proposals.
- ADMIN creating Business/Expert accounts.

## Affected Product Docs

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/data-dictionary.md`
- `docs/openapi/openapi-v1.json`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Non-Goals

- No frontend implementation.
- No automatic proposal creation when Business selects an Expert.
- No paid package quota changes.

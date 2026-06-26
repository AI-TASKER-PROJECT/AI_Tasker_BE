# US-XXX Story Title

## Status

planned

## Lane

tiny | normal | high-risk

## Product Contract

Describe the behavior this story must make true.

## Relevant Product Docs

- `docs/product/...`

## Acceptance Criteria

- Criterion 1.
- Criterion 2.
- Criterion 3.

## Design Notes

- Commands:
- Queries:
- API:
- Tables:
- Domain rules:
- UI surfaces:

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

Only set a proof value to `1` after the matching evidence has been added below.
If a command could not run, keep the value `0` and document the blocker.

| Layer | Expected proof |
| --- | --- |
| Unit | |
| Integration | |
| E2E | |
| Platform | |
| Release | |

## Harness Delta

Document any harness updates made or proposed because of this story.

## Evidence

Add command results, reports, screenshots, links, and the final trace id after
validation exists. Do not mark this story `implemented` while this section is
empty or while validation placeholders remain.

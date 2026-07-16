# Overview

## Current Behavior

Milestone progress reports and deliverables can carry `sourceCodeUrl`, but the
contract execution API has no dedicated source-code archive upload route and no
separate field for the returned Firebase storage path.

## Target Behavior

An approved Expert assigned to an active milestone can upload a ZIP source-code
archive. Progress reports may reference that archive through
`sourceCodeFileUrl`. Final deliverables must provide at least one of
`sourceCodeUrl` or `sourceCodeFileUrl`; clients may provide both. `demoLink`
remains an independent runnable-product URL.

## Affected Users

- Expert submitting milestone progress and final deliverables.
- Business reviewing milestone output.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`
- `docs/postman-api-test-guide.md`
- `docs/openapi/openapi-v1.json`

## Non-Goals

- Uploading demo builds instead of using `demoLink`.
- Extracting or executing uploaded archives.
- Making both source-code submission forms mandatory.

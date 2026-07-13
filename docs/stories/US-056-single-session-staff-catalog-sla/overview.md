# Overview

## Current Behavior

- JWT access and refresh tokens are stateless. A second login for the same account does not invalidate existing tokens.
- `default_sla_days` is seeded as 7, so milestone review SLA auto-approval waits 7 days after the latest deliverable.
- Demo Staff data has one structured Staff record; richer Staff coverage depends on manual Admin setup.
- Domain and Skill catalog descriptions are still English even though the user-facing product copy is Vietnamese.

## Target Behavior

- Each account has one active token version. New login/register/Google auth increments the version and issues access/refresh tokens bound to it.
- Protected HTTP requests, WebSocket CONNECT, and refresh-token renewal reject tokens whose version no longer matches the account.
- Migration seeds 10 additional approved Staff accounts with structured domain and skill mappings.
- Domain and Skill descriptions are Vietnamese.
- `default_sla_days` becomes 3.

## Affected Users

- All authenticated users.
- Admin and Staff users.
- Business and Expert users in milestone review flows.

## Affected Product Docs

- `docs/product/contract-management.md`
- `docs/ARCHITECTURE.md`

## Non-Goals

- Frontend notification copy for forced logout.
- Refresh-token rotation history, device lists, or multi-device management UI.
- Changing domain or skill names/codes.

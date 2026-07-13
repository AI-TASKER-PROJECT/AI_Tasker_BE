# 0031 Admin Dashboard Aggregate APIs

Date: 2026-07-14

## Status

Accepted

## Context

The admin dashboard needs cards and charts for revenue, contracts, users,
finance, disputes, memberships, and marketplace activity. Existing APIs expose
list/detail data, but forcing the frontend to group large datasets is slow and
risks inconsistent business definitions.

## Decision

Add admin-only aggregate endpoints under `/api/v1/admin/dashboard/*`. Keep
existing list/detail APIs unchanged for drill-down management screens.

## Alternatives Considered

1. Reuse only list APIs and group on the frontend.
2. Replace existing list/detail APIs with dashboard APIs.

## Consequences

Positive:

- Frontend receives chart-ready data.
- Backend owns the revenue/status definitions.
- Existing dashboard table/drill-down APIs remain stable.

Tradeoffs:

- More API surface and docs to maintain.
- Aggregate definitions need tests to avoid silent chart drift.

## Follow-Up

- Add database indexes later if production dashboard queries become slow.

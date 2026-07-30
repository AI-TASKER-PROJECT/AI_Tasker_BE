# Coherent Demo Data Rebuild

## Purpose

Replace the accumulated demo data from historical Flyway seeds with one
deterministic, internally consistent dataset. The rebuilt data is intended for
local development, Swagger demonstrations, dashboards, and repeatable tests.

This work changes product seed data only. It does not change Harness flow,
Harness logic, or the application API contract.

## Current-State Audit

The database at Flyway version 66 contains data from several generations of
seeds (`V15`, `V39`, `V41`, `V44`, and `V57`). The measured state before the
rebuild is:

| Item | Current | Target |
| --- | ---: | ---: |
| Accounts | 74 | 31 |
| Admin accounts | 1 | 1 |
| Business accounts | 11 | 10 |
| Expert accounts | 51 | 10 |
| Staff accounts | 11 | 10 |
| Domains | 21 | 30 |
| Skills | 20 | 30 |
| Technologies | 12 | 30 |

The old dataset also mixes generic English names, copied recommendation data,
legacy SoW shapes, unrelated wallet balances, and incomplete audit/notification
histories.

## Replacement Boundary

The replacement migration truncates account-owned and catalog-owned product
data through PostgreSQL foreign-key cascading, then rebuilds it. It preserves:

- `roles`;
- `membership_packages`;
- active `system_settings`;
- `knowledge_chunks`;
- Flyway history;
- the exact email and password hashes of the four internal accounts.

The internal accounts are:

- `business@aitasker.local`;
- `expert@aitasker.local`;
- `admin@aitasker.local`;
- `staff@aitasker.local`.

All other accounts use a role-identifying, person- or company-specific email and
the shared demo password `12345678` (stored as the existing BCrypt hash).

## Deterministic Identity Map

Account ids are stable:

- 1: internal Business;
- 2: internal Expert;
- 3: internal Admin;
- 4: internal profile-review Staff;
- 5-13: nine additional Business accounts;
- 14-22: nine additional Expert accounts;
- 23-31: nine additional Staff accounts.

Business, Expert, portfolio, and Staff ids use compact ranges 1-10. Jobs,
milestones, proposals, contracts, and event records also use explicit ids so
URLs, JSON metadata, audit rows, and test assertions remain readable.

## Catalog Design

The dataset contains exactly 90 catalog rows:

- 30 domains;
- 30 skills;
- 30 technologies.

Domain codes cover general AI capabilities and multiple industries rather than
copying one AITASKER project. `PROFILE_REVIEW` is included as the thirtieth,
internal-only domain. Its English name is `Profile Review`; only the internal
Staff account is mapped to it, and it is never assigned to a marketplace job.

## Profile And Portfolio Rules

- Every Business has one complete Business profile.
- Every Expert has one complete Expert profile and one complete portfolio.
- Every Staff account has one Staff record and explicit domain/skill mappings.
- Person names are Vietnamese and include diacritics.
- Synthetic identifiers and URLs are visibly marked as demo data to avoid
  impersonating real people or companies.
- Portfolio catalog ids reference the rebuilt catalogs exactly and support the
  recommendation service's numeric-token matching rules.

## Marketplace Dataset

The compact marketplace fixture covers the main lifecycle states without giving
every account an artificial job or contract:

- 10 jobs across eight Business accounts;
- two `DRAFT`, three `OPEN`, three `IN_PROGRESS`, and two `CLOSED` jobs;
- every job has one valid SoW row, three milestones, milestone-owned acceptance
  criteria, catalog mappings, duration, and an exact budget allocation;
- proposals use the runtime JSON array shape
  `[{"milestoneId": ..., "proposedBudget": ...}]` and each proposal total
  equals its bid;
- contracts are created only from accepted proposals and snapshot the related
  job milestones;
- contract participants, budgets, timelines, signatures, deposit state, job
  state, and milestone state agree with one another.

## Event And Audit Coverage

Seeded workflow events use the same stable machine codes and entity names as
runtime services. Related records include:

- audit logs for job creation/publication, proposal submission/acceptance,
  contract creation/signing/deposit activation/completion, profile approval,
  and finance events;
- Vietnamese notification titles and messages with URLs and JSON metadata that
  point to the actual seeded ids;
- wallet ledger rows with operation keys and operation legs for transparent
  paired transfers;
- legacy milestone transactions only where they are needed to demonstrate a
  completed payout and platform commission.

## Financial Reconciliation

For every account wallet:

```text
current_balance = available_balance + escrow_balance
                + holding_balance + disputed_balance
```

Every wallet transaction records a chronological before/after balance. Paired
operations share an `operation_key` and use the runtime leg names.

The Admin wallet follows `SystemWalletService.syncWallet()`:

```text
total_revenue = successful legacy commission fees
              + posted membership/credit purchaser debits

available_balance = total_revenue
escrow_balance    = successful legacy deposits - payouts - refunds
                  + posted wallet escrow holds - releases - debits
current_balance   = available_balance + escrow_balance
```

The migration ends with SQL assertions that abort Flyway if account/catalog
counts, profiles, job detail completeness, proposal budgets, contract linkage,
or wallet equations do not match.

## Implementation Phases

1. Preserve internal credentials and clear old account/catalog-owned data.
2. Rebuild the 90 catalog rows.
3. Rebuild the 31 accounts, profiles, portfolios, Staff mappings, quotas, and
   empty wallets.
4. Insert jobs, SoWs, milestones, acceptance criteria, and catalog mappings.
5. Insert proposals, contracts, contract snapshots, finance rows, audit logs,
   and notifications.
6. Reconcile wallets, reset sequences, and run migration assertions.
7. Validate Flyway on a clean database and an existing version-66 database,
   run focused/full backend tests, and verify dashboard queries.
8. Refresh Swagger-facing and DOCX demonstration documentation without
   changing the public API shape.

## Validation Contract

Required proof:

- clean Flyway migration through the new version;
- upgrade from the existing version-66 database;
- exactly 31 accounts with role distribution 1/10/10/10;
- exactly 30 rows in each catalog;
- complete Business/Expert/portfolio/Staff coverage;
- no orphaned foreign keys;
- every job has SoW, milestones, acceptance criteria, and catalog mappings;
- proposal milestone JSON parses and sums to the bid;
- every contract traces to its accepted proposal and matching job/participants;
- wallet balances and Admin revenue reconcile with ledger-source queries;
- backend tests and JPA schema validation pass;
- no frontend hard-coded data is introduced;
- no Harness file is modified.

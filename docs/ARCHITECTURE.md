# Architecture - AITASKER Backend

This document is the current architecture contract for the running backend. It
replaces the generic Harness placeholder and backfills product, runtime,
boundary, and domain rules from the existing docs and source.

Primary sources for this document:

- `README.md`
- `docs/backend-logic-notes.md`
- `docs/swagger-api-overview.md`
- `docs/data-dictionary.md`
- `docs/product/contract-management.md`
- `src/main/resources/application.properties`
- `pom.xml`

When older hand-written guides disagree with current source, generated Swagger,
or Flyway migrations, treat the current source and migrations as the stronger
runtime truth and refresh the stale guide in the same change when practical.

## Product Shape

AITASKER is a backend for an AI project marketplace. The core product connects
business accounts that publish AI jobs with expert accounts that submit
proposals, negotiate contracts, execute milestones, exchange deliverables,
handle payments, and resolve disputes. Admin and staff roles operate profile
approval, account management, settings, audit, review, and dispute workflows.

Current product domains:

- Auth and account lifecycle.
- Email OTP, tax-code check, and optional Google sign-in input.
- Business KYB and expert KYC profiles.
- Expert portfolio and certificate/license file handling.
- Marketplace jobs, proposals, and proposal review.
- Domain, skill, technology, and acceptance-criteria catalogs.
- AI SoW generation and job assistant support.
- Expert candidate recommendation and matching.
- Contract draft, signatures, NDA signatures, milestones, acceptance
  criteria, deliverables, and termination.
- Finance, wallet, payment order, PayOS payment, wallet ledger, membership,
  credit, quota, contract deposit, withdrawal, system wallet, and legacy
  transaction flows.
- Dispute assignment, demo testing, technical report, and resolution.
- Reviews, admin settings, account/staff management, analytics, and audit logs.
- Notifications over REST and WebSocket/STOMP.
- Chatbot/RAG support over local knowledge and optional OpenAI APIs.

## Runtime Stack

The backend is a Java 21 Spring Boot application.

Key runtime choices:

- Spring Boot 4.0.6.
- Spring Web MVC REST controllers.
- Spring Security with JWT authentication.
- Spring Data JPA with PostgreSQL.
- Flyway for database migrations.
- Spring Validation for request DTO validation.
- Springdoc OpenAPI/Swagger UI.
- Spring WebSocket/STOMP for realtime notifications.
- Firebase Admin SDK for storage-backed file flows.
- PayOS Java SDK for payment-order integration.
- Redis client dependency for cache/session/OTP-adjacent capabilities.
- Spring Mail for Gmail SMTP OTP/email flows.
- JJWT for access and refresh token handling.
- Optional OpenAI completions/embeddings for SoW/RAG capabilities.

Local runtime:

- Backend port defaults to `8080`.
- PostgreSQL is expected on `127.0.0.1:5433`.
- Database name defaults to `aitasker_db`.
- JPA `ddl-auto` is `validate`; schema changes must come from Flyway.
- Swagger/OpenAPI is enabled by `SWAGGER_ENABLED=true` by default.
- Healthcheck is `GET /api/health`.

## Source Layout

Current package boundaries:

```text
src/main/java/com/aitasker/be/
  common/        shared response envelope, exceptions, JSON helpers
  config/        runtime provider and framework configuration
  controller/    REST controllers grouped by auth/core/test
  dto/           request and response DTOs by product area
  entity/        JPA entities and enums mapped to Flyway schema
  repository/    Spring Data/JDBC persistence access
  security/      JWT, Spring Security, filters, WebSocket auth
  service/       auth, AI, and core application services
```

The practical layering is:

```text
controller
  -> service
      -> repository
          -> entity / database
```

Controllers receive HTTP requests and wrap responses in `ApiResponse`.
Business rules belong in services, not controllers. Repositories should stay
focused on persistence. Entities map the Flyway-managed database shape.

## Main Services

The following service boundaries are established and should be preserved:

- `service/auth/AuthServiceImpl`: registration, login, BCrypt password hashing,
  JWT issuance.
- `service/auth/EmailOtpService`: email OTP flow.
- `service/auth/TaxCheckService`: business tax-code lookup/check.
- `service/core/AccessService`: current account lookup from JWT and role checks.
- `service/core/ProfileService`: KYB/KYC, portfolio, approval, and approval
  audit behavior, including Firebase-backed business license, expert
  certificate, and expert portfolio file uploads.
- `service/core/MarketplaceService`: job lifecycle, proposal submission, and
  proposal review.
- `service/core/CatalogService`: domain, skill, technology, acceptance criteria,
  and job metadata mappings.
- `service/core/ContractExecutionService`: contract draft/signature,
  NDA, milestones, criteria, deliverables, finance-adjacent legacy
  transactions, disputes, and SLA simulation.
- `service/core/AdminService`: reviews, settings, staff, account management,
  audit logs, and analytics overview.
- `service/core/ExpertRecommendationService` and
  `ExpertCandidateRankingService`: candidate search and ranking.
- `service/core/AiSowGenerationService`, `SowKeywordExtractionService`,
  `RagRetrievalService`, and `ChatbotService`: SoW, keyword extraction, RAG, and
  assistant flows.
- `service/core/FirebaseStorageService`: Firebase-backed upload/view-url flows.
- `service/core/PayOSPaymentService`, `PaymentWalletService`,
  `WalletLedgerService`, and `SystemWalletService`: payment order, wallet,
  membership, credit, quota, deposit, withdrawal, and system-wallet behavior.
- `service/core/NotificationService`: notification records and unread state.

## API Surface

Swagger/OpenAPI is the active REST contract. The generated API is available at:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

Controller groups currently include:

- `admin-controller`
- `auth-controller`
- `catalog-controller`
- `chatbot-controller`
- `contract-execution-controller`
- `email-otp-controller`
- `health-controller`
- `marketplace-controller`
- `notification-controller`
- `payos-payment-controller`
- `membership-controller`
- `credit-controller`
- `user-quota-controller`
- `withdrawal-controller`
- `profile-controller`
- `SoW Generation`
- `tax-check-controller`
- `test-controller`
- `wallet-controller`

REST endpoints use a common JSON envelope through `ApiResponse`. Protected
endpoints depend on JWT and service-level role/ownership checks. WebSocket/STOMP
does not appear in Swagger and must be verified separately.

Important runtime notes:

- Google auth endpoints exist under `/api/auth/google/login` and
  `/api/auth/google/register`.
- Expert candidate and recommendation endpoints exist under `/api/jobs/...`.
- PayOS endpoints exist under `/api/payments/payos/...`.
- Contract activation is now signature-driven through
  `/api/v1/contracts/{contractId}/sign`; older `/activate` references in legacy
  guides should not be reintroduced.
- Contract activation moves the job to `IN_PROGRESS`; completion after all
  milestones are completed moves the job to `CLOSED`.
- The legacy `invoices` table was removed by `V13`; older invoice endpoint
  references are stale unless a new migration and controller intentionally bring
  invoice behavior back.

When adding or changing public APIs:

- Update Swagger-facing controller/DTO annotations when needed.
- Update `docs/swagger-api-overview.md` if the route inventory or behavior
  changes.
- Update product docs or story evidence for the affected workflow.
- Keep role, ownership, and state-transition rules in services.

## Domain Model

Core data concepts include:

- `roles`, `accounts`, `staffs`
- `business_profiles`, `expert_profiles`, `portfolios`
- `jobs`, `proposals`
- `domains`, `skills`, `technologies`
- `job_domains`, `job_skills`, `job_technologies`
- `sows`, `acceptance_criteria`, `milestone_acceptance_criteria`
- `contracts`, `contract_change_requests`, `milestones`,
  `contract_milestones`, `deliverables`
- `transactions`, `payment_orders`, `wallet_transactions`, `system_wallets`,
  `membership_packages`, `membership_purchases`, `user_quotas`,
  `quota_usage_logs`, `contract_deposits`, `withdrawal_requests`
- `reviews`, `disputes`, `audit_logs`, `system_settings`
- `notifications`, `expert_recommendations`, `knowledge_chunks`

Database schema is source-controlled through Flyway migrations under
`src/main/resources/db/migration`. Do not edit a migration that may already have
run. Add a new `Vn__description.sql` migration for every schema/data migration.

JPA schema validation is enabled, so entities and migrations must stay aligned.

## Authorization And Ownership Rules

JWT includes the real business role used by RBAC:

- `BUSINESS`
- `EXPERT`
- `ADMIN`
- `STAFF`

Controllers may declare route-level security, but business authorization must
also be enforced in services because most workflows depend on ownership and
state, not only role.

Current rules to preserve:

- Only approved businesses can create jobs or change job status.
- Only approved experts can submit proposals.
- Experts can submit proposals only to jobs in `OPEN` status.
- Public `GET /api/v1/jobs` returns only `OPEN` jobs.
- Public `GET /api/v1/jobs/{jobId}/milestones` is readable for `OPEN` jobs.
- Public `GET /api/v1/profiles/business/{businessId}` is Guest-readable without
  JWT (US-017); it returns the business profile with `fullName` and `404` when
  not found. Sibling routes `/business/me`, `/business/by-job/{jobId}`, and
  `/business` remain protected by their service-layer role/ownership checks.
- Public `GET /api/v1/profiles/business/by-job/{jobId}` is Guest-readable without
  JWT (US-018) only for `OPEN` jobs; the service layer
  `businessProfileByJob(jobId)` still requires `STAFF/ADMIN/BUSINESS` for
  non-`OPEN` jobs. `/business/me` and `/business` remain private.
- Business dashboard behavior that needs draft/open/closed jobs uses
  `GET /api/v1/jobs/my`.
- A proposal can move into contract creation only after it is `Accepted`.
- Contract, milestone, criteria, deliverable, transaction, and dispute list/read
  operations must check participant or operator access.
- Business users can create deposit transactions for their own eligible
  workflow.
- Payout/refund/system finance operations are admin/staff responsibilities.
- Catalog creation/update is admin-only.
- Job catalog assignments can be changed by admin or the owning business.
- Profile approval is staff/admin work and should write audit evidence.
- Profile rejection stores a staff-provided `rejection_reason` on the business
  or expert profile; the reason is required when `status=Rejected` and cleared
  when the profile is approved or resubmitted.

## Marketplace Rules

Marketplace behavior is not just CRUD. Preserve these state rules:

- Business KYB must be `Approved` before creating a job or changing job status.
- Expert KYC must be `Approved` before submitting a proposal.
- Proposal submission is blocked unless the job status is `OPEN`.
- Duplicate proposal by the same expert for the same job is not allowed.
- When a job becomes `OPEN`, `published_at` is set if missing.
- Public job listing must not leak draft, cancelled, closed, or other non-open
  jobs.
- Business-owned job management uses `/api/v1/jobs/my`, not the public list.

## Contract Execution Rules

Contract execution enforces a multi-step agreement model:

- Contracts are created from accepted proposals as `DRAFT`.
- Business signature sets `business_accepted_at`.
- Expert signature sets `expert_accepted_at`.
- Business NDA signature sets `business_nda_signed_at`.
- Expert NDA signature sets `expert_nda_signed_at`.
- Contract becomes `PENDING` after both parties have signed both the
  contract and NDA.
- Business pays a 20% security deposit from wallet available balance before
  work can start.
- When the deposit is held, the contract becomes `ACTIVE` and the job becomes
  `IN_PROGRESS`.
- If any required signature/NDA is missing, the contract remains `DRAFT`.
- The negotiation/change-request lifecycle is disabled and must not reset
  signatures or move contracts into an intermediate negotiation state.
- Expert rejection is allowed only from `DRAFT` or `PENDING`; it cancels the
  contract and moves the job back to `OPEN`.
- Business can create milestones and acceptance criteria only for eligible
  contracts they own.
- Expert can submit deliverables only for their own active contract, after both
  NDA signatures exist.
- Submitting a deliverable moves the milestone into review.
- Business completion of all reviewed milestones moves the contract to
  `COMPLETED` and the job to `CLOSED`.
- Admin deposit refund/resolution keeps completed contracts `COMPLETED` and
  cancelled contracts `CANCELLED` after final deposit handling.
- SLA auto-approve is currently a manual API simulation, not a scheduler.

## Finance And Payment Rules

Finance is partially MVP and partially integrated:

- PayOS payment-order support exists through `PayOSPaymentService` and related
  controller/config classes. PayOS is only used for wallet top-up.
- Wallet and system-wallet services exist.
- Membership package purchase and credit purchase use wallet available balance
  and create wallet/quota ledger records.
- Premium membership entitlement is based only on
  `user_quotas.premium_expired_at`; lower-tier package purchases must not clear
  or shorten that timestamp.
- `GET /api/users/me/quota` is the authoritative source for active package,
  quota, and Premium permission display. Frontend-local values such as
  `aitasker_active_package` are not business truth.
- Business job publishing consumes one job-post credit only when publish to
  `OPEN` succeeds and the job has a saved SoW.
- Expert proposal submission consumes one proposal credit only when the
  proposal save succeeds.
- Active Business Premium entitlement is required to view AI expert
  recommendations.
- Contract security deposit moves Business available balance to escrow.
- Withdrawal requests move available balance to holding; admin approval removes
  holding and admin rejection returns holding to available.
- Legacy transaction endpoints still model deposit, payout, refund, webhook,
  and status updates for contract/milestone flows.
- Legacy invoice storage no longer exists in the active schema.
- Payment status confirmation for PayOS wallet top-up uses active provider
  sync by order code (`/api/payments/payos/{orderCode}/sync`) instead of a
  public PayOS webhook endpoint. Refund/payout behavior remains a sensitive
  external-system boundary.

Current known limitations:

- Full escrow ledger behavior is not production-complete.
- Legacy transaction webhook behavior is still an admin simulation; PayOS
  wallet top-up status is synced from the provider by order code.
- Refund/payout reconciliation needs stronger provider-backed verification.
- Disputes do not yet fully lock funds, snapshot evidence, or enforce penalties.

## AI, RAG, And File Boundaries

AI and file workflows are boundary-heavy and should remain isolated behind
services/config:

- OpenAI configuration is environment-driven.
- SoW generation uses prompt/RAG knowledge under `src/main/resources/knowledge`.
- `knowledge_chunks` supports RAG-style storage.
- File uploads/view URLs are Firebase-backed where enabled.
- Request DTOs should parse and validate user input before service logic.
- Provider payloads, model output, signed URLs, upload paths, webhook payloads,
  and environment variables are untrusted until parsed/validated.

Current MVP limitations:

- AI Job Assistant and matching are still simplified.
- Matching relies on tags, keywords, proposal text, and ranking heuristics; it
  is not yet a full portfolio/skill/domain scoring engine.
- Firebase/file upload coverage is not complete for all deliverable/license
  flows.
- NDA PDF generation/upload is not yet implemented.

## Notification And Realtime

Notifications are available over REST for list, unread count, read-one, and
read-all behavior. WebSocket/STOMP exists for realtime delivery, but it is not a
REST API and does not appear in Swagger. Any change to realtime behavior must
include separate WebSocket verification.

## Boundary Rules

Unknown data must be parsed and validated at system boundaries before it enters
inner service logic.

Boundaries include:

- HTTP request bodies, path variables, params, and query strings.
- JWT/session identity claims.
- Environment variables and provider config.
- Database rows crossing custom JDBC or native query boundaries.
- Provider webhooks and payment callbacks.
- Firebase upload paths, signed URLs, and file metadata.
- OpenAI/model outputs and embeddings.
- WebSocket/STOMP payloads.
- Email OTP and tax-check provider responses.

Target flow:

```text
unknown input
  -> request DTO / parser / validator
  -> service command
  -> domain rule and repository interaction
  -> response DTO
  -> ApiResponse envelope
```

## Observability And Audit

Application logs and audit logs have different jobs:

- Application logs are operational signals.
- Audit logs are product records for sensitive business actions.

Audit should cover profile approval, account/admin changes, settings changes,
contract and dispute decisions, transaction/payment state changes, and other
sensitive operator actions. Audit coverage is not complete yet and should be
expanded as sensitive flows are hardened.

The desired request log shape remains:

- timestamp
- level
- request_id
- user_id when known
- action
- duration_ms
- status_code
- message

## Validation Ladder

Preferred local validation:

```bash
docker compose up -d
.\mvnw.cmd test
```

Useful manual/runtime checks:

- `GET http://localhost:8080/api/health`
- Swagger UI at `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON at `http://localhost:8080/v3/api-docs`
- Postman flows in `POSTMAN_MANUAL_CHECKLIST.md`
- Backend flow guide in `BACKEND_TEST_GUIDE.md`

For documentation-only architecture updates, validate by checking the changed
Markdown and Harness trace. For API or behavior changes, run tests and update
the relevant guides, Swagger overview, and product docs.

## Current Non-Production Areas

The following areas are intentionally not yet production-complete:

- AI Job Assistant and matching quality.
- Provider-backed payment, refund, payout, and escrow ledger.
- Full Firebase/file coverage for all evidence and deliverable flows.
- NDA PDF generation and storage.
- Scheduled SLA auto-approval.
- Dispute fund lock, evidence snapshot, refund, and penalty handling.
- Complete audit coverage for every sensitive operation.
- WebSocket/STOMP test coverage.

Treat work in these areas as higher risk unless the requested change is purely
documentation.

## Architecture Change Rules

- Keep business rules in services, not controllers.
- Keep external providers behind dedicated service/config classes.
- Keep schema changes additive through new Flyway migrations.
- Keep public API changes synchronized with Swagger/OpenAPI docs.
- Keep authorization as role plus ownership plus state transition checks.
- Keep product docs, story packets, and Harness trace current when behavior
  changes.

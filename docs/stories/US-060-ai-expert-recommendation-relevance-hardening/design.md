# Design

## Domain Model

`JobRequirementResolver` produces active skill, domain, and technology IDs.
Existing job mapping tables are authoritative per group; SoW/title/raw
requirements/milestones fill only groups without mappings. Mandatory job skills
are hard gates.

`ExpertMatchScoringService` parses the current portfolio text columns as exact,
delimited numeric IDs. It scores active skill/domain/technology groups and an
optional batch-loaded rating component, then normalizes by active weights.

## Application Flow

1. Resolve requirements.
2. Query at most 200 KYC-approved, account-approved EXPERT portfolios with one
   exact-ID PostgreSQL query.
3. Batch-load ratings and calculate deterministic Top 20 candidates.
4. Build Top 5 recommendations from backend order and evidence.
5. Optionally accept only `{expertId, reason}` from OpenAI.
6. Pin a still-eligible Business-selected Expert and persist at most five rows.

## Interface Contract

Existing routes and response fields remain unchanged. FE has no required code
change. AI cannot alter identity, portfolio, rank, score, or matched evidence.

## Data Model

`V58__harden_expert_recommendation_integrity.sql` audits existing rows and adds
job/expert and job/rank uniqueness, rank/score checks, foreign keys, and a
portfolio foreign-key index. Portfolio storage is not redesigned in Release A.

## UI / Platform Impact

No required FE change. Users receive more consistent results and selection
state survives regeneration when the Expert remains eligible.

## Observability

Focused benchmark fixtures and Harness traces are the release evidence.

## Alternatives Considered

1. Keep broad `LIKE` and post-filter in Java. Rejected due substring collisions
   and repeated queries.
2. Let the LLM rank candidates. Rejected because behavior changes with provider
   availability and evidence cannot be trusted.
3. Normalize portfolio taxonomy now. Deferred to Release B to keep DB impact
   additive and FE-compatible.

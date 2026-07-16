# AI Expert Recommendation Relevance Hardening

## Status

implemented (Release A, 2026-07-13)

## Lane

high-risk (existing matching behavior, recommendation persistence, OpenAI
boundary, database integrity, and Business-to-Expert invitation workflow)

## Problem Statement

The current recommendation flow first extracts a small, hard-coded set of
keywords from the saved SoW, retrieves portfolios by text `LIKE` queries, and
calculates a heuristic score before optionally asking an LLM to select and
reorder the final recommendations.

This flow is suitable for a narrow Chatbot/RAG demo but is not yet a reliable
general Expert-matching contract:

1. BI Dashboard, Computer Vision, and Data Pipeline requirements can produce
   no extracted skill or domain even when matching portfolio data exists.
2. Structured job assignments in `job_skills`, `job_domains`, and
   `job_technologies` are not the primary candidate source.
3. `portfolio.technology_ids` does not contribute to the current score.
4. Missing requirement groups reduce the maximum possible score instead of
   re-normalizing active weights.
5. Candidate retrieval does not enforce Expert KYC and account eligibility.
6. The LLM can currently provide score, portfolio id, matched evidence, and
   rank values that should be owned and validated by the backend.
7. Regeneration deletes saved rows before inserting the new list, which can
   erase `business_selected` state.
8. Existing tests prove parsing and persistence behavior but do not measure
   multi-domain candidate recall or Top-5 ranking quality.

## Product Contract

- Structured job taxonomy is the primary source of matching requirements.
- SoW text extraction supplements a missing taxonomy group; it must not
  override explicit Business-selected job skills, domains, or technologies.
- Only eligible Experts may become recommendation candidates.
- The backend is the source of truth for candidate identity, portfolio
  identity, score, rank, and matched evidence.
- OpenAI may generate a human-readable explanation but must not manufacture or
  overwrite backend matching facts in Release A.
- Matching must continue working when OpenAI is disabled or unavailable.
- Recommendation generation returns at most five saved recommendations.
- When at least five eligible candidates pass the accepted quality gate, the
  API returns five recommendations.
- Regeneration must not silently clear an existing Business selection.
- Recommendation selection remains an invitation to submit a proposal; it does
  not create a proposal or contract.
- Existing recommendation endpoint paths and required response fields remain
  backward compatible for the frontend in Release A.

## Compatibility Contract

Release A must preserve these endpoints:

```text
GET  /api/jobs/{jobPostingId}/expert-candidates
POST /api/jobs/{jobPostingId}/expert-recommendations
GET  /api/jobs/{jobPostingId}/expert-recommendations
POST /api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select
```

The recommendation response continues exposing:

```text
expertId
portfolioId
rankPosition
matchScore
matchedSkills
matchedDomains
reason
businessSelected
```

`matchScore` remains a number from 0 to 100. Its implementation and semantics
become backend-owned and comparable across jobs. `generatedByAi=true` means an
AI explanation was successfully generated; it does not mean the model owns the
score or matching evidence.

Release A does not require frontend code changes. The frontend must continue
supporting a list containing zero to five recommendations.

New fields such as `matchedTechnologies`, `scoreBreakdown`, `scoringVersion`,
or `stale` require separate API documentation and frontend acceptance before
they become required UI behavior.

## Target Design

### 1. Requirement Resolution

Introduce a dedicated `JobRequirementResolver` that produces one normalized
internal requirement object for a job.

Resolution order:

1. Load explicit assignments from `job_skills`, `job_domains`, and
   `job_technologies`.
2. Read job title, raw requirements, saved SoW, and milestone names and
   descriptions.
3. For any taxonomy group with no explicit assignments, match the normalized
   text against active catalog codes, names, descriptions, and approved alias
   rules.
4. De-duplicate requirements by catalog id.
5. Preserve `job_skills.is_mandatory` and the configured experience requirement
   when those values exist.

Hard-coded aliases may remain for Vietnamese phrases and common abbreviations,
but the supported domain and skill universe must come from the active catalog,
not from a fixed Java list.

### 2. Candidate Eligibility

Candidate retrieval must enforce all of the following before scoring:

- the portfolio exists and belongs to the returned Expert;
- `expert_profiles.kyc_status` is `Approved`;
- the related account status is `Approved`;
- the account is not locked or rejected;
- required portfolio taxonomy values can be parsed and validated;
- every mandatory job skill is satisfied when mandatory skills are configured.

Eligibility must be implemented as a set-based repository query or a bounded
query pipeline. It must not issue one broad database query for every keyword
alias.

### 3. Candidate Retrieval

Release A uses existing job mapping tables and exact numeric catalog ids stored
in the current portfolio fields.

Candidate retrieval uses the union of:

1. exact skill matches;
2. exact domain matches;
3. exact technology matches;
4. catalog-resolved text matches only for taxonomy groups that were not
   explicitly assigned.

Substring matching must not allow catalog id `1` to match `10`, `11`, or other
unrelated ids. Candidate identity is de-duplicated by Expert id, with one
portfolio per Expert under the current schema.

The first release may retrieve a bounded candidate pool larger than the final
Top 5, but the bound and ordering must be deterministic.

### 4. Deterministic Scoring

Create an `ExpertMatchScoringService` separate from retrieval and persistence.

Initial calibration weights:

| Component | Weight |
|---|---:|
| Required skill coverage | 35% |
| Domain coverage | 30% |
| Technology coverage | 25% |
| Rating or accepted trust signal | 10% |

These weights are an initial hypothesis, not proof of quality. They may be
changed only with benchmark evidence.

Scoring rules:

- Re-normalize across active components when a job legitimately lacks one
  component. A skill-only job must not be capped at 80 and a domain-only job
  must not be capped at 50.
- Mandatory skill failure excludes the candidate instead of being hidden by
  high rating or description scores.
- Certificate and self-description text are supporting evidence only. They
  must not outweigh structured taxonomy.
- Every score component is clamped and the final score is rounded to two
  decimals between 0 and 100.
- Experience remains a deterministic tie-breaker rather than a score
  component. Remaining ties use stable Expert or portfolio ids.
- The minimum recommendation score must be calibrated from the benchmark and
  must not be introduced as an unexplained constant.

### 5. AI Boundary

Release A treats the LLM as an explanation service, not a scoring authority.

The model input may include:

- the bounded SoW summary;
- backend-resolved requirements;
- the backend-ranked candidate facts;
- the backend score and matched evidence.

The accepted model output contains only:

```json
{
  "recommendations": [
    {
      "expertId": 1,
      "reason": "Explanation grounded in the provided backend facts."
    }
  ]
}
```

The backend must:

- reject unknown or duplicate Expert ids;
- ignore any model-provided portfolio id, score, rank, or matched evidence;
- use the backend candidate's portfolio id and score;
- sanitize and length-limit the explanation;
- fall back to a deterministic Vietnamese explanation when OpenAI fails;
- backfill missing valid model items from the backend ranking;
- return the same candidate identities, score values, and rank order with
  OpenAI enabled or disabled in Release A.

Provider failures must be logged with a safe error category. API keys, full
provider payloads, and sensitive portfolio data must not be logged.

### 6. Recommendation Persistence And Selection

`expert_recommendations` remains a snapshot table for the saved Top 5.

Generation must audit existing selected rows before replacing or upserting the
new snapshot. It must not blindly delete `business_selected=true` state.

Recommended Release A behavior:

- selected Experts remain pinned in the saved list during regeneration;
- the remaining positions are filled from the new deterministic ranking;
- the total saved and returned list remains at most five;
- an already selected Expert is not notified a second time;
- rank positions are recalculated to a unique sequence from 1 to list size;
- recommendation rows are unique by `(job_posting_id, expert_id)`.

The existing open product decision about whether a Business may select one or
multiple recommended Experts is not silently resolved by this initiative. The
current behavior remains until a separate accepted decision changes it.

### 7. Release A Database Scope

Release A does not require a portfolio schema redesign.

Add one new Flyway migration using the next available version. Never edit V24,
V37, V41, or V44 after they may have run.

Before adding constraints, audit:

```text
duplicate (job_posting_id, expert_id) rows
orphan job_posting_id values
orphan expert_id values
portfolio_id values that do not belong to expert_id
rank values outside the expected positive range
match scores outside 0..100
```

Recommended additive constraints:

```text
UNIQUE (job_posting_id, expert_id)
CHECK (rank_position > 0)
CHECK (match_score IS NULL OR (match_score >= 0 AND match_score <= 100))
FOREIGN KEY job_posting_id -> jobs(job_id)
FOREIGN KEY expert_id      -> expert_profiles(expert_id)
FOREIGN KEY portfolio_id   -> portfolios(portfolio_id)
```

Delete behavior must follow the accepted recommendation-integrity plan:

- job: cascade recommendation snapshots;
- Expert: restrict while referenced;
- portfolio: set recommendation portfolio id to null when deletion is an
  accepted product operation.

If existing data prevents an integrity constraint, stop and produce an audit
report. Do not delete or rewrite recommendation rows silently inside Flyway.

The missing local V44 demo data is a test-environment concern, not a reason to
redesign production tables. Prefer deterministic integration fixtures. Add an
idempotent repair migration only if the team explicitly keeps the shared demo
accounts as a supported environment contract.

### 8. Release B Optional Normalization

After Release A passes its benchmark, a separate story may normalize portfolio
taxonomy with:

```text
portfolio_skills(portfolio_id, skill_id)
portfolio_domains(portfolio_id, domain_id)
portfolio_technologies(portfolio_id, technology_id)
```

Release B requires additive tables, foreign keys, backfill of valid numeric ids,
dual-read/dual-write compatibility, and a separate removal decision for the old
text fields. It must not block the Release A correctness fix.

Semantic retrieval or embedding-based reranking is also a Release B capability.
It may influence production rank only after labeled benchmark results prove an
improvement over the deterministic baseline.

## Affected Code Paths

At minimum, implementation must inspect and update:

- `SowKeywordExtractionService`;
- `ExpertCandidateRankingService`;
- `ExpertRecommendationService`;
- `PortfolioRepository`;
- job skill, domain, and technology repositories;
- Expert profile and account eligibility queries;
- review/rating lookup used by scoring;
- recommendation entity/repository and migration constraints;
- recommendation generation, saved-read, and select endpoint tests;
- Swagger/OpenAPI descriptions when semantics change.

Recommended new service boundaries:

```text
JobRequirementResolver
ExpertCandidateRetrievalService
ExpertMatchScoringService
ExpertRecommendationService
```

Controllers remain thin and must not calculate score or eligibility.

## Quality Benchmark

Create deterministic labeled fixtures for at least these six groups:

1. API Testing;
2. BI Dashboard;
3. Computer Vision;
4. Data Pipeline;
5. Generic AI;
6. Chatbot/RAG.

Each group must include:

- at least five eligible relevant Experts;
- near-match Experts that should rank below the relevant group;
- unrelated Experts;
- one rejected KYC profile;
- one locked account;
- one portfolio containing misleading keyword stuffing;
- one candidate with matching technology but missing a mandatory skill.

Required benchmark outputs:

```text
candidate Recall@20
recommendation Precision@5
recommendation NDCG@5
eligible-candidate violation count
score/rank drift with OpenAI on versus off
```

Minimum fixture targets for Release A:

- Recall@20 = 1.00 for labeled eligible Experts;
- Precision@5 >= 0.80 for each supported group;
- NDCG@5 >= 0.80 for each supported group;
- zero rejected or locked Experts returned;
- zero identity, portfolio, or matched-evidence values accepted from invalid
  model output;
- zero score or rank drift when only the AI explanation mode changes.

Fixture metrics are regression evidence, not a claim of real-world production
quality. Production thresholds require anonymized feedback and accepted
proposal outcomes.

## Acceptance Criteria

| # | Criterion | Required proof |
|---|---|---|
| AC1 | All six supported demo groups resolve structured or catalog-backed requirements. | Resolver unit tests |
| AC2 | BI Dashboard, Computer Vision, and Data Pipeline jobs return relevant candidates when fixtures exist. | Candidate ranking tests |
| AC3 | Explicit job taxonomy takes precedence over conflicting free-text keywords. | Resolver conflict tests |
| AC4 | Skill, domain, and technology ids are matched exactly without substring collisions. | Repository/integration tests |
| AC5 | Rejected KYC and non-Approved accounts never enter the candidate pool. | Eligibility integration tests |
| AC6 | Missing requirement components re-normalize the score and do not create artificial score caps. | Scoring unit tests |
| AC7 | Mandatory skill failure excludes a candidate regardless of experience or keyword text. | Scoring unit tests |
| AC8 | Backend facts remain identical with OpenAI enabled, disabled, malformed, or unavailable. | Recommendation service tests |
| AC9 | Invalid AI expert ids are rejected and incomplete AI output is backfilled from backend rank. | AI normalization tests |
| AC10 | AI cannot override portfolio id, score, rank, matched skills, or matched domains. | Malicious-output tests |
| AC11 | Generation persists at most five unique rows with sequential ranks. | Service + PostgreSQL test |
| AC12 | Regeneration preserves selected Experts and does not send duplicate notifications. | Selection regression tests |
| AC13 | Recommendation integrity migration rejects duplicates, invalid ranks, invalid scores, and orphan references. | Flyway/PostgreSQL tests |
| AC14 | Existing endpoint paths and required response fields remain compatible with the frontend. | OpenAPI contract diff |
| AC15 | The six-group benchmark reaches the required Recall@20, Precision@5, and NDCG@5 targets. | Benchmark report |
| AC16 | Focused tests, full Maven suite, fresh-database Flyway validation, story verification, and detailed Harness trace pass. | Release proof |

## Execution Plan

| Step | Area | Action |
|---|---|---|
| 1 | Harness | Create a high-risk story and map AC1-AC16 to executable proof. |
| 2 | Contract | Add the recommendation product doc and scoring-source decision record. |
| 3 | Benchmark | Add six-domain positive, negative, eligibility, and adversarial fixtures before changing scoring. |
| 4 | Requirements | Implement structured taxonomy resolution with catalog-backed text fallback. |
| 5 | Eligibility | Add set-based Approved Expert/account candidate filtering. |
| 6 | Retrieval | Replace per-alias broad `LIKE` retrieval with exact skill/domain/technology matching. |
| 7 | Scoring | Extract deterministic active-weight scoring and mandatory-skill gates. |
| 8 | AI boundary | Restrict model output to Expert id and grounded explanation; add fallback and backfill. |
| 9 | Persistence | Preserve selected rows, enforce unique ranks and recommendation identity, and prevent repeat notifications. |
| 10 | Migration | Audit existing rows and add recommendation integrity constraints through a new Flyway migration. |
| 11 | Integration | Run focused service tests and PostgreSQL-backed migration/repository tests. |
| 12 | API proof | Run recommendation generate/read/select flow and verify the backward-compatible OpenAPI contract. |
| 13 | Quality proof | Produce Recall@20, Precision@5, NDCG@5, eligibility, and AI-drift results. |
| 14 | Release | Run the full suite on a fresh database, story verification, diff checks, and a detailed high-risk Harness trace. |

## Validation Commands

Expected focused command:

```powershell
.\mvnw.cmd "-Dtest=SowKeywordExtractionServiceTest,ExpertCandidateRankingServiceTest,ExpertRecommendationServiceTest,ExpertRecommendationIntegrationTest" test
```

Expected full validation:

```powershell
docker compose up -d
.\mvnw.cmd test
```

Because long-lived local databases may have Flyway history drift, migration and
OpenAPI proof must also run on a fresh temporary PostgreSQL database. Do not
repair or delete the user's working database as part of automated validation.

Manual API proof:

```text
1. Login as an approved Premium Business.
2. Create or select a job with saved SoW and explicit taxonomy.
3. GET expert candidates and inspect resolved requirements and backend scores.
4. POST recommendation generation.
5. GET saved recommendations and compare ids, scores, rank, and evidence.
6. Select one recommendation.
7. Regenerate and verify selection plus notification idempotency.
8. Repeat with OpenAI disabled and compare backend facts.
```

## Non-Goals

- Do not create proposals or contracts automatically from a recommendation.
- Do not change Premium access, job ownership, or Business authorization rules.
- Do not require frontend changes in Release A.
- Do not add embedding or vector ranking before the deterministic benchmark is
  accepted.
- Do not normalize portfolio taxonomy tables in the same Release A migration.
- Do not let certificate or self-description keyword stuffing dominate rank.
- Do not modify historical Flyway migrations.
- Do not delete recommendation or portfolio data to make new constraints pass.
- Do not resolve the separate one-versus-many Expert selection policy without
  an explicit product decision.

## Stop Conditions

Stop implementation and request a decision if:

- explicit job taxonomy conflicts with the accepted product meaning of the SoW;
- the team wants the LLM to own or materially change final scores or ranks;
- preserving a selected Expert during regeneration conflicts with the desired
  one-versus-many invitation policy;
- existing duplicate or orphan recommendation data prevents additive integrity
  constraints;
- Release A requires changing endpoint paths or removing response fields;
- benchmark thresholds would need to be weakened to mark the story complete;
- normalization of production portfolio data cannot distinguish valid ids from
  free-text legacy values;
- a migration would delete or rewrite user data without a reviewed audit and
  rollback plan.

## Completion Gate

This initiative is not complete when only the existing unit tests pass. Release
A completion requires all AC1-AC16 evidence, a fresh PostgreSQL migration run,
the six-domain benchmark report, OpenAI on/off determinism proof, selection
regression proof, a backward-compatible OpenAPI diff, the full Maven suite,
story verification, updated product/API documentation, and a detailed
high-risk Harness trace.

# Validation

## Proof Strategy

Unit tests prove source precedence, exact-ID behavior, mandatory gates, active
weight normalization, six benchmark domains, AI authority isolation, fallback,
and selection preservation. A fresh PostgreSQL database must prove all Flyway
migrations and repository SQL.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Resolver, six keyword fixtures, exact IDs, mandatory skills, scoring, AI/fallback invariants, selection preservation |
| Integration | Fresh PostgreSQL Flyway V1-V58 and native candidate query |
| E2E | Existing API contract only; no FE change |
| Platform | Full Maven suite |
| Performance | One bounded candidate query plus one batch rating query; no per-token/per-candidate query |
| Logs/Audit | Harness completed trace |

## Fixtures

API Testing, BI Dashboard, Computer Vision, Data Pipeline, generic AI, and
Chatbot/RAG positive fixtures plus numeric substring and mandatory-skill
negative fixtures.

## Commands

```text
command: .\mvnw.cmd "-Dtest=SowKeywordExtractionServiceTest,JobRequirementResolverTest,ExpertMatchScoringServiceTest,ExpertCandidateRankingServiceTest,ExpertRecommendationQualityBenchmarkTest,ExpertRecommendationServiceTest" test
result: PASS
notes: 27 tests, 0 failures, 0 errors, 0 skipped after calibrating active weights to skill 35%, domain 30%, technology 25%, and rating 10%. Six benchmark groups remain covered.

command: TEST_DB_URL=<fresh-v58-db> .\mvnw.cmd "-Dtest=ExpertCandidateRepositoryIntegrationTest" test
result: PASS
notes: 2 tests prove exact numeric tokens plus Rejected account/KYC exclusion; Flyway validated 58 migrations.

command: TEST_DB_URL=<fresh-v58-db> .\mvnw.cmd test
result: PASS
notes: 339 tests, 0 failures, 0 errors, 0 skipped on a fresh PostgreSQL database.

command: apply V1-V58 in numeric order to an empty PostgreSQL database and inspect expert_recommendations constraints
result: PASS
notes: all 58 SQL migrations applied; PK, 2 unique constraints, 2 checks, and 3 foreign keys present.

command: git diff --check
result: PASS
notes: no whitespace errors; only expected LF/CRLF normalization warnings.
```

## Acceptance Evidence

- Structured-first resolver, exact-ID query, eligibility gates, deterministic
  scoring, explanation-only AI, selection preservation, and V58 constraints are
  implemented.
- API paths and response DTO fields are unchanged; no FE change is required.
- Focused tests, repository integration, six-group quality benchmark, full
  suite, and fresh-database migration validation passed.
- Harness story verification passed and detailed trace `#106` meets the
  high-risk `3/3` requirement.

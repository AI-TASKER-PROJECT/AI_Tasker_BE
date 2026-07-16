# Expert Recommendation

## Product Contract

- Existing candidate/recommendation/select routes and response fields remain
  backward compatible.
- Structured `job_skills`, `job_domains`, and `job_technologies` are primary per
  requirement group. Saved job/SoW/milestone text fills only missing groups.
- Candidates must be EXPERT accounts with `Approved` account status and
  `Approved` Expert KYC status.
- Portfolio taxonomy values are matched only as exact numeric catalog IDs.
  Broad substring `LIKE` matching is not part of Release A.
- Mandatory skills are eligibility gates. Skill, domain, technology, and rating
  scoring is deterministic and normalized across active components.
- Active scoring weights are skill 35%, domain 30%, technology 25%, and rating
  10%. Experience is a deterministic tie-breaker rather than a score component.
- Backend owns expert/portfolio identity, score, rank, and matched evidence.
  OpenAI may add only sanitized reason text and is optional.
- Results contain at most five unique Experts. Regeneration pins and preserves a
  previously selected Expert only while that Expert remains eligible.

## Compatibility

No FE code change is required. The response remains:
`expertId`, `portfolioId`, `rankPosition`, `matchScore`, `matchedSkills`,
`matchedDomains`, `reason`, and `businessSelected`.

## Release Boundaries

Release A keeps `portfolios.skill_ids`, `domain_ids`, and `technology_ids` as
text storage but reads numeric tokens exactly. Release B may add normalized join
tables and backfill data; embeddings require a separate benchmarked decision.

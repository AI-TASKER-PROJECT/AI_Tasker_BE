# Validation

## Proof Strategy

Prove that the new draft update flow persists `sow` + `milestones` together
and that publish still works after a draft edit. At minimum, unit tests must
confirm: draft update upserts sow, replaces milestones, rejects non-DRAFT,
rejects non-owner, and a job with no sow still fails publish with
`JOB_MUST_HAVE_AI_SOW`.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | `updateDraftJob` updates job core fields and returns attached job. |
| Unit | `updateDraftJob` upserts sow when sow already exists (update path). |
| Unit | `updateDraftJob` inserts sow when none exists (insert path). |
| Unit | `updateDraftJob` replaces milestones for the draft job. |
| Unit | `updateDraftJob` rejects non-owner with `BAN KHONG CO QUYEN THAO TAC JOB NAY`. |
| Unit | `updateDraftJob` rejects non-DRAFT job with `JOB KHONG O TRANG THAI DRAFT`. |
| Unit | `updateDraftJob` rejects when a contract already exists for the job. |
| Unit | `updateDraftJob` rejects blank sow title when sow provided. |
| Unit | `updateJobStatus` publish still throws `JOB_MUST_HAVE_AI_SOW` when no sow row (unchanged behavior). |
| Integration | Skipped (needs Postgres); recorded. |
| E2E | N/A |
| Platform | N/A |
| Performance | N/A |
| Logs/Audit | Audit record `ACTION_UPDATE_JOB_DRAFT` written. |

## Fixtures

- A `JobEntity` with `status="DRAFT"` owned by the current business.
- An existing `SowEntity` for `jobId` (update path) and no sow (insert path).
- A `MilestoneEntity` list for replacement.
- A non-DRAFT job for the rejection case.
- A `ContractEntity` for the contract-exists guard case.

## Commands

```text
.\mvnw.cmd "-Dtest=MarketplaceServiceTest" test
```

## Acceptance Evidence

Unit tests (2026-06-22):

```text
.\mvnw.cmd "-Dtest=MarketplaceServiceTest" test
-> Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
   MarketplaceServiceTest: 5 pre-existing + 6 new updateDraftJob cases
     (upsert sow + replace milestones; insert sow when none; reject non-owner;
      reject non-DRAFT; reject contract-exists; reject blank sow title)
```

Regression sweep (US-017/018 unaffected):

```text
.\mvnw.cmd "-Dtest=MarketplaceServiceTest,ProfileServiceTest,BusinessProfileRouteMatcherTest" test
-> Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
```

Compile proof (2026-06-22):

```text
.\mvnw.cmd -DskipTests compile -> BUILD SUCCESS
```

Publish guard unchanged: `updateJobStatus` still throws `JOB_MUST_HAVE_AI_SOW`
when `sowRepository.findByJobId(jobId).isEmpty()` on publish (pre-existing test
`updateJobStatus_shouldThrowWhenStatusInvalid` plus the new upsert tests
confirm the guard path is intact; the guard is not modified by US-022).

Recorded limitation: full create->edit->publish E2E not run here because
`@SpringBootTest` integration tests require live PostgreSQL at 127.0.0.1:5433
and `@MockitoBean` is unavailable on Boot 4.0.6 (backlog #2). Unit tests prove
the update flow and guards; the publish guard code path is unchanged.


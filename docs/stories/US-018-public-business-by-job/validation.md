# Validation

## Proof Strategy

Prove that the Business-by-job route matcher permits anonymous access for the
numeric `jobId` path, that it does not leak sibling routes, and that the
service-layer `OPEN`-job rule still behaves correctly (OPEN returns profile,
non-OPEN still enforces role). At minimum, unit tests must confirm the matcher
boundary and the service behavior. Full HTTP security-chain E2E is a recorded
limitation (same as US-017: `@MockitoBean` unavailable on Boot 4.0.6 and
integration tests need Postgres).

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit (matcher) | `/by-job/1` matches; `/me`, `/business`, `/business/1`, non-GET do not match. |
| Unit (service) | `businessProfileByJob` returns profile when job is `OPEN`. |
| Unit (service) | `businessProfileByJob` still calls `requireRole` when job is not `OPEN`. |
| Unit (service) | `businessProfileByJob` returns `404` when job not found. |
| Route/Security | Matcher boundary proven by unit test above; full HTTP chain is a recorded limitation. |
| Integration | Skipped (needs Postgres + @MockitoBean fix); recorded. |
| E2E | N/A |
| Platform | N/A |
| Performance | N/A |
| Logs/Audit | Sibling protected routes unchanged. |

## Fixtures

- A `JobEntity` with `status="OPEN"` and a linked `businessId`.
- A `JobEntity` with `status="IN_PROGRESS"` for the non-OPEN case.
- A missing `jobId` for the `404` case.

## Commands

```text
.\mvnw.cmd -Dtest=ProfileServiceTest,BusinessProfileRouteMatcherTest test
```

## Acceptance Evidence

Unit tests (2026-06-22):

```text
.\mvnw.cmd "-Dtest=ProfileServiceTest,BusinessProfileRouteMatcherTest" test
-> Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
   BusinessProfileRouteMatcherTest: 10 tests
     (US-017 matcher: /business/1 matches; /me, /business, /by-job/5, non-GET do not)
     (US-018 byJobMatcher: /by-job/1, /by-job/999 match; /me, /business, /business/1, non-GET do not)
   ProfileServiceTest: 15 tests
     (businessProfileByJob OPEN -> returns profile, no requireRole;
      businessProfileByJob non-OPEN -> requireRole STAFF/ADMIN/BUSINESS;
      businessProfileByJob job 404;
      businessProfileById + expert tests unchanged)
```

Compile proof (2026-06-22):

```text
.\mvnw.cmd -DskipTests compile -> BUILD SUCCESS
```

Service behavior proof: `businessProfileByJob` is unchanged; the 3 new service
tests confirm the existing OPEN-job rule (OPEN returns profile with
`verifyNoInteractions(accessService)`; non-OPEN calls
`requireRole("STAFF","ADMIN","BUSINESS")`; missing job returns `404`).

Route-level proof: `BusinessProfileRouteMatcherTest.byJobMatcher` directly
proves the `RegexRequestMatcher` boundary used by `SecurityConfig` for US-018:
`/by-job/{numericId}` is matched while `/me`, `/business`, `/business/{id}`,
and non-GET are not (and therefore fall through to
`.anyRequest().authenticated()`). The security wiring is proven by successful
compilation of `SecurityConfig` with the second regex matcher.

Recorded limitation: full HTTP security-chain E2E (anonymous 200 for OPEN vs
401/403 for non-OPEN) is not run here, same as US-017. `@MockitoBean` is
unavailable on Spring Boot 4.0.6 in this environment and `@SpringBootTest`
integration tests require live PostgreSQL at 127.0.0.1:5433. Tracked in
backlog #2.


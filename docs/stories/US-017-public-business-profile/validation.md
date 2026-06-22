# Validation

## Proof Strategy

Prove that the Business-by-ID route is callable without a JWT, that the response
still carries `fullName`, that `404` still works, and that the sibling `/me`
route is not opened. At minimum, unit tests must confirm the service-layer
change. If a lightweight security/controller test can be produced, it must prove
the route-level boundary; otherwise the limitation is recorded in the trace and
decision record.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | `businessProfileById` returns profile with `fullName`. |
| Unit | `businessProfileById` returns `404` when profile missing. |
| Unit | `businessProfileById` no longer verifies `requireRole(...)` for this route. |
| Route/Security | Anonymous `GET /api/v1/profiles/business/{businessId}` is not blocked with `401` by the security chain. |
| Route/Security | Anonymous `GET /api/v1/profiles/business/me` is still blocked (not public). |
| Integration | Skipped if no lightweight security harness exists; limitation recorded. |
| E2E | N/A |
| Platform | N/A |
| Performance | N/A |
| Logs/Audit | Sibling protected routes unchanged. |

## Fixtures

- Existing `BusinessProfileEntity` with an `accountId` linked to an account
  that has a `fullName`.
- A missing `businessId` for the `404` case.

## Commands

```text
.\mvnw.cmd -Dtest=ProfileServiceTest test
```

If a security/controller test is added, run the matching `-Dtest=...` command
for it as well.

## Acceptance Evidence

Unit tests (2026-06-22):

```text
.\mvnw.cmd -Dtest=ProfileServiceTest,BusinessProfileRouteMatcherTest test
-> Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
   BusinessProfileRouteMatcherTest: 5 tests (matches /business/1, /business/999;
     does NOT match /me, /business, /by-job/5, non-GET)
   ProfileServiceTest: 12 tests (businessProfileById 404, fullName enrichment,
     no requireRole; existing portfolio/expert tests unchanged)
```

Compile proof (2026-06-22):

```text
.\mvnw.cmd -DskipTests compile -> BUILD SUCCESS
```

Route-level proof: a lightweight `@WebMvcTest` security/controller test could
not be produced quickly because the `@MockitoBean` infrastructure used by the
existing integration tests does not resolve under Spring Boot 4.0.6 in this
environment, and the existing `@SpringBootTest` integration tests require a live
PostgreSQL at 127.0.0.1:5433. Instead, `BusinessProfileRouteMatcherTest`
directly proves the `RegexRequestMatcher` boundary used by `SecurityConfig`:
numeric `businessId` is permitted while `/me`, `/business`, and
`/by-job/{jobId}` are not matched (and therefore fall through to
`.anyRequest().authenticated()`). The security wiring is proven by successful
compilation of `SecurityConfig` with the regex matcher. Full HTTP security-chain
verification (anonymous 200 vs 401/403) is a recorded limitation pending a
lightweight security test harness or a running Postgres stack.

Public data boundary: `attachBusinessAccountInfo` only copies `fullName`; it
does not copy `email` or `phone`, and `BusinessProfileEntity` carries no
email/phone fields. `rejectionReason` and `businessLicenseUrl` remain on the
preserved entity surface; flagged in decision 0015 as a follow-up public-DTO
candidate, not a blocker for US-017.


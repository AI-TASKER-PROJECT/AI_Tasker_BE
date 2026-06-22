# Design

## Domain Model

No entity or schema changes. The authorization boundary moves from a
service-only role gate to a route-level anonymous allowance plus the existing
`404` lookup.

## Application Flow

Spring Security permits unauthenticated
`GET /api/v1/profiles/business/{businessId}` requests.
`ProfileService.businessProfileById` no longer calls `requireRole(...)`; it
performs the lookup, attaches `fullName`, and returns `404` when the profile is
missing. Sibling routes keep their service-layer guards:

- `/business/me` -> `currentBusinessProfile()` keeps `requireRole("BUSINESS")`.
- `/business/by-job/{jobId}` -> `businessProfileByJob(jobId)` keeps its
  `OPEN`-job rule plus `requireRole("STAFF","ADMIN","BUSINESS")` for non-OPEN.
- `/business` -> `allBusinessProfiles()` keeps `requireRole("STAFF")`.

## Interface Contract

- Route: `GET /api/v1/profiles/business/{businessId}`
- Public behavior: anonymous callers receive the business profile with
  `fullName`; `404` when not found.
- Protected sibling behavior: `/me`, `/by-job/{jobId}`, and `/business` remain
  authenticated/authorized as before.

### Route Matcher Choice (critical)

The profile controller exposes single-segment siblings under
`/api/v1/profiles/business/`:

```text
GET /api/v1/profiles/business           -> listBusiness
GET /api/v1/profiles/business/{id}      -> getBusinessById   (to open)
GET /api/v1/profiles/business/me        -> myBusiness        (must stay private)
GET /api/v1/profiles/business/by-job/{jobId} -> businessByJob (two segments)
```

An ant matcher `GET /api/v1/profiles/business/*` (single segment) would also
match `/me`, which is exactly the leak the SPEC warns against. The ant matcher
cannot distinguish a numeric `businessId` from the literal `me`.

Decision: use a **regex matcher** that matches only a numeric path segment:

```text
/api/v1/profiles/business/\d+
```

This never matches `/me`, `/by-job/...`, or the bare `/business` route. The
controller's `@PathVariable Integer businessId` already guarantees that
non-numeric segments (other than the dedicated `/me` route) would fail MVC
conversion; the regex matcher makes the security boundary explicit instead of
relying on MVC dispatch order.

## Data Model

No migration.

## Public Data Boundary Analysis

`attachBusinessAccountInfo` only copies `account.getFullName()` onto the
`@Transient fullName` field. It does NOT copy `email`, `phone`, or any other
private account field. The entity `BusinessProfileEntity` does not carry email
or phone either. So the SPEC's "do not expose email/phone/other private account
fields" rule is satisfied.

The current entity response surface includes all `business_profiles` columns:
`businessId`, `accountId`, `taxCode`, `companyName`, `address`,
`businessLicenseUrl`, `kybStatus`, `approvedBy`, `rejectionReason`,
`created_at`, `updated_at`, plus `fullName`.

The SPEC permits preserving the current surface ("Only the current surface of
the route may be preserved") and excludes switching to a new DTO (non-goal).
Two fields warrant a recorded tradeoff rather than silent opening:

- `rejectionReason` - staff feedback written when `kybStatus=Rejected`. Exposing
  it to anonymous visitors is potentially reputation-sensitive.
- `businessLicenseUrl` - storage path/URL for the business license document.

Because the SPEC explicitly excludes a new DTO and permits preserving the
current surface, this story keeps the existing entity shape and records the
tradeoff in `docs/decisions/0015-business-public-profile-guest-access.md` with a
follow-up recommendation to introduce a dedicated public Business profile DTO
in a future story if these fields should be hidden from Guests.

## UI / Platform Impact

Public business profile pages can render without forcing login.

## Observability

No new audit event. The anonymous read reuses the existing audit filter chain
behavior for permitAll routes (the audit filter already runs for authenticated
requests; anonymous permitAll reads follow the same path as other public GET
routes like `/api/v1/jobs`).

## Alternatives Considered

1. Ant matcher `GET /api/v1/profiles/business/*` with `/me` ordered before it as
   `.authenticated()`. Rejected: the SPEC explicitly warns against the broad
   matcher, and any future single-segment sibling would silently leak.
2. Keep the service role gate and only relax Spring Security. Rejected: the
   SPEC requires removing the mandatory logged-in role gate so Guests without a
   JWT can pass the service layer.
3. Introduce a public Business profile DTO that omits `rejectionReason` and
   `businessLicenseUrl`. Rejected as a non-goal of US-017; recorded as a
   follow-up instead.

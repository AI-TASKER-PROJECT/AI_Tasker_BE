# Validation

## Planned Proof

- `.\mvnw.cmd -Dtest=ProfileServiceTest,CatalogServiceTest test`
- `.\mvnw.cmd -DskipTests compile`
- `git diff --check`
- Docker-backed Flyway/full suite if Docker is available.

## Acceptance Criteria

| AC | Behavior | Proof |
| --- | --- | --- |
| AC1 | `PROFILE_REVIEW` exists in V57 and `staff@aitasker.local` has only that domain | Migration review; Flyway if available |
| AC2 | Non-admin/anonymous domain list hides `PROFILE_REVIEW` | `CatalogServiceTest` |
| AC3 | Admin domain list includes `PROFILE_REVIEW` | `CatalogServiceTest` |
| AC4 | Business cannot assign `PROFILE_REVIEW` to a job | `CatalogServiceTest` |
| AC5 | Profile submissions notify only Staff assigned `PROFILE_REVIEW` | `ProfileServiceTest` |
| AC6 | Staff without `PROFILE_REVIEW` cannot approve/list profile review work | `ProfileServiceTest` |

## Results

- `.\mvnw.cmd "-Dtest=ProfileServiceTest,CatalogServiceTest" test`: pass, 33 tests, 0 failures, 0 errors.
- `.\mvnw.cmd -DskipTests compile`: pass.
- `.\scripts\bin\harness-cli.exe story verify US-057`: pass, reran the 33 focused tests.
- `git diff --check`: pass, with CRLF conversion warnings only.
- `docker compose ps`: failed because Docker Desktop daemon pipe `dockerDesktopLinuxEngine` was unavailable, so Docker-backed Flyway/full-suite proof was not collected.

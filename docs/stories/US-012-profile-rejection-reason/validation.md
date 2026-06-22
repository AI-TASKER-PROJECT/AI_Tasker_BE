# Validation

## Commands

```powershell
.\mvnw.cmd -Dtest=ProfileServiceTest test
.\mvnw.cmd -DskipTests compile
```

## Acceptance Evidence

- `ProfileServiceTest` passed 11 tests on 2026-06-22.
- `.\mvnw.cmd -DskipTests compile` passed on 2026-06-22.
- Static OpenAPI path/method list still matches controller source: 126 source
  endpoints, 126 OpenAPI endpoints, 0 missing, 0 stale.
- Full `.\mvnw.cmd test` was attempted on 2026-06-22 but integration/context
  tests could not connect to PostgreSQL at `127.0.0.1:5433`.
- `docker compose ps` could not connect to Docker Desktop
  `dockerDesktopLinuxEngine`, so local database startup was unavailable in this
  run.

# Validation

## Planned Proof

- `.\mvnw.cmd "-Dtest=AdminServiceTest,CatalogServiceTest,PaymentWalletServiceTest" test`
- `.\mvnw.cmd -DskipTests compile`
- `git diff --check`
- Swagger/OpenAPI docs route scan.
- Docker-backed runtime OpenAPI/full suite if Docker is available.

## Acceptance Criteria

| AC | Behavior | Proof |
| --- | --- | --- |
| AC1 | Admin can create/update/deactivate system settings | AdminServiceTest |
| AC2 | Admin can delete/deactivate domain/skill/technology | CatalogServiceTest |
| AC3 | Admin can list/create/update/deactivate membership packages | PaymentWalletServiceTest |
| AC4 | User package list/purchase only uses active packages | Existing PaymentWalletServiceTest regression |
| AC5 | Public docs mention new/changed API routes | Docs route scan |
| AC6 | Compile succeeds | Maven compile |

## Results

| Check | Command / Evidence | Result |
| --- | --- | --- |
| Focused service tests | `.\mvnw.cmd '-Dtest=AdminServiceTest,CatalogServiceTest,PaymentWalletServiceTest' test` | Pass: 40 tests, 0 failures |
| Compile | Covered by Maven focused test compile phase | Pass |
| OpenAPI snapshot parses | `python` JSON parse/count check for `docs/openapi/openapi-v1.json` | Pass: 176 operations |
| Swagger overview coverage | Count route rows in `docs/swagger-api-overview.md` | Pass: 176 rows |
| Swagger/Postman guide coverage | Count endpoint sections in `docs/swagger-api-test-guide.md` and `docs/postman-api-test-guide.md` | Pass: 176 sections each |
| Whitespace scan | `git diff --check` | Pass; only CRLF normalization warnings from Git |
| Docker/runtime OpenAPI | `docker ps` | Blocked: Docker Desktop daemon pipe `dockerDesktopLinuxEngine` is unavailable |

## Notes

- Runtime `/v3/api-docs` could not be fetched because Docker is not running, so `docs/openapi/openapi-v1.json` was updated with a structured JSON parser from the current controller/DTO shape and then parsed/counted locally.

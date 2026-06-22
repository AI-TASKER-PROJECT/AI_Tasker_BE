# US-023 Update Membership Package Prices

## Status

implemented

## Lane

normal

## Product Contract

Membership package prices updated to low test values:

| Package | Old | New |
|---|---|---|
| BUSINESS_STANDARD | 200000 | 200 |
| BUSINESS_PLUS | 500000 | 500 |
| BUSINESS_PREMIUM | 1000000 | 1000 |
| EXPERT_STANDARD | 150000 | 100 |
| EXPERT_PLUS | 400000 | 200 |
| EXPERT_PREMIUM | 800000 | 600 |

Package codes, quota values, badge durations, and role types are unchanged.

## Relevant Product Docs

- `docs/ARCHITECTURE.md` (finance/payment rules)
- `docs/swagger-api-overview.md`
- `docs/swagger-api-test-guide.md`

## Acceptance Criteria

- Business package prices: Standard 200, Plus 500, Premium 1000.
- Expert package prices: Standard 100, Plus 200, Premium 600.
- No unrelated behavior changed.
- No package codes, quotas, or credit prices changed.

## Design Notes

- Migration V30: seed INSERT VALUES updated for fresh installs.
- Migration V38: UPDATE statement for existing databases that already ran V30.
- Service reads prices from `membership_packages.price` at purchase time; no code change needed.
- No tests hardcode package prices; unit tests use builders without explicit price.

## Validation

| Layer | Expected proof |
|---|---|
| Unit | Compile pass; existing PaymentWalletServiceTest 8 tests unaffected. |
| Integration | DB verification query after migration. |

## Evidence

```text
.\mvnw.cmd -DskipTests compile -> BUILD SUCCESS
.\mvnw.cmd "-Dtest=PaymentWalletServiceTest" test -> 8 tests pass
```

DB verification query (after migration):
```sql
select package_code, package_name, role_type, price
from membership_packages order by package_id;
```

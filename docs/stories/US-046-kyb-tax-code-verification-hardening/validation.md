# Validation

## Commands

```
./mvnw -DskipTests compile
./mvnw test -Dtest=ProfileServiceTest
```

## Acceptance Criteria

| # | Criteria | Status |
|---|----------|--------|
| AC1 | Blank tax code -> `TAX CODE KHONG DUOC DE TRONG` | ✅ |
| AC2 | Invalid MST format (not 10/13 digits) -> `MA SO THUE KHONG HOP LE` | ✅ |
| AC3 | MST already used by another account -> `MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC` | ✅ |
| AC4 | MST not found in VietQR -> `NotFoundException` | ✅ |
| AC5 | VietQR API unreachable -> `BadGatewayException` | ✅ |
| AC6 | Valid MST -> `companyName`/`address` from VietQR, `verifiedRepresentative` stored | ✅ |
| AC7 | Resubmit same MST (same account) -> success, no duplicate error | ✅ |
| AC8 | Staff notification and audit log still functional | ✅ |

## Evidence

```text
$ ./mvnw test -Dtest=ProfileServiceTest
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS (2026-07-10)
```

## Trace

- `./mvnw -DskipTests compile` — BUILD SUCCESS on 2026-07-10.
- `./mvnw test -Dtest=ProfileServiceTest` — 26/26 passed on 2026-07-10.
- Static compile proof: 214 main sources + 23 test sources compiled without errors.

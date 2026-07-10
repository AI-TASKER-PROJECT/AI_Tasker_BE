# US-029 Backfill Legacy Source Notes

## Status

implemented

## Lane

normal

## Product Contract

Tat ca file Java backend chua co note theo phong cach cu phai duoc bo sung note
dong nhat voi nhom file da note san, de nguoi doc source co cung mot cach giai
thich file, annotation va ham chinh.

## Relevant Product Docs

- `README.md`
- `docs/ARCHITECTURE.md`

## Acceptance Criteria

- Moi file Java chua co `NOTE FILE:` trong `src/main/java` duoc bo sung note
  theo phong cach cu.
- Ghi chu moi chi giai thich file, annotation, field, method quan trong; khong
  doi logic nghiep vu.
- Source van compile sau khi backfill note.

## Design Notes

- Commands: `rg --files`, `rg -L "NOTE FILE:"`, `.\mvnw.cmd -DskipTests compile`
- Queries: inventory file da note/chua note theo package.
- API: none.
- Tables: none.
- Domain rules: chi backfill source note, khong doi behavior.
- UI surfaces: none.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | N/A |
| Integration | N/A |
| E2E | N/A |
| Platform | `.\mvnw.cmd -DskipTests compile` pass |
| Release | N/A |

## Harness Delta

Neu phat hien phong cach note cu khong du ro de ap dung dong nhat cho file moi,
ghi nhan friction trong trace.

## Evidence

- Backfill note da duoc them cho `81` file Java con thieu trong `src/main/java`.
- Sau khi backfill, `199/199` file Java deu co header `NOTE FILE:`.
- `.\mvnw.cmd -DskipTests compile` pass ngay 2026-06-23.

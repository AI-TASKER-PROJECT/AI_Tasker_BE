# Contract Milestone Sprint Spec

## Scope tổng

Sprint này tập trung xử lý 4 nhóm chỉnh sửa chính:

1. Bổ sung duration cho milestone và contract milestone snapshot.
2. Sửa notification để FE điều hướng được và có metadata rõ ràng.
3. Làm chắc dữ liệu snapshot của contract milestone.
4. Bổ sung test để tránh lỗi regression.

Các story trong scope:

* USER-013: Add duration for milestone and contract milestone snapshot
* USER-014: Improve deliverable notification targetUrl and metadata
* USER-015: Strengthen contract milestone snapshot fields
* USER-016: Add regression tests for contract milestone and notification flow

---

# USER-013 — Add duration for milestone and contract milestone snapshot

## User Story

As a Business user and system user,
I want each milestone to have a clear estimated duration,
so that the frontend can display milestone timeline before contract creation and preserve the agreed duration after the contract is created.

## Problem

Hiện tại milestone chưa có thông tin thời lượng rõ ràng. FE khó hiển thị timeline cho từng milestone khi job còn ở trạng thái draft/open.

Ngoài ra, khi contract được tạo, `contract_milestones` cần lưu snapshot riêng. Nếu chỉ lưu duration ở bảng `milestones`, sau này milestone gốc bị sửa thì hợp đồng cũ có thể bị sai dữ liệu lịch sử.

## Business Rule

Cần thêm duration vào cả 2 bảng:

### `milestones`

Dùng cho trạng thái trước khi tạo contract:

* job draft
* job open
* milestone đang được Business tạo/sửa
* FE hiển thị timeline dự kiến

### `contract_milestones`

Dùng làm snapshot cố định sau khi contract được tạo:

* hợp đồng đã ký/tạo thì duration không đổi theo milestone gốc
* FE render lịch sử hợp đồng dựa trên snapshot
* tránh mất tính nhất quán nếu milestone gốc bị sửa sau này

## Database Changes

Tạo Flyway migration mới. Không chỉnh migration cũ.

Thêm vào bảng `milestones`:

```sql
duration INT;
duration_unit VARCHAR(20);
```

Thêm vào bảng `contract_milestones`:

```sql
duration INT;
duration_unit VARCHAR(20);
```

Enum hợp lệ cho `duration_unit`:

```text
DAY
WEEK
MONTH
```

Khuyến nghị constraint:

```sql
duration IS NULL OR duration > 0
```

```sql
duration_unit IS NULL OR duration_unit IN ('DAY', 'WEEK', 'MONTH')
```

## Backend Changes

### 1. Update enum

Tạo enum nếu chưa có:

```java
public enum DurationUnit {
    DAY,
    WEEK,
    MONTH
}
```

### 2. Update `MilestoneEntity`

Thêm field:

```java
private Integer duration;

@Enumerated(EnumType.STRING)
private DurationUnit durationUnit;
```

### 3. Update `ContractMilestoneEntity`

Thêm field:

```java
private Integer duration;

@Enumerated(EnumType.STRING)
private DurationUnit durationUnit;
```

### 4. Update milestone request DTO

Nếu project đang có DTO tạo/sửa milestone, thêm:

```java
private Integer duration;
private DurationUnit durationUnit;
```

Áp dụng cho các request liên quan:

* create milestone
* update milestone
* create job with milestones nếu có
* AI SoW milestone generation nếu flow đang map milestone từ AI response

### 5. Update milestone response DTO

Các response trả milestone cho FE nên có:

```java
private Integer duration;
private DurationUnit durationUnit;
```

### 6. Update contract snapshot creation

Trong method tạo snapshot contract, ví dụ:

```java
createContractMilestones()
```

Khi copy từ `MilestoneEntity` sang `ContractMilestoneEntity`, cần copy thêm:

```java
contractMilestone.setDuration(milestone.getDuration());
contractMilestone.setDurationUnit(milestone.getDurationUnit());
```

## Validation Rule

* `duration` có thể nullable để không phá dữ liệu cũ.
* Nếu FE gửi duration thì duration phải lớn hơn 0.
* Nếu FE gửi durationUnit thì phải thuộc `DAY`, `WEEK`, `MONTH`.
* Nếu duration có giá trị nhưng durationUnit null, nên reject request.
* Nếu durationUnit có giá trị nhưng duration null, nên reject request.

## Acceptance Criteria

* Business có thể tạo milestone với `duration` và `durationUnit`.
* Business có thể sửa milestone duration trước khi contract được tạo.
* API milestone response trả về duration cho FE.
* Khi contract được tạo, `contract_milestones` copy duration từ milestone gốc.
* Sau khi contract đã tạo, nếu milestone gốc bị sửa duration thì contract milestone snapshot không bị thay đổi.
* Migration không phá dữ liệu cũ.
* Existing tests vẫn pass.

## Implementation Stages

### Stage 1 — Migration

* Tạo migration mới.
* Add columns vào `milestones`.
* Add columns vào `contract_milestones`.
* Add check constraint nếu style migration hiện tại đang dùng constraint.

### Stage 2 — Entity and enum

* Tạo hoặc tái sử dụng `DurationUnit`.
* Update `MilestoneEntity`.
* Update `ContractMilestoneEntity`.

### Stage 3 — DTO mapping

* Update request DTO.
* Update response DTO.
* Update mapper/manual mapping nếu có.

### Stage 4 — Snapshot copy logic

* Tìm logic tạo contract milestone snapshot.
* Copy duration và durationUnit từ milestone sang contract milestone.

### Stage 5 — Validation and test

* Validate duration/durationUnit pair.
* Thêm hoặc sửa test cho create/update milestone và contract snapshot.

---

# USER-014 — Improve deliverable notification targetUrl and metadata

## User Story

As a Business user,
I want deliverable notifications to open the correct contract workspace,
so that I can review the submitted deliverable immediately from the notification.

As a Frontend developer,
I want notifications to include metadata,
so that the FE can route users accurately and open the correct milestone or deliverable context.

## Problem

Hiện tại notification deliverable đang dùng route yếu:

```text
/business/milestones/{milestoneId}/deliverables
```

Route này không đủ context contract. FE khó biết deliverable thuộc contract nào, workspace nào, milestone nào.

## Business Rule

Notification liên quan deliverable nên trỏ về contract workspace:

```text
/contracts/{contractId}/workspace?milestoneId={milestoneId}
```

Ngoài `targetUrl`, notification nên có metadata để FE dùng linh hoạt hơn.

Metadata mẫu:

```json
{
  "contractId": 1,
  "milestoneId": 3,
  "deliverableId": 11
}
```

## Database Changes

Tạo Flyway migration mới. Không chỉnh migration cũ.

Thêm cột vào bảng `notifications`:

Ưu tiên:

```sql
metadata JSONB;
```

Nếu project chưa mapping JSONB tốt, có thể dùng:

```sql
metadata TEXT;
```

Trong trường hợp dùng TEXT, backend sẽ serialize metadata thành JSON string.

## Backend Changes

### 1. Update `NotificationEntity`

Thêm field:

Nếu dùng JSONB:

```java
private Map<String, Object> metadata;
```

Nếu dùng TEXT:

```java
private String metadata;
```

Chọn cách phù hợp với style hiện tại của project.

### 2. Update `NotificationResponse`

Response cần trả thêm:

```java
private Object metadata;
```

Hoặc:

```java
private Map<String, Object> metadata;
```

Nếu lưu TEXT thì khi response nên deserialize ra object/map nếu project đã có `ObjectMapper`.

### 3. Update `NotificationService.createAndPush(...)`

Cần hỗ trợ metadata nhưng không làm vỡ các call cũ.

Có thể dùng overload:

```java
createAndPush(userId, title, message, targetUrl);
```

và thêm bản mới:

```java
createAndPush(userId, title, message, targetUrl, metadata);
```

Các notification cũ vẫn dùng method cũ.

Notification mới liên quan deliverable dùng method mới có metadata.

### 4. Update deliverable submit notification

Khi Expert submit deliverable, notification gửi cho Business phải có:

```text
targetUrl = /contracts/{contractId}/workspace?milestoneId={milestoneId}
```

Metadata:

```json
{
  "contractId": contractId,
  "milestoneId": milestoneId,
  "deliverableId": deliverableId
}
```

### 5. Resolve contractId

Nếu deliverable chỉ có milestoneId, backend cần resolve contractId qua relationship:

* deliverable -> milestone -> contract
* hoặc milestone -> contract milestone -> contract
* hoặc repository query đang có sẵn

Không hardcode contractId.

## Acceptance Criteria

* Deliverable notification targetUrl đúng format:

```text
/contracts/{contractId}/workspace?milestoneId={milestoneId}
```

* Notification response có metadata.
* Metadata có đủ:

```text
contractId
milestoneId
deliverableId
```

* Existing notification creation không bị lỗi compile.
* Notification cũ không bắt buộc có metadata.
* FE có thể dùng targetUrl để điều hướng vào contract workspace.
* FE có thể dùng metadata để mở đúng milestone/deliverable.

## Implementation Stages

### Stage 1 — Migration

* Tạo migration mới.
* Add `notifications.metadata`.

### Stage 2 — Entity and response

* Update `NotificationEntity`.
* Update `NotificationResponse`.
* Update mapper notification entity -> response.

### Stage 3 — Service compatibility

* Update `NotificationService.createAndPush(...)`.
* Giữ backward compatibility cho call cũ.

### Stage 4 — Deliverable notification

* Tìm flow submit deliverable.
* Sửa targetUrl.
* Gắn metadata.

### Stage 5 — Test

* Test notification targetUrl.
* Test metadata có đủ contractId, milestoneId, deliverableId.

---

# USER-015 — Strengthen contract milestone snapshot fields

## User Story

As a Business user and Expert user,
I want contract milestone data to preserve the agreed content at the time of contract creation,
so that the contract history remains stable even if the original job milestone changes later.

## Problem

`contract_milestones` hiện đang đóng vai trò snapshot, nhưng snapshot chưa đủ chắc.

Sau khi contract đã được tạo, một số nội dung quan trọng như acceptance criteria hoặc deliverable expectation nên được lưu lại trong contract snapshot. Nếu chỉ đọc từ milestone gốc, dữ liệu lịch sử có thể bị thay đổi ngoài ý muốn.

## Business Rule

`contract_milestones` là snapshot hợp đồng.

Nó nên giữ các thông tin dùng để render nội dung hợp đồng tại thời điểm tạo contract.

Không nên normalize quá sớm nếu FE chỉ cần render snapshot.

## Suggested Snapshot Fields

Thêm vào `contract_milestones`:

```sql
criteria_snapshot TEXT
deliverable_expectation TEXT
```

Hoặc nếu project đã dùng JSONB ổn định:

```sql
criteria_snapshot JSONB
deliverable_expectation JSONB
```

Khuyến nghị an toàn cho sprint này:

```sql
criteria_snapshot TEXT
deliverable_expectation TEXT
```

Lý do:

* dễ migration
* ít lỗi mapping JPA
* đủ cho FE render
* sau này có thể refactor JSONB nếu cần query sâu

## Backend Changes

### 1. Update `ContractMilestoneEntity`

Thêm:

```java
private String criteriaSnapshot;
private String deliverableExpectation;
```

Nếu chọn JSONB thì dùng type phù hợp với project.

### 2. Update contract snapshot creation

Trong method tạo contract milestone snapshot, copy thêm:

```java
contractMilestone.setCriteriaSnapshot(...);
contractMilestone.setDeliverableExpectation(...);
```

Nguồn copy có thể đến từ:

* `MilestoneEntity.acceptanceCriteria`
* `MilestoneEntity.criteria`
* `MilestoneEntity.deliverableExpectation`
* `MilestoneEntity.description`
* hoặc field tương đương đang có trong project

Cần inspect entity hiện tại trước khi chọn field nguồn.

### 3. Update contract milestone response

Nếu `/contracts/{contractId}/milestones` đang trả `ContractMilestoneViewResponse`, bổ sung:

```java
private String criteriaSnapshot;
private String deliverableExpectation;
```

## Acceptance Criteria

* Contract milestone snapshot có thể lưu acceptance criteria snapshot.
* Contract milestone snapshot có thể lưu deliverable expectation snapshot.
* Khi contract được tạo, dữ liệu snapshot được copy từ milestone/job milestone gốc.
* Sau khi contract đã tạo, sửa milestone gốc không làm thay đổi snapshot của contract.
* FE có thể render snapshot từ contract milestone response.
* Không normalize phức tạp nếu chưa cần.

## Implementation Stages

### Stage 1 — Inspect existing fields

* Kiểm tra `MilestoneEntity` đang có field nào tương đương:

    * acceptance criteria
    * criteria
    * deliverable expectation
    * description
    * deliverable notes

### Stage 2 — Migration

* Tạo migration mới.
* Add snapshot columns vào `contract_milestones`.

### Stage 3 — Entity update

* Update `ContractMilestoneEntity`.

### Stage 4 — Snapshot copy logic

* Update method tạo contract milestones.
* Copy dữ liệu từ milestone gốc vào snapshot.

### Stage 5 — Response update

* Update `ContractMilestoneViewResponse`.
* Đảm bảo FE nhận được field snapshot.

### Stage 6 — Test

* Test contract snapshot giữ nguyên khi milestone gốc bị sửa sau contract creation.

---

# USER-016 — Add regression tests for contract milestone and notification flow

## User Story

As a backend developer,
I want regression tests for milestone status, contract snapshot, and notification payload,
so that future changes do not reintroduce status drift, missing snapshot data, or broken FE routing.

## Problem

Các lỗi hiện tại dễ bị tái diễn:

* `contract_milestones.status` lệch với `milestones.status`
* API contract milestones trả sai trạng thái execution
* notification deliverable targetUrl không dùng được cho FE
* notification thiếu metadata
* snapshot contract thiếu field hoặc bị thay đổi ngoài ý muốn

Cần test để khóa behavior đúng.

## Required Test Cases

### Test 1 — Submit deliverable updates live milestone status view

Given:

* Có contract.
* Có contract milestone linked với milestone thật qua `job_milestone_id`.
* Milestone ban đầu chưa ở trạng thái review.

When:

* Expert submit deliverable.

Then:

* Milestone thật chuyển sang `UNDER_REVIEW`.
* API `/contracts/{contractId}/milestones` trả status là `UNDER_REVIEW`.
* Status này đến từ `milestones.status`, không phụ thuộc vào `contract_milestones.status`.

### Test 2 — Complete milestone returns COMPLETED in contract milestone API

Given:

* Có contract milestone linked với milestone thật.

When:

* Business/Admin/System complete milestone.

Then:

* Milestone thật chuyển sang `COMPLETED`.
* API `/contracts/{contractId}/milestones` trả status là `COMPLETED`.

### Test 3 — SLA auto approve returns COMPLETED in contract milestone API

Given:

* Có deliverable đang chờ review.
* Milestone đang ở trạng thái `UNDER_REVIEW`.
* SLA auto approve condition đã đạt.

When:

* SLA auto approve job chạy hoặc service auto approve được gọi.

Then:

* Milestone thật chuyển sang `COMPLETED`.
* API `/contracts/{contractId}/milestones` trả status là `COMPLETED`.

### Test 4 — Deliverable notification has correct targetUrl and metadata

Given:

* Expert submit deliverable cho một milestone thuộc contract.

When:

* Notification được tạo cho Business.

Then:

`targetUrl` phải là:

```text
/contracts/{contractId}/workspace?milestoneId={milestoneId}
```

Metadata phải có:

```json
{
  "contractId": 1,
  "milestoneId": 3,
  "deliverableId": 11
}
```

### Test 5 — Contract milestone duration snapshot is stable

Given:

* Milestone gốc có duration và durationUnit.
* Contract được tạo từ milestone đó.

When:

* Milestone gốc bị sửa duration sau khi contract đã tạo.

Then:

* `contract_milestones.duration` không đổi.
* `contract_milestones.duration_unit` không đổi.
* API contract milestones vẫn trả duration snapshot từ `contract_milestones`.

### Test 6 — Contract snapshot fields are stable

Given:

* Milestone gốc có criteria/deliverable expectation.
* Contract được tạo.

When:

* Milestone gốc bị sửa criteria/deliverable expectation sau khi contract đã tạo.

Then:

* Contract milestone snapshot không đổi.
* API contract milestone response vẫn trả snapshot cũ.

## Backend Test Scope

Ưu tiên dùng test style hiện tại của project:

* Service test nếu project đang ưu tiên service layer.
* Controller/integration test nếu đã có MockMvc/WebMvc test.
* Repository test nếu cần verify query join.
* Không cần over-test private methods.

## Acceptance Criteria

* Có test cho submit deliverable -> `UNDER_REVIEW`.
* Có test cho complete milestone -> `COMPLETED`.
* Có test cho SLA auto approve -> `COMPLETED`.
* Có test cho deliverable notification `targetUrl`.
* Có test cho deliverable notification `metadata`.
* Có test cho duration snapshot nếu USER-013 đã implement.
* Có test cho snapshot criteria/deliverable expectation nếu USER-015 đã implement.
* Test suite pass.
* Không tạo test phụ thuộc dữ liệu local cố định.

## Implementation Stages

### Stage 1 — Inspect existing tests

* Tìm test liên quan contract.
* Tìm test liên quan milestone.
* Tìm test liên quan deliverable.
* Tìm test liên quan notification.

### Stage 2 — Prepare test fixtures

Tạo fixture/helper nếu cần:

* Business account
* Expert account
* Job
* Milestone
* Contract
* Contract milestone
* Deliverable

### Stage 3 — Add milestone status tests

* Submit deliverable test.
* Complete milestone test.
* SLA auto approve test.

### Stage 4 — Add notification tests

* Verify targetUrl.
* Verify metadata.

### Stage 5 — Add snapshot tests

* Duration snapshot stability.
* Criteria/deliverable expectation snapshot stability.

### Stage 6 — Run validation

Run project test command:

```bash
./mvnw test
```

Nếu project chạy trên Windows PowerShell:

```powershell
.\mvnw test
```

## Notes

Nếu SLA auto approve hiện chưa dễ test vì phụ thuộc scheduler, nên test trực tiếp service method xử lý auto approve thay vì chờ scheduler chạy.

Nếu notification push realtime đang phụ thuộc WebSocket, test phần persisted notification/response payload trước. Không cần test WebSocket trong scope này trừ khi project đã có test sẵn.

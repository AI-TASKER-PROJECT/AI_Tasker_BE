# Backend Logic Notes - AITASKER

Tai lieu nay ghi chu nhanh cac khoi backend, rule da enforce va cac diem can tiep tuc lam that de team BE/FE de doc code.

## 1) Kien truc chung

- `controller/*`: nhan HTTP request, boc `ApiResponse`, khong nen dat logic nghiep vu phuc tap o day.
- `service/auth`: dang ky, dang nhap, ma hoa password bang BCrypt, cap JWT.
- `service/core/AccessService`: lay account hien tai tu JWT va check role.
- `service/core/ProfileService`: KYB/KYC, portfolio AI 4 thanh phan, duyet profile, audit khi duyet.
- `service/core/MarketplaceService`: job, proposal, proposal review.
- `service/core/ContractExecutionService`: contract, negotiation, NDA, milestone, criteria, deliverable, finance, dispute, SLA.
- `service/core/AdminService`: review, settings, staff, analytics overview.
- `service/core/CatalogService`: domain/skill catalog va gan domain/skill vao job.
- `entity/*` + `repository/*`: JPA mapping theo Flyway schema.
- `resources/db/migration`: moi thay doi DB phai them migration moi, khong sua migration da chay.

## 2) Catalog domain/skill moi

Bang moi da co entity/repository/service/controller:

- `domains`: danh muc linh vuc AI, co `domain_code`, `domain_name`, `is_active`, `sort_order`.
- `skills`: danh muc ky nang AI, co `skill_code`, `skill_name`, `is_active`.
- `job_domains`: bang mapping many-to-many giua job va domain.
- `job_skills`: bang mapping many-to-many giua job va skill, co `required_level`, `is_mandatory`, `min_years_experience`.

API moi:

- `GET /api/v1/domains?activeOnly=true|false`
- `POST /api/v1/domains`
- `PATCH /api/v1/domains/{domainId}`
- `GET /api/v1/skills?activeOnly=true|false`
- `POST /api/v1/skills`
- `PATCH /api/v1/skills/{skillId}`
- `GET /api/v1/jobs/{jobId}/domains`
- `PUT /api/v1/jobs/{jobId}/domains`
- `GET /api/v1/jobs/{jobId}/skills`
- `PUT /api/v1/jobs/{jobId}/skills`

Quyen:

- Tao/sua domain/skill: `ADMIN`.
- Gan domain/skill cho job: `ADMIN` hoac business owner cua job.
- Doc domain/skill va job mapping: read API.

## 3) Rule da siết trong Marketplace

`MarketplaceService` hien enforce:

- Business phai co `BusinessProfiles.kyb_status = Approved` moi duoc tao job hoac doi status job.
- Expert phai co `ExpertProfiles.kyc_status = Approved` moi duoc nop proposal.
- Expert chi duoc nop proposal khi job status la `OPEN`; khong con cho proposal vao job `DRAFT`.
- Khi job chuyen `OPEN`, backend set `published_at` neu chua co.
- Public `GET /api/v1/jobs` chi tra job `OPEN`, tranh lo draft/cancelled.

Luu y:

- Neu FE can trang "My Jobs" gom ca draft cua doanh nghiep, nen them endpoint rieng `GET /api/v1/jobs/mine` thay vi dung public list.

## 4) Rule da siết trong Contract/Execution

`ContractExecutionService` hien enforce:

- Tao contract chi tu proposal da `Accepted`.
- Contract sign dung rule hai ben cung ky hop dong va cung ky NDA:
  - Business goi `/sign` thi set `business_accepted_at`.
  - Expert goi `/sign` thi set `expert_accepted_at`.
  - Business goi `/nda-sign` thi set `business_nda_signed_at`.
  - Expert goi `/nda-sign` thi set `expert_nda_signed_at`.
  - Chi khi du 4 timestamp tren thi status moi thanh `Active`.
  - Neu chua du 4 timestamp thi status la `Negotiating`.
- Change request reset acceptance/NDA cua hai ben va dua contract ve `Negotiating`.
- Business va expert deu phai ky NDA truoc khi contract active.
- Business chi tao milestone/criteria cho contract cua minh.
- Expert chi submit deliverable cho contract cua minh, contract phai `Active` va co du NDA cua business/expert.
- Khi submit deliverable, milestone duoc chuyen sang `Under Review`.
- Business chi tao transaction loai `Deposit`; `Payout`/`Refund` danh cho `ADMIN`/`STAFF`.
- Tao invoice truc tiep chi cho `ADMIN`/`STAFF`.
- List contract/milestone/criteria/deliverable/transaction/dispute deu check participant hoac operator.

API doc them de FE dung du lieu that:

- `GET /api/v1/milestones/{milestoneId}/deliverables`
- `GET /api/v1/milestones/{milestoneId}/transactions`
- `GET /api/v1/transactions/{transactionId}/invoice`
- `GET /api/v1/contracts/{contractId}/disputes`
- `GET /api/v1/disputes/{disputeId}`

## 5) DB migration moi

`V11__catalog_and_contract_acceptance_alignment.sql` them:

- `domains.domain_code`, `domains.is_active`, `domains.sort_order`.
- `skills.skill_code`, `skills.is_active`.
- `job_skills.required_level`, `job_skills.is_mandatory`, `job_skills.min_years_experience`.
- `contracts.business_accepted_at`, `contracts.expert_accepted_at`.

Ly do:

- Khong sua `V10` vi Flyway migration da chay.
- Dong bo hon voi DB Description trong Excel.
- Co noi luu acceptance cua hai ben theo rule `CON-01`.

## 6) Cac diem van con la MVP/mo phong

Nhung phan sau chua phai production:

- AI Job Assistant chua goi NLP service that.
- Matching van con don gian, moi dua tren `ai_tag`/keyword va proposal text; chua score theo portfolio/skill/domain.
- VNPay/IPN/refund/payout chua that; webhook hien mo phong cap nhat status va invoice.
- Firebase/file upload chua that; deliverable/license/receipt van la URL text.
- NDA PDF chua generate/upload.
- SLA auto approve co API manual, chua co scheduler chay dinh ky.
- Dispute chua khoa dong tien/snapshot/refund/penalty that.
- Notification/WebSocket chua co.
- Audit log chua phu het moi thao tac nhay cam.

## 7) Huong tiep theo nen lam

Uu tien de ket noi FE voi DB that hon:

1. Them `GET /api/v1/jobs/mine` cho business dashboard.
2. Them public expert directory aggregate tu `expert_profiles`, `portfolios`, `reviews`.
3. Them API list `audit_logs` va ghi audit cho setting/staff/contract/dispute/transaction.
4. Tach bang hoac cot rieng cho dispute evidence, demo testing result, technical report.
5. Them payment service rieng cho VNPay va escrow ledger.
6. Them scheduler cho SLA auto approve.

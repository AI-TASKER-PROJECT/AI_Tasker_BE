# Huong dan test API Back-end bang Postman

Tai lieu nay duoc dong bo tu runtime OpenAPI hien tai. Test theo thu tu flow trong Postman.

- Postman: import `docs/openapi/openapi-v1.json` hoac dung Swagger UI de tham chieu request.
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Route `Public`: test duoc khi khong co token.
- Route `Bearer JWT`: login bang `POST /api/auth/login`, copy `accessToken`, them header `Authorization: Bearer <accessToken>` trong Postman.
- Endpoint multipart khong dung body JSON; chon `multipart/form-data` va upload dung field file.

## Tai khoan seed thuong dung

| Role | Email | Mat khau |
| --- | --- | --- |
| BUSINESS | `business@aitasker.local` | `12345678` |
| EXPERT | `expert@aitasker.local` | `12345678` |
| ADMIN | `admin@aitasker.local` | `12345678` |
| STAFF | `staff@aitasker.local` | `12345678` |

Bo du lieu V67 co dung 31 tai khoan: 1 Admin, 10 Business, 10 Expert va 10
Staff. Bon tai khoan tren la tai khoan noi bo duoc giu nguyen thong tin dang
nhap; 27 tai khoan con lai cung dung mat khau `12345678` va co email theo vai
tro/ten ro rang. `staff@aitasker.local` chi co domain noi bo `PROFILE_REVIEW`
(`Profile Review`).

Du lieu nghiep vu mau de kiem tra nhanh:

| Nhom | ID mau | Trang thai / muc dich |
| --- | --- | --- |
| Job | `1001`-`1010` | Co du `DRAFT`, `OPEN`, `IN_PROGRESS`, `CLOSED`; moi job co SoW va 3 milestone |
| Proposal | `3001`-`3012` | Co `Pending`, `Accepted`, `Rejected`; proposal milestone khop dung job va bid |
| Contract | `4001`-`4006` | Co `DRAFT`, `ACTIVE`, `COMPLETED`, `CLOSED` va snapshot 3 milestone |
| Catalog | `1`-`30` moi bang | 30 domain, 30 skill, 30 technology; khong gan `PROFILE_REVIEW` cho job |
| Wallet | 31 vi | Co top-up, membership revenue, contract deposit, milestone escrow/release/refund lien ket |

Chi tiet mapping va quy tac doi soat nam tai
`docs/product/demo-data-rebuild.md`. OpenAPI route/schema khong thay doi trong
V67; thay doi nay chi lam sach va dong bo du lieu test.

## Milestone Dispute Smoke Flow

1. Business/Expert sign contract and NDA, then Business pays contract deposit.
2. Business deposits milestone escrow with `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`.
3. Business milestone deposit auto-starts the milestone; Expert submits progress reports if needed, then submits deliverable.
4. Business approves to release escrow, or rejects to create/update one active dispute.
5. Either party can request escalation; Admin assigns reviewer; Staff decision is recorded first, then Admin executes settlement separately.
6. For termination, create a termination request, assign Staff, approve/reject, execute settlement, then refund deposit before contract is CLOSED and reviews are allowed.

## Auth Flow

- Muc tieu flow: Dang ky, dang nhap, OTP email va tra cuu ma so thue.

### POST `/api/auth/reset-password`
- OperationId: `resetPassword`
- Auth: Public
- Giai thich: Operation resetPassword.
- Params: Khong co.
- Body raw:
```json
{ "schema": "ResetPasswordRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/register`
- OperationId: `register`
- Auth: Public
- Giai thich: Operation register.
- Params: Khong co.
- Body raw:
```json
{ "schema": "RegisterRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/refresh`
- OperationId: `refresh`
- Auth: Public
- Giai thich: Operation refresh.
- Params: Khong co.
- Body raw:
```json
{ "schema": "RefreshTokenRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/login`
- OperationId: `login`
- Auth: Public
- Giai thich: Operation login.
- Params: Khong co.
- Body raw:
```json
{ "schema": "LoginRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/google/register`
- OperationId: `googleRegister`
- Auth: Public
- Giai thich: Operation googleRegister.
- Params: Khong co.
- Body raw:
```json
{ "schema": "GoogleAuthRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/google/login`
- OperationId: `googleLogin`
- Auth: Public
- Giai thich: Operation googleLogin.
- Params: Khong co.
- Body raw:
```json
{ "schema": "GoogleAuthRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/forgot-password`
- OperationId: `forgotPassword`
- Auth: Public
- Giai thich: Operation forgotPassword.
- Params: Khong co.
- Body raw:
```json
{ "schema": "ForgotPasswordRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/email/verify-otp`
- OperationId: `verifyOtp`
- Auth: Public
- Giai thich: Operation verifyOtp.
- Params: Khong co.
- Body raw:
```json
{ "schema": "VerifyOtpRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/auth/email/send-otp`
- OperationId: `sendOtp`
- Auth: Public
- Giai thich: Operation sendOtp.
- Params: Khong co.
- Body raw:
```json
{ "schema": "SendOtpRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/auth/tax-check/{mst}`
- OperationId: `checkTaxCode`
- Auth: Public
- Giai thich: Operation checkTaxCode.
- Params:
  - `mst` (path, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/auth/me`
- OperationId: `me`
- Auth: Bearer JWT
- Giai thich: Operation me.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/auth/check-email`
- OperationId: `checkEmail`
- Auth: Public
- Giai thich: Operation checkEmail.
- Params:
  - `email` (query, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Profile Verification Flow

- Muc tieu flow: Tao, xem va duyet ho so Business/Expert va portfolio.

### GET `/api/v1/profiles/portfolio`
- OperationId: `listPortfolio`
- Auth: Bearer JWT
- Giai thich: Operation listPortfolio.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/portfolio`
- OperationId: `upsertPortfolio`
- Auth: Bearer JWT
- Giai thich: Operation upsertPortfolio.
- Params: Khong co.
- Body raw:
```json
{ "schema": "PortfolioEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/portfolio/certificate-file`
- OperationId: `uploadExpertCertificate`
- Auth: Bearer JWT
- Giai thich: Operation uploadExpertCertificate.
- Params: Khong co.
- Body raw:
```json
{ "schema": "object" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/expert`
- OperationId: `listExpert`
- Auth: Bearer JWT
- Giai thich: Operation listExpert.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/expert`
- OperationId: `upsertExpert`
- Auth: Bearer JWT
- Giai thich: Operation upsertExpert.
- Params: Khong co.
- Body raw:
```json
{ "schema": "ExpertProfileEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/expert/portfolio-file`
- OperationId: `uploadExpertPortfolio`
- Auth: Bearer JWT
- Giai thich: Operation uploadExpertPortfolio.
- Params: Khong co.
- Body raw:
```json
{ "schema": "object" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/business`
- OperationId: `listBusiness`
- Auth: Bearer JWT
- Giai thich: Operation listBusiness.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/business`
- OperationId: `upsertBusiness`
- Auth: Bearer JWT
- Giai thich: Operation upsertBusiness.
- Params: Khong co.
- Body raw:
```json
{ "schema": "BusinessProfileEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/business/license-file`
- OperationId: `uploadBusinessLicense`
- Auth: Bearer JWT
- Giai thich: Operation uploadBusinessLicense.
- Params: Khong co.
- Body raw:
```json
{ "schema": "object" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/profiles/approve/{type}/{id}`
- OperationId: `approve`
- Auth: Bearer JWT
- Giai thich: Operation approve.
- Params:
  - `type` (path, required, string)
  - `id` (path, required, integer)
  - `status` (query, required, string)
  - `reason` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/portfolio/me`
- OperationId: `myPortfolio`
- Auth: Bearer JWT
- Giai thich: Operation myPortfolio.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/files/view-url`
- OperationId: `fileViewUrl`
- Auth: Bearer JWT
- Giai thich: Operation fileViewUrl.
- Params:
  - `path` (query, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/expert/{expertId}`
- OperationId: `getExpertById`
- Auth: Bearer JWT
- Giai thich: Operation getExpertById.
- Params:
  - `expertId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/expert/me`
- OperationId: `myExpert`
- Auth: Bearer JWT
- Giai thich: Operation myExpert.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/business/{businessId}`
- OperationId: `getBusinessById`
- Auth: Public
- Giai thich: Operation getBusinessById.
- Params:
  - `businessId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/business/me`
- OperationId: `myBusiness`
- Auth: Bearer JWT
- Giai thich: Operation myBusiness.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/profiles/business/by-job/{jobId}`
- OperationId: `businessByJob`
- Auth: Public
- Giai thich: Operation businessByJob.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Job Draft & Publish Flow

- Muc tieu flow: Tao draft, cap nhat, gan taxonomy, sinh SoW va publish job.

### GET `/api/v1/jobs/{jobId}`
- OperationId: `jobDetail`
- Auth: Public
- Giai thich: Operation jobDetail.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PUT `/api/v1/jobs/{jobId}`
- OperationId: `updateDraftJob`
- Auth: Bearer JWT
- Giai thich: Operation updateDraftJob.
- Params:
  - `jobId` (path, required, integer)
- Body raw:
```json
{ "schema": "JobEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/{jobId}/technologies`
- OperationId: `listJobTechnologies`
- Auth: Bearer JWT
- Giai thich: Operation listJobTechnologies.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PUT `/api/v1/jobs/{jobId}/technologies`
- OperationId: `replaceJobTechnologies`
- Auth: Bearer JWT
- Giai thich: Operation replaceJobTechnologies.
- Params:
  - `jobId` (path, required, integer)
- Body raw:
```json
[ { "schema": "array" } ]
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/{jobId}/skills`
- OperationId: `listJobSkills`
- Auth: Bearer JWT
- Giai thich: Operation listJobSkills.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PUT `/api/v1/jobs/{jobId}/skills`
- OperationId: `replaceJobSkills`
- Auth: Bearer JWT
- Giai thich: Operation replaceJobSkills.
- Params:
  - `jobId` (path, required, integer)
- Body raw:
```json
[ { "schema": "array" } ]
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/{jobId}/domains`
- OperationId: `listJobDomains`
- Auth: Bearer JWT
- Giai thich: Operation listJobDomains.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PUT `/api/v1/jobs/{jobId}/domains`
- OperationId: `replaceJobDomains`
- Auth: Bearer JWT
- Giai thich: Operation replaceJobDomains.
- Params:
  - `jobId` (path, required, integer)
- Body raw:
```json
[ { "schema": "array" } ]
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs`
- OperationId: `listJobs`
- Auth: Public
- Giai thich: Operation listJobs.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/jobs`
- OperationId: `createJob`
- Auth: Bearer JWT
- Giai thich: Operation createJob.
- Params: Khong co.
- Body raw:
```json
{ "schema": "JobEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/jobs/{jobId}/publish`
- OperationId: `publishJob`
- Auth: Bearer JWT
- Giai thich: Operation publishJob.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/jobs/generate-sow`
- OperationId: `generateSow`
- Auth: Bearer JWT
- Giai thich: Generate SoW, danh gia khoang ngan sach AI tham khao va tra ca phan bo theo budget Business/de xuat. Business van la nguoi chot gia.
- Params: Khong co.
- Body raw:
```json
{
  "projectTitle": "RAG customer support bot",
  "rawRequirement": "Chatbot tra loi san pham, tra cuu don hang va chuyen tiep nhan vien",
  "budget": 50000000,
  "duration": 10,
  "durationUnit": "WEEK",
  "supportFields": ["Generative AI Applications"],
  "requiredSkills": ["RAG", "API Integration"]
}
```
- Response can kiem tra:
  - `budgetAssessment.businessBudget` bang dung budget request.
  - `budgetAssessment.estimatedMin <= recommendedBudget <= estimatedMax`.
  - `budgetAssessment.status` thuoc `TOO_LOW|LOW|SUITABLE|HIGH`.
  - Tong `milestones[].budget` bang `businessBudget`.
  - Tong `milestones[].recommendedBudget` bang `recommendedBudget`.
  - `requiresBusinessConfirmation=false` only for `HIGH`; otherwise `true`.
    Backend never overwrites the Business amount.
  - Bare AI values `80/100/130` are normalized to full VND
    `80000000/100000000/130000000` before status comparison.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/jobs/reallocate-sow-budget`
- OperationId: `reallocateSowBudget`
- Auth: Bearer JWT
- Giai thich: Chia lai milestone theo gia tuy chinh do Business chot; khong goi AI, khong tao Job va khong ghi database.
- Params: Khong co.
- Body raw:
```json
{
  "selectedBudget": 110000000,
  "milestones": [
    { "milestoneIndex": 0, "referenceBudget": 40000000 },
    { "milestoneIndex": 1, "referenceBudget": 100000000 }
  ]
}
```
- Response can kiem tra:
  - `currency=VND`.
  - `allocationTotal` bang chinh xac `selectedBudget`.
  - Ket qua sap xep theo `milestoneIndex`, khong phu thuoc thu tu request.
  - Frontend map `allocations[].fundsAllocated` vao milestone cung index.
  - `milestoneIndex` phai duy nhat; budget/reference phai la so VND nguyen duong.
- Ma phan hoi thuong gap:
  - `200`: Phan bo thanh cong.
  - `400`: Budget/reference khong hop le, milestones rong/qua 50, hoac trung `milestoneIndex`.
  - `401`/`403`: Sai token, het han token hoac sai quyen truy cap.
  - `500`: Loi he thong; doi chieu log backend.

### PATCH `/api/v1/jobs/{jobId}/status`
- OperationId: `updateJobStatus`
- Auth: Bearer JWT
- Giai thich: Operation updateJobStatus.
- Params:
  - `jobId` (path, required, integer)
  - `status` (query, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/{jobId}/milestones`
- OperationId: `listJobMilestones`
- Auth: Public
- Giai thich: Operation listJobMilestones.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/my`
- OperationId: `listMyJobs`
- Auth: Bearer JWT
- Giai thich: Operation listMyJobs.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Proposal Flow

- Muc tieu flow: Submit proposal, review proposal, matching va de xuat expert.

### POST `/api/v1/proposals`
- OperationId: `submitProposal`
- Auth: Bearer JWT
- Giai thich: Operation submitProposal.
- Params: Khong co.
- Body raw:
```json
{ "schema": "ProposalRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/proposals/file`
- OperationId: `uploadProposalFile`
- Auth: Bearer JWT
- Giai thich: Operation uploadProposalFile.
- Params: Khong co.
- Body raw:
```json
{ "schema": "object" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/jobs/{jobPostingId}/expert-recommendations`
- OperationId: `getRecommendations`
- Auth: Bearer JWT
- Giai thich: Get saved expert recommendations
- Params:
  - `jobPostingId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/jobs/{jobPostingId}/expert-recommendations`
- OperationId: `generateRecommendations`
- Auth: Bearer JWT
- Giai thich: Generate expert recommendations
- Params:
  - `jobPostingId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select`
- OperationId: `selectRecommendedExpert`
- Auth: Bearer JWT
- Giai thich: Select a recommended expert
- Params:
  - `jobPostingId` (path, required, integer)
  - `expertId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/proposals/{proposalId}/status`
- OperationId: `reviewProposal`
- Auth: Bearer JWT
- Giai thich: Operation reviewProposal.
- Params:
  - `proposalId` (path, required, integer)
  - `status` (query, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/proposals/my`
- OperationId: `listMyProposals`
- Auth: Bearer JWT
- Giai thich: Operation listMyProposals.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/{jobId}/proposals`
- OperationId: `listProposals`
- Auth: Bearer JWT
- Giai thich: Operation listProposals.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/jobs/{jobId}/matching`
- OperationId: `matching`
- Auth: Bearer JWT
- Giai thich: Operation matching.
- Params:
  - `jobId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/jobs/{jobPostingId}/expert-candidates`
- OperationId: `findTopCandidates`
- Auth: Bearer JWT
- Giai thich: Find top expert candidates
- Params:
  - `jobPostingId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Wallet & Payment Flow

- Muc tieu flow: Wallet, top-up, membership, credits, quota va withdrawal.

### GET `/api/v1/withdrawal-requests`
- OperationId: `listMyWithdrawalRequests`
- Auth: Bearer JWT
- Giai thich: Operation listMyWithdrawalRequests.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/withdrawal-requests`
- OperationId: `createWithdrawalRequest`
- Auth: Bearer JWT
- Giai thich: Operation createWithdrawalRequest.
- Params: Khong co.
- Body raw:
```json
{ "schema": "WithdrawalRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/withdrawal-requests/{withdrawalId}/reject`
- OperationId: `rejectWithdrawal`
- Auth: Bearer JWT
- Giai thich: Operation rejectWithdrawal.
- Params:
  - `withdrawalId` (path, required, integer)
- Body raw:
```json
{ "schema": "WithdrawalReviewRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/withdrawal-requests/{withdrawalId}/approve`
- OperationId: `approveWithdrawal`
- Auth: Bearer JWT
- Giai thich: Operation approveWithdrawal.
- Params:
  - `withdrawalId` (path, required, integer)
- Body raw:
```json
{ "schema": "WithdrawalReviewRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/payments/payos/{orderCode}/sync`
- OperationId: `syncPaymentStatus`
- Auth: Bearer JWT
- Giai thich: Operation syncPaymentStatus.
- Params:
  - `orderCode` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/payments/payos/create`
- OperationId: `createPayment`
- Auth: Bearer JWT
- Giai thich: Operation createPayment.
- Params: Khong co.
- Body raw:
```json
{ "schema": "CreateWalletTopupPaymentRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/membership/packages/{packageId}/purchase`
- OperationId: `purchasePackage`
- Auth: Bearer JWT
- Giai thich: Operation purchasePackage.
- Params:
  - `packageId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/credits/proposal/purchase`
- OperationId: `purchaseProposalCredits`
- Auth: Bearer JWT
- Giai thich: Operation purchaseProposalCredits.
- Params: Khong co.
- Body raw:
```json
{ "schema": "CreditPurchaseRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/credits/job-post/purchase`
- OperationId: `purchaseJobPostCredits`
- Auth: Bearer JWT
- Giai thich: Operation purchaseJobPostCredits.
- Params: Khong co.
- Body raw:
```json
{ "schema": "CreditPurchaseRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/wallet/transactions`
- OperationId: `walletTransactions`
- Auth: Bearer JWT
- Giai thich: Operation walletTransactions.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/wallet/current`
- OperationId: `currentWallet`
- Auth: Bearer JWT
- Giai thich: Operation currentWallet.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/wallet/me`
- OperationId: `currentWallet_1`
- Auth: Bearer JWT
- Giai thich: Operation currentWallet_1.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/withdrawal-requests`
- OperationId: `listWithdrawalRequestsForAdmin`
- Auth: Bearer JWT
- Giai thich: Operation listWithdrawalRequestsForAdmin.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/users/me/quota`
- OperationId: `currentQuota`
- Auth: Bearer JWT
- Giai thich: Operation currentQuota.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/payments/payos/return`
- OperationId: `handleReturn`
- Auth: Public
- Giai thich: Operation handleReturn.
- Params:
  - `params` (query, required, object)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/membership/packages`
- OperationId: `listPackages`
- Auth: Bearer JWT
- Giai thich: Operation listPackages.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Contract Execution Flow

- Muc tieu flow: Contract, milestone, deliverable, dispute, termination va review.

### PUT `/api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- OperationId: `updateCriteria`
- Auth: Bearer JWT
- Giai thich: Operation updateCriteria.
- Params:
  - `milestoneId` (path, required, integer)
  - `criteriaId` (path, required, integer)
- Body raw:
```json
{ "schema": "AcceptanceCriteriaRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/milestones/{milestoneId}/criteria/{criteriaId}`
- OperationId: `deleteCriteria`
- Auth: Bearer JWT
- Giai thich: Operation deleteCriteria.
- Params:
  - `milestoneId` (path, required, integer)
  - `criteriaId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/withdraw`
- OperationId: `withdrawTermination`
- Auth: Bearer JWT
- Giai thich: Operation withdrawTermination.
- Params:
  - `terminationRequestId` (path, required, integer)
  - `reason` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/reject`
- OperationId: `rejectTermination`
- Auth: Bearer JWT
- Giai thich: Operation rejectTermination.
- Params:
  - `terminationRequestId` (path, required, integer)
  - `reason` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/refund-deposit`
- OperationId: `refundTerminationDeposit`
- Auth: Bearer JWT
- Giai thich: Operation refundTerminationDeposit.
- Params:
  - `terminationRequestId` (path, required, integer)
- Body raw:
```json
{ "schema": "DepositRefundRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/partial-evidence`
- OperationId: `submitPartialEvidence`
- Auth: Bearer JWT
- Giai thich: Operation submitPartialEvidence.
- Params:
  - `terminationRequestId` (path, required, integer)
- Body raw:
```json
{ "schema": "TerminationRequestEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/execute-settlement`
- OperationId: `executeTerminationSettlement`
- Auth: Bearer JWT
- Giai thich: Operation executeTerminationSettlement.
- Params:
  - `terminationRequestId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/assign-staff`
- OperationId: `assignTerminationStaff`
- Auth: Bearer JWT
- Giai thich: Operation assignTerminationStaff.
- Params:
  - `terminationRequestId` (path, required, integer)
  - `staffId` (query, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/approve`
- OperationId: `approveTermination`
- Auth: Bearer JWT
- Giai thich: Operation approveTermination.
- Params:
  - `terminationRequestId` (path, required, integer)
- Body raw:
```json
{ "schema": "TerminationRequestEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/dispute`
- OperationId: `disputeTermination`
- Auth: Bearer JWT
- Giai thich: Operation disputeTermination.
- Params:
  - `terminationRequestId` (path, required, integer)
  - `reason` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/{terminationRequestId}/accept`
- OperationId: `acceptTermination`
- Auth: Bearer JWT
- Giai thich: Operation acceptTermination.
- Params:
  - `terminationRequestId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/termination-requests/expire-awaiting-expert`
- OperationId: `expireTerminationResponses`
- Auth: Bearer JWT
- Giai thich: Operation expireTerminationResponses.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones`
- OperationId: `createMilestone`
- Auth: Bearer JWT
- Giai thich: Operation createMilestone.
- Params: Khong co.
- Body raw:
```json
{ "schema": "MilestoneEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/start`
- OperationId: `startMilestone`
- Auth: Bearer JWT
- Giai thich: Compatibility endpoint. Milestone escrow deposit now auto-starts the milestone; this route is idempotent when the milestone is already `IN_PROGRESS` and only transitions legacy `DEPOSITED` rows.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/reject`
- OperationId: `rejectMilestone`
- Auth: Bearer JWT
- Giai thich: Business tu choi final deliverable, gui ly do tong va co the gui cac acceptance criteria khong dat kem ly do rieng.
- Params:
  - `milestoneId` (path, required, integer)
  - `reason` (query, optional, string, compatibility fallback neu client cu chua gui body)
- Body raw:
```json
{
  "reason": "San pham chua du dieu kien nghiem thu.",
  "failedCriteria": [
    {
      "criteriaId": 12,
      "reason": "OTP het han nhung he thong van cho xac thuc."
    }
  ]
}
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Reason rong, criteria khong thuoc milestone, hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/disputes`
- OperationId: `initiateMilestoneDispute`
- Auth: Bearer JWT
- Giai thich: Operation initiateMilestoneDispute.
- Params:
  - `milestoneId` (path, required, integer)
  - `contractId` (query, required, integer)
  - `initiatedBy` (query, optional, string)
  - `initiationType` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/milestones/{milestoneId}/deliverables`
- OperationId: `listDeliverables`
- Auth: Bearer JWT
- Giai thich: Operation listDeliverables.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/deliverables`
- OperationId: `submitMilestoneDeliverable`
- Auth: Bearer JWT
- Giai thich: Expert nop san pham lan dau hoac nop lai truoc deadline cua contract milestone; qua han tra `MILESTONE_DA_QUA_HAN_NOP_SAN_PHAM`.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw:
```json
{
  "sourceCodeUrl": "https://github.com/expert/project",
  "sourceCodeFileUrl": "milestone-source-code/milestones/10/accounts/99/source.zip",
  "demoLink": "https://demo.example.com",
  "submissionNotes": "Release v1.0"
}
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi, milestone qua deadline, hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/source-code-file`
- OperationId: `uploadMilestoneSourceCode`
- Auth: Bearer JWT (approved assigned EXPERT)
- Giai thich: Upload source code ZIP truoc deadline, toi da 50 MB; tra ve Firebase storage path de gan vao `sourceCodeFileUrl`.
- Params:
  - `milestoneId` (path, required, integer)
- Body: `multipart/form-data`, key `file`, chi nhan `.zip`.
- Ma phan hoi thuong gap:
  - `200`: Upload thanh cong, `data` la storage path.
  - `400`: File rong, sai dinh dang, qua 50 MB, milestone qua deadline, hoac contract/milestone sai trang thai.
  - `401`/`403`: Sai token, sai role, Expert khong thuoc contract.
  - `500`: Firebase chua cau hinh hoac upload that bai.

### GET `/api/v1/milestones/{milestoneId}/criteria`
- OperationId: `listCriteria`
- Auth: Bearer JWT
- Giai thich: Operation listCriteria.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/criteria`
- OperationId: `createCriteria`
- Auth: Bearer JWT
- Giai thich: Operation createCriteria.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw:
```json
{ "schema": "AcceptanceCriteriaRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/complete`
- OperationId: `completeMilestone`
- Auth: Bearer JWT
- Giai thich: Operation completeMilestone.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/milestones/{milestoneId}/approve`
- OperationId: `approveMilestone`
- Auth: Bearer JWT
- Giai thich: Operation approveMilestone.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/disputes/{disputeId}/staff-decision`
- OperationId: `staffDecideAlias`
- Auth: Bearer JWT
- Giai thich: Operation staffDecideAlias.
- Params:
  - `disputeId` (path, required, integer)
  - `expertPercent` (query, required, integer)
  - `note` (query, optional, string)
  - `staffReport` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/disputes/{disputeId}/route-staff`
- OperationId: `routeDisputeStaff`
- Auth: Bearer JWT
- Giai thich: Operation routeDisputeStaff. Staff or system routes dispute to a Staff member.
- Params:
  - `disputeId` (path, required, integer)
  - `staffId` (query, optional, integer) — pass null for auto-pick
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/disputes/{disputeId}/execute-settlement`
- OperationId: `executeDisputeSettlementAlias`
- Auth: Bearer JWT
- Giai thich: Operation executeDisputeSettlementAlias.
- Params:
  - `disputeId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/disputes/{disputeId}/escalation-request`
- OperationId: `requestEscalation`
- Auth: Bearer JWT
- Giai thich: Operation requestEscalation.
- Params:
  - `disputeId` (path, required, integer)
  - `reason` (query, optional, string)
  - `evidenceFile` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/disputes/{disputeId}/cancel`
- OperationId: `cancelDispute`
- Auth: Bearer JWT
- Giai thich: Operation cancelDispute.
- Params:
  - `disputeId` (path, required, integer)
  - `reason` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/cancel-draft`
- OperationId: `cancelDraftContract`
- Auth: Bearer JWT
- Giai thich: Operation cancelDraftContract. Business cancels untouched DRAFT contract before any signature.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts/{contractId}/termination-requests`
- OperationId: `listTerminationRequests`
- Auth: Bearer JWT
- Giai thich: Operation listTerminationRequests.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/termination-requests`
- OperationId: `requestTermination`
- Auth: Bearer JWT
- Giai thich: Operation requestTermination.
- Params:
  - `contractId` (path, required, integer)
- Body raw:
```json
{ "schema": "TerminationRequestEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/sign`
- OperationId: `signContract`
- Auth: Bearer JWT
- Giai thich: Operation signContract.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts/{contractId}/reviews`
- OperationId: `listContractReviews`
- Auth: Bearer JWT
- Giai thich: Operation listContractReviews.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/reviews`
- OperationId: `createContractReview`
- Auth: Bearer JWT
- Giai thich: Operation createContractReview.
- Params:
  - `contractId` (path, required, integer)
- Body raw:
```json
{ "schema": "ReviewEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/reject`
- OperationId: `rejectContract`
- Auth: Bearer JWT
- Giai thich: Operation rejectContract.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/nda-sign`
- OperationId: `signNda`
- Auth: Bearer JWT
- Giai thich: Operation signNda.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- OperationId: `listProgressReports`
- Auth: Bearer JWT
- Giai thich: Operation listProgressReports.
- Params:
  - `contractId` (path, required, integer)
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports`
- OperationId: `submitProgressReport`
- Auth: Bearer JWT
- Giai thich: Operation submitProgressReport.
- Params:
  - `contractId` (path, required, integer)
  - `milestoneId` (path, required, integer)
- Body raw:
```json
{
  "content": "Da hoan thanh API va giao dien chinh",
  "percentComplete": 70,
  "attachmentUrl": "https://docs.example.com/report",
  "sourceCodeUrl": "https://github.com/expert/project",
  "sourceCodeFileUrl": "milestone-source-code/milestones/10/accounts/99/source.zip",
  "demoLink": "https://demo.example.com",
  "submissionNotes": "Checkpoint lan 1"
}
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/acknowledge`
- OperationId: `acknowledgeProgressReport`
- Auth: Bearer JWT
- Giai thich: Operation acknowledgeProgressReport. Business acknowledges the latest progress report to unlock the next submission.
- Params:
  - `contractId` (path, required, integer)
  - `milestoneId` (path, required, integer)
  - `progressReportId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback`
- OperationId: `feedbackProgressReport`
- Auth: Bearer JWT
- Giai thich: Business records feedback for a progress report; feedback also acknowledges it when pending.
- Params:
  - `contractId` (path, required, integer)
  - `milestoneId` (path, required, integer)
  - `progressReportId` (path, required, integer)
- Body raw:
```json
{ "schema": "ProgressReportFeedbackRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Feedback rong, milestone terminal, hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay report thuoc contract/milestone.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`
- OperationId: `depositMilestone`
- Auth: Bearer JWT
- Giai thich: Operation depositMilestone.
- Params:
  - `contractId` (path, required, integer)
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/deposit/pay`
- OperationId: `payContractDeposit`
- Auth: Bearer JWT
- Giai thich: Operation payContractDeposit.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/from-proposals/{proposalId}`
- OperationId: `createDraft`
- Auth: Bearer JWT
- Giai thich: Operation createDraft.
- Params:
  - `proposalId` (path, required, integer)
- Body raw:
```json
{ "schema": "ContractEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/case-attachments`
- OperationId: `listCaseAttachments`
- Auth: Bearer JWT
- Giai thich: Operation listCaseAttachments.
- Params:
  - `ownerType` (query, required, string)
  - `ownerId` (query, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/case-attachments`
- OperationId: `createCaseAttachment`
- Auth: Bearer JWT
- Giai thich: Operation createCaseAttachment.
- Params: Khong co.
- Body raw:
```json
{ "schema": "CaseAttachmentEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/milestones/{milestoneId}`
- OperationId: `updateMilestone`
- Auth: Bearer JWT
- Giai thich: Operation updateMilestone.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw:
```json
{ "schema": "MilestoneEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/termination-requests/{terminationRequestId}`
- OperationId: `getTerminationRequest`
- Auth: Bearer JWT
- Giai thich: Operation getTerminationRequest.
- Params:
  - `terminationRequestId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/disputes/{disputeId}`
- OperationId: `getDispute`
- Auth: Bearer JWT
- Giai thich: Operation getDispute.
- Params:
  - `disputeId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts`
- OperationId: `listContracts`
- Auth: Bearer JWT
- Giai thich: Operation listContracts.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts/{contractId}`
- OperationId: `getContract`
- Auth: Bearer JWT
- Giai thich: Operation getContract.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts/{contractId}/milestones`
- OperationId: `listMilestones`
- Auth: Bearer JWT
- Giai thich: Operation listMilestones.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/contracts/{contractId}/disputes`
- OperationId: `listDisputes`
- Auth: Bearer JWT
- Giai thich: Operation listDisputes.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/staff/disputes`
- OperationId: `listStaffDisputes`
- Auth: Bearer JWT (STAFF role)
- Giai thich: Staff inbox disputes duoc gan, co phan trang va loc theo status.
- Params:
  - `page` (query, optional, integer, default: 0)
  - `size` (query, optional, integer, default: 20, min: 1, max: 100)
  - `status` (query, optional, string — loc theo dispute status)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger (StaffDisputeListResponse).
  - `400`: Validation loi (page < 0, size ngoai 1..100).
  - `401`/`403`: Sai token, het han token, sai role (chi STAFF).
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/disputes/staff-sla-escalate`
- OperationId: `escalateOverdueStaffDisputes`
- Auth: Bearer JWT
- Giai thich: Operation escalateOverdueStaffDisputes.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/{milestoneId}/progress-report-request`
- OperationId: `requestProgressReport`
- Auth: Bearer JWT
- Giai thich: Operation requestProgressReport.
- Params:
  - `contractId` (path, required, integer)
  - `milestoneId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/sla-auto-approve`
- OperationId: `autoApproveReviewSla`
- Auth: Bearer JWT
- Giai thich: Operation autoApproveReviewSla.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/milestones/check-overdue`
- OperationId: `checkOverdue`
- Auth: Bearer JWT
- Giai thich: Operation checkOverdue.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/immediate-termination`
- OperationId: `immediateTermination`
- Auth: Bearer JWT
- Giai thich: Operation immediateTermination.
- Params:
  - `contractId` (path, required, integer)
- Body raw:
```json
{ "schema": "ImmediateTerminationRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/contracts/{contractId}/expert-deposit/pay`
- OperationId: `payExpertContractDeposit`
- Auth: Bearer JWT
- Giai thich: Operation payExpertContractDeposit.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/contracts/{contractId}/deposits/refund`
- OperationId: `refundContractDeposits`
- Auth: Bearer JWT
- Giai thich: Operation refundContractDeposits.
- Params:
  - `contractId` (path, required, integer)
- Body raw:
```json
{ "schema": "DepositRefundRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/disputes/{disputeId}/staff-candidates`
- OperationId: `listStaffCandidates`
- Auth: Bearer JWT
- Giai thich: Operation listStaffCandidates.
- Params:
  - `disputeId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Notification Flow

- Muc tieu flow: Thong bao trong he thong.

### PATCH `/api/v1/notifications/{notificationId}/read`
- OperationId: `markAsRead`
- Auth: Bearer JWT
- Giai thich: Operation markAsRead.
- Params:
  - `notificationId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/notifications/read-all`
- OperationId: `markAllAsRead`
- Auth: Bearer JWT
- Giai thich: Operation markAllAsRead.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/notifications`
- OperationId: `listMine`
- Auth: Bearer JWT
- Giai thich: Operation listMine.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/notifications/unread-count`
- OperationId: `countUnreadMine`
- Auth: Bearer JWT
- Giai thich: Operation countUnreadMine.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Catalog & Reference Flow

- Muc tieu flow: Danh muc domain, skill va technology.

### GET `/api/v1/technologies`
- OperationId: `listTechnologies`
- Auth: Bearer JWT
- Giai thich: Operation listTechnologies.
- Params:
  - `activeOnly` (query, optional, boolean)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/technologies`
- OperationId: `createTechnology`
- Auth: Bearer JWT
- Giai thich: Operation createTechnology.
- Params: Khong co.
- Body raw:
```json
{ "schema": "TechnologyRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/skills`
- OperationId: `listSkills`
- Auth: Public
- Giai thich: Operation listSkills.
- Params:
  - `activeOnly` (query, optional, boolean)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/skills`
- OperationId: `createSkill`
- Auth: Bearer JWT
- Giai thich: Operation createSkill.
- Params: Khong co.
- Body raw:
```json
{ "schema": "SkillRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/domains`
- OperationId: `listDomains`
- Auth: Public
- Giai thich: Operation listDomains.
- Params:
  - `activeOnly` (query, optional, boolean)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/domains`
- OperationId: `createDomain`
- Auth: Bearer JWT
- Giai thich: Operation createDomain.
- Params: Khong co.
- Body raw:
```json
{ "schema": "DomainRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/technologies/{technologyId}`
- OperationId: `updateTechnology`
- Auth: Bearer JWT
- Giai thich: Operation updateTechnology.
- Params:
  - `technologyId` (path, required, integer)
- Body raw:
```json
{ "schema": "TechnologyRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/technologies/{technologyId}`
- OperationId: `deleteTechnology`
- Auth: Bearer JWT
- Giai thich: Admin vo hieu hoa technology bang `isActive=false`, khong xoa vat ly.
- Params:
  - `technologyId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Technology duoc deactivate.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay technology.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/skills/{skillId}`
- OperationId: `updateSkill`
- Auth: Bearer JWT
- Giai thich: Operation updateSkill.
- Params:
  - `skillId` (path, required, integer)
- Body raw:
```json
{ "schema": "SkillRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/skills/{skillId}`
- OperationId: `deleteSkill`
- Auth: Bearer JWT
- Giai thich: Admin vo hieu hoa skill bang `isActive=false`, khong xoa vat ly.
- Params:
  - `skillId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Skill duoc deactivate.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay skill.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/domains/{domainId}`
- OperationId: `updateDomain`
- Auth: Bearer JWT
- Giai thich: Operation updateDomain.
- Params:
  - `domainId` (path, required, integer)
- Body raw:
```json
{ "schema": "DomainRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/domains/{domainId}`
- OperationId: `deleteDomain`
- Auth: Bearer JWT
- Giai thich: Admin vo hieu hoa domain bang `isActive=false`, khong xoa vat ly. Domain noi bo `PROFILE_REVIEW` van bi an voi user khong phai admin.
- Params:
  - `domainId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Domain duoc deactivate.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay domain.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## AI & Matching Flow

- Muc tieu flow: Chatbot va cac endpoint AI ho tro nghiep vu.

### POST `/api/chatbot/ask`
- OperationId: `ask`
- Auth: Public
- Giai thich: Operation ask.
- Params: Khong co.
- Body raw:
```json
{ "schema": "ChatRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## Admin & Governance Flow

- Muc tieu flow: Quan tri account, settings, analytics, reviews va wallet system.

### POST `/api/v1/admin/wallet/sync`
- OperationId: `syncSystemWallet`
- Auth: Bearer JWT
- Giai thich: Operation syncSystemWallet.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/staffs`
- OperationId: `listStaffs`
- Auth: Bearer JWT
- Giai thich: Operation listStaffs.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/staffs`
- OperationId: `createStaff`
- Auth: Bearer JWT
- Giai thich: Operation createStaff.
- Params: Khong co.
- Body raw:
```json
{ "schema": "StaffEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/reviews`
- OperationId: `createReview`
- Auth: Bearer JWT
- Giai thich: Operation createReview.
- Params: Khong co.
- Body raw:
```json
{ "schema": "ReviewEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/accounts`
- OperationId: `listAccounts`
- Auth: Bearer JWT
- Giai thich: Operation listAccounts.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/accounts`
- OperationId: `createAccount`
- Auth: Bearer JWT
- Giai thich: Operation createAccount.
- Params: Khong co.
- Body raw:
```json
{ "schema": "AccountRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/admin/staffs/{staffId}`
- OperationId: `updateStaff`
- Auth: Bearer JWT
- Giai thich: Operation updateStaff.
- Params:
  - `staffId` (path, required, integer)
- Body raw:
```json
{ "schema": "StaffEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/admin/settings/{key}`
- OperationId: `updateSetting`
- Auth: Bearer JWT
- Giai thich: Operation updateSetting.
- Params:
  - `key` (path, required, string)
  - `value` (query, optional, string)
  - `isActive` (query, optional, boolean)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/settings`
- OperationId: `createSetting`
- Auth: Bearer JWT
- Giai thich: Admin tao system setting moi. `valueType` hop le: `STRING`, `INT`, `DECIMAL`, `BOOLEAN`, `JSON`.
- Params: Khong co.
- Body raw:
```json
{
  "settingKey": "approval.sla_days",
  "settingValue": "3",
  "valueType": "INT",
  "description": "So ngay SLA xet duyet",
  "isActive": true
}
```
- Ma phan hoi thuong gap:
  - `200`: Setting duoc tao.
  - `400`: Thieu key/value/valueType, sai valueType, hoac key da ton tai.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PUT `/api/v1/admin/settings/{key}`
- OperationId: `updateSettingBody`
- Auth: Bearer JWT
- Giai thich: Admin cap nhat setting bang request body; dung khi can doi value, valueType, description hoac isActive.
- Params:
  - `key` (path, required, string)
- Body raw:
```json
{
  "settingValue": "3",
  "valueType": "INT",
  "description": "So ngay SLA xet duyet",
  "isActive": true
}
```
- Ma phan hoi thuong gap:
  - `200`: Setting duoc cap nhat.
  - `400`: Sai valueType hoac body khong hop le.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay setting.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/admin/settings/{key}`
- OperationId: `deleteSetting`
- Auth: Bearer JWT
- Giai thich: Admin vo hieu hoa setting bang `isActive=false`, khong xoa vat ly.
- Params:
  - `key` (path, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Setting duoc deactivate.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay setting.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/membership/packages`
- OperationId: `listMembershipPackagesForAdmin`
- Auth: Bearer JWT
- Giai thich: Admin xem tat ca package membership; them `activeOnly=true` de chi lay package dang active.
- Params:
  - `activeOnly` (query, optional, boolean)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve danh sach package.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### POST `/api/v1/admin/membership/packages`
- OperationId: `createMembershipPackage`
- Auth: Bearer JWT
- Giai thich: Admin tao package membership cho `BUSINESS` hoac `EXPERT`.
- Params: Khong co.
- Body raw:
```json
{
  "roleType": "BUSINESS",
  "packageCode": "BUSINESS_STARTER",
  "packageName": "Business Starter",
  "price": 99000,
  "badgeDurationDays": 30,
  "jobPostQuota": 5,
  "proposalQuota": 0,
  "recommendVisibility": false,
  "isActive": true
}
```
- Ma phan hoi thuong gap:
  - `200`: Package duoc tao.
  - `400`: Thieu truong bat buoc, role/price/quota khong hop le, hoac packageCode da ton tai.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/admin/membership/packages/{packageId}`
- OperationId: `updateMembershipPackage`
- Auth: Bearer JWT
- Giai thich: Admin cap nhat tung phan package membership.
- Params:
  - `packageId` (path, required, integer)
- Body raw:
```json
{
  "packageName": "Business Starter Plus",
  "price": 149000,
  "jobPostQuota": 9,
  "isActive": true
}
```
- Ma phan hoi thuong gap:
  - `200`: Package duoc cap nhat.
  - `400`: Du lieu khong hop le hoac packageCode trung.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay package.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/admin/membership/packages/{packageId}`
- OperationId: `deleteMembershipPackage`
- Auth: Bearer JWT
- Giai thich: Admin vo hieu hoa package bang `isActive=false`, cac lich su mua goi cu van duoc giu.
- Params:
  - `packageId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Package duoc deactivate.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `404`: Khong tim thay package.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### DELETE `/api/v1/admin/accounts/{accountId}`
- OperationId: `deactivateAccount`
- Auth: Bearer JWT
- Giai thich: Operation deactivateAccount.
- Params:
  - `accountId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/admin/accounts/{accountId}`
- OperationId: `updateAccount`
- Auth: Bearer JWT
- Giai thich: Operation updateAccount.
- Params:
  - `accountId` (path, required, integer)
- Body raw:
```json
{ "schema": "AccountRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/admin/accounts/{accountId}/status`
- OperationId: `setAccountStatus`
- Auth: Bearer JWT
- Giai thich: Operation setAccountStatus.
- Params:
  - `accountId` (path, required, integer)
  - `status` (query, required, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### PATCH `/api/v1/admin/accounts/{accountId}/active`
- OperationId: `setAccountActive`
- Auth: Bearer JWT
- Giai thich: Operation setAccountActive.
- Params:
  - `accountId` (path, required, integer)
  - `active` (query, required, boolean)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/wallet`
- OperationId: `systemWallet`
- Auth: Bearer JWT
- Giai thich: Operation systemWallet.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/wallet/transactions`
- OperationId: `platformWalletTransactions`
- Auth: Bearer JWT
- Giai thich: Compatibility alias for platform-wide user activity wallet history. Use this when Admin wants to review user-originated wallet actions across the platform, not the platform wallet's own ledger.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/wallet/platform-ledger`
- OperationId: `platformWalletLedger`
- Auth: Bearer JWT
- Giai thich: Admin reads only the platform/Admin wallet ledger rows, including platform balance-changing rows such as platform revenue credits.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/wallet/user-activity-transactions`
- OperationId: `platformUserActivityTransactions`
- Auth: Bearer JWT
- Giai thich: Admin reads platform-wide user activity wallet history, grouped/filtered so internal transfer legs do not appear as duplicate business events.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/settings`
- OperationId: `listSettings`
- Auth: Bearer JWT
- Giai thich: Operation listSettings.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/reviews/contracts/{contractId}`
- OperationId: `listReviewsByContract`
- Auth: Bearer JWT
- Giai thich: Operation listReviewsByContract.
- Params:
  - `contractId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/audit-logs`
- OperationId: `listAuditLogs`
- Auth: Bearer JWT
- Giai thich: Operation listAuditLogs.
- Params:
  - `actorGroup` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/analytics/overview`
- OperationId: `analyticsOverview`
- Auth: Bearer JWT
- Giai thich: Operation analyticsOverview.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/disputes`
- OperationId: `listDisputes_1`
- Auth: Bearer JWT
- Giai thich: Operation listDisputes_1.
- Params:
  - `page` (query, optional, integer)
  - `size` (query, optional, integer)
  - `status` (query, optional, string)
  - `assignedStaffId` (query, optional, integer)
  - `from` (query, optional, string)
  - `to` (query, optional, string)
  - `q` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/disputes/{disputeId}`
- OperationId: `getDisputeDetail`
- Auth: Bearer JWT
- Giai thich: Operation getDisputeDetail.
- Params:
  - `disputeId` (path, required, integer)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/summary`
- OperationId: `summary`
- Auth: Bearer JWT
- Giai thich: Tra ve card tong quan cho admin dashboard: user, profile backlog, job/proposal, contract, dispute, membership, wallet va withdrawal.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardSummaryResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/revenue`
- OperationId: `revenue`
- Auth: Bearer JWT
- Giai thich: Tra ve series doanh thu/gross movement theo ky va breakdown theo transaction type.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
  - `groupBy` (query, optional, `day|week|month`)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardSeriesResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/contracts`
- OperationId: `contracts`
- Auth: Bearer JWT
- Giai thich: Tra ve contract status breakdown va trend hop dong tao moi.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
  - `groupBy` (query, optional, `day|week|month`)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardContractsResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/users`
- OperationId: `users`
- Auth: Bearer JWT
- Giai thich: Tra ve user role/status breakdown, pending profile reviews va trend user moi.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
  - `groupBy` (query, optional, `day|week|month`)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardUsersResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/jobs-proposals`
- OperationId: `jobsProposals`
- Auth: Bearer JWT
- Giai thich: Tra ve funnel job/proposal, acceptance rate va trend job/proposal tao moi.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
  - `groupBy` (query, optional, `day|week|month`)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardJobsProposalsResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/disputes`
- OperationId: `disputes`
- Auth: Bearer JWT
- Giai thich: Tra ve dispute status breakdown, open/resolved count, overdue SLA count va trend dispute tao moi.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
  - `groupBy` (query, optional, `day|week|month`)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardDisputesResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/membership`
- OperationId: `membership`
- Auth: Bearer JWT
- Giai thich: Tra ve membership purchase trend, total revenue va package breakdown.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
  - `groupBy` (query, optional, `day|week|month`)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardMembershipResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/v1/admin/dashboard/finance-breakdown`
- OperationId: `financeBreakdown`
- Auth: Bearer JWT
- Giai thich: Tra ve so du vi he thong, gross transaction volume, withdrawal totals va transaction/withdrawal breakdown.
- Params:
  - `from` (query, optional, date)
  - `to` (query, optional, date)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Tra ve `DashboardFinanceBreakdownResponse`.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

## System & Test Flow

- Muc tieu flow: Health check va endpoint test ky thuat.

### GET `/api/test/secure`
- OperationId: `secure`
- Auth: Bearer JWT
- Giai thich: Operation secure.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

### GET `/api/health`
- OperationId: `health`
- Auth: Public
- Giai thich: Operation health.
- Params: Khong co.
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.
## US-068 Editable Marketplace And Contract Change APIs

### PUT `/api/v1/proposals/{proposalId}`
- Auth: Bearer JWT Expert.
- Use when the owning Expert updates a `Pending` or `Accepted` proposal before any contract exists.
- Body:
```json
{
  "technicalSolution": "Updated architecture and delivery approach",
  "proposalDescription": "Updated implementation plan",
  "bidAmount": 15000000,
  "proposalFileUrl": "proposal-files/experts/5/revised.pdf"
}
```

### POST `/api/v1/jobs/{jobId}/milestones`
- Auth: Bearer JWT Business.
- Preferred job-scoped alias for milestone creation.

### PATCH `/api/v1/jobs/{jobId}/milestones/{milestoneId}`
- Auth: Bearer JWT Business.
- Preferred job-scoped alias for milestone update.

### POST `/api/v1/contracts/{contractId}/change-requests`
- Auth: Bearer JWT Business or Expert participant.
- Body:
```json
{
  "changeType": "GENERAL",
  "changeSummary": "Add deployment handover",
  "proposedBudget": 18000000,
  "proposedTimelineDays": 21,
  "proposedScope": "Include deployment handover and documentation",
  "proposedMilestones": []
}
```

### GET `/api/v1/contracts/{contractId}/change-requests`
- Auth: Bearer JWT participant/Admin/Staff.

### POST `/api/v1/contracts/{contractId}/change-requests/{requestId}/accept`
- Auth: Bearer JWT counterparty only.
- Body:
```json
{ "reviewNote": "Accepted" }
```

### POST `/api/v1/contracts/{contractId}/change-requests/{requestId}/reject`
- Auth: Bearer JWT counterparty only.
- Body:
```json
{ "reviewNote": "Rejected because scope is not aligned" }
```

### Preferred Contract-Scoped Milestone Aliases
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deliverables`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/source-code-file`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/approve`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/reject`
- `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/disputes`

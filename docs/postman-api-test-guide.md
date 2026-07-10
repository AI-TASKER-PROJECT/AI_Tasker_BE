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

## Milestone Dispute Smoke Flow

1. Business/Expert sign contract and NDA, then Business pays contract deposit.
2. Business deposits milestone escrow with `POST /api/v1/contracts/{contractId}/milestones/{milestoneId}/deposit`.
3. Expert starts milestone, submits progress reports if needed, then submits deliverable.
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
- Giai thich: Generate SoW
- Params: Khong co.
- Body raw:
```json
{ "schema": "GenerateSowRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

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
- Giai thich: Operation startMilestone.
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
- Giai thich: Operation rejectMilestone.
- Params:
  - `milestoneId` (path, required, integer)
  - `reason` (query, optional, string)
- Body raw: Khong co.
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
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
- Giai thich: Operation submitMilestoneDeliverable.
- Params:
  - `milestoneId` (path, required, integer)
- Body raw:
```json
{ "schema": "DeliverableEntity" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
  - `500`: Loi he thong hoac du lieu nen bat thuong; doi chieu log backend.

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

### POST `/api/v1/disputes/{disputeId}/reject-intervention`
- OperationId: `rejectInterventionAlias`
- Auth: Bearer JWT
- Giai thich: Operation rejectInterventionAlias.
- Params:
  - `disputeId` (path, required, integer)
  - `reason` (query, optional, string)
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

### POST `/api/v1/disputes/{disputeId}/assign-staff`
- OperationId: `assignDisputeStaff`
- Auth: Bearer JWT
- Giai thich: Operation assignDisputeStaff.
- Params:
  - `disputeId` (path, required, integer)
  - `staffId` (query, required, integer)
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
{ "schema": "ProgressReportRequest" }
```
- Ma phan hoi thuong gap:
  - `200`: Thanh cong theo message/schema tren Swagger.
  - `400`: Validation loi hoac vi pham business rule/state transition.
  - `401`/`403`: Sai token, het han token, sai role, ownership hoac participant/operator guard.
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

### POST `/api/v1/admin/contracts/{contractId}/deposit/refund`
- OperationId: `refundContractDeposit`
- Auth: Bearer JWT
- Giai thich: Operation refundContractDeposit.
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
- Giai thich: Operation platformWalletTransactions.
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

# CHECKLIST TEST THU CONG BANG POSTMAN (AITASKER-BE)

## 1) CHUAN BI MOI TRUONG
1. CHAY DATABASE:
```bash
docker compose up -d
```
2. CHAY BACKEND:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```
3. BASE URL:
- `http://localhost:8080`
4. XAC NHAN HEALTH:
- `GET /api/health` -> `200`

## 2) CAU HINH POSTMAN
1. TAO ENVIRONMENT `AITASKER_LOCAL`:
- `base_url = http://localhost:8080`
- `business_token =`
- `expert_token =`
- `admin_token =`
- `staff_token =`
- `job_id =`
- `proposal_id =`
- `contract_id =`
- `milestone_id =`
- `transaction_id =`
- `dispute_id =`

2. CAU HINH AUTH HEADER CHUAN:
- KEY: `Authorization`
- VALUE: `Bearer {{business_token}}` (HOAC token role khac tuong ung)

## 3) BO REQUEST THEO FOLDER (GOI Y)
1. `00-HEALTH`
2. `01-AUTH`
3. `02-PROFILE`
4. `03-MARKETPLACE`
5. `04-CONTRACT-EXECUTION`
6. `05-FINANCE-DISPUTE`
7. `06-ADMIN`
8. `07-NEGATIVE-SECURITY`
 
## 4) CHECKLIST CHI TIET THEO LUONG

## 4.1 AUTH + RBAC
1. REGISTER BUSINESS  
- `POST {{base_url}}/api/auth/register`  
- BODY:
```json
{
  "email": "biz_manual_001@mail.com",
  "password": "12345678",
  "fullName": "BUSINESS MANUAL",
  "phone": "0900000001",
  "role": "BUSINESS"
}
```
- EXPECT: `200`, CO `data.accessToken`, `data.role=BUSINESS`

2. REGISTER EXPERT / ADMIN / STAFF (LAM TUONG TU, DOI `role`)

3. LOGIN BUSINESS  
- `POST {{base_url}}/api/auth/login`  
- BODY:
```json
{
  "email": "biz_manual_001@mail.com",
  "password": "12345678"
}
```
- EXPECT: `200`, CO `accessToken`
- GAN VAO ENV `business_token`

4. LOGIN CAC ROLE CON LAI, GAN `expert_token/admin_token/staff_token`

## 4.2 PROFILE (KYC/KYB + PORTFOLIO)
1. BUSINESS UPSERT PROFILE  
- `POST {{base_url}}/api/v1/profiles/business`  
- AUTH: `{{business_token}}`  
- BODY:
```json
{
  "taxCode": "0312345678",
  "companyName": "AITASKER BIZ",
  "address": "HCM",
  "businessLicenseUrl": "https://example.com/license.pdf"
}
```
- EXPECT: `200`, `kybStatus=Pending`

2. EXPERT UPSERT PROFILE  
- `POST {{base_url}}/api/v1/profiles/expert`  
- AUTH: `{{expert_token}}`

3. EXPERT UPSERT PORTFOLIO  
- `POST {{base_url}}/api/v1/profiles/portfolio`  
- AUTH: `{{expert_token}}`  
- BODY:
```json
{
  "context": "Bai toan OCR hoa don",
  "dataProcessing": "Lam sach + chuan hoa du lieu",
  "modelArchitecture": "Transformer OCR",
  "performanceMetrics": "F1=0.93",
  "pocUrl": "https://example.com/poc"
}
```

4. ADMIN/STAFF DUYET HO SO  
- `POST {{base_url}}/api/v1/profiles/approve/BUSINESS/{id}?status=Approved`  
- `POST {{base_url}}/api/v1/profiles/approve/EXPERT/{id}?status=Approved`

## 4.3 MARKETPLACE
1. BUSINESS TAO JOB  
- `POST {{base_url}}/api/v1/jobs`  
- AUTH: `{{business_token}}`  
- BODY:
```json
{
  "title": "XAY DUNG CHATBOT CSKH",
  "rawRequirements": "CAN CHATBOT TU VAN SAN PHAM",
  "budget": 5000,
  "status": "OPEN"
}
```
- EXPECT: `200`, LAY `jobId` -> ENV `job_id`

2. EXPERT NOP PROPOSAL  
- `POST {{base_url}}/api/v1/proposals`  
- AUTH: `{{expert_token}}`  
- BODY:
```json
{
  "jobId": {{job_id}},
  "technicalSolution": "RAG + VECTOR DB",
  "bidAmount": 4500
}
```
- EXPECT: `200`, LAY `proposalId` -> ENV `proposal_id`

3. BUSINESS REVIEW PROPOSAL  
- `PATCH {{base_url}}/api/v1/proposals/{{proposal_id}}/status?status=Accepted`  
- AUTH: `{{business_token}}`  
- EXPECT: `200`

## 4.4 CONTRACT + EXECUTION
1. TAO CONTRACT DRAFT TU PROPOSAL  
- `POST {{base_url}}/api/v1/contracts/from-proposals/{{proposal_id}}`  
- AUTH: `{{business_token}}`  
- BODY:
```json
{
  "technologyUsed": "Spring Boot, PostgreSQL",
  "totalBudget": 4500,
  "timelineDays": 30
}
```
- EXPECT: `200`, LAY `contractId` -> ENV `contract_id`

3. ACTIVATE CONTRACT  
- `POST {{base_url}}/api/v1/contracts/{{contract_id}}/activate`  
- AUTH: `{{business_token}}`

4. EXPERT KY NDA  
- `POST {{base_url}}/api/v1/contracts/{{contract_id}}/nda-sign`  
- AUTH: `{{expert_token}}`

5. TAO MILESTONE  
- `POST {{base_url}}/api/v1/milestones`  
- AUTH: `{{business_token}}`  
- BODY:
```json
{
  "contractId": {{contract_id}},
  "milestoneName": "PHASE 1",
  "fundsAllocated": 2000,
  "orderIndex": 1,
  "status": "Under Review"
}
```
- LAY `milestoneId` -> ENV `milestone_id`

6. TAO ACCEPTANCE CRITERIA  
- `POST {{base_url}}/api/v1/criteria`  
- AUTH: `{{business_token}}
```json
{
  "milestoneId": {{milestone_id}},
  "description": "MODEL DAT DO CHINH XAC TOI THIEU 92% TREN TAP KIEM THU",
  "isPassed": false
}
```

7. SUBMIT DELIVERABLE  
- `POST {{base_url}}/api/v1/deliverables`  
- AUTH: `{{expert_token}}`
```json
{
  "milestoneId": {{milestone_id}},
  "deliverableUrl": "https://example.com/deliverables/model-v1.zip",
  "deliveryNote": "BAN GIAO MODEL V1 + HUONG DAN RUN + TEST REPORT"
}
```

8. CHAY SLA AUTO APPROVE  
- `POST {{base_url}}/api/v1/milestones/sla-auto-approve`  
- AUTH: `{{admin_token}}` HOAC `{{staff_token}}`

9. CHAM DUT CONTRACT (NEU CAN TEST RSK-02)  
- `POST {{base_url}}/api/v1/contracts/{{contract_id}}/terminate?reason=CLIENT_STOP_PROJECT`  
- AUTH: `{{business_token}}` HOAC `{{admin_token}}`

## 4.5 FINANCE + DISPUTE
1. TAO TRANSACTION  
- `POST {{base_url}}/api/v1/transactions`  
- AUTH: `{{business_token}}`  
- BODY:
```json
{
  "milestoneId": {{milestone_id}},
  "amount": 2000,
  "commissionFee": 200,
  "transactionType": "Deposit",
  "status": "Pending"
}
```
- LAY `transactionId` -> ENV `transaction_id`

2. WEBHOOK PAYMENT (MO PHONG IPN)  
- `POST {{base_url}}/api/v1/transactions/{{transaction_id}}/webhook?paymentStatus=Success&bankTxCode=VNP123&receiptImgUrl=https://example.com/bill.jpg`  
- AUTH: `{{admin_token}}` HOAC `{{staff_token}}`

3. TAO INVOICE (NEU CHUA CO)  
- `POST {{base_url}}/api/v1/invoices`  
- AUTH: `{{business_token}}`
```json
{
  "transactionId": {{transaction_id}}
}
```

4. TAO DISPUTE  
- `POST {{base_url}}/api/v1/disputes`  
- AUTH: `{{business_token}}` HOAC `{{expert_token}}`
- LAY `disputeId` -> ENV `dispute_id`
```json
{
  "contractId": {{contract_id}},
  "disputeType": "QUALITY",
  "issueSummary": "KET QUA BAN GIAO KHONG DAT ACCEPTANCE CRITERIA",
  "evidenceReport": "DINH KEM LOG TEST, SCREENSHOT, VA BAO CAO DOI SOAT",
  "status": "Open"
}
```

5. ASSIGN DISPUTE  
- `PATCH {{base_url}}/api/v1/disputes/{{dispute_id}}/assign?staffId={staff_id}`  
- AUTH: `{{admin_token}}`

6. STAFF DEMO TESTING  
- `POST {{base_url}}/api/v1/disputes/{{dispute_id}}/demo-testing?testResult=KET_QUA_TEST_OK`  
- AUTH: `{{staff_token}}`

7. STAFF TECHNICAL REPORT  
- `POST {{base_url}}/api/v1/disputes/{{dispute_id}}/technical-report?reportContent=BAO_CAO_CHI_TIET&proposedAction=REFUND_PARTIAL`  
- AUTH: `{{staff_token}}`

8. ADMIN RESOLVE DISPUTE  
- `PATCH {{base_url}}/api/v1/disputes/{{dispute_id}}/resolve?proposedAction=REFUND_PARTIAL`  
- AUTH: `{{admin_token}}`

## 4.6 ADMIN
1. LIST SETTINGS  
- `GET {{base_url}}/api/v1/admin/settings`  
- AUTH: `{{admin_token}}` HOAC `{{staff_token}}`

2. UPDATE SETTING  
- `PATCH {{base_url}}/api/v1/admin/settings/default_sla_days?value=7&isActive=true`  
- AUTH: `{{admin_token}}`

3. CREATE STAFF  
- `POST {{base_url}}/api/v1/admin/staffs`  
- AUTH: `{{admin_token}}`
```json
{
  "accountId": 123,
  "specialization": "EDUCATIONAL"
}
```

4. ANALYTICS OVERVIEW  
- `GET {{base_url}}/api/v1/admin/analytics/overview`  
- AUTH: `{{admin_token}}` HOAC `{{staff_token}}`

## 5) NEGATIVE CHECKLIST BAT BUOC
1. KHONG TOKEN GOI API PRIVATE -> `401`
2. ROLE SAI GOI API ADMIN -> BI CHAN (`401/403` THEO FLOW HIEN TAI)
3. DUPLICATE PROPOSAL CUNG `jobId/expert` -> LOI CONFLICT
4. `terminate` KHONG CO `reason` -> LOI VALIDATION NGHIEP VU
5. `nda-sign` KHI CONTRACT CHUA `Active` -> LOI NGHIEP VU
6. `webhook` VOI `paymentStatus` KHONG HOP LE -> LOI NGHIEP VU

## 6) KET QUA DAT (PASS CRITERIA)
1. TOAN BO API TRA VE JSON KHUNG `ApiResponse`.
2. TOKEN THEO ROLE CHAY DUNG LUONG.
3. DU LIEU DUOC LUU DAY DU CHO JOB -> PROPOSAL -> CONTRACT -> MILESTONE -> TRANSACTION -> DISPUTE.
4. OPENAPI/README/TEST GUIDE KHOP ENDPOINT THUC TE.
5. TEST TU DONG `./mvnw -q test` PASS.

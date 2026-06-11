# BAO CAO DOI CHIEU 23 BUSINESS RULES (WEEK 1-8)

## 1) Tong quan
- Nguon doi chieu: `SWP391_Group1_BR_DB_Updated.xlsx` (sheet `Business Rules New`).
- Trang thai hien tai: backend da co khung va API chinh cho phan lon rule tuan 1-8.
- Ghi chu: mot so rule can thanh phan ngoai backend core (AI service, scheduler, payment gateway, storage) dang o muc MVP/mock.
 
## 2) Bang coverage
| Rule ID | Trang thai | Ghi chu |
|---|---|---|
| REG-01 | DONE (MVP) | Register theo role + email unique + bcrypt. |
| REG-02 | DONE (MVP) | Ho so BUSINESS/EXPERT + Pending/Approved/Rejected flow. |
| AUTH-01 | DONE | Login/JWT stateless. |
| AUTH-02 | DONE | JWT filter + role-based authorization. |
| PRF-01 | DONE (MVP) | Portfolio 4 thanh phan + POC URL. |
| JOB-01 | DONE (MVP) | Tao job + truong `structured_sow`/`ai_tag`; chua tich hop AI NLP service that. |
| MATCH-01 | DONE (MVP) | API matching keyword cho tab de xuat. |
| MATCH-02 | DONE | Nop proposal + chan duplicate theo job/expert. |
| CON-01 | DONE (MVP) | Tao draft, request change, activate contract. |
| CON-02 | DONE (MVP) | Da co endpoint expert ky NDA tren contract active (`/contracts/{id}/nda-sign`). |
| EXEC-01 | DONE | Milestone + Acceptance Criteria. |
| EXEC-02 | DONE (MVP) | Da co endpoint chay tac vu SLA auto-approve milestone (`/milestones/sla-auto-approve`). |
| FIN-01 | DONE (MVP) | Da co endpoint webhook mo phong cap nhat giao dich + doi soat invoice/bill URL. |
| RSK-01 | DONE (MVP) | Create/assign/resolve dispute flow core. |
| RSK-02 | DONE (MVP) | Da co endpoint cham dut contract (`/contracts/{id}/terminate`). |
| REV-01 | DONE (MVP) | Mutual review sau contract ket thuc. |
| ADM-01 | DONE (MVP) | Master CRUD cho profile/staff/settings + audit profile approval. |
| ADM-02 | DONE (MVP) | Da co endpoint tong hop chi so `/admin/analytics/overview`. |
| ADM-03 | DONE | List/update system settings. |
| STF-01 | DONE (MVP) | Create/list staff profile. |
| STF-02 | DONE (MVP) | Da co auto-assign staff cho dispute khi bat setting `auto_assign_staff_enabled`. |
| STF-03 | DONE (MVP) | Da co endpoint staff ghi ket qua demo testing dispute (`/disputes/{id}/demo-testing`). |
| STF-04 | DONE (MVP) | Da co endpoint staff ban hanh technical report (`/disputes/{id}/technical-report`). |

## 3) Ket luan
- Backend week 1-8 da dat muc chay duoc end-to-end cho cac flow cot loi: auth, profile, marketplace, contract execution, dispute, review, settings.
- 23/23 business rule da duoc cover o muc MVP backend (co endpoint/logic/test co ban).

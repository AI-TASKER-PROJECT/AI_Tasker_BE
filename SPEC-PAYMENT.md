# Payment & Wallet Specification — AI Tasker MVP

## 1. Document Purpose

This specification defines the payment and wallet flow for the AI Tasker platform.

The main goals are:

| Goal                           | Description                                                                                     |
| ------------------------------ | ----------------------------------------------------------------------------------------------- |
| Wallet top-up                  | Business and Expert users can top up their internal wallet through PayOS QR code.               |
| Internal wallet usage          | After money enters the wallet, all platform purchases and payments use internal wallet balance. |
| Membership and credit purchase | Business and Expert users can buy membership packages and usage credits.                        |
| Job and proposal quota         | Business uses job-post credits; Expert uses proposal credits.                                   |
| Contract security deposit      | Business pays a 20% security deposit to activate a contract.                                    |
| Deposit refund                 | Admin manually decides and confirms the deposit refund amount.                                  |
| Withdrawal                     | Business and Expert users can submit withdrawal requests for Admin review.                      |
| Ledger consistency             | All real balance movements must be recorded in `wallet_transactions`.                           |

---

## 2. Scope

| Feature                               | Status                | Note                                                                                                                   |
| ------------------------------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| PayOS wallet top-up                   | In scope              | PayOS is only used for wallet top-up.                                                                                  |
| Wallet balance management             | In scope              | available, escrow, holding, disputed balances.                                                                         |
| Business membership                   | In scope              | Standard, Plus, Premium.                                                                                               |
| Expert membership                     | In scope              | Standard, Plus, Premium.                                                                                               |
| Business job-post credits             | In scope              | Package credits and single-credit purchase.                                                                            |
| Expert proposal credits               | In scope              | Initial free credits, package credits, and single-credit purchase.                                                     |
| AI SoW job publishing                 | In scope              | Business must use AI-generated SoW before publishing a job.                                                            |
| AI expert recommendation visibility   | In scope              | Visible only for active Business Premium.                                                                              |
| Contract security deposit             | In scope              | 20% deposit to activate contract.                                                                                      |
| Admin deposit refund                  | In scope              | Admin can choose refund amount.                                                                                        |
| Withdrawal request                    | In scope              | Manual Admin approval, no automatic bank transfer.                                                                     |
| Milestone payment ledger              | Partial / Later phase | Existing `transactions` can remain for business workflow. Real balance movement must go through `wallet_transactions`. |
| Hot job package                       | Out of scope          | Will be handled later.                                                                                                 |
| Dispute payment flow                  | Out of scope          | Dispute has a separate flow.                                                                                           |
| Automatic bank transfer               | Out of scope          | Admin transfers money manually outside the system.                                                                     |
| Platform commission on project budget | Out of scope          | Current project payment commission is not included.                                                                    |

---

## 3. Core Design Decision

| Table                 | Purpose                                                        |
| --------------------- | -------------------------------------------------------------- |
| `transactions`        | Stores high-level milestone/contract payment workflow records. |
| `wallet_transactions` | Stores the real wallet ledger and actual balance movements.    |

`transactions` and `wallet_transactions` must not be merged.

`transactions` answers:

```text
What is this business payment event for?
Example: milestone deposit, payout, refund workflow.
```

`wallet_transactions` answers:

```text
How did the wallet balance actually change?
Example: available balance decreased, escrow increased, holding released.
```

All real money movement must go through `WalletLedgerService`.

---

## 4. Wallet Model

### 4.1 Wallet Types

| Wallet Type           | Owner                 | Description                                                                              |
| --------------------- | --------------------- | ---------------------------------------------------------------------------------------- |
| `BUSINESS_WALLET`     | Business account      | Wallet for Business payments, top-up, deposit, and withdrawal.                           |
| `EXPERT_WALLET`       | Expert account        | Wallet for Expert package purchase, proposal credits, received payments, and withdrawal. |
| `ADMIN_SYSTEM_WALLET` | Platform/Admin system | Tracks platform-held funds and revenue. This is not a personal Admin wallet.             |

### 4.2 Wallet Rules

| Rule ID | Rule                                                                                    |
| ------- | --------------------------------------------------------------------------------------- |
| WAL-001 | One account has exactly one role.                                                       |
| WAL-002 | One account has one main wallet based on its role.                                      |
| WAL-003 | Business and Expert wallets are separate by role.                                       |
| WAL-004 | Wallet balance must never become negative.                                              |
| WAL-005 | All wallet changes must be executed inside a database transaction.                      |
| WAL-006 | Services must not update wallet balances directly. They must use `WalletLedgerService`. |

### 4.3 Balance Meaning

| Balance            | Meaning                                                                  | Can user spend/withdraw directly? |
| ------------------ | ------------------------------------------------------------------------ | --------------------------------- |
| `currentBalance`   | Total wallet balance snapshot                                            | No                                |
| `availableBalance` | Usable money for purchases, deposits, milestone payments, and withdrawal | Yes                               |
| `escrowBalance`    | Money locked for contract deposit or milestone escrow                    | No                                |
| `holdingBalance`   | Money locked while waiting for withdrawal review                         | No                                |
| `disputedBalance`  | Money locked due to dispute                                              | No                                |
| `totalRevenue`     | Revenue tracking for system wallet                                       | No                                |

---

## 5. PayOS Top-up

### 5.1 Business Rules

| Rule ID   | Rule                                                                                  |
| --------- | ------------------------------------------------------------------------------------- |
| TOPUP-001 | PayOS is only used for wallet top-up.                                                 |
| TOPUP-002 | Minimum top-up amount is 2,000 VND.                                                   |
| TOPUP-003 | There is no platform-side maximum top-up limit.                                       |
| TOPUP-004 | Wallet must not be credited when a PayOS order is only created.                       |
| TOPUP-005 | Wallet is credited only after PayOS confirms `PAID`.                                  |
| TOPUP-006 | Backend must validate the paid amount matches the `payment_order.amount`.             |
| TOPUP-007 | Top-up processing must be idempotent. One payment order must not credit wallet twice. |

### 5.2 User Story

| ID           | User Story                                                                                                                |
| ------------ | ------------------------------------------------------------------------------------------------------------------------- |
| US-PAYOS-001 | As a Business or Expert user, I want to top up my wallet using PayOS QR code so that I can use money inside the platform. |

### 5.3 Acceptance Criteria

| AC ID        | Acceptance Criteria                                                  |
| ------------ | -------------------------------------------------------------------- |
| AC-PAYOS-001 | User can create a PayOS top-up order.                                |
| AC-PAYOS-002 | System rejects top-up amount below 2,000 VND.                        |
| AC-PAYOS-003 | System creates `payment_order` with status `PENDING`.                |
| AC-PAYOS-004 | System returns `checkoutUrl`, `qrCode`, and `orderCode`.             |
| AC-PAYOS-005 | When PayOS returns `PAID`, system updates `payment_order` to `PAID`. |
| AC-PAYOS-006 | System credits user wallet only once.                                |
| AC-PAYOS-007 | System creates `wallet_transactions` record with type `TOPUP`.       |

### 5.4 Flow

```text
User creates top-up request
→ Backend validates amount >= 2,000 VND
→ Backend creates payment_order PENDING
→ Backend calls PayOS and receives checkoutUrl, qrCode, orderCode
→ User pays through QR
→ PayOS redirects or backend syncs by orderCode
→ Backend calls PayOS get(orderCode)
→ If status = PAID:
   - validate amount
   - update payment_order = PAID
   - credit user availableBalance
   - record wallet_transactions TOPUP
→ If already processed:
   - do not credit wallet again
```

---

## 6. Membership Packages

### 6.1 Business Packages

| Package           | Benefits                                                                               |
| ----------------- | -------------------------------------------------------------------------------------- |
| Business Standard | Blue verified badge for 1 month. No job-post credits.                                  |
| Business Plus     | Blue verified badge for 2 months + 10 job-post credits.                                |
| Business Premium  | Blue verified badge for 3 months + 30 job-post credits + AI recommendation visibility. |

### 6.2 Expert Packages

| Package         | Benefits                                                 |
| --------------- | -------------------------------------------------------- |
| Expert Standard | Green verified badge for 1 month.                        |
| Expert Plus     | Green verified badge for 2 months + 30 proposal credits. |
| Expert Premium  | Green verified badge for 3 months + 90 proposal credits. |

### 6.3 Business Rules

| Rule ID | Rule                                                                                       |
| ------- | ------------------------------------------------------------------------------------------ |
| MEM-001 | Membership package prices are configurable values.                                         |
| MEM-002 | Prices should be stored in DB/config, not hard-coded in service logic.                     |
| MEM-003 | Package purchase uses wallet availableBalance.                                             |
| MEM-004 | If balance is insufficient, system returns missing amount and top-up redirect information. |
| MEM-005 | Membership duration is accumulated.                                                        |
| MEM-006 | Unused quota does not expire.                                                              |
| MEM-007 | Business can only buy Business packages.                                                   |
| MEM-008 | Expert can only buy Expert packages.                                                       |

### 6.4 User Stories

| ID         | User Story                                                                                                          | Role     |
| ---------- | ------------------------------------------------------------------------------------------------------------------- | -------- |
| US-MEM-001 | As a Business, I want to buy a membership package so that I can receive a verified badge and job-post credits.      | Business |
| US-MEM-002 | As an Expert, I want to buy a membership package so that I can receive a green verified badge and proposal credits. | Expert   |

### 6.5 Acceptance Criteria

| AC ID      | Acceptance Criteria                                                   |
| ---------- | --------------------------------------------------------------------- |
| AC-MEM-001 | User can view available packages for their role.                      |
| AC-MEM-002 | User cannot buy packages for another role.                            |
| AC-MEM-003 | System checks wallet availableBalance before purchase.                |
| AC-MEM-004 | If sufficient, system debits user wallet.                             |
| AC-MEM-005 | System records purchase history.                                      |
| AC-MEM-006 | System updates badge expiration.                                      |
| AC-MEM-007 | System grants package quota if applicable.                            |
| AC-MEM-008 | System creates `wallet_transactions` and `quota_transaction` records. |

---

## 7. Credit Purchase

### 7.1 Credit Types

| Credit Type     | Role     |        Default Price | Expiration      |
| --------------- | -------- | -------------------: | --------------- |
| Job-post credit | Business | 100,000 VND / credit | Does not expire |
| Proposal credit | Expert   |  50,000 VND / credit | Does not expire |

### 7.2 Business Rules

| Rule ID    | Rule                                               |
| ---------- | -------------------------------------------------- |
| CREDIT-001 | Business can buy one or multiple job-post credits. |
| CREDIT-002 | Expert can buy one or multiple proposal credits.   |
| CREDIT-003 | Credit prices are configurable.                    |
| CREDIT-004 | Purchased credits do not expire.                   |
| CREDIT-005 | Credit purchase uses wallet availableBalance.      |

### 7.3 User Stories

| ID            | User Story                                                                        | Role     |
| ------------- | --------------------------------------------------------------------------------- | -------- |
| US-CREDIT-001 | As a Business, I want to buy job-post credits so that I can publish more jobs.    | Business |
| US-CREDIT-002 | As an Expert, I want to buy proposal credits so that I can submit more proposals. | Expert   |

### 7.4 Acceptance Criteria

| AC ID         | Acceptance Criteria                                        |
| ------------- | ---------------------------------------------------------- |
| AC-CREDIT-001 | Business can buy job-post credits using wallet balance.    |
| AC-CREDIT-002 | Expert can buy proposal credits using wallet balance.      |
| AC-CREDIT-003 | If balance is insufficient, system returns missing amount. |
| AC-CREDIT-004 | Successful purchase debits user wallet and adds quota.     |
| AC-CREDIT-005 | System records wallet transaction and quota transaction.   |

---

## 8. Job Publishing Quota

### 8.1 Business Rules

| Rule ID | Rule                                                                  |
| ------- | --------------------------------------------------------------------- |
| JOB-001 | Business must use AI-generated SoW before publishing a job.           |
| JOB-002 | Manual job publishing is not allowed.                                 |
| JOB-003 | Business can generate AI SoW multiple times in one job creation flow. |
| JOB-004 | AI generation does not consume job-post credit.                       |
| JOB-005 | One job-post credit is consumed only when job publish succeeds.       |
| JOB-006 | Business Standard includes no job-post credits.                       |
| JOB-007 | Job-post credits do not expire.                                       |

### 8.2 User Story

| ID         | User Story                                                                                                                         |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| US-JOB-001 | As a Business, I want to publish jobs through AI-generated SoW so that job content is standardized and ready for Expert proposals. |

### 8.3 Acceptance Criteria

| AC ID      | Acceptance Criteria                                                   |
| ---------- | --------------------------------------------------------------------- |
| AC-JOB-001 | Business can create or edit job draft without consuming quota.        |
| AC-JOB-002 | Business can call AI SoW generation multiple times before publishing. |
| AC-JOB-003 | System prevents publish if AI SoW has not been generated.             |
| AC-JOB-004 | System prevents publish if job-post quota is exhausted.               |
| AC-JOB-005 | System consumes 1 job-post credit only after successful publish.      |
| AC-JOB-006 | System creates quota transaction for quota consumption.               |

---

## 9. Proposal Quota

### 9.1 Business Rules

| Rule ID  | Rule                                                                                     |
| -------- | ---------------------------------------------------------------------------------------- |
| PROP-001 | Expert receives 3 initial proposal credits when account is created.                      |
| PROP-002 | The 3 initial proposal credits do not reset monthly.                                     |
| PROP-003 | Proposal quota is a common balance. Free and paid credits do not need separate balances. |
| PROP-004 | Successful proposal submission consumes 1 proposal credit.                               |
| PROP-005 | Proposal cannot be withdrawn.                                                            |
| PROP-006 | Rejected proposal does not refund credit.                                                |
| PROP-007 | Expert must not submit duplicate proposal to the same job.                               |

### 9.2 User Story

| ID          | User Story                                                                                      |
| ----------- | ----------------------------------------------------------------------------------------------- |
| US-PROP-001 | As an Expert, I want to submit proposals using my proposal credits so that I can apply to jobs. |

### 9.3 Acceptance Criteria

| AC ID       | Acceptance Criteria                                     |
| ----------- | ------------------------------------------------------- |
| AC-PROP-001 | New Expert account receives 3 initial proposal credits. |
| AC-PROP-002 | System checks proposal quota before submission.         |
| AC-PROP-003 | System prevents duplicate proposal to the same job.     |
| AC-PROP-004 | Successful submission consumes 1 proposal credit.       |
| AC-PROP-005 | Proposal cannot be withdrawn.                           |
| AC-PROP-006 | Rejected proposal does not refund credit.               |

---

## 10. AI Expert Recommendation Visibility

### 10.1 Business Rules

| Rule ID | Rule                                                                                                   |
| ------- | ------------------------------------------------------------------------------------------------------ |
| REC-001 | AI recommendations may be generated internally for all jobs.                                           |
| REC-002 | Only active Business Premium users can view recommendation results.                                    |
| REC-003 | When Premium expires, all recommendations are hidden.                                                  |
| REC-004 | When Premium is reactivated, recommendations become visible again.                                     |
| REC-005 | Recommendations for jobs created before Premium activation can also be visible when Premium is active. |
| REC-006 | Recommendation records should not be deleted when Premium expires.                                     |

### 10.2 User Story

| ID         | User Story                                                                                                                |
| ---------- | ------------------------------------------------------------------------------------------------------------------------- |
| US-REC-001 | As a Premium Business user, I want to view AI-recommended Experts so that I can quickly find suitable Experts for my job. |

### 10.3 Acceptance Criteria

| AC ID      | Acceptance Criteria                                           |
| ---------- | ------------------------------------------------------------- |
| AC-REC-001 | Premium Business can view recommendations.                    |
| AC-REC-002 | Non-Premium Business receives a premium-required response.    |
| AC-REC-003 | Expired Premium hides all recommendation results.             |
| AC-REC-004 | Reactivated Premium shows old and new recommendation results. |

---

## 11. Contract Status

### 11.1 Contract Status Values

| Status            | Meaning                                                                                                        |
| ----------------- | -------------------------------------------------------------------------------------------------------------- |
| `DRAFT`           | Business is creating the job/SoW. Not published yet.                                                           |
| `ACTIVE`          | Job/contract posting is active and visible. Experts can submit proposals. This replaces the old `OPEN` status. |
| `PROPOSAL_REVIEW` | Business is reviewing proposals or selecting an Expert.                                                        |
| `PENDING_DEPOSIT` | Business and Expert have confirmed the contract. Business must pay 20% security deposit.                       |
| `IN_PROGRESS`     | Security deposit has been paid. Project work is ongoing.                                                       |
| `COMPLETE`        | Work has been completed, but final Admin deposit handling may still be pending.                                |
| `CLOSED`          | Contract is fully closed after Admin completes final deposit refund/resolution.                                |
| `CANCELLED`       | Contract was cancelled. Deposit handling is decided by Admin.                                                  |

### 11.2 Contract Status Flow

```text
DRAFT
→ ACTIVE
→ PROPOSAL_REVIEW
→ PENDING_DEPOSIT
→ IN_PROGRESS
→ COMPLETE
→ CLOSED
```

Alternative ending:

```text
DRAFT / ACTIVE / PROPOSAL_REVIEW / PENDING_DEPOSIT / IN_PROGRESS / COMPLETE
→ CANCELLED
```

---

## 12. Contract Security Deposit

### 12.1 Business Rules

| Rule ID | Rule                                                                             |
| ------- | -------------------------------------------------------------------------------- |
| DEP-001 | Business must pay a 20% security deposit to activate the contract.               |
| DEP-002 | Deposit amount = total contract value * 20%.                                     |
| DEP-003 | Deposit is not a milestone payment.                                              |
| DEP-004 | Deposit is not paid to Expert.                                                   |
| DEP-005 | Deposit is not part of the 100% project budget paid to Expert.                   |
| DEP-006 | Deposit is held until Admin handles refund/resolution.                           |
| DEP-007 | If Business wallet is insufficient, system returns required top-up amount.       |
| DEP-008 | Contract moves from `PENDING_DEPOSIT` to `IN_PROGRESS` after successful deposit. |

### 12.2 User Story

| ID         | User Story                                                                                                                              |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| US-DEP-001 | As a Business, I want to pay a 20% security deposit after confirming a contract so that the contract becomes active and work can start. |

### 12.3 Acceptance Criteria

| AC ID      | Acceptance Criteria                                                                                 |
| ---------- | --------------------------------------------------------------------------------------------------- |
| AC-DEP-001 | Contract must be in `PENDING_DEPOSIT` before deposit payment.                                       |
| AC-DEP-002 | System calculates deposit as 20% of total contract value.                                           |
| AC-DEP-003 | System checks Business availableBalance.                                                            |
| AC-DEP-004 | If sufficient, Business availableBalance decreases and escrow/deposit balance increases.            |
| AC-DEP-005 | System creates `contract_deposit` record with status `HELD`.                                        |
| AC-DEP-006 | System creates `wallet_transactions` record with type `CONTRACT_SECURITY_DEPOSIT_HOLD`.             |
| AC-DEP-007 | Contract status becomes `IN_PROGRESS`.                                                              |
| AC-DEP-008 | If insufficient, system returns `currentBalance`, `requiredAmount`, `needTopup`, and `redirectUrl`. |

---

## 13. Admin Deposit Refund / Resolution

### 13.1 Business Rules

| Rule ID | Rule                                                                                                            |
| ------- | --------------------------------------------------------------------------------------------------------------- |
| REF-001 | Admin handles deposit refund when contract is `COMPLETE`, `CLOSED`, or `CANCELLED`, depending on business flow. |
| REF-002 | Admin can choose the refund amount.                                                                             |
| REF-003 | Refund amount must be greater than or equal to 0.                                                               |
| REF-004 | Refund amount must not exceed the held deposit amount.                                                          |
| REF-005 | If refund amount is less than held deposit, remaining amount is resolved by Admin decision.                     |
| REF-006 | Deposit refund is not automatic.                                                                                |
| REF-007 | Contract becomes `CLOSED` after final deposit handling is completed.                                            |

### 13.2 User Story

| ID         | User Story                                                                                                                          |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| US-REF-001 | As an Admin, I want to review and process a contract security deposit so that I can decide how much should be refunded to Business. |

### 13.3 Acceptance Criteria

| AC ID      | Acceptance Criteria                                                           |
| ---------- | ----------------------------------------------------------------------------- |
| AC-REF-001 | Admin can view held deposit for a contract.                                   |
| AC-REF-002 | Admin can enter `refundAmount` and `adminNote`.                               |
| AC-REF-003 | System rejects refundAmount below 0.                                          |
| AC-REF-004 | System rejects refundAmount greater than held deposit.                        |
| AC-REF-005 | If refundAmount > 0, system returns that amount to Business availableBalance. |
| AC-REF-006 | Remaining non-refunded amount is recorded as Admin-resolved amount.           |
| AC-REF-007 | System creates wallet transaction records.                                    |
| AC-REF-008 | Contract becomes `CLOSED` after Admin completes final deposit handling.       |

---

## 14. Withdrawal Request

### 14.1 Business Rules

| Rule ID | Rule                                                                                      |
| ------- | ----------------------------------------------------------------------------------------- |
| WDR-001 | Business and Expert can request withdrawal.                                               |
| WDR-002 | Withdrawal has no fee.                                                                    |
| WDR-003 | Withdrawal is not an automatic bank transfer.                                             |
| WDR-004 | User submits withdrawal request with bank information.                                    |
| WDR-005 | No separate bank code field is required.                                                  |
| WDR-006 | User can only withdraw availableBalance.                                                  |
| WDR-007 | User cannot withdraw escrowBalance, holdingBalance, or disputedBalance.                   |
| WDR-008 | When withdrawal request is created, amount moves from availableBalance to holdingBalance. |
| WDR-009 | Admin manually transfers money outside the system.                                        |
| WDR-010 | Admin clicks APPROVE after manual transfer.                                               |
| WDR-011 | Admin can reject the withdrawal request and return holdingBalance to availableBalance.    |

### 14.2 User Stories

| ID         | User Story                                                                                                                       | Role             |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------- | ---------------- |
| US-WDR-001 | As a Business or Expert, I want to submit a withdrawal request so that I can receive money in my bank account.                   | Business, Expert |
| US-WDR-002 | As an Admin, I want to approve a withdrawal request after manual transfer so that the system records money leaving the platform. | Admin            |
| US-WDR-003 | As an Admin, I want to reject an invalid withdrawal request so that the locked money is returned to the user wallet.             | Admin            |

### 14.3 Acceptance Criteria

| AC ID      | Acceptance Criteria                                                         |
| ---------- | --------------------------------------------------------------------------- |
| AC-WDR-001 | User can submit withdrawal request with amount and bank information.        |
| AC-WDR-002 | System checks amount > 0.                                                   |
| AC-WDR-003 | System checks amount <= availableBalance.                                   |
| AC-WDR-004 | If valid, system moves amount from availableBalance to holdingBalance.      |
| AC-WDR-005 | System creates `withdraw_request` with status `PENDING`.                    |
| AC-WDR-006 | Admin can approve only PENDING requests.                                    |
| AC-WDR-007 | On approval, user holdingBalance decreases.                                 |
| AC-WDR-008 | System wallet decreases to reflect money leaving platform.                  |
| AC-WDR-009 | Admin can reject only PENDING requests.                                     |
| AC-WDR-010 | On rejection, user holdingBalance decreases and availableBalance increases. |
| AC-WDR-011 | No withdrawal fee is charged.                                               |

---

## 15. Data Model Specification

### 15.1 `payment_order`

| Field                    | Suggested Type     | Description                               |
| ------------------------ | ------------------ | ----------------------------------------- |
| id                       | BIGSERIAL          | Primary key                               |
| account_id               | BIGINT             | Account that created the top-up order     |
| amount                   | BIGINT             | Top-up amount in VND                      |
| currency                 | VARCHAR            | VND                                       |
| purpose                  | VARCHAR            | `WALLET_TOPUP` for MVP                    |
| provider                 | VARCHAR            | PAYOS                                     |
| provider_order_code      | VARCHAR            | PayOS orderCode                           |
| provider_payment_link_id | VARCHAR            | PayOS payment link ID                     |
| checkout_url             | TEXT               | PayOS checkout URL                        |
| qr_code_url              | TEXT               | QR code URL or QR data                    |
| return_url               | TEXT               | Return URL                                |
| status                   | VARCHAR            | PENDING, PAID, EXPIRED, FAILED, CANCELLED |
| description              | TEXT               | Payment description                       |
| paid_at                  | TIMESTAMP nullable | Paid timestamp                            |
| created_at               | TIMESTAMP          | Created time                              |
| updated_at               | TIMESTAMP          | Updated time                              |

Important rule:

```text
For MVP, payment_order is only for external PayOS wallet top-up.
Internal purchases must use wallet ledger records, not PayOS payment orders.
```

---

### 15.2 `system_wallet`

| Field             | Suggested Type     | Description                                         |
| ----------------- | ------------------ | --------------------------------------------------- |
| system_wallet_id  | BIGSERIAL          | Primary key                                         |
| account_id        | BIGINT nullable    | User account ID; null for platform wallet           |
| role_id           | BIGINT nullable    | Role reference                                      |
| wallet_type       | VARCHAR            | BUSINESS_WALLET, EXPERT_WALLET, ADMIN_SYSTEM_WALLET |
| current_balance   | BIGINT             | Current total balance                               |
| available_balance | BIGINT             | Usable balance                                      |
| escrow_balance    | BIGINT             | Escrow/deposit balance                              |
| holding_balance   | BIGINT             | Withdrawal holding balance                          |
| dispute_balance   | BIGINT             | Dispute-locked balance                              |
| currency          | VARCHAR            | VND                                                 |
| last_synced_at    | TIMESTAMP nullable | Last sync timestamp                                 |
| created_at        | TIMESTAMP          | Created time                                        |
| updated_at        | TIMESTAMP          | Updated time                                        |

---

### 15.3 `wallet_transactions`

| Field            | Suggested Type  | Description                                                                                                            |
| ---------------- | --------------- | ---------------------------------------------------------------------------------------------------------------------- |
| id               | BIGSERIAL       | Primary key                                                                                                            |
| system_wallet_id | BIGINT          | Related wallet                                                                                                         |
| account_id       | BIGINT nullable | Related account                                                                                                        |
| payment_order_id | BIGINT nullable | Related PayOS top-up order                                                                                             |
| transaction_type | VARCHAR         | TOPUP, MEMBERSHIP_PURCHASE, CREDIT_PURCHASE, DEPOSIT_HOLD, REFUND, WITHDRAW_HOLD, WITHDRAW_APPROVED, WITHDRAW_REJECTED |
| direction        | VARCHAR         | CREDIT, DEBIT, HOLD, RELEASE                                                                                           |
| balance_type     | VARCHAR         | AVAILABLE, ESCROW, HOLDING, DISPUTE                                                                                    |
| amount           | BIGINT          | Amount in VND                                                                                                          |
| balance_before   | BIGINT          | Balance before transaction                                                                                             |
| balance_after    | BIGINT          | Balance after transaction                                                                                              |
| status           | VARCHAR         | PENDING, SUCCESS, FAILED, CANCELLED                                                                                    |
| reference_type   | VARCHAR         | PAYMENT_ORDER, MEMBERSHIP, CREDIT_PURCHASE, CONTRACT_DEPOSIT, WITHDRAW_REQUEST, MILESTONE                              |
| reference_id     | BIGINT nullable | Related business record ID                                                                                             |
| description      | TEXT            | Description                                                                                                            |
| created_at       | TIMESTAMP       | Created time                                                                                                           |

---

### 15.4 `transactions`

| Field            | Suggested Type  | Description              |
| ---------------- | --------------- | ------------------------ |
| transaction_id   | BIGSERIAL       | Primary key              |
| milestone_id     | BIGINT nullable | Related milestone        |
| amount           | DECIMAL(18,2)   | Business-level amount    |
| commission_fee   | DECIMAL(18,2)   | Commission fee if any    |
| transaction_type | VARCHAR         | DEPOSIT, PAYOUT, REFUND  |
| status           | VARCHAR         | PENDING, SUCCESS, FAILED |
| created_at       | TIMESTAMP       | Created time             |
| updated_at       | TIMESTAMP       | Updated time             |

Recommended usage:

```text
Use transactions for milestone/contract business workflow.
Use wallet_transactions for real wallet balance movement.
```

---

### 15.5 `membership_packages`

| Field                | Suggested Type | Description                                                                                      |
| -------------------- | -------------- | ------------------------------------------------------------------------------------------------ |
| package_id           | BIGSERIAL      | Primary key                                                                                      |
| role_type            | VARCHAR        | BUSINESS, EXPERT                                                                                 |
| package_code         | VARCHAR        | BUSINESS_STANDARD, BUSINESS_PLUS, BUSINESS_PREMIUM, EXPERT_STANDARD, EXPERT_PLUS, EXPERT_PREMIUM |
| package_name         | VARCHAR        | Display name                                                                                     |
| price                | BIGINT         | Configurable package price                                                                       |
| badge_duration_days  | INT            | 30, 60, 90                                                                                       |
| job_post_quota       | INT            | Business package quota                                                                           |
| proposal_quota       | INT            | Expert package quota                                                                             |
| recommend_visibility | BOOLEAN        | True for Business Premium                                                                        |
| is_active            | BOOLEAN        | Whether package is available                                                                     |
| created_at           | TIMESTAMP      | Created time                                                                                     |
| updated_at           | TIMESTAMP      | Updated time                                                                                     |

---

### 15.6 `membership_purchases`

| Field                 | Suggested Type  | Description                |
| --------------------- | --------------- | -------------------------- |
| purchase_id           | BIGSERIAL       | Primary key                |
| account_id            | BIGINT          | Buyer account              |
| package_id            | BIGINT          | Purchased package          |
| amount                | BIGINT          | Paid amount                |
| status                | VARCHAR         | SUCCESS, FAILED, CANCELLED |
| badge_start_at        | TIMESTAMP       | Badge start                |
| badge_end_at          | TIMESTAMP       | Badge expiration           |
| wallet_transaction_id | BIGINT nullable | Related wallet transaction |
| created_at            | TIMESTAMP       | Created time               |
| updated_at            | TIMESTAMP       | Updated time               |

---

### 15.7 `user_quotas`

| Field                  | Suggested Type     | Description                         |
| ---------------------- | ------------------ | ----------------------------------- |
| quota_id               | BIGSERIAL          | Primary key                         |
| account_id             | BIGINT             | User account                        |
| job_post_quota_balance | INT                | Remaining Business job-post credits |
| proposal_quota_balance | INT                | Remaining Expert proposal credits   |
| badge_expired_at       | TIMESTAMP nullable | Current badge expiration            |
| created_at             | TIMESTAMP          | Created time                        |
| updated_at             | TIMESTAMP          | Updated time                        |

Default values:

| Role     | job_post_quota_balance | proposal_quota_balance |
| -------- | ---------------------: | ---------------------: |
| Business |                      0 |                      0 |
| Expert   |                      0 |                      3 |

---

### 15.8 `quota_usage_logs`

| Field          | Suggested Type  | Description                                |
| -------------- | --------------- | ------------------------------------------ |
| quota_usage_id | BIGSERIAL       | Primary key                                |
| account_id     | BIGINT          | User account                               |
| quota_type     | VARCHAR         | JOB_POST, PROPOSAL                         |
| action_type    | VARCHAR         | GRANT, PURCHASE, CONSUME, ADJUST           |
| amount         | INT             | Quota change amount                        |
| balance_before | INT             | Balance before                             |
| balance_after  | INT             | Balance after                              |
| reference_type | VARCHAR         | MEMBERSHIP, CREDIT_PURCHASE, JOB, PROPOSAL |
| reference_id   | BIGINT nullable | Related record                             |
| created_at     | TIMESTAMP       | Created time                               |

---

### 15.9 `contract_deposits`

| Field                 | Suggested Type     | Description                                                |
| --------------------- | ------------------ | ---------------------------------------------------------- |
| deposit_id            | BIGSERIAL          | Primary key                                                |
| contract_id           | BIGINT             | Related contract                                           |
| business_id           | BIGINT             | Business account                                           |
| deposit_amount        | BIGINT             | 20% of total contract value                                |
| held_amount           | BIGINT             | Currently held amount                                      |
| refunded_amount       | BIGINT             | Refunded amount                                            |
| resolved_amount       | BIGINT             | Non-refunded/Admin-resolved amount                         |
| status                | VARCHAR            | UNPAID, HELD, PARTIALLY_REFUNDED, REFUNDED, ADMIN_RESOLVED |
| hold_transaction_id   | BIGINT nullable    | Wallet transaction for holding deposit                     |
| refund_transaction_id | BIGINT nullable    | Wallet transaction for refund                              |
| admin_id              | BIGINT nullable    | Admin who resolved deposit                                 |
| admin_note            | TEXT nullable      | Admin note                                                 |
| paid_at               | TIMESTAMP nullable | Deposit payment time                                       |
| refunded_at           | TIMESTAMP nullable | Refund time                                                |
| created_at            | TIMESTAMP          | Created time                                               |
| updated_at            | TIMESTAMP          | Updated time                                               |

---

### 15.10 `withdrawal_requests`

| Field               | Suggested Type     | Description                            |
| ------------------- | ------------------ | -------------------------------------- |
| withdrawal_id       | BIGSERIAL          | Primary key                            |
| account_id          | BIGINT             | Requesting user                        |
| wallet_id           | BIGINT             | User wallet                            |
| amount              | BIGINT             | Withdrawal amount                      |
| bank_name           | VARCHAR            | Bank name                              |
| bank_account_number | VARCHAR            | Bank account number                    |
| bank_account_holder | VARCHAR            | Bank account holder name               |
| status              | VARCHAR            | PENDING, APPROVED, REJECTED, CANCELLED |
| admin_id            | BIGINT nullable    | Reviewing Admin                        |
| admin_note          | TEXT nullable      | Admin note                             |
| requested_at        | TIMESTAMP          | Request time                           |
| reviewed_at         | TIMESTAMP nullable | Review time                            |
| created_at          | TIMESTAMP          | Created time                           |
| updated_at          | TIMESTAMP          | Updated time                           |

No `withdraw_fee` field is required.
No separate `bank_code` field is required.

---

## 16. Recommended APIs

### 16.1 PayOS APIs

| API                                    | Method | Role                 | Purpose                          |
| -------------------------------------- | ------ | -------------------- | -------------------------------- |
| `/api/payments/payos/create`           | POST   | Business, Expert     | Create PayOS wallet top-up order |
| `/api/payments/payos/return`           | GET    | Public redirect      | PayOS return endpoint            |
| `/api/payments/payos/{orderCode}/sync` | POST   | Authenticated/System | Sync PayOS payment status        |

---

### 16.2 Wallet APIs

| API                           | Method | Role             | Purpose                         |
| ----------------------------- | ------ | ---------------- | ------------------------------- |
| `/api/wallet/current`         | GET    | Business, Expert | View current wallet             |
| `/api/wallet/transactions`    | GET    | Business, Expert | View wallet transaction history |
| `/api/v1/admin/system-wallet` | GET    | Admin            | View system wallet              |

---

### 16.3 Membership and Credit APIs

| API                                             | Method | Role             | Purpose                     |
| ----------------------------------------------- | ------ | ---------------- | --------------------------- |
| `/api/membership/packages`                      | GET    | Business, Expert | View packages               |
| `/api/membership/packages/{packageId}/purchase` | POST   | Business, Expert | Purchase membership package |
| `/api/credits/job-post/purchase`                | POST   | Business         | Purchase job-post credits   |
| `/api/credits/proposal/purchase`                | POST   | Expert           | Purchase proposal credits   |
| `/api/users/me/quota`                           | GET    | Business, Expert | View current quota          |

---

### 16.4 Job and Proposal APIs

| API                                 | Method | Role     | Purpose                                          |
| ----------------------------------- | ------ | -------- | ------------------------------------------------ |
| `/api/jobs/{jobId}/publish`         | POST   | Business | Publish job and consume job-post credit          |
| `/api/proposals`                    | POST   | Expert   | Submit proposal and consume proposal credit      |
| `/api/jobs/{jobId}/recommendations` | GET    | Business | View AI expert recommendations if Premium active |

---

### 16.5 Contract APIs

| API                                                | Method | Role                    | Purpose                                   |
| -------------------------------------------------- | ------ | ----------------------- | ----------------------------------------- |
| `/api/contracts/{contractId}`                      | GET    | Business, Expert, Admin | View contract                             |
| `/api/contracts/{contractId}/confirm-business`     | POST   | Business                | Business confirms contract                |
| `/api/contracts/{contractId}/confirm-expert`       | POST   | Expert                  | Expert confirms contract                  |
| `/api/contracts/{contractId}/deposit/pay`          | POST   | Business                | Pay 20% security deposit                  |
| `/api/admin/contracts/{contractId}/deposit/refund` | POST   | Admin                   | Admin processes deposit refund/resolution |

---

### 16.6 Withdrawal APIs

| API                                                     | Method | Role             | Purpose                                      |
| ------------------------------------------------------- | ------ | ---------------- | -------------------------------------------- |
| `/api/withdrawal-requests`                              | POST   | Business, Expert | Create withdrawal request                    |
| `/api/withdrawal-requests`                              | GET    | Business, Expert | View own withdrawal requests                 |
| `/api/admin/withdrawal-requests`                        | GET    | Admin            | View withdrawal requests                     |
| `/api/admin/withdrawal-requests/{withdrawalId}/approve` | POST   | Admin            | Approve withdrawal after manual transfer     |
| `/api/admin/withdrawal-requests/{withdrawalId}/reject`  | POST   | Admin            | Reject withdrawal and return holding balance |

---

## 17. Error Codes

| Code                        | Meaning                                                |
| --------------------------- | ------------------------------------------------------ |
| `INVALID_TOPUP_AMOUNT`      | Top-up amount is below 2,000 VND.                      |
| `PAYMENT_ORDER_NOT_FOUND`   | Payment order does not exist.                          |
| `PAYMENT_AMOUNT_MISMATCH`   | PayOS paid amount does not match local order amount.   |
| `PAYMENT_ALREADY_PROCESSED` | Payment order has already credited wallet.             |
| `INSUFFICIENT_BALANCE`      | User availableBalance is not enough.                   |
| `INVALID_ROLE`              | User role is not allowed for this action.              |
| `PACKAGE_NOT_FOUND`         | Membership package does not exist.                     |
| `PACKAGE_INACTIVE`          | Membership package is inactive.                        |
| `QUOTA_EXHAUSTED`           | User does not have enough job/proposal quota.          |
| `DUPLICATE_PROPOSAL`        | Expert already submitted proposal to the same job.     |
| `CONTRACT_NOT_FOUND`        | Contract does not exist.                               |
| `CONTRACT_INVALID_STATUS`   | Contract status is invalid for the requested action.   |
| `DEPOSIT_ALREADY_HELD`      | Contract security deposit has already been paid.       |
| `INVALID_REFUND_AMOUNT`     | Refund amount is below 0 or greater than held deposit. |
| `WITHDRAWAL_NOT_FOUND`      | Withdrawal request does not exist.                     |
| `WITHDRAWAL_INVALID_STATUS` | Withdrawal request status is not valid for action.     |
| `WITHDRAWAL_AMOUNT_INVALID` | Withdrawal amount is invalid.                          |

---

## 18. Final Payment Flow Summary

| Step | Flow                                                                  |
| ---- | --------------------------------------------------------------------- |
| 1    | Business/Expert tops up wallet through PayOS QR.                      |
| 2    | PayOS confirms payment as PAID.                                       |
| 3    | Backend credits user wallet and records wallet ledger.                |
| 4    | Business/Expert buys membership packages using wallet balance.        |
| 5    | Business buys job-post credits if needed.                             |
| 6    | Expert buys proposal credits if needed.                               |
| 7    | Business creates job through AI-generated SoW.                        |
| 8    | Business publishes job and consumes 1 job-post credit.                |
| 9    | Expert submits proposal and consumes 1 proposal credit.               |
| 10   | Business reviews proposals and selects Expert.                        |
| 11   | Business and Expert confirm contract.                                 |
| 12   | Contract becomes `PENDING_DEPOSIT`.                                   |
| 13   | Business pays 20% security deposit.                                   |
| 14   | Contract becomes `IN_PROGRESS`.                                       |
| 15   | Work is completed and contract becomes `COMPLETE`.                    |
| 16   | Admin reviews deposit and chooses refund amount.                      |
| 17   | System refunds selected amount to Business wallet.                    |
| 18   | Contract becomes `CLOSED`.                                            |
| 19   | Business/Expert can create withdrawal request.                        |
| 20   | Withdrawal amount moves from availableBalance to holdingBalance.      |
| 21   | Admin manually transfers money outside the system.                    |
| 22   | Admin approves request and system records money leaving platform.     |
| 23   | If Admin rejects request, holdingBalance returns to availableBalance. |

---

## 19. Overall Acceptance Criteria

| Area                | Acceptance Criteria                                                                                  |
| ------------------- | ---------------------------------------------------------------------------------------------------- |
| PayOS top-up        | User can create top-up order; paid order credits wallet once; duplicate sync does not double-credit. |
| Wallet ledger       | Every real balance movement creates `wallet_transactions`.                                           |
| Membership          | Package purchase debits wallet and grants badge/quota.                                               |
| Credit purchase     | Business and Expert can buy credits using wallet balance.                                            |
| Job publish         | Business cannot publish without AI SoW and job-post credit.                                          |
| Proposal            | Expert cannot submit without proposal credit; duplicate proposal is blocked.                         |
| Recommendation      | Only active Premium Business can view recommendations.                                               |
| Contract deposit    | Business can pay 20% deposit only from `PENDING_DEPOSIT`.                                            |
| Contract progress   | Successful deposit changes contract to `IN_PROGRESS`.                                                |
| Contract completion | Work completion changes contract to `COMPLETE`.                                                      |
| Deposit refund      | Admin chooses refund amount and finalizes contract to `CLOSED`.                                      |
| Withdrawal          | Withdrawal request moves available to holding; approve releases holding; reject returns holding.     |
| Security            | User can only access own wallet; Admin-only APIs are protected.                                      |
| Consistency         | No negative balance; no duplicate payment processing; all ledger references are traceable.           |

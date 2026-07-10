# Design

## Domain Model

Payment MVP keeps `transactions` for legacy milestone/contract workflow records
and uses `wallet_transactions` for real balance movements.

New domain records:

- `membership_packages`: configurable Business/Expert packages.
- `membership_purchases`: purchase history and badge validity.
- `user_quotas`: per-account job-post/proposal quota and badge expiration.
- `quota_usage_logs`: grant, purchase, consume, and adjust evidence.
- `contract_deposits`: 20% business security deposit lifecycle.
- `withdrawal_requests`: manual withdrawal review records.

## Application Flow

1. PayOS top-up remains provider-sync based and credits available balance once.
2. Membership purchase debits wallet available balance, records purchase, extends
   badge expiration, grants role-specific quota, and extends
   `premium_expired_at` only for Premium packages.
3. Credit purchase debits wallet available balance and grants quota.
4. Publishing a job requires a saved SoW and one job-post credit, consumed only
   after successful status change to `OPEN`.
5. Submitting a proposal requires one proposal credit, consumed only after the
   proposal is saved.
6. Fully signed/NDA-signed contracts move to `PENDING`.
7. Paying the security deposit moves Business available balance to escrow,
   creates/updates `contract_deposits`, and activates contract execution.
8. Admin deposit finalization optionally returns part of held escrow to Business
   available balance and closes the contract.
9. Withdrawal request moves available balance to holding. Admin approval removes
   holding from the platform balance; rejection returns holding to available.

## Interface Contract

New API groups:

- `GET /api/membership/packages`
- `POST /api/membership/packages/{packageId}/purchase`
- `POST /api/credits/job-post/purchase`
- `POST /api/credits/proposal/purchase`
- `GET /api/users/me/quota`
- `POST /api/v1/jobs/{jobId}/publish`
- `POST /api/v1/contracts/{contractId}/deposit/pay`
- `POST /api/v1/admin/contracts/{contractId}/deposit/refund`
- `POST /api/v1/withdrawal-requests`
- `GET /api/v1/withdrawal-requests`
- `GET /api/v1/admin/withdrawal-requests`
- `POST /api/v1/admin/withdrawal-requests/{withdrawalId}/approve`
- `POST /api/v1/admin/withdrawal-requests/{withdrawalId}/reject`

Existing PayOS, wallet, marketplace, proposal, and recommendation APIs remain.

## Data Model

Additive Flyway migration only. Existing migrations are not edited. Wallet
transaction constraints are expanded to allow `HOLD`, `RELEASE`, `HOLDING`, and
`DISPUTE` while preserving current top-up rows.

## UI / Platform Impact

Frontend can keep the existing wallet top-up flow. New screens can use package,
credit, quota, deposit, and withdrawal endpoints. Non-Premium Business users get
a clear premium-required failure when reading saved recommendations. Frontend
state such as `aitasker_active_package` must not be treated as business truth;
`GET /api/users/me/quota` is authoritative for active package, quota, and
Premium permission data.

## Observability

Sensitive wallet, quota, deposit, and withdrawal operations record durable
database rows. Admin-facing changes also record audit events where the existing
audit service is available.

## Alternatives Considered

1. Merge `transactions` and `wallet_transactions`. Rejected because
   `SPEC-PAYMENT.md` explicitly separates business workflow records from real
   wallet balance movements.
2. Keep contract activation on signatures only. Rejected because the payment
   spec requires a 20% security deposit before work starts.

# US-046 KYB Tax Code Verification Hardening

## Goal

When a Business submits their KYB profile, the system automatically verifies the tax code (MST) via VietQR government API before allowing the profile to enter staff review. The system also auto-fills company information from the verified data and checks for duplicate tax codes.

## Scope

- Add `verified_representative` column to `business_profiles` table.
- Validate MST format (10 or 13 digits) at submission time.
- Check for duplicate MST across accounts.
- Call VietQR API during `upsertBusiness()` and auto-fill `companyName` / `address`.
- Store `verifiedRepresentative` from VietQR response.
- Expose `verifiedRepresentative` in profile responses for staff review.

## Non-goals

- Do not change staff approval/rejection logic.
- Do not migrate verified data for already-approved profiles.
- Do not change the public `GET /api/auth/tax-check/{mst}` endpoint.
- Do not apply to Expert KYC flow (Business KYB only).
- Do not auto-approve profiles when MST is verified (staff still reviews business license).

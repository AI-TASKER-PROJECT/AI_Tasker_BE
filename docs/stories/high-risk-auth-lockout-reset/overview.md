# Overview - Failed Login Lockout And Password Reset Flow

## Current Behavior

Backend hien co:

- `POST /api/auth/login`: dang nhap bang email/password. Su dung `passwordEncoder.matches()` de kiem tra mat khau. Chi tra ve `UnauthorizedException("Sai email hoac mat khau")` khi sai. Khong co gioi han so lan nhap sai.
- `POST /api/auth/register`: dang ky, yeu cau email da verify OTP.
- `EmailOtpService`: gui email qua `JavaMailSender` va luu OTP/verified marker trong Redis voi key prefix `EMAIL_OTP:<email>`.
- `account.status`: `Pending`, `Approved`, `Rejected`, `Lock`. Chi `Lock` bi chan dang nhap trong `buildAuthResponse()`.
- `AccountEntity`: co cac truong `email`, `password`, `status`, `emailVerified`, `fullName`, `phone`, `role_id`.
- Khong co tracking `failed_login_attempts`, `locked_until`, hay `lock_reason`.
- Khong co API quen mat khau/reset password.
- Khong co config `app.frontend-url`.

## Target Behavior

Sau khi implement:

1. **Khoa tam (Temporary Lockout)**: Sau 5 lan nhap sai mat khau lien tiep, account bi chan dang nhap trong 5 phut (`locked_until = now + 5 minutes`). Gui email canh bao.
2. **Khoa bao mat (Security Lock)**: Sau khi da bi khoa tam 1 lan va tiep tuc sai them 5 lan nua, account bi security lock (`status=Lock`, `lock_reason=TOO_MANY_FAILED_LOGIN_ATTEMPTS`). Gui email khoa bao mat.
3. **Quen mat khau**: `POST /api/auth/forgot-password` gui reset link qua email, khong tiet lo email co ton tai hay khong.
4. **Reset password**: `POST /api/auth/reset-password` nhan token va newPassword, hash mat khau moi, mo khoa account neu bi lock do sai password.
5. **Reset counter**: Dang nhap thanh cong reset `failed_login_attempts`, `lockout_count`, `locked_until`.

## Affected Users

- All roles (BUSINESS, EXPERT, ADMIN, STAFF) - login flow.
- Users who forget passwords or get locked out.

## Affected Product Docs

- `docs/product/` - will create/edit auth-related product docs.
- `docs/swagger-api-overview.md` - new endpoints.
- `README.md` - new API entries.

## Non-Goals

- Multi-factor authentication.
- CAPTCHA.
- Device fingerprinting.
- IP-based rate limiting.
- OAuth/Google password reset.
- Admin UI changes beyond fields needed to represent lock reason.
- Changing JWT token format.
- Changing existing registration OTP behavior unless code reuse requires private helper refactor.

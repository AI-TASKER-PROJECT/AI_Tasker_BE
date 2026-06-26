package com.aitasker.be.service.auth;

import com.aitasker.be.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public void sendTemporaryLockoutEmail(AccountEntity account) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(account.getEmail());
        message.setSubject("Canh bao dang nhap sai nhieu lan");
        message.setText(
                "Xin chao " + account.getFullName() + ",\n\n"
                        + "Chung toi phat hien nhieu lan dang nhap that bai vao tai khoan cua ban.\n"
                        + "Tai khoan cua ban da bi tam khoa trong 5 phut de bao ve an toan.\n\n"
                        + "Neu ban khong thuc hien cac lan dang nhap nay, vui long dat lai mat khau ngay lap tuc.\n\n"
                        + "Tran trong,\nAITASKER"
        );
        mailSender.send(message);
    }

    public void sendSecurityLockEmail(AccountEntity account, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(account.getEmail());
        message.setSubject("Tai khoan da bi khoa vi ly do bao mat");
        message.setText(
                "Xin chao " + account.getFullName() + ",\n\n"
                        + "Tai khoan cua ban da bi khoa vi ly do bao mat sau nhieu lan dang nhap sai mat khau.\n"
                        + "De mo khoa tai khoan, vui long dat lai mat khau qua link duoi day:\n\n"
                        + resetLink + "\n\n"
                        + "Link nay se het han sau 15 phut.\n\n"
                        + "Tran trong,\nAITASKER"
        );
        mailSender.send(message);
    }

    public void sendResetPasswordEmail(AccountEntity account, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(account.getEmail());
        message.setSubject("Dat lai mat khau AITASKER");
        message.setText(
                "Xin chao " + account.getFullName() + ",\n\n"
                        + "Ban da yeu cau dat lai mat khau cho tai khoan AITASKER.\n"
                        + "Vui long nhan vao link duoi day de dat lai mat khau:\n\n"
                        + resetLink + "\n\n"
                        + "Link nay se het han sau 15 phut.\n"
                        + "Neu ban khong yeu cau dat lai mat khau, vui long bo qua email nay.\n\n"
                        + "Tran trong,\nAITASKER"
        );
        mailSender.send(message);
    }

    public void sendPasswordChangedEmail(AccountEntity account) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(account.getEmail());
        message.setSubject("Mat khau da duoc thay doi");
        message.setText(
                "Xin chao " + account.getFullName() + ",\n\n"
                        + "Mat khau tai khoan AITASKER cua ban da duoc thay doi thanh cong.\n"
                        + "Neu ban khong thuc hien thay doi nay, vui long lien he Admin ngay lap tuc.\n\n"
                        + "Tran trong,\nAITASKER"
        );
        mailSender.send(message);
    }
}

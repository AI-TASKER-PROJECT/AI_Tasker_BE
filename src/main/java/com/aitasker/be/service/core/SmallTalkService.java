/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/SmallTalkService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.core;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
public class SmallTalkService {
    private static final Pattern GREETING_PATTERN = Pattern.compile(
            "^(hello|hi|hey|alo|xin chao|chao)(\\s+(ban|ad|admin|bot|chatbot|nhe|nha|a|oi))*$"
    );
    private static final Pattern THANKS_PATTERN = Pattern.compile(
            "^(cam on|thanks|thank you)(\\s+(ban|ad|admin|bot|chatbot|nhe|nha|nhieu|a))*$"
    );
    private static final Pattern GOODBYE_PATTERN = Pattern.compile(
            "^(tam biet|bye|goodbye)(\\s+(ban|ad|admin|bot|chatbot|nhe|nha|a))*$"
    );

    // Note: Ham `isSmallTalk` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public boolean isSmallTalk(String message) {
        return detectIntent(message).isPresent();
    }

    // Note: Ham `reply` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public String reply(String message) {
        return switch (detectIntent(message).orElse(SmallTalkIntent.GREETING)) {
            case GREETING -> "Xin chào! Mình có thể hỗ trợ bạn về đăng ký, đăng nhập, hồ sơ chuyên gia, đăng bài job, hợp đồng hoặc thanh toán trên AI Tasker.";
            case THANKS -> "Rất vui được hỗ trợ bạn. Bạn đang cần hỏi thêm phần nào của hệ thống AI Tasker không?";
            case GOODBYE -> "Tạm biệt! Khi cần hỗ trợ về AI Tasker, bạn cứ nhắn lại nhé.";
        };
    }

    // Note: Ham `detectIntent` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Optional<SmallTalkIntent> detectIntent(String message) {
        String normalizedMessage = normalize(message);
        if (normalizedMessage.isBlank()) {
            return Optional.empty();
        }

        if (GREETING_PATTERN.matcher(normalizedMessage).matches()) {
            return Optional.of(SmallTalkIntent.GREETING);
        }

        if (THANKS_PATTERN.matcher(normalizedMessage).matches()) {
            return Optional.of(SmallTalkIntent.THANKS);
        }

        if (GOODBYE_PATTERN.matcher(normalizedMessage).matches()) {
            return Optional.of(SmallTalkIntent.GOODBYE);
        }

        return Optional.empty();
    }

    // Note: Ham `normalize` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String normalize(String message) {
        if (message == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private enum SmallTalkIntent {
        GREETING,
        THANKS,
        GOODBYE
    }
}

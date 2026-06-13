package com.aitasker.be.service.core;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

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

    public boolean isSmallTalk(String message) {
        return detectIntent(message).isPresent();
    }

    public String reply(String message) {
        return switch (detectIntent(message).orElse(SmallTalkIntent.GREETING)) {
            case GREETING -> "Xin chào! Mình có thể hỗ trợ bạn về đăng ký, đăng nhập, hồ sơ chuyên gia, đăng bài job, hợp đồng hoặc thanh toán trên AI Tasker.";
            case THANKS -> "Rất vui được hỗ trợ bạn. Bạn đang cần hỏi thêm phần nào của hệ thống AI Tasker không?";
            case GOODBYE -> "Tạm biệt! Khi cần hỗ trợ về AI Tasker, bạn cứ nhắn lại nhé.";
        };
    }

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

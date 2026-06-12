package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.ChatRequest;
import com.aitasker.be.dto.core.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private static final String NO_CONTEXT_ANSWER = "Mình chưa tìm thấy thông tin phù hợp trong tài liệu hệ thống. Bạn có thể mô tả chi tiết hơn để mình hỗ trợ chính xác hơn không?";

    private final RagRetrievalService ragRetrievalService;
    private final AiCompletionService aiCompletionService;
    private final SmallTalkService smallTalkService;

    public ChatResponse ask(ChatRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new AppException("Câu hỏi không được để trống");
        }

        String question = request.getQuestion().trim();
        if (smallTalkService.isSmallTalk(question)) {
            return ChatResponse.builder()
                    .answer(smallTalkService.reply(question))
                    .sources(List.of())
                    .build();
        }

        Map<String, String> contexts = ragRetrievalService.retrieveRelevantContext(question);
        String answer = contexts.isEmpty()
                ? NO_CONTEXT_ANSWER
                : aiCompletionService.generateAnswer(question, contexts);

        return ChatResponse.builder()
                .answer(answer)
                .sources(new ArrayList<>(contexts.keySet()))
                .build();
    }
}

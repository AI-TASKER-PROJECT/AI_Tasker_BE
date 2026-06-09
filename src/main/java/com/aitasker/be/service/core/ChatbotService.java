package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.ChatRequest;
import com.aitasker.be.dto.core.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private static final String NO_CONTEXT_ANSWER = "Hiện tại tôi chưa có đủ thông tin để trả lời câu hỏi này.";

    private final RagRetrievalService ragRetrievalService;
    private final AiCompletionService aiCompletionService;

    public ChatResponse ask(ChatRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new AppException("Cau hoi khong duoc de trong");
        }

        String question = request.getQuestion().trim();
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

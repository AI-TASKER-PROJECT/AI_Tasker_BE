package com.aitasker.be.service.core;

import com.aitasker.be.dto.core.ChatRequest;
import com.aitasker.be.dto.core.ChatResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChatbotServiceTest {

    @Test
    void ask_whenSmallTalk_shouldReplyNaturallyWithoutRag() {
        RagRetrievalService ragRetrievalService = mock(RagRetrievalService.class);
        AiCompletionService aiCompletionService = mock(AiCompletionService.class);
        ChatbotService service = new ChatbotService(
                ragRetrievalService,
                aiCompletionService,
                new SmallTalkService()
        );

        ChatResponse response = service.ask(new ChatRequest("hello"));

        assertTrue(response.getAnswer().contains("Xin chào"));
        assertFalse(response.getAnswer().contains("Hiện tại tôi chưa có đủ thông tin"));
        assertTrue(response.getSources().isEmpty());
        verifyNoInteractions(ragRetrievalService, aiCompletionService);
    }

    @Test
    void ask_whenNoContext_shouldReturnSoftFallbackWithoutCallingAi() {
        RagRetrievalService ragRetrievalService = mock(RagRetrievalService.class);
        AiCompletionService aiCompletionService = mock(AiCompletionService.class);
        ChatbotService service = new ChatbotService(
                ragRetrievalService,
                aiCompletionService,
                new SmallTalkService()
        );
        String question = "Chính sách ngoài hệ thống là gì?";
        when(ragRetrievalService.retrieveRelevantContext(question)).thenReturn(Map.of());

        ChatResponse response = service.ask(new ChatRequest(question));

        assertTrue(response.getAnswer().contains("Mình chưa tìm thấy thông tin phù hợp"));
        assertTrue(response.getSources().isEmpty());
        verifyNoInteractions(aiCompletionService);
    }

    @Test
    void ask_whenContextExists_shouldCallAiWithContextAndReturnSources() {
        RagRetrievalService ragRetrievalService = mock(RagRetrievalService.class);
        AiCompletionService aiCompletionService = mock(AiCompletionService.class);
        ChatbotService service = new ChatbotService(
                ragRetrievalService,
                aiCompletionService,
                new SmallTalkService()
        );
        String question = "Tôi quên mật khẩu thì làm sao?";
        Map<String, String> contexts = new LinkedHashMap<>();
        contexts.put("auth.md - Đăng nhập & Tài khoản", "Chọn chức năng Quên mật khẩu.");
        when(ragRetrievalService.retrieveRelevantContext(question)).thenReturn(contexts);
        when(aiCompletionService.generateAnswer(question, contexts)).thenReturn("Bạn hãy chọn chức năng Quên mật khẩu.");

        ChatResponse response = service.ask(new ChatRequest(question));

        assertEquals("Bạn hãy chọn chức năng Quên mật khẩu.", response.getAnswer());
        assertEquals("auth.md - Đăng nhập & Tài khoản", response.getSources().get(0));
        verify(aiCompletionService).generateAnswer(question, contexts);
    }
}

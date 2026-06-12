package com.aitasker.be.service.core;

import com.aitasker.be.config.RagProperties;
import com.aitasker.be.repository.KnowledgeChunkJdbcRepository;
import com.aitasker.be.repository.KnowledgeChunkJdbcRepository.KnowledgeChunkSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagRetrievalServiceTest {
    private EmbeddingService embeddingService;
    private KnowledgeChunkJdbcRepository knowledgeChunkRepository;
    private RagRetrievalService ragRetrievalService;

    @BeforeEach
    void setUp() {
        RagProperties ragProperties = new RagProperties();
        ragProperties.setTopK(5);
        ragProperties.setSearchThreshold(0.35);

        embeddingService = mock(EmbeddingService.class);
        knowledgeChunkRepository = mock(KnowledgeChunkJdbcRepository.class);
        ragRetrievalService = new RagRetrievalService(embeddingService, knowledgeChunkRepository, ragProperties);
    }

    @Test
    void retrieveRelevantContext_shouldSearchByEmbeddingAndReturnAuthChunk() {
        String question = "Tôi quên mật khẩu thì làm sao?";
        when(embeddingService.embed(question)).thenReturn(List.of(0.1, 0.2, 0.3));
        when(knowledgeChunkRepository.search("[0.1,0.2,0.3]", 5, 0.35))
                .thenReturn(List.of(new KnowledgeChunkSearchResult(
                        1L,
                        "auth.md",
                        "Đăng nhập & Tài khoản",
                        "Chọn chức năng Quên mật khẩu và làm theo hướng dẫn qua email.",
                        0.12
                )));

        Map<String, String> contexts = ragRetrievalService.retrieveRelevantContext(question);

        assertEquals(
                "Chọn chức năng Quên mật khẩu và làm theo hướng dẫn qua email.",
                contexts.get("auth.md - Đăng nhập & Tài khoản")
        );
        verify(embeddingService).embed(question);
        verify(knowledgeChunkRepository).search("[0.1,0.2,0.3]", 5, 0.35);
    }

    @Test
    void retrieveRelevantContext_whenNoChunkMatches_shouldReturnEmptyMap() {
        String question = "Một câu hỏi ngoài phạm vi";
        when(embeddingService.embed(question)).thenReturn(List.of(0.1, 0.2, 0.3));
        when(knowledgeChunkRepository.search("[0.1,0.2,0.3]", 5, 0.35)).thenReturn(List.of());

        Map<String, String> contexts = ragRetrievalService.retrieveRelevantContext(question);

        assertTrue(contexts.isEmpty());
    }
}

package com.aitasker.be.service.core;

import com.aitasker.be.config.RagProperties;
import com.aitasker.be.repository.KnowledgeChunkJdbcRepository;
import com.aitasker.be.repository.KnowledgeChunkJdbcRepository.KnowledgeChunkSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalService {
    private static final double MAX_DISTANCE_FROM_BEST_MATCH = 0.12;

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkJdbcRepository knowledgeChunkRepository;
    private final RagProperties ragProperties;

    public Map<String, String> retrieveRelevantContext(String question) {
        if (question == null || question.isBlank()) {
            return Map.of();
        }

        String queryEmbedding = EmbeddingService.toPgVector(embeddingService.embed(question));
        List<KnowledgeChunkSearchResult> chunks = knowledgeChunkRepository.search(
                queryEmbedding,
                Math.max(1, ragProperties.getTopK()),
                ragProperties.getSearchThreshold()
        );

        Map<String, String> contexts = new LinkedHashMap<>();
        if (chunks.isEmpty()) {
            return contexts;
        }

        double maxAcceptedDistance = chunks.get(0).distance() + MAX_DISTANCE_FROM_BEST_MATCH;
        for (KnowledgeChunkSearchResult chunk : chunks) {
            if (chunk.distance() > maxAcceptedDistance) {
                continue;
            }

            log.info(
                    "RAG chunk selected source_file={} section_title={} distance={}",
                    chunk.sourceFile(),
                    chunk.sectionTitle(),
                    chunk.distance()
            );
            contexts.put(buildContextKey(chunk), chunk.content());
        }
        return contexts;
    }

    private String buildContextKey(KnowledgeChunkSearchResult chunk) {
        if (chunk.sectionTitle() == null || chunk.sectionTitle().isBlank()) {
            return chunk.sourceFile();
        }
        return chunk.sourceFile() + " - " + chunk.sectionTitle();
    }
}

package com.aitasker.be.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeChunkJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public KnowledgeChunkJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertChunk(String sourceFile, String sectionTitle, String content, String embedding) {
        jdbcTemplate.update("""
                        INSERT INTO knowledge_chunks (source_file, section_title, content, embedding)
                        VALUES (?, ?, ?, CAST(? AS vector))
                        """,
                sourceFile,
                sectionTitle,
                content,
                embedding
        );
    }

    public void deleteBySourceFile(String sourceFile) {
        jdbcTemplate.update("DELETE FROM knowledge_chunks WHERE source_file = ?", sourceFile);
    }

    public List<KnowledgeChunkSearchResult> search(String embedding, int limit, double threshold) {
        return jdbcTemplate.query("""
                        SELECT id, source_file, section_title, content, distance
                        FROM (
                            SELECT id,
                                   source_file,
                                   section_title,
                                   content,
                                   embedding <=> CAST(? AS vector) AS distance
                            FROM knowledge_chunks
                            WHERE embedding IS NOT NULL
                        ) ranked_chunks
                        WHERE distance <= ?
                        ORDER BY distance
                        LIMIT ?
                        """,
                (rs, rowNum) -> new KnowledgeChunkSearchResult(
                        rs.getLong("id"),
                        rs.getString("source_file"),
                        rs.getString("section_title"),
                        rs.getString("content"),
                        rs.getDouble("distance")
                ),
                embedding,
                threshold,
                limit
        );
    }

    public record KnowledgeChunkSearchResult(
            Long id,
            String sourceFile,
            String sectionTitle,
            String content,
            double distance
    ) {
    }
}

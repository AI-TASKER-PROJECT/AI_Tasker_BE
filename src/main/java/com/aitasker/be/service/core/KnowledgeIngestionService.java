package com.aitasker.be.service.core;

import com.aitasker.be.config.RagProperties;
import com.aitasker.be.repository.KnowledgeChunkJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestionService {
    private static final String KNOWLEDGE_PATTERN = "classpath*:knowledge/chatbot/*.md";
    private static final int MAX_CHUNK_CHARS = 1_200;
    private static final int MIN_SPLIT_CHARS = 800;
    private static final int SECTION_TITLE_MAX_LENGTH = 255;

    private final KnowledgeChunkJdbcRepository knowledgeChunkRepository;
    private final EmbeddingService embeddingService;
    private final RagProperties ragProperties;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ingestOnStartup() {
        if (!ragProperties.isIngestOnStartup()) {
            log.info("RAG knowledge ingestion on startup is disabled");
            return;
        }

        ingestAll();
    }

    @Transactional
    public void ingestAll() {
        Resource[] resources = loadKnowledgeResources();
        if (resources.length == 0) {
            log.warn("No chatbot knowledge markdown files found at {}", KNOWLEDGE_PATTERN);
            return;
        }

        for (Resource resource : resources) {
            ingestResource(resource);
        }
    }

    private Resource[] loadKnowledgeResources() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(KNOWLEDGE_PATTERN);
            Arrays.sort(resources, Comparator.comparing(resource -> safeFilename(resource).toLowerCase()));
            return resources;
        } catch (IOException ex) {
            throw new IllegalStateException("Khong doc duoc danh sach tai lieu chatbot", ex);
        }
    }

    private void ingestResource(Resource resource) {
        String sourceFile = safeFilename(resource);
        String markdown = readResource(resource);
        List<PreparedChunk> chunks = splitMarkdown(sourceFile, markdown);

        knowledgeChunkRepository.deleteBySourceFile(sourceFile);

        for (PreparedChunk chunk : chunks) {
            String embedding = EmbeddingService.toPgVector(embeddingService.embed(chunk.embeddingText()));
            knowledgeChunkRepository.insertChunk(
                    sourceFile,
                    truncate(chunk.sectionTitle(), SECTION_TITLE_MAX_LENGTH),
                    chunk.content(),
                    embedding
            );
        }

        log.info("Imported {} RAG chunks from {}", chunks.size(), sourceFile);
    }

    private String readResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Khong doc duoc tai lieu chatbot: " + safeFilename(resource), ex);
        }
    }

    private List<PreparedChunk> splitMarkdown(String sourceFile, String markdown) {
        List<PreparedChunk> chunks = new ArrayList<>();
        for (MarkdownSection section : splitByHeading(sourceFile, markdown)) {
            List<String> contentParts = splitLongContent(section.content());
            for (int index = 0; index < contentParts.size(); index++) {
                String sectionTitle = contentParts.size() == 1
                        ? section.title()
                        : section.title() + " (part " + (index + 1) + ")";
                String content = contentParts.get(index).trim();
                if (!content.isBlank()) {
                    chunks.add(new PreparedChunk(sectionTitle, content, sectionTitle + "\n\n" + content));
                }
            }
        }
        return chunks;
    }

    private List<MarkdownSection> splitByHeading(String sourceFile, String markdown) {
        List<MarkdownSection> sections = new ArrayList<>();
        String fallbackTitle = sourceFile;
        String currentTitle = fallbackTitle;
        StringBuilder currentContent = new StringBuilder();

        for (String line : markdown.split("\\R", -1)) {
            if (line.startsWith("## ")) {
                addSection(sections, currentTitle, currentContent);
                currentTitle = cleanHeading(line.substring(3), fallbackTitle);
                currentContent = new StringBuilder();
                continue;
            }

            if (line.startsWith("# ") && currentContent.isEmpty() && fallbackTitle.equals(currentTitle)) {
                currentTitle = cleanHeading(line.substring(2), fallbackTitle);
                continue;
            }

            currentContent.append(line).append('\n');
        }

        addSection(sections, currentTitle, currentContent);
        return sections;
    }

    private void addSection(List<MarkdownSection> sections, String title, StringBuilder content) {
        String sectionContent = content.toString().trim();
        if (!sectionContent.isBlank()) {
            sections.add(new MarkdownSection(title, sectionContent));
        }
    }

    private List<String> splitLongContent(String content) {
        if (content.length() <= MAX_CHUNK_CHARS) {
            return List.of(content);
        }

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        String[] paragraphs = content.split("(\\R\\s*\\R)+");

        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isBlank()) {
                continue;
            }

            if (trimmedParagraph.length() > MAX_CHUNK_CHARS) {
                flushChunk(chunks, currentChunk);
                chunks.addAll(splitOversizedParagraph(trimmedParagraph));
                continue;
            }

            int nextLength = currentChunk.isEmpty()
                    ? trimmedParagraph.length()
                    : currentChunk.length() + 2 + trimmedParagraph.length();

            if (nextLength > MAX_CHUNK_CHARS) {
                flushChunk(chunks, currentChunk);
            }

            if (!currentChunk.isEmpty()) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmedParagraph);
        }

        flushChunk(chunks, currentChunk);
        return chunks;
    }

    private List<String> splitOversizedParagraph(String paragraph) {
        List<String> chunks = new ArrayList<>();
        String remaining = paragraph.trim();
        while (remaining.length() > MAX_CHUNK_CHARS) {
            int splitAt = findSplitPoint(remaining);
            chunks.add(remaining.substring(0, splitAt).trim());
            remaining = remaining.substring(splitAt).trim();
        }
        if (!remaining.isBlank()) {
            chunks.add(remaining);
        }
        return chunks;
    }

    private int findSplitPoint(String value) {
        int max = Math.min(MAX_CHUNK_CHARS, value.length());
        for (int index = max - 1; index >= MIN_SPLIT_CHARS; index--) {
            if (Character.isWhitespace(value.charAt(index))) {
                return index + 1;
            }
        }

        for (int index = max - 1; index > 0; index--) {
            if (Character.isWhitespace(value.charAt(index))) {
                return index + 1;
            }
        }
        return max;
    }

    private void flushChunk(List<String> chunks, StringBuilder currentChunk) {
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
            currentChunk.setLength(0);
        }
    }

    private String cleanHeading(String heading, String fallbackTitle) {
        String cleanedHeading = heading.replace("#", "").trim();
        return cleanedHeading.isBlank() ? fallbackTitle : cleanedHeading;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim();
    }

    private String safeFilename(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalStateException("Tai lieu chatbot khong co ten file hop le");
        }
        return filename;
    }

    private record MarkdownSection(String title, String content) {
    }

    private record PreparedChunk(String sectionTitle, String content, String embeddingText) {
    }
}

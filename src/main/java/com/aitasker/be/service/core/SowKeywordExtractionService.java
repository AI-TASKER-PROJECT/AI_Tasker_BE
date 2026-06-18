package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SowKeywordExtractionService {
    private static final List<KeywordRule> SKILL_RULES = List.of(
            rule("AI", "artificial intelligence", "tro ly ai", "trợ lý ai"),
            rule("Chatbot", "bot", "tro ly ao", "trợ lý ảo"),
            rule("RAG / Knowledge Base", "rag", "knowledge base", "co so du lieu kien thuc", "cơ sở dữ liệu kiến thức", "kb"),
            rule("API Integration", "api", "api integration", "tich hop", "tích hợp", "integration", "tich hop api"),
            rule("Spring Boot", "spring boot", "java spring", "java spring boot"),
            rule("NLP", "natural language processing", "xu ly ngon ngu", "xử lý ngôn ngữ", "xu ly ngon ngu tu nhien", "xử lý ngôn ngữ tự nhiên"),
            rule("Testing", "kiem thu", "kiểm thử", "testing", "test", "qa"),
            rule("Deployment", "trien khai", "triển khai", "deployment", "deploy", "devops", "ci cd", "ci/cd")
    );

    private static final List<KeywordRule> DOMAIN_RULES = List.of(
            rule("Customer Support", "cham soc khach hang", "chăm sóc khách hàng", "customer support", "customer service", "ho tro khach hang", "hỗ trợ khách hàng"),
            rule("E-commerce", "e commerce", "e-commerce", "ecommerce", "thuong mai dien tu", "thương mại điện tử", "ban hang", "bán hàng", "retail"),
            rule("CRM", "customer relationship management"),
            rule("Order Management", "don hang", "đơn hàng", "order", "order management")
    );

    private static final Map<String, List<String>> SKILL_ALIASES = aliasesByLabel(SKILL_RULES);
    private static final Map<String, List<String>> DOMAIN_ALIASES = aliasesByLabel(DOMAIN_RULES);
    private static final Map<String, List<String>> ALL_ALIASES = allAliases();

    private final ObjectMapper objectMapper;

    public SowKeywordExtractionService() {
        this(new ObjectMapper());
    }

    SowKeywordExtractionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SowKeywordExtractionResult extractKeywordsFromSow(String sowJson) {
        List<String> textChunks = extractTextChunks(sowJson);
        String text = String.join(" ", textChunks);

        List<String> skills = matchRules(text, SKILL_RULES);
        List<String> domains = matchRules(text, DOMAIN_RULES);
        List<String> keywords = combineKeywords(skills, domains);

        return SowKeywordExtractionResult.builder()
                .skills(skills)
                .domains(domains)
                .keywords(keywords)
                .build();
    }

    public Map<String, List<String>> skillAliasesByKeyword() {
        return SKILL_ALIASES;
    }

    public Map<String, List<String>> domainAliasesByKeyword() {
        return DOMAIN_ALIASES;
    }

    public List<String> aliasesForKeyword(String keyword) {
        return ALL_ALIASES.getOrDefault(keyword, List.of(keyword));
    }

    public List<String> aliasesForKeywords(Collection<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        for (String keyword : keywords) {
            aliases.addAll(aliasesForKeyword(keyword));
        }
        return List.copyOf(aliases);
    }

    public boolean textContainsKeyword(String text, String keyword) {
        if (text == null || text.isBlank() || keyword == null || keyword.isBlank()) {
            return false;
        }
        return containsAnyAlias(normalizeForMatching(text), aliasesForKeyword(keyword));
    }

    public long countKeywordsInText(String text, Collection<String> keywords) {
        if (text == null || text.isBlank() || keywords == null || keywords.isEmpty()) {
            return 0;
        }
        return keywords.stream()
                .filter(keyword -> textContainsKeyword(text, keyword))
                .count();
    }

    public String normalizeForMatching(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String lower = value.toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .replace('Đ', 'D');
        String withoutAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<String> extractTextChunks(String sowJson) {
        if (sowJson == null || sowJson.isBlank()) {
            return List.of();
        }

        String payload = extractJsonPayload(sowJson);
        try {
            JsonNode root = objectMapper.readTree(payload);
            List<String> chunks = new ArrayList<>();
            collectKnownSowFields(root, chunks);
            if (chunks.isEmpty()) {
                appendText(root, chunks);
            }
            return chunks;
        } catch (JsonProcessingException ex) {
            return List.of(sowJson);
        }
    }

    private void collectKnownSowFields(JsonNode root, List<String> chunks) {
        JsonNode sowNode = root.path("sow");
        if (sowNode.isMissingNode() || sowNode.isNull()) {
            sowNode = root;
        }

        appendField(sowNode, chunks, "title");
        appendField(sowNode, chunks, "overview");
        appendField(sowNode, chunks, "objectives");
        appendField(sowNode, chunks, "scopeOfWork");
        appendField(sowNode, chunks, "scope_of_work");
        appendField(sowNode, chunks, "deliverables");
        appendField(sowNode, chunks, "deliverable");

        JsonNode milestonesNode = root.path("milestones");
        if (!milestonesNode.isArray()) {
            return;
        }

        for (JsonNode milestoneNode : milestonesNode) {
            appendField(milestoneNode, chunks, "name");
            appendField(milestoneNode, chunks, "milestoneName");
            appendField(milestoneNode, chunks, "milestone_name");
            appendField(milestoneNode, chunks, "description");
        }
    }

    private void appendField(JsonNode node, List<String> chunks, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }

        JsonNode value = node.get(fieldName);
        if (value != null && !value.isNull()) {
            appendText(value, chunks);
        }
    }

    private void appendText(JsonNode node, List<String> chunks) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isValueNode()) {
            String value = node.asText();
            if (value != null && !value.isBlank()) {
                chunks.add(value);
            }
            return;
        }

        node.forEach(child -> appendText(child, chunks));
    }

    private String extractJsonPayload(String value) {
        String content = value.trim();
        if (!content.startsWith("```")) {
            return content;
        }

        int firstLineBreak = content.indexOf('\n');
        int lastFence = content.lastIndexOf("```");
        if (firstLineBreak >= 0 && lastFence > firstLineBreak) {
            return content.substring(firstLineBreak + 1, lastFence).trim();
        }
        return content;
    }

    private List<String> matchRules(String text, List<KeywordRule> rules) {
        String normalizedText = normalizeForMatching(text);
        if (normalizedText.isBlank()) {
            return List.of();
        }

        List<String> matches = new ArrayList<>();
        for (KeywordRule rule : rules) {
            if (containsAnyAlias(normalizedText, rule.aliases())) {
                matches.add(rule.label());
            }
        }
        return matches;
    }

    private boolean containsAnyAlias(String normalizedText, Collection<String> aliases) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return false;
        }

        for (String alias : aliases) {
            String normalizedAlias = normalizeForMatching(alias);
            if (!normalizedAlias.isBlank() && containsNormalizedTerm(normalizedText, normalizedAlias)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNormalizedTerm(String normalizedText, String normalizedTerm) {
        return (" " + normalizedText + " ").contains(" " + normalizedTerm + " ");
    }

    private List<String> combineKeywords(List<String> skills, List<String> domains) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        if (skills != null) {
            keywords.addAll(skills);
        }
        if (domains != null) {
            keywords.addAll(domains);
        }
        return List.copyOf(keywords);
    }

    private static KeywordRule rule(String label, String... aliases) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        values.add(label);
        values.addAll(Arrays.asList(aliases));
        return new KeywordRule(label, List.copyOf(values));
    }

    private static Map<String, List<String>> aliasesByLabel(List<KeywordRule> rules) {
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        for (KeywordRule rule : rules) {
            aliases.put(rule.label(), rule.aliases());
        }
        return Map.copyOf(aliases);
    }

    private static Map<String, List<String>> allAliases() {
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        aliases.putAll(SKILL_ALIASES);
        aliases.putAll(DOMAIN_ALIASES);
        return Map.copyOf(aliases);
    }

    private record KeywordRule(String label, List<String> aliases) {
    }
}

/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java
 * Đây là file gì: File service xử lý luồng AI generate Statement of Work cho job.
 * Nhiệm vụ: Lấy ngữ cảnh RAG, tạo prompt, gọi OpenAI, parse JSON trả về và chuẩn hóa milestone/ngân sách.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.BadGatewayException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.sow.BudgetAssessmentDto;
import com.aitasker.be.dto.sow.GenerateSowRequest;
import com.aitasker.be.dto.sow.GenerateSowResponse;
import com.aitasker.be.dto.sow.MilestoneBudgetAllocationDto;
import com.aitasker.be.dto.sow.MilestoneBudgetReferenceDto;
import com.aitasker.be.dto.sow.MilestoneDto;
import com.aitasker.be.dto.sow.ReallocateSowBudgetRequest;
import com.aitasker.be.dto.sow.ReallocateSowBudgetResponse;
import com.aitasker.be.dto.sow.SowDto;
import com.aitasker.be.service.ai.RagRetrievalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

// Note: Annotation này cho Spring quản lý class như một service nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AiSowGenerationService {
    private static final String SYSTEM_MESSAGE = "Ban la Senior AI Solution Architect. Bat buoc tra ve JSON hop le, khong markdown, khong giai thich ngoai JSON.";
    private static final BigDecimal DEFAULT_MIN_ESTIMATE_RATIO = new BigDecimal("0.80");
    private static final BigDecimal DEFAULT_MAX_ESTIMATE_RATIO = new BigDecimal("1.20");
    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");
    private static final BigDecimal ONE_BILLION = new BigDecimal("1000000000");
    private static final BigDecimal MAX_ABBREVIATED_MILLION_VALUE = new BigDecimal("10000");
    private static final Pattern MONEY_NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:[.,]\\d+)*");
    private static final int MAX_BUDGET_FACTORS = 8;
    private static final int MAX_GENERATED_MILESTONES = 50;

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;
    private final RagRetrievalService ragRetrievalService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Note: Hàm chính của luồng generate SoW; kiểm tra cấu hình AI, lấy RAG context, gọi AI và
    // chuẩn hóa kết quả. Luôn trả draft + milestones; clarification là advisory, không chặn flow.
    public GenerateSowResponse generateSow(GenerateSowRequest request) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new BadGatewayException("Chua cau hinh OPENAI_API_KEY");
        }

        String ragContext = ragRetrievalService.retrieveContext(request);
        GenerateSowResponse response = callAndParse(buildPrompt(request, ragContext), request);
        List<String> violations = generationViolations(response, request);

        // Retry once for an incomplete draft or an infeasible milestone count.
        if (!violations.isEmpty()) {
            GenerateSowResponse retry = callAndParse(
                    buildRecoveryPrompt(request, ragContext, violations),
                    request);
            List<String> retryViolations = generationViolations(retry, request);
            if (!retryViolations.isEmpty()) {
                throw new AppException("AI response khong dat rang buoc sau recovery: "
                        + String.join("; ", retryViolations));
            }
            response = retry;
        }

        finalizeResponse(response, request);
        return response;
    }

    // Note: Hàm gọi AI và parse kết quả thành response DTO.
    private GenerateSowResponse callAndParse(String prompt, GenerateSowRequest request) {
        return parseAiResponse(callAi(prompt, request));
    }

    // Note: Hàm kiểm tra response có SoW và milestones hợp lệ để sử dụng ngay.
    private boolean hasValidDraft(GenerateSowResponse response) {
        return response != null
                && response.getSow() != null
                && response.getMilestones() != null
                && !response.getMilestones().isEmpty()
                && response.getMilestones().stream()
                .allMatch(milestone -> milestone != null
                        && milestone.getAcceptanceCriteria() != null
                        && milestone.getAcceptanceCriteria().stream()
                        .anyMatch(criterion -> criterion != null && !criterion.isBlank()));
    }

    private List<String> generationViolations(GenerateSowResponse response, GenerateSowRequest request) {
        List<String> violations = new ArrayList<>();
        if (!hasValidDraft(response)) {
            violations.add("Bat buoc co sow, milestone va acceptanceCriteria khong rong");
            return violations;
        }

        int milestoneLimit = milestoneLimit(request == null ? null : request.getDuration());
        if (response.getMilestones().size() > milestoneLimit) {
            violations.add("So milestone " + response.getMilestones().size()
                    + " vuot gioi han " + milestoneLimit
                    + " de moi milestone co duration nguyen duong");
        }
        return violations;
    }

    // Note: Hàm chuẩn hóa cuối cùng: questions tối đa 3, needMoreInfo theo questions, assumptions không null,
    // và ngân sách/thời lượng milestone khớp yêu cầu. Không xóa sow hay milestones khi có questions.
    private void finalizeResponse(GenerateSowResponse response, GenerateSowRequest request) {
        List<String> questions = Boolean.TRUE.equals(request.getClarificationAlreadyAsked())
                ? new ArrayList<>()
                : limitQuestions(defaultList(response.getQuestions()));
        response.setQuestions(questions);
        response.setNeedMoreInfo(!questions.isEmpty());

        if (response.getSow() == null) {
            throw new AppException("AI response thieu thong tin sow");
        }
        if (response.getMilestones() == null || response.getMilestones().isEmpty()) {
            throw new AppException("AI response thieu milestones");
        }

        normalizeAssumptions(response.getSow());
        response.getMilestones().forEach(this::normalizeAcceptanceCriteria);
        normalizeMilestoneDuration(response, request.getDuration(), request.getDurationUnit());
        validateDurationInvariant(response.getMilestones(), request.getDuration(), request.getDurationUnit());
        normalizeBudgetAssessment(response, request.getBudget());
        normalizeMilestoneRecommendedBudget(response, response.getBudgetAssessment().getRecommendedBudget());
        normalizeMilestoneBudget(response, request.getBudget());
    }

    private void normalizeAcceptanceCriteria(MilestoneDto milestone) {
        if (milestone == null || milestone.getAcceptanceCriteria() == null) {
            return;
        }
        LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        for (String criterion : milestone.getAcceptanceCriteria()) {
            if (criterion == null || criterion.isBlank()) {
                continue;
            }
            String trimmed = criterion.trim();
            unique.putIfAbsent(trimmed.toLowerCase(java.util.Locale.ROOT), trimmed);
        }
        milestone.setAcceptanceCriteria(new ArrayList<>(unique.values()));
    }

    // Note: Hàm lọc bỏ câu hỏi null/rỗng và câu hỏi trùng (không phân biệt hoa thường/khoảng trắng),
    // sau đó giới hạn batch clarification thành tối đa 3 câu hỏi ngắn gọn.
    private List<String> limitQuestions(List<String> questions) {
        if (questions == null) {
            return new ArrayList<>();
        }
        List<String> cleaned = new ArrayList<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for (String question : questions) {
            if (question == null) {
                continue;
            }
            String trimmed = question.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String key = trimmed.toLowerCase(java.util.Locale.ROOT);
            if (!seen.add(key)) {
                continue;
            }
            cleaned.add(trimmed);
        }
        if (cleaned.size() <= 3) {
            return cleaned;
        }
        return new ArrayList<>(cleaned.subList(0, 3));
    }

    // Note: Hàm đảm bảo sow.assumptions không null: model omit/null -> [].
    private void normalizeAssumptions(SowDto sow) {
        if (sow.getAssumptions() == null) {
            sow.setAssumptions(new ArrayList<>());
        }
    }

    // Note: Hàm dựng prompt đầy đủ từ yêu cầu dự án và RAG context để AI trả về JSON đúng cấu trúc hệ thống.
    public String buildPrompt(GenerateSowRequest request, String ragContext) {
        return buildPromptInternal(request, ragContext, false);
    }

    // Note: Hàm dựng prompt cho lần recovery retry nội bộ khi model chỉ trả questions mà thiếu SoW/milestone.
    public String buildRecoveryPrompt(GenerateSowRequest request, String ragContext) {
        return buildRecoveryPrompt(request, ragContext, List.of());
    }

    private String buildRecoveryPrompt(GenerateSowRequest request, String ragContext, List<String> violations) {
        return buildPromptInternal(request, ragContext, true, violations);
    }

    private String buildPromptInternal(GenerateSowRequest request, String ragContext, boolean recovery) {
        return buildPromptInternal(request, ragContext, recovery, List.of());
    }

    private String buildPromptInternal(GenerateSowRequest request, String ragContext,
                                       boolean recovery, List<String> violations) {
        int milestoneLimit = milestoneLimit(request == null ? null : request.getDuration());
        String recoveryViolations = violations == null || violations.isEmpty()
                ? ""
                : "\nLoi can sua trong lan recovery nay:\n- " + String.join("\n- ", violations);
        String template = """
                Ban la Senior AI Solution Architect.

                Su dung RAG CONTEXT ben duoi de tao SoW dung nghiep vu he thong.
                Neu RAG CONTEXT khong lien quan, hay bo qua phan khong lien quan.
                Khong duoc copy may moc context, chi dung no lam quy tac tham khao.
                Cac planning dimensions trong context chi la goi y phan tich,
                KHONG phai danh sach phase hoac milestone bat buoc. Khong bien moi
                context bullet thanh mot milestone.
                Tuyet doi khong liet ke milestones hoac milestone guidance (ten milestone,
                mo ta milestone, phan bo ngan sach %%) trong cac field sow.overview,
                sow.scopeOfWork, sow.deliverables. Cac field sow chi mo ta tong quan
                du an, pham vi cong viec va san pham ban giao. Milestones duoc liet ke
                doc lap trong array milestones.

                RAG CONTEXT:
                %s

                Nhiem vu:
                1. Phan tich yeu cau tho.
                2. Luon sinh draft SoW day du usable va danh sach milestones khong
                   rong tu thong tin da nhap, ke ca khi con thieu thong tin.
                3. Khi thieu thong tin, hay SUY LUAN gia dinh hop ly, de dang theo
                   nghiep vu (domain-appropriate) va ghi tung gia dinh do vao
                   sow.assumptions. Khong bao gio de viec thieu thong tin lam bo
                   sot sow hay milestones.
                4. Neu con thieu thong tin material co the lam ban draft tot hon,
                   %s
                5. needMoreInfo=true Chi khi questions khong rong; neu khong can cau
                   hoi thi needMoreInfo=false va questions=[].
                6. Khong bao gio bo sot sow hay milestones vi co questions.
                7. Khong trung lap thong tin milestone guidance vao cac field sow.
                8. Voi MOI milestone, bat buoc sinh danh sach acceptanceCriteria
                   rieng gom cac dieu kien nghiem thu cu the, do duoc va phu hop
                   voi san pham ban giao cua milestone do. Khong dung catalog hoac
                   danh sach tieu chi mac dinh giong nhau cho moi milestone.
                   Moi criterion phai neu ket qua quan sat duoc va cach kiem tra,
                   bang chung hoac nguong pass/fail. Khong dung rieng cac cau mo ho
                   nhu "hoat dong dung", "hieu qua", "hoan thanh", "duoc phe duyet".
                 9. Viet Statement of Work chuyen nghiep va TU QUYET DINH so luong
                    milestone phu hop voi scope, do phuc tap, cac ket qua co the
                    nghiem thu doc lap va tong thoi luong user cung cap.
                    - Khong dung so luong, ten milestone hoac phase co dinh.
                    - Khong sao chep planning dimensions thanh danh sach milestone.
                    - Gop cac cong viec lien quan va bo qua nhom khong can thiet.
                    - Moi milestone phai tao ra mot ket qua co the nghiem thu.
                    - So milestone phai cho phep moi duration >= 1 va tong duration
                      cua milestones bang dung Duration trong Input.
                    - So milestone toi da la %s.
                    - Phan bo ngan sach theo cong suc va rui ro thuc te; khong dung
                      ty le phase co dinh tu RAG context.
                10. Bao phu tat ca yeu cau ro rang trong Raw requirement, Support
                    fields va Required skills. Moi scope item, deliverable va
                    milestone phai truy vet duoc ve input hoac mot phu thuoc bat
                    buoc duoc ghi trong sow.assumptions. Khong tu them CI/CD,
                    monitoring, training, deployment hay tinh nang khac chi vi
                    RAG context co nhac toi; neu chi la goi y thi dua vao outOfScope.
                11. Uoc luong mot khoang ngan sach VND DOC LAP cho TOAN BO scope da
                    generate. Tu tinh gia dua tren do phuc tap scope, thoi luong,
                    vai tro va cong suc can thiet, tich hop, du lieu, kiem thu,
                    bao mat, ha tang, trien khai va du phong rui ro. Cac du an khac
                    nhau ve nhung yeu to nay phai co khoang gia khac nhau ro rang.
                    Khong suy doan, copy, neo hoac scale theo ngan sach Business;
                    ngan sach Business khong duoc cung cap cho model.
                12. budgetAssessment phai co estimatedMin <= recommendedBudget <=
                    estimatedMax, confidence LOW|MEDIUM|HIGH va toi da 8 factors
                    ngan gon giai thich cac driver chinh cua gia.
                13. Tong milestones[].budget phai bang budgetAssessment.recommendedBudget.
                    Day la phan bo de xuat cho full scope, khong phai quyet dinh
                    cuoi cung cua Business.
                14. Tat ca field tien phai la JSON number, so nguyen VND day du
                    va lon hon 0. Khong viet dang rut gon, khong kem don vi tien
                    trong gia tri va khong sao chep gia tu schema.
                 """ + (recovery ? """

                        BUOC PHUC HOI NOI BO: Ket qua truoc vi pham hop dong sinh SoW.
                        Lan nay bat buoc sinh ngay SoW day du, sua cac loi duoc neu,
                        tao milestones khong rong, ghi gia dinh vao sow.assumptions,
                        sinh acceptanceCriteria rieng cho tung milestone, va chi
                        tra toi da 3 cau hoi optional.%s
                        """ : "%s") + """

                Bat buoc tra ve JSON hop le, khong markdown, khong giai thich ngoai JSON.

                Cau truc va kieu du lieu duoc he thong ep bang Structured Outputs.

                Input:
                Project title: %s
                Raw requirement: %s
                Duration: %s %s
                Support fields: %s
                Required skills: %s
                """;
        return template.formatted(
                ragContext == null ? "" : ragContext,
                clarificationInstruction(request),
                milestoneLimit,
                recoveryViolations,
                request.getProjectTitle(),
                request.getRawRequirement(),
                request.getDuration(),
                request.getDurationUnit(),
                defaultList(request.getSupportFields()),
                defaultList(request.getRequiredSkills())
        );
    }

    private String clarificationInstruction(GenerateSowRequest request) {
        if (Boolean.TRUE.equals(request.getClarificationAlreadyAsked())) {
            return """
                    KHONG duoc tra them cau hoi nao nua vi he thong da hoi user mot lan.
                    Bat buoc suy luan gia dinh hop ly, ghi vao sow.assumptions,
                    dat needMoreInfo=false va questions=[].
                    """;
        }
        return """
                tra TOI DA 3 cau hoi ngan gon, khong trung thong tin da co trong
                input. Cau hoi chi la de xuat (advisory), khong bat buoc nguoi dung
                tra loi. Khong bien domain checklist thanh form phai dien day du.
                """;
    }

    // Note: Hàm parse nội dung AI trả về, làm sạch các field dễ sai kiểu rồi map sang response DTO.
    public GenerateSowResponse parseAiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BadGatewayException("AI khong tra ve noi dung");
        }

        String jsonPayload = extractJsonPayload(aiResponse);
        try {
            JsonNode responseNode = objectMapper.readTree(jsonPayload);
            normalizeStringListFields(responseNode);
            stripMilestoneGuidanceFromSow(responseNode);
            normalizeBudgetAssessmentFields(responseNode);
            normalizeBudgetFields(responseNode);
            normalizeDurationFields(responseNode);
            return objectMapper.treeToValue(responseNode, GenerateSowResponse.class);
        } catch (JsonProcessingException ex) {
            throw new AppException("AI response khong phai JSON hop le: " + truncate(ex.getOriginalMessage(), 200));
        }
    }

    // Note: Hàm chuẩn hóa các field dạng danh sách để nếu AI trả chuỗi đơn thì hệ thống vẫn đọc được.
    private void normalizeStringListFields(JsonNode responseNode) {
        if (!(responseNode instanceof ObjectNode response)) {
            return;
        }

        normalizeArrayField(response, "questions");

        JsonNode budgetAssessmentNode = response.get("budgetAssessment");
        if (budgetAssessmentNode instanceof ObjectNode budgetAssessment) {
            normalizeArrayField(budgetAssessment, "factors");
        }

        JsonNode sowNode = response.get("sow");
        if (sowNode instanceof ObjectNode sow) {
            normalizeArrayField(sow, "objectives");
            normalizeArrayField(sow, "scopeOfWork");
            normalizeArrayField(sow, "deliverables");
            normalizeArrayField(sow, "assumptions");
            normalizeArrayField(sow, "outOfScope");
            ensureAssumptionsArray(sow);
        }

        JsonNode milestonesNode = response.get("milestones");
        if (milestonesNode instanceof ArrayNode milestones) {
            for (JsonNode milestoneNode : milestones) {
                if (milestoneNode instanceof ObjectNode milestone) {
                    normalizeArrayField(milestone, "acceptanceCriteria");
                }
            }
        }
    }

    // Note: Hàm chuyển một field không phải array thành array một phần tử để đúng schema response.
    private void normalizeArrayField(ObjectNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.isArray()) {
            return;
        }

        ArrayNode values = objectMapper.createArrayNode();
        if (field.isTextual()) {
            values.add(field.asText());
        } else {
            values.add(field.toString());
        }
        node.set(fieldName, values);
    }

    // Note: Hàm đảm bảo sow.assumptions là array: nếu model omit/null thì set [].
    private void ensureAssumptionsArray(ObjectNode sow) {
        JsonNode assumptions = sow.get("assumptions");
        if (assumptions == null || assumptions.isNull()) {
            sow.set("assumptions", objectMapper.createArrayNode());
        }
    }

    private void stripMilestoneGuidanceFromSow(JsonNode responseNode) {
        JsonNode sowNode = responseNode.get("sow");
        if (!(sowNode instanceof ObjectNode sow)) {
            return;
        }
        Pattern milestoneSectionStart = milestoneSectionStartPattern();
        stripOverviewMilestoneBlock(sow, milestoneSectionStart);
        stripFromTextField(sow, "scopeOfWork", milestoneSectionStart);
        stripFromTextField(sow, "deliverables", milestoneSectionStart);
    }

    private Pattern milestoneSectionStartPattern() {
        return Pattern.compile(
                "(?i)^(?:(recommended|suggested|proposed)\\s+)?milestones?\\s*:"
                        + "|^milestone\\s+(recommendations?|breakdown|plan)\\s*:",
                Pattern.UNICODE_CHARACTER_CLASS);
    }

    private void stripOverviewMilestoneBlock(ObjectNode sow, Pattern sectionPattern) {
        JsonNode fieldNode = sow.get("overview");
        if (fieldNode == null || !fieldNode.isTextual()) {
            return;
        }

        String overview = stripInlineOverviewMilestoneSentence(fieldNode.asText());
        overview = stripOverviewMilestoneLines(overview, sectionPattern);
        sow.put("overview", trimBlankLines(overview));
    }

    private String stripInlineOverviewMilestoneSentence(String overview) {
        if (overview == null || overview.isBlank()) {
            return overview;
        }
        Pattern headerPattern = Pattern.compile("(?i)(recommended|suggested|proposed)\\s+milestones?\\s*:");
        java.util.regex.Matcher headerMatcher = headerPattern.matcher(overview);
        if (!headerMatcher.find()) {
            return overview;
        }

        String prefix = overview.substring(0, headerMatcher.start()).trim();
        String suffix = overview.substring(headerMatcher.end());
        Pattern itemPattern = Pattern.compile(
                "\\s*\\d+\\.\\s*.*?(?:\\.(?=\\s+[A-Z])|(?=\\s*\\d+\\.\\s*)|$)",
                Pattern.DOTALL);
        java.util.regex.Matcher itemMatcher = itemPattern.matcher(suffix);
        int cursor = 0;
        boolean consumedAnyItem = false;
        while (cursor < suffix.length()) {
            itemMatcher.region(cursor, suffix.length());
            if (!itemMatcher.lookingAt()) {
                break;
            }
            consumedAnyItem = true;
            cursor = itemMatcher.end();
        }
        if (!consumedAnyItem) {
            return overview;
        }

        String remaining = suffix.substring(Math.min(cursor, suffix.length())).trim();
        return (prefix + " " + remaining).replaceAll("\\s{2,}", " ").trim();
    }

    private String stripOverviewMilestoneLines(String overview, Pattern sectionPattern) {
        String[] lines = overview.split("(?:\\\\n|\\R)", -1);
        List<String> cleaned = new ArrayList<>();
        boolean inMilestoneSection = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (sectionPattern.matcher(trimmed).find()) {
                inMilestoneSection = true;
                continue;
            }
            if (inMilestoneSection) {
                if (!trimmed.isBlank() && !isMilestoneListItem(trimmed)) {
                    inMilestoneSection = false;
                    cleaned.add(line);
                }
                continue;
            }
            cleaned.add(line);
        }
        return String.join("\n", cleaned);
    }

    private void stripFromTextField(ObjectNode sow, String fieldName, Pattern sectionPattern) {
        JsonNode fieldNode = sow.get(fieldName);
        if (!(fieldNode instanceof ArrayNode items)) {
            return;
        }
        ArrayNode cleaned = objectMapper.createArrayNode();
        boolean inMilestoneSection = false;
        for (JsonNode item : items) {
            String text = item.isTextual() ? item.asText() : "";
            String trimmed = text.trim();
            if (sectionPattern.matcher(trimmed).find()) {
                inMilestoneSection = true;
                continue;
            }
            if (inMilestoneSection) {
                if (!trimmed.isBlank() && !isMilestoneListItem(trimmed)) {
                    inMilestoneSection = false;
                    cleaned.add(text);
                }
                continue;
            }
            cleaned.add(text);
        }
        sow.set(fieldName, cleaned);
    }

    private boolean isMilestoneListItem(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (trimmed.matches("^\\d+\\..*")) {
            return true;
        }
        if (trimmed.matches("^[-•*]\\s+.*")) {
            return true;
        }
        return false;
    }

    // Note: Hàm chuẩn hóa budget milestone khi AI trả tiền dạng text như "30 triệu" hoặc có ký tự phân cách.
    private void normalizeBudgetFields(JsonNode responseNode) {
        JsonNode milestonesNode = responseNode.get("milestones");
        if (!(milestonesNode instanceof ArrayNode milestones)) {
            return;
        }

        for (JsonNode milestoneNode : milestones) {
            if (!(milestoneNode instanceof ObjectNode milestone)) {
                continue;
            }

            JsonNode budgetNode = milestone.get("budget");
            if (budgetNode == null || !budgetNode.isTextual()) {
                continue;
            }

            String normalizedBudget = normalizeMoneyText(budgetNode.asText());
            if (normalizedBudget.isBlank()) {
                milestone.putNull("budget");
            } else {
                milestone.put("budget", new BigDecimal(normalizedBudget));
            }
        }
    }

    private void normalizeBudgetAssessmentFields(JsonNode responseNode) {
        JsonNode assessmentNode = responseNode.get("budgetAssessment");
        if (!(assessmentNode instanceof ObjectNode assessment)) {
            return;
        }
        normalizeMoneyField(assessment, "estimatedMin");
        normalizeMoneyField(assessment, "recommendedBudget");
        normalizeMoneyField(assessment, "estimatedMax");
    }

    private void normalizeMoneyField(ObjectNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isTextual()) {
            return;
        }
        String normalized = normalizeMoneyText(value.asText());
        if (normalized.isBlank()) {
            node.putNull(fieldName);
        } else {
            node.put(fieldName, new BigDecimal(normalized));
        }
    }

    private void normalizeDurationFields(JsonNode responseNode) {
        JsonNode milestonesNode = responseNode.get("milestones");
        if (!(milestonesNode instanceof ArrayNode milestones)) {
            return;
        }

        for (JsonNode milestoneNode : milestones) {
            if (!(milestoneNode instanceof ObjectNode milestone)) {
                continue;
            }

            JsonNode durationNode = milestone.get("duration");
            if (durationNode != null && durationNode.isTextual()) {
                String normalizedDuration = normalizeIntegerText(durationNode.asText());
                if (normalizedDuration.isBlank()) {
                    milestone.putNull("duration");
                } else {
                    milestone.put("duration", Integer.parseInt(normalizedDuration));
                }
            }
        }
    }

    private String normalizeMoneyText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalizedUnitText = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        BigDecimal multiplier = null;
        if (normalizedUnitText.matches(".*(?:trieu|million|\\d\\s*tr\\b).*")) {
            multiplier = ONE_MILLION;
        } else if (normalizedUnitText.matches(".*(?:\\bty\\b|billion).*")) {
            multiplier = ONE_BILLION;
        }

        if (multiplier != null) {
            var matcher = MONEY_NUMBER_PATTERN.matcher(normalizedUnitText);
            if (!matcher.find()) {
                return "";
            }
            BigDecimal abbreviatedAmount = parseAbbreviatedMoneyNumber(matcher.group());
            return abbreviatedAmount.multiply(multiplier)
                    .setScale(0, RoundingMode.HALF_UP)
                    .toPlainString();
        }

        return normalizeIntegerText(value);
    }

    private String normalizeIntegerText(String value) {
        String digits = value.replaceAll("[^0-9-]", "");
        if (digits.equals("-")) {
            return "";
        }
        return digits;
    }

    private BigDecimal parseAbbreviatedMoneyNumber(String value) {
        int lastComma = value.lastIndexOf(',');
        int lastDot = value.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            int decimalSeparator = Math.max(lastComma, lastDot);
            String integerPart = value.substring(0, decimalSeparator).replaceAll("[.,]", "");
            String decimalPart = value.substring(decimalSeparator + 1);
            return new BigDecimal(integerPart + "." + decimalPart);
        }

        int separator = Math.max(lastComma, lastDot);
        if (separator < 0) {
            return new BigDecimal(value);
        }

        char separatorChar = value.charAt(separator);
        long separatorCount = value.chars().filter(character -> character == separatorChar).count();
        int trailingDigits = value.length() - separator - 1;
        if (separatorCount == 1 && trailingDigits > 0 && trailingDigits <= 2) {
            return new BigDecimal(value.replace(separatorChar, '.'));
        }
        return new BigDecimal(value.replace(String.valueOf(separatorChar), ""));
    }

    // Note: Hàm tách phần JSON thật từ response AI, kể cả khi AI bọc trong markdown code block.
    private String extractJsonPayload(String aiResponse) {
        String content = aiResponse.trim();

        if (content.startsWith("```")) {
            int firstLineBreak = content.indexOf('\n');
            int lastFence = content.lastIndexOf("```");
            if (firstLineBreak >= 0 && lastFence > firstLineBreak) {
                content = content.substring(firstLineBreak + 1, lastFence).trim();
            }
        }

        if (content.startsWith("{")) {
            return content;
        }

        int jsonStart = content.indexOf('{');
        int jsonEnd = content.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1).trim();
        }

        return content;
    }

    // Note: Hàm điều chỉnh tổng ngân sách các milestone khớp với ngân sách doanh nghiệp nhập.
    public void normalizeMilestoneBudget(GenerateSowResponse response, BigDecimal totalBudget) {
        List<MilestoneDto> milestones = response.getMilestones();
        if (milestones == null || milestones.isEmpty() || !isPositive(totalBudget)) {
            return;
        }
        List<BigDecimal> normalized = normalizedMilestoneBudgetValues(milestones, totalBudget);
        for (int i = 0; i < milestones.size(); i++) {
            milestones.get(i).setBudget(normalized.get(i));
        }
    }

    public ReallocateSowBudgetResponse reallocateSowBudget(ReallocateSowBudgetRequest request) {
        if (request == null) {
            throw new AppException("request khong duoc rong");
        }
        BigDecimal selectedBudget = requirePositiveWholeVnd(request.getSelectedBudget(), "selectedBudget");
        List<MilestoneBudgetReferenceDto> requestMilestones = request.getMilestones();
        if (requestMilestones == null || requestMilestones.isEmpty()) {
            throw new AppException("milestones khong duoc rong");
        }
        if (requestMilestones.size() > 50) {
            throw new AppException("milestones khong duoc vuot qua 50 phan tu");
        }

        Set<Integer> seenIndexes = new HashSet<>();
        List<MilestoneBudgetReferenceDto> milestones = new ArrayList<>();
        for (MilestoneBudgetReferenceDto milestone : requestMilestones) {
            if (milestone == null || milestone.getMilestoneIndex() == null
                    || milestone.getMilestoneIndex() < 0) {
                throw new AppException("milestoneIndex khong hop le");
            }
            if (!seenIndexes.add(milestone.getMilestoneIndex())) {
                throw new AppException("milestoneIndex bi trung: " + milestone.getMilestoneIndex());
            }
            milestones.add(MilestoneBudgetReferenceDto.builder()
                    .milestoneIndex(milestone.getMilestoneIndex())
                    .referenceBudget(requirePositiveWholeVnd(
                            milestone.getReferenceBudget(), "referenceBudget"))
                    .build());
        }
        milestones.sort(Comparator.comparing(MilestoneBudgetReferenceDto::getMilestoneIndex));

        BigDecimal referenceTotal = milestones.stream()
                .map(MilestoneBudgetReferenceDto::getReferenceBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allocated = BigDecimal.ZERO;
        List<MilestoneBudgetAllocationDto> allocations = new ArrayList<>();
        for (int i = 0; i < milestones.size(); i++) {
            MilestoneBudgetReferenceDto milestone = milestones.get(i);
            BigDecimal fundsAllocated;
            if (i == milestones.size() - 1) {
                fundsAllocated = selectedBudget.subtract(allocated);
            } else {
                fundsAllocated = milestone.getReferenceBudget()
                        .multiply(selectedBudget)
                        .divide(referenceTotal, 0, RoundingMode.DOWN);
                allocated = allocated.add(fundsAllocated);
            }
            allocations.add(MilestoneBudgetAllocationDto.builder()
                    .milestoneIndex(milestone.getMilestoneIndex())
                    .referenceBudget(milestone.getReferenceBudget())
                    .fundsAllocated(fundsAllocated)
                    .build());
        }

        BigDecimal allocationTotal = allocations.stream()
                .map(MilestoneBudgetAllocationDto::getFundsAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (allocationTotal.compareTo(selectedBudget) != 0) {
            throw new AppException("Tong milestone khong khop selectedBudget");
        }
        return ReallocateSowBudgetResponse.builder()
                .currency("VND")
                .selectedBudget(selectedBudget)
                .allocationTotal(allocationTotal)
                .allocations(allocations)
                .build();
    }

    public void normalizeBudgetAssessment(GenerateSowResponse response, BigDecimal businessBudget) {
        BudgetAssessmentDto providerAssessment = response.getBudgetAssessment();
        boolean repairedMoneyScale = normalizeAbbreviatedProviderMoney(providerAssessment);
        boolean hasProviderRecommendation = providerAssessment != null
                && isPositive(providerAssessment.getRecommendedBudget());
        BigDecimal milestoneFallback = sumValidMilestoneBudgets(response.getMilestones());
        if (!hasProviderRecommendation && isAbbreviatedMillionValue(milestoneFallback)) {
            milestoneFallback = milestoneFallback.multiply(ONE_MILLION);
            repairedMoneyScale = true;
        }

        BigDecimal recommended;
        String source;
        if (hasProviderRecommendation) {
            recommended = roundVnd(providerAssessment.getRecommendedBudget());
            source = "AI_ADVISORY";
        } else if (isPositive(milestoneFallback)) {
            recommended = roundVnd(milestoneFallback);
            source = "AI_MILESTONE_FALLBACK";
        } else {
            throw new AppException("AI response thieu uoc luong ngan sach hop le");
        }

        boolean repairedRange = false;
        BigDecimal estimatedMin = providerAssessment == null ? null : providerAssessment.getEstimatedMin();
        if (!isPositive(estimatedMin) || estimatedMin.compareTo(recommended) > 0) {
            estimatedMin = recommended.multiply(DEFAULT_MIN_ESTIMATE_RATIO);
            repairedRange = true;
        }
        estimatedMin = roundVnd(estimatedMin);

        BigDecimal estimatedMax = providerAssessment == null ? null : providerAssessment.getEstimatedMax();
        if (!isPositive(estimatedMax) || estimatedMax.compareTo(recommended) < 0) {
            estimatedMax = recommended.multiply(DEFAULT_MAX_ESTIMATE_RATIO);
            repairedRange = true;
        }
        estimatedMax = roundVnd(estimatedMax);

        String confidence = normalizeConfidence(providerAssessment == null ? null : providerAssessment.getConfidence());
        if (!hasProviderRecommendation || repairedRange || repairedMoneyScale) {
            confidence = "LOW";
        }

        BigDecimal normalizedBusinessBudget = roundVnd(businessBudget);
        String status = budgetStatus(normalizedBusinessBudget, estimatedMin, recommended, estimatedMax);
        BigDecimal gapToMinimum = estimatedMin.subtract(normalizedBusinessBudget).max(BigDecimal.ZERO);

        response.setBudgetAssessment(BudgetAssessmentDto.builder()
                .currency("VND")
                .businessBudget(normalizedBusinessBudget)
                .estimatedMin(estimatedMin)
                .recommendedBudget(recommended)
                .estimatedMax(estimatedMax)
                .status(status)
                .gapToMinimum(gapToMinimum)
                .confidence(confidence)
                .source(source)
                .requiresBusinessConfirmation(!"HIGH".equals(status))
                .message(budgetMessage(status))
                .factors(cleanBudgetFactors(providerAssessment == null ? null : providerAssessment.getFactors()))
                .build());
    }

    private boolean normalizeAbbreviatedProviderMoney(BudgetAssessmentDto assessment) {
        if (assessment == null || !isAbbreviatedMillionValue(assessment.getRecommendedBudget())) {
            return false;
        }

        assessment.setRecommendedBudget(assessment.getRecommendedBudget().multiply(ONE_MILLION));
        if (isAbbreviatedMillionValue(assessment.getEstimatedMin())) {
            assessment.setEstimatedMin(assessment.getEstimatedMin().multiply(ONE_MILLION));
        }
        if (isAbbreviatedMillionValue(assessment.getEstimatedMax())) {
            assessment.setEstimatedMax(assessment.getEstimatedMax().multiply(ONE_MILLION));
        }
        return true;
    }

    private boolean isAbbreviatedMillionValue(BigDecimal value) {
        return isPositive(value)
                && value.stripTrailingZeros().scale() <= 0
                && value.compareTo(MAX_ABBREVIATED_MILLION_VALUE) <= 0;
    }

    private void normalizeMilestoneRecommendedBudget(GenerateSowResponse response, BigDecimal recommendedTotal) {
        List<MilestoneDto> milestones = response.getMilestones();
        if (milestones == null || milestones.isEmpty() || !isPositive(recommendedTotal)) {
            return;
        }
        List<BigDecimal> normalized = normalizedMilestoneBudgetValues(milestones, recommendedTotal);
        for (int i = 0; i < milestones.size(); i++) {
            milestones.get(i).setRecommendedBudget(normalized.get(i));
        }
    }

    private List<BigDecimal> normalizedMilestoneBudgetValues(List<MilestoneDto> milestones, BigDecimal totalBudget) {
        boolean hasInvalidBudget = milestones.stream()
                .anyMatch(milestone -> milestone.getBudget() == null || milestone.getBudget().compareTo(BigDecimal.ZERO) < 0);
        if (hasInvalidBudget) {
            return equalBudgetValues(milestones.size(), totalBudget);
        }

        BigDecimal currentTotal = milestones.stream()
                .map(MilestoneDto::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (currentTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return equalBudgetValues(milestones.size(), totalBudget);
        }
        if (currentTotal.compareTo(totalBudget) == 0) {
            return milestones.stream().map(MilestoneDto::getBudget).toList();
        }

        List<BigDecimal> normalized = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < milestones.size(); i++) {
            BigDecimal value;
            if (i == milestones.size() - 1) {
                value = totalBudget.subtract(allocated);
            } else {
                value = milestones.get(i).getBudget()
                        .multiply(totalBudget)
                        .divide(currentTotal, 0, RoundingMode.HALF_UP);
                allocated = allocated.add(value);
            }
            normalized.add(value);
        }
        return normalized;
    }

    private List<BigDecimal> equalBudgetValues(int size, BigDecimal totalBudget) {
        BigDecimal baseBudget = totalBudget.divide(BigDecimal.valueOf(size), 0, RoundingMode.DOWN);
        List<BigDecimal> values = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < size; i++) {
            BigDecimal value = i == size - 1 ? totalBudget.subtract(allocated) : baseBudget;
            values.add(value);
            allocated = allocated.add(value);
        }
        return values;
    }

    private BigDecimal sumValidMilestoneBudgets(List<MilestoneDto> milestones) {
        if (milestones == null || milestones.isEmpty()
                || milestones.stream().anyMatch(item -> item == null || !isPositive(item.getBudget()))) {
            return null;
        }
        return milestones.stream().map(MilestoneDto::getBudget).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal requirePositiveWholeVnd(BigDecimal value, String fieldName) {
        if (!isPositive(value) || value.stripTrailingZeros().scale() > 0) {
            throw new AppException(fieldName + " phai la so VND nguyen lon hon 0");
        }
        return value.setScale(0, RoundingMode.UNNECESSARY);
    }

    private BigDecimal roundVnd(BigDecimal value) {
        if (!isPositive(value)) {
            return BigDecimal.ONE;
        }
        return value.setScale(0, RoundingMode.HALF_UP).max(BigDecimal.ONE);
    }

    private String normalizeConfidence(String value) {
        if (value == null) {
            return "LOW";
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "LOW", "MEDIUM", "HIGH" -> normalized;
            default -> "LOW";
        };
    }

    private List<String> cleanBudgetFactors(List<String> factors) {
        if (factors == null || factors.isEmpty()) {
            return new ArrayList<>();
        }
        LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        for (String factor : factors) {
            if (factor == null || factor.isBlank()) {
                continue;
            }
            String trimmed = factor.trim();
            unique.putIfAbsent(trimmed.toLowerCase(java.util.Locale.ROOT), trimmed);
            if (unique.size() == MAX_BUDGET_FACTORS) {
                break;
            }
        }
        return new ArrayList<>(unique.values());
    }

    private String budgetStatus(BigDecimal businessBudget, BigDecimal estimatedMin,
                                BigDecimal recommended, BigDecimal estimatedMax) {
        if (businessBudget.compareTo(estimatedMin) < 0) {
            return "TOO_LOW";
        }
        if (businessBudget.compareTo(recommended) < 0) {
            return "LOW";
        }
        if (businessBudget.compareTo(estimatedMax) <= 0) {
            return "SUITABLE";
        }
        return "HIGH";
    }

    private String budgetMessage(String status) {
        return switch (status) {
            case "TOO_LOW" -> "Ngân sách Business nhập thấp hơn mức tối thiểu AI ước tính cho toàn bộ phạm vi.";
            case "LOW" -> "Ngân sách Business nhập nằm trong khoảng ước tính nhưng thấp hơn mức AI đề xuất.";
            case "SUITABLE" -> "Ngân sách Business nhập phù hợp với khoảng AI ước tính.";
            case "HIGH" -> "Ngân sách Business nhập cao hơn khoảng AI ước tính; Business vẫn có quyền giữ nguyên.";
            default -> "AI đã tạo ước tính tham khảo; Business cần xác nhận ngân sách cuối cùng.";
        };
    }

    public void normalizeMilestoneDuration(GenerateSowResponse response, Integer totalDuration, String durationUnit) {
        List<MilestoneDto> milestones = response.getMilestones();
        if (milestones == null || milestones.isEmpty() || totalDuration == null || totalDuration <= 0) {
            return;
        }
        if (milestones.size() > milestoneLimit(totalDuration)) {
            throw new AppException("So milestone vuot qua tong duration; khong the phan bo duration nguyen duong");
        }

        boolean hasInvalidDuration = milestones.stream()
                .anyMatch(milestone -> milestone.getDuration() == null || milestone.getDuration() <= 0);

        boolean hasMismatchedUnit = milestones.stream()
                .map(MilestoneDto::getDurationUnit)
                .anyMatch(unit -> unit == null || unit.isBlank() || !isSameDurationUnit(unit, durationUnit));

        if (hasInvalidDuration || hasMismatchedUnit) {
            distributeDurationEqually(milestones, totalDuration, durationUnit);
            return;
        }

        int currentTotal = milestones.stream()
                .map(MilestoneDto::getDuration)
                .reduce(0, Integer::sum);

        if (currentTotal <= 0) {
            distributeDurationEqually(milestones, totalDuration, durationUnit);
            return;
        }

        if (currentTotal == totalDuration) {
            milestones.forEach(milestone -> milestone.setDurationUnit(durationUnit));
            return;
        }

        int allocated = 0;
        for (int i = 0; i < milestones.size(); i++) {
            MilestoneDto milestone = milestones.get(i);
            int normalizedDuration;

            if (i == milestones.size() - 1) {
                normalizedDuration = totalDuration - allocated;
            } else {
                normalizedDuration = BigDecimal.valueOf(milestone.getDuration())
                        .multiply(BigDecimal.valueOf(totalDuration))
                        .divide(BigDecimal.valueOf(currentTotal), 0, RoundingMode.HALF_UP)
                        .intValue();
                allocated += normalizedDuration;
            }

            milestone.setDuration(Math.max(normalizedDuration, 1));
            milestone.setDurationUnit(durationUnit);
        }

        rebalanceDurationTotal(milestones, totalDuration);
    }

    private void validateDurationInvariant(List<MilestoneDto> milestones, Integer totalDuration, String durationUnit) {
        if (milestones == null || milestones.isEmpty() || totalDuration == null || totalDuration <= 0) {
            return;
        }
        boolean invalid = milestones.stream().anyMatch(milestone -> milestone == null
                || milestone.getDuration() == null
                || milestone.getDuration() <= 0
                || !isSameDurationUnit(milestone.getDurationUnit(), durationUnit));
        int actualTotal = milestones.stream()
                .filter(java.util.Objects::nonNull)
                .map(MilestoneDto::getDuration)
                .filter(java.util.Objects::nonNull)
                .reduce(0, Integer::sum);
        if (invalid || actualTotal != totalDuration) {
            throw new AppException("AI milestone duration khong khop tong duration cua du an");
        }
    }

    private String callAi(String prompt, GenerateSowRequest sowRequest) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                buildRequestBody(prompt, sowRequest),
                buildHeaders());

        try {
            ResponseEntity<Map> response = callOpenAi(request);
            return extractContent(response.getBody());
        } catch (RestClientResponseException ex) {
            throw new BadGatewayException(buildOpenAiErrorMessage(ex));
        } catch (RestClientException ex) {
            throw new BadGatewayException("Khong goi duoc AI API");
        }
    }

    // Note: Hàm gọi OpenAI có retry ngắn với lỗi 5xx để giảm lỗi tạm thời từ phía AI.
    private ResponseEntity<Map> callOpenAi(HttpEntity<Map<String, Object>> request) {
        RestClientResponseException lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return restTemplate.exchange(openAiProperties.getChatCompletionsUrl(), HttpMethod.POST, request, Map.class);
            } catch (RestClientResponseException ex) {
                lastException = ex;
                if (!shouldRetry(ex.getStatusCode()) || attempt == 2) {
                    throw ex;
                }
            }
        }

        throw lastException;
    }

    // Note: Hàm xác định loại HTTP status nào nên thử gọi lại.
    private boolean shouldRetry(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError();
    }

    // Note: Build a strict Structured Outputs contract for Chat Completions.
    private Map<String, Object> buildRequestBody(String prompt, GenerateSowRequest sowRequest) {
        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_MESSAGE);

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        Map<String, Object> responseFormat = new LinkedHashMap<>();
        responseFormat.put("type", "json_schema");
        responseFormat.put("json_schema", Map.of(
                "name", "ai_sow_response",
                "strict", true,
                "schema", buildSowResponseSchema(sowRequest)));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("messages", List.of(systemMessage, userMessage));
        if (openAiProperties.supportsCustomTemperature()) {
            requestBody.put("temperature", 0.1);
        }
        requestBody.put("response_format", responseFormat);
        return requestBody;
    }

    private Map<String, Object> buildSowResponseSchema(GenerateSowRequest request) {
        Map<String, Object> nonBlankString = Map.of("type", "string", "minLength", 1);
        Map<String, Object> stringArray = Map.of(
                "type", "array",
                "items", nonBlankString);
        Map<String, Object> durationUnitSchema = request != null
                && request.getDurationUnit() != null
                && !request.getDurationUnit().isBlank()
                ? Map.of("type", "string", "const", request.getDurationUnit())
                : nonBlankString;

        Map<String, Object> budgetAssessment = strictObject(
                List.of("currency", "estimatedMin", "recommendedBudget", "estimatedMax", "confidence", "factors"),
                Map.of(
                        "currency", Map.of("type", "string", "const", "VND"),
                        "estimatedMin", Map.of("type", "integer", "minimum", 1),
                        "recommendedBudget", Map.of("type", "integer", "minimum", 1),
                        "estimatedMax", Map.of("type", "integer", "minimum", 1),
                        "confidence", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH")),
                        "factors", Map.of(
                                "type", "array",
                                "maxItems", MAX_BUDGET_FACTORS,
                                "items", nonBlankString)));

        Map<String, Object> sow = strictObject(
                List.of("title", "overview", "objectives", "scopeOfWork",
                        "deliverables", "assumptions", "outOfScope"),
                Map.of(
                        "title", nonBlankString,
                        "overview", nonBlankString,
                        "objectives", stringArray,
                        "scopeOfWork", stringArray,
                        "deliverables", stringArray,
                        "assumptions", stringArray,
                        "outOfScope", stringArray));

        Map<String, Object> milestone = strictObject(
                List.of("name", "description", "duration", "durationUnit", "budget", "acceptanceCriteria"),
                Map.of(
                        "name", nonBlankString,
                        "description", nonBlankString,
                        "duration", Map.of("type", "integer", "minimum", 1),
                        "durationUnit", durationUnitSchema,
                        "budget", Map.of("type", "integer", "minimum", 1),
                        "acceptanceCriteria", Map.of(
                                "type", "array",
                                "minItems", 1,
                                "items", nonBlankString)));

        return strictObject(
                List.of("needMoreInfo", "questions", "budgetAssessment", "sow", "milestones"),
                Map.of(
                        "needMoreInfo", Map.of("type", "boolean"),
                        "questions", Map.of(
                                "type", "array",
                                "maxItems", 3,
                                "items", nonBlankString),
                        "budgetAssessment", budgetAssessment,
                        "sow", sow,
                        "milestones", Map.of(
                                "type", "array",
                                "minItems", 1,
                                "maxItems", milestoneLimit(request == null ? null : request.getDuration()),
                                "items", milestone)));
    }

    private Map<String, Object> strictObject(List<String> required, Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", required);
        schema.put("properties", properties);
        return schema;
    }

    private int milestoneLimit(Integer totalDuration) {
        if (totalDuration == null || totalDuration <= 0) {
            return 1;
        }
        return Math.min(totalDuration, MAX_GENERATED_MILESTONES);
    }

    // Note: Hàm dựng header gọi AI, bao gồm Content-Type JSON và Bearer API key.
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());
        return headers;
    }

    // Note: Hàm lấy phần content text từ response Chat Completions của AI.
    private String extractContent(Map<?, ?> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new BadGatewayException("AI khong tra ve response hop le");
        }

        Object choices = responseBody.get("choices");
        if (!(choices instanceof List<?> choiceItems) || choiceItems.isEmpty()) {
            throw new BadGatewayException("AI khong tra ve choices hop le");
        }

        Object firstChoice = choiceItems.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            throw new BadGatewayException("AI khong tra ve choice hop le");
        }

        Object message = choiceMap.get("message");
        if (!(message instanceof Map<?, ?> messageMap)) {
            throw new BadGatewayException("AI khong tra ve message hop le");
        }

        Object content = messageMap.get("content");
        if (!(content instanceof String text) || text.isBlank()) {
            throw new BadGatewayException("AI khong tra ve noi dung hop le");
        }

        return text;
    }

    private void distributeDurationEqually(List<MilestoneDto> milestones, Integer totalDuration, String durationUnit) {
        int baseDuration = totalDuration / milestones.size();
        int allocated = 0;

        for (int i = 0; i < milestones.size(); i++) {
            int milestoneDuration = i == milestones.size() - 1
                    ? totalDuration - allocated
                    : baseDuration;
            milestones.get(i).setDuration(Math.max(milestoneDuration, 1));
            milestones.get(i).setDurationUnit(durationUnit);
            allocated += milestoneDuration;
        }

        rebalanceDurationTotal(milestones, totalDuration);
    }

    private void rebalanceDurationTotal(List<MilestoneDto> milestones, int totalDuration) {
        int diff = milestones.stream()
                .map(MilestoneDto::getDuration)
                .reduce(0, Integer::sum) - totalDuration;

        for (int i = milestones.size() - 1; diff > 0 && i >= 0; i--) {
            MilestoneDto milestone = milestones.get(i);
            while (diff > 0 && milestone.getDuration() > 1) {
                milestone.setDuration(milestone.getDuration() - 1);
                diff--;
            }
        }
    }

    private boolean isSameDurationUnit(String left, String right) {
        return normalizeDurationUnit(left).equals(normalizeDurationUnit(right));
    }

    private String normalizeDurationUnit(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? new ArrayList<>() : values;
    }

    // Note: Hàm rút gọn lỗi OpenAI để trả message đủ thông tin nhưng không quá dài.
    private String buildOpenAiErrorMessage(RestClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return "AI API loi: " + ex.getStatusCode();
        }

        return "AI API loi: " + ex.getStatusCode() + " - " + truncate(responseBody, 500);
    }

    // Note: Hàm cắt chuỗi dài, dùng cho thông báo lỗi từ AI hoặc JSON parser.
    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }
    private String trimBlankLines(String text) {
        String normalized = text == null ? "" : text;
        return normalized
                .replaceAll("(?m)^[ \\t]*\\r?\\n", "")
                .trim();
    }
}

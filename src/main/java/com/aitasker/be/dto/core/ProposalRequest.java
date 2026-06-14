/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/core/ProposalRequest.java
 * Đây là file gì: DTO nhận dữ liệu chuyên gia gửi proposal, tách request API khỏi entity lưu database.
 * Mục đích note: giải thích annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.core;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.math.BigDecimal;

// Note: Annotation này giúp Lombok sinh getter/setter cho dữ liệu request proposal.
@Data
public class ProposalRequest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Integer jobId;
    private String technicalSolution;
    @JsonAlias({"projectDescription", "expertProjectDescription"})
    private String proposalDescription;
    @JsonAlias({"proposalFile", "proposal_file_url"})
    private String proposalFileUrl;
    private BigDecimal bidAmount;
    @JsonAlias("proposal_milestone")
    private Object proposalMilestone;

    // Note: Hàm `proposalMilestoneText` chuyển JSON array/object hoặc chuỗi JSON thành text để service validate và lưu vào proposals.
    public String proposalMilestoneText() {
        if (proposalMilestone == null) {
            return null;
        }
        if (proposalMilestone instanceof String text) {
            return text;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(proposalMilestone);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("PROPOSAL MILESTONE KHONG PHAI JSON HOP LE");
        }
    }
}

package com.aitasker.be.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SowKeywordsDto {
    private List<String> requiredSkills;
    private List<String> domains;
    private List<String> industries;
    private List<String> projectScope;
    private BudgetRangeDto budgetRange;
    private String timeline;
    private String experienceLevel;
    private List<String> deliverables;
    private List<String> keywords;
}

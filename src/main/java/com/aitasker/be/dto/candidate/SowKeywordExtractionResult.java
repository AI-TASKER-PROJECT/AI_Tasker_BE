package com.aitasker.be.dto.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SowKeywordExtractionResult {
    private List<String> skills;
    private List<String> domains;
    private List<String> keywords;
}

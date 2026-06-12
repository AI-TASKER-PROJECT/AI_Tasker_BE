package com.aitasker.be.dto.sow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SowDto {
    private String title;
    private String overview;
    private List<String> objectives;
    private List<String> scopeOfWork;
    private List<String> deliverables;
    private List<String> assumptions;
    private List<String> outOfScope;
}

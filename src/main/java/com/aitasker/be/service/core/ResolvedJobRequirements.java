package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record ResolvedJobRequirements(
        Map<Integer, String> skillsById,
        Map<Integer, String> domainsById,
        Map<Integer, String> technologiesById,
        Set<Integer> mandatorySkillIds,
        SowKeywordExtractionResult keywordSummary
) {
    public ResolvedJobRequirements {
        skillsById = Collections.unmodifiableMap(new LinkedHashMap<>(skillsById));
        domainsById = Collections.unmodifiableMap(new LinkedHashMap<>(domainsById));
        technologiesById = Collections.unmodifiableMap(new LinkedHashMap<>(technologiesById));
        mandatorySkillIds = Collections.unmodifiableSet(new LinkedHashSet<>(mandatorySkillIds));
    }

    public List<Integer> skillIds() {
        return List.copyOf(skillsById.keySet());
    }

    public List<Integer> domainIds() {
        return List.copyOf(domainsById.keySet());
    }

    public List<Integer> technologyIds() {
        return List.copyOf(technologiesById.keySet());
    }

    public boolean isEmpty() {
        return skillsById.isEmpty() && domainsById.isEmpty() && technologiesById.isEmpty();
    }
}

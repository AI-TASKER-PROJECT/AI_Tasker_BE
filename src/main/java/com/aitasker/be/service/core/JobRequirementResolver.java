package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.JobDomainEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.JobSkillEntity;
import com.aitasker.be.entity.JobTechnologyEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.SkillEntity;
import com.aitasker.be.entity.SowEntity;
import com.aitasker.be.entity.TechnologyEntity;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.JobDomainRepository;
import com.aitasker.be.repository.JobSkillRepository;
import com.aitasker.be.repository.JobTechnologyRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.SkillRepository;
import com.aitasker.be.repository.SowRepository;
import com.aitasker.be.repository.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobRequirementResolver {
    private final SowRepository sowRepository;
    private final MilestoneRepository milestoneRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobDomainRepository jobDomainRepository;
    private final JobTechnologyRepository jobTechnologyRepository;
    private final SkillRepository skillRepository;
    private final DomainRepository domainRepository;
    private final TechnologyRepository technologyRepository;
    private final SowKeywordExtractionService keywordExtractionService;

    public ResolvedJobRequirements resolve(JobEntity job) {
        List<SkillEntity> skills = skillRepository.findByIsActiveTrueOrderBySkillNameAsc();
        List<DomainEntity> domains = domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc();
        List<TechnologyEntity> technologies = technologyRepository
                .findByIsActiveTrueOrderBySortOrderAscTechnologyNameAsc();

        List<JobSkillEntity> mappedSkills = jobSkillRepository.findByIdJobId(job.getJobId());
        List<JobDomainEntity> mappedDomains = jobDomainRepository.findByIdJobId(job.getJobId());
        List<JobTechnologyEntity> mappedTechnologies = jobTechnologyRepository.findByIdJobId(job.getJobId());

        String sourceText = buildSourceText(job);
        SowKeywordExtractionResult extracted = keywordExtractionService.extractKeywordsFromSow(sourceText);

        Map<Integer, SkillEntity> skillCatalog = indexBy(skills, SkillEntity::getSkillId);
        Map<Integer, DomainEntity> domainCatalog = indexBy(domains, DomainEntity::getDomainId);
        Map<Integer, TechnologyEntity> technologyCatalog = indexBy(technologies, TechnologyEntity::getTechnologyId);

        Set<Integer> skillIds = mappedSkills.isEmpty()
                ? resolveSkillFallback(sourceText, extracted.getSkills(), skills)
                : mappedSkills.stream()
                        .map(item -> item.getId().getSkillId())
                        .filter(skillCatalog::containsKey)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Integer> domainIds = mappedDomains.isEmpty()
                ? resolveDomainFallback(sourceText, extracted.getKeywords(), domains)
                : mappedDomains.stream()
                        .map(item -> item.getId().getDomainId())
                        .filter(domainCatalog::containsKey)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Integer> technologyIds = mappedTechnologies.isEmpty()
                ? resolveTechnologyFallback(sourceText, technologies)
                : mappedTechnologies.stream()
                        .map(item -> item.getId().getTechnologyId())
                        .filter(technologyCatalog::containsKey)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Integer> mandatorySkillIds = mappedSkills.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsMandatory()))
                .map(item -> item.getId().getSkillId())
                .filter(skillIds::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Integer, String> skillsById = labels(skillIds, skillCatalog, SkillEntity::getSkillName);
        Map<Integer, String> domainsById = labels(domainIds, domainCatalog, DomainEntity::getDomainName);
        Map<Integer, String> technologiesById = labels(
                technologyIds,
                technologyCatalog,
                TechnologyEntity::getTechnologyName
        );
        List<String> keywordLabels = new ArrayList<>(skillsById.values());
        keywordLabels.addAll(domainsById.values());
        keywordLabels.addAll(technologiesById.values());

        return new ResolvedJobRequirements(
                skillsById,
                domainsById,
                technologiesById,
                mandatorySkillIds,
                SowKeywordExtractionResult.builder()
                        .skills(List.copyOf(skillsById.values()))
                        .domains(List.copyOf(domainsById.values()))
                        .keywords(List.copyOf(new LinkedHashSet<>(keywordLabels)))
                        .build()
        );
    }

    private String buildSourceText(JobEntity job) {
        List<String> parts = new ArrayList<>();
        add(parts, job.getTitle());
        add(parts, job.getRawRequirements());
        add(parts, job.getStructuredSow());
        sowRepository.findByJobId(job.getJobId()).ifPresent(sow -> addSow(parts, sow));
        for (MilestoneEntity milestone : milestoneRepository.findByJobIdOrderByOrderIndexAsc(job.getJobId())) {
            add(parts, milestone.getMilestoneName());
            add(parts, milestone.getDescription());
        }
        return String.join("\n", parts);
    }

    private void addSow(List<String> parts, SowEntity sow) {
        add(parts, sow.getTitle());
        add(parts, sow.getOverview());
        add(parts, sow.getObjectives());
        add(parts, sow.getScopeOfWork());
        add(parts, sow.getDeliverable());
        add(parts, sow.getAssumptions());
        add(parts, sow.getOutOfScope());
    }

    private void add(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }

    private Set<Integer> resolveSkillFallback(
            String sourceText,
            Collection<String> extractedLabels,
            List<SkillEntity> catalog
    ) {
        return catalog.stream()
                .filter(item -> matchesCatalog(
                        sourceText,
                        item.getSkillCode(),
                        item.getSkillName(),
                        item.getDescription(),
                        extractedLabels
                ))
                .map(SkillEntity::getSkillId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Integer> resolveDomainFallback(
            String sourceText,
            Collection<String> extractedLabels,
            List<DomainEntity> catalog
    ) {
        return catalog.stream()
                .filter(item -> matchesCatalog(
                        sourceText,
                        item.getDomainCode(),
                        item.getDomainName(),
                        item.getDescription(),
                        extractedLabels
                ))
                .map(DomainEntity::getDomainId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Integer> resolveTechnologyFallback(String sourceText, List<TechnologyEntity> catalog) {
        return catalog.stream()
                .filter(item -> sourceContainsCatalogTerm(sourceText, item.getTechnologyCode(), item.getTechnologyName()))
                .map(TechnologyEntity::getTechnologyId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean matchesCatalog(
            String sourceText,
            String code,
            String name,
            String description,
            Collection<String> extractedLabels
    ) {
        if (sourceContainsCatalogTerm(sourceText, code, name)) {
            return true;
        }
        String catalogText = String.join(" ", safe(code), safe(name), safe(description));
        for (String label : extractedLabels == null ? List.<String>of() : extractedLabels) {
            if (keywordExtractionService.aliasesForKeyword(label).stream()
                    .anyMatch(alias -> catalogContainsAlias(catalogText, alias))) {
                return true;
            }
        }
        return false;
    }

    private boolean sourceContainsCatalogTerm(String sourceText, String code, String name) {
        return containsTerm(sourceText, code) || containsTerm(sourceText, name);
    }

    private boolean containsTerm(String text, String term) {
        String normalizedText = keywordExtractionService.normalizeForMatching(text);
        String normalizedTerm = keywordExtractionService.normalizeForMatching(term);
        if (normalizedTerm.length() < 3) {
            return false;
        }
        return (" " + normalizedText + " ").contains(" " + normalizedTerm + " ");
    }

    private boolean catalogContainsAlias(String catalogText, String alias) {
        String normalizedCatalog = keywordExtractionService.normalizeForMatching(catalogText);
        String normalizedAlias = keywordExtractionService.normalizeForMatching(alias);
        if ("ai".equals(normalizedAlias)) {
            return (" " + normalizedCatalog + " ").contains(" ai ");
        }
        return normalizedAlias.length() >= 3 && normalizedCatalog.contains(normalizedAlias);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private <T> Map<Integer, T> indexBy(List<T> items, Function<T, Integer> idGetter) {
        return items.stream().collect(Collectors.toMap(
                idGetter,
                Function.identity(),
                (first, second) -> first,
                LinkedHashMap::new
        ));
    }

    private <T> Map<Integer, String> labels(
            Set<Integer> ids,
            Map<Integer, T> catalog,
            Function<T, String> labelGetter
    ) {
        Map<Integer, String> result = new LinkedHashMap<>();
        for (Integer id : ids) {
            T item = catalog.get(id);
            if (item != null) {
                result.put(id, labelGetter.apply(item));
            }
        }
        return result;
    }
}

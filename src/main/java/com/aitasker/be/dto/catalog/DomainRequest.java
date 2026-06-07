package com.aitasker.be.dto.catalog;

import lombok.Data;

@Data
public class DomainRequest {
    private String domainCode;
    private String domainName;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
}

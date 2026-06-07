package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class JobDomainId implements Serializable {
    @Column(name = "job_id") private Integer jobId;
    @Column(name = "domain_id") private Integer domainId;
}

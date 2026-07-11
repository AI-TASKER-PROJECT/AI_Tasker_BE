package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class StaffDomainId implements Serializable {
    @Column(name = "staff_id") private Integer staffId;
    @Column(name = "domain_id") private Integer domainId;
}

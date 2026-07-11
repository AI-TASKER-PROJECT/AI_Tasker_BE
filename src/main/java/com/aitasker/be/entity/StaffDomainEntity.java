package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "staff_domains")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StaffDomainEntity {
    @EmbeddedId private StaffDomainId id;
}

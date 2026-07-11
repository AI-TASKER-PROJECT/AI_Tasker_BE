package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class StaffSkillId implements Serializable {
    @Column(name = "staff_id") private Integer staffId;
    @Column(name = "skill_id") private Integer skillId;
}

package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "staff_skills")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StaffSkillEntity {
    @EmbeddedId private StaffSkillId id;
}

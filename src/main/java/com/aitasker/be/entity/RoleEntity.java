package com.aitasker.be.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default // KHI BULDER MÀ KO TRUYỀN GIÁ TRỊ THÌ SẼ DÙNG GIÁ TRỊ MẶC ĐỊNH ĐÃ KHAI BÁO SẴN
    private List<AccountEntity> accounts = new ArrayList<>();
}

package com.aitasker.be.repository;

import com.aitasker.be.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByRoleName(String roleName);
    Optional<RoleEntity> findByRoleNameIgnoreCase(String roleName);
}

package com.aitasker.be.repository;

import com.aitasker.be.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    Optional<AccountEntity> findByEmail(String email);
    Optional<AccountEntity> findByEmailIgnoreCase(String email);
    Optional<AccountEntity> findFirstByRoleRoleNameOrderByAccountIdAsc(String roleName);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("select a from AccountEntity a join fetch a.role where lower(a.email) = lower(:email)")
    Optional<AccountEntity> findByEmailWithRole(@Param("email") String email);
}

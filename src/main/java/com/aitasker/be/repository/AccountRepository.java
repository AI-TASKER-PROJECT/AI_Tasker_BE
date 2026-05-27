package com.aitasker.be.repository;

import com.aitasker.be.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    Optional<AccountEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("select a from AccountEntity a join fetch a.role where a.email = :email")
    Optional<AccountEntity> findByEmailWithRole(@Param("email") String email);
}
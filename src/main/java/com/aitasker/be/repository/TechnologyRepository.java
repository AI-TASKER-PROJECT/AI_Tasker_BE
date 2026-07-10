/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/TechnologyRepository.java
 * Đây là file gì: Repository truy cập dữ liệu danh mục technologies.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.TechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnologyRepository extends JpaRepository<TechnologyEntity, Integer> {
    List<TechnologyEntity> findByIsActiveTrueOrderBySortOrderAscTechnologyNameAsc();
    Optional<TechnologyEntity> findByTechnologyCode(String technologyCode);
    boolean existsByTechnologyCode(String technologyCode);
}

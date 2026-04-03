package com.solusi.erp.inventory.brand.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandJpaRepository extends JpaRepository<BrandEntity, Long> {

    @Query("SELECT b FROM BrandEntity b WHERE " +
           "LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BrandEntity> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

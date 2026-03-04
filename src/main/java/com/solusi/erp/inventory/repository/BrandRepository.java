package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Brand.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Search brands by code or name (case-insensitive).
     */
    @Query("SELECT b FROM Brand b WHERE " +
           "LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Brand> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

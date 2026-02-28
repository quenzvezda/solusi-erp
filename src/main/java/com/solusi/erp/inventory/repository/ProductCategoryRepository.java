package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ProductCategory.
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    /**
     * Search product categories by code or name (case-insensitive).
     */
    @Query("SELECT pc FROM ProductCategory pc WHERE " +
           "LOWER(pc.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(pc.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductCategory> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

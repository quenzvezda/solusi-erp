package com.solusi.erp.inventory.productcategory.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryEntity, Long> {

    @Query("SELECT pc FROM ProductCategoryEntity pc WHERE " +
           "LOWER(pc.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(pc.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductCategoryEntity> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

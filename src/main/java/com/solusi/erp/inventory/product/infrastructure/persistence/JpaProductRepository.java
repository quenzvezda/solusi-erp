package com.solusi.erp.inventory.product.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByBrandId(Long brandId);

    @Query("SELECT p FROM ProductEntity p WHERE " +
           "(:keyword IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProductEntity> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

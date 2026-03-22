package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.ProductUomConversion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductUomConversionRepository extends JpaRepository<ProductUomConversion, Long> {
    
    List<ProductUomConversion> findByProductId(Long productId);

    Optional<ProductUomConversion> findByProductIdAndFromUomIdAndToUomId(Long productId, Long fromUomId, Long toUomId);

    /**
     * Search conversions by product code or name (case-insensitive).
     */
    @Query("SELECT puc FROM ProductUomConversion puc " +
           "JOIN puc.product p " +
           "WHERE (:keyword IS NULL OR :keyword = '') OR " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProductUomConversion> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByProductIdAndFromUomId(Long productId, Long fromUomId);

    boolean existsByProductIdAndFromUomIdAndIdNot(Long productId, Long fromUomId, Long id);
}

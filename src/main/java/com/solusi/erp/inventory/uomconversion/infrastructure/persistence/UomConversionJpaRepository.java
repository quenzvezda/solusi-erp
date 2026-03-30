package com.solusi.erp.inventory.uomconversion.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UomConversionJpaRepository extends JpaRepository<UomConversionEntity, Long> {

    List<UomConversionEntity> findByProductId(Long productId);

    Optional<UomConversionEntity> findByProductIdAndFromUomIdAndToUomId(
            Long productId, Long fromUomId, Long toUomId);

    @Query("SELECT e FROM UomConversionEntity e " +
           "WHERE (:keyword IS NULL OR :keyword = '') OR " +
           "EXISTS (SELECT 1 FROM com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity p " +
           "WHERE p.id = e.productId AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<UomConversionEntity> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByProductIdAndFromUomId(Long productId, Long fromUomId);

    boolean existsByProductIdAndFromUomIdAndIdNot(Long productId, Long fromUomId, Long id);
}

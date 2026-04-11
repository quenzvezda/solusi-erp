package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SupplierPriceListJpaRepository extends JpaRepository<SupplierPriceListEntity, Long> {

    @Query("SELECT s FROM SupplierPriceListEntity s WHERE " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(s.unitPrice AS string) LIKE CONCAT('%', :keyword, '%')")
    Page<SupplierPriceListEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM SupplierPriceListEntity s WHERE " +
           "s.supplierId = :supplierId AND s.productId = :productId AND " +
           "s.uomId = :uomId AND s.currencyId = :currencyId AND " +
           "s.active = true AND " +
           "(:excludeId IS NULL OR s.id <> :excludeId) AND " +
           "s.effectiveFrom <= COALESCE(:effectiveTo, s.effectiveFrom) AND " +
           "(s.effectiveTo IS NULL OR s.effectiveTo >= :effectiveFrom)")
    boolean existsOverlapping(@Param("supplierId") Long supplierId,
                               @Param("productId") Long productId,
                               @Param("uomId") Long uomId,
                               @Param("currencyId") Long currencyId,
                               @Param("effectiveFrom") LocalDate effectiveFrom,
                               @Param("effectiveTo") LocalDate effectiveTo,
                               @Param("excludeId") Long excludeId);
}

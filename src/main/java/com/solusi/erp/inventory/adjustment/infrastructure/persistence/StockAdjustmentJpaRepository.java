package com.solusi.erp.inventory.adjustment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockAdjustmentJpaRepository extends JpaRepository<StockAdjustmentEntity, Long> {

    @Query("SELECT s FROM StockAdjustmentEntity s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StockAdjustmentEntity> search(@Param("keyword") String keyword, Pageable pageable);
    boolean existsByFacilityId(Long facilityId);
}

package com.solusi.erp.inventory.stock.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ValuationLayerJpaRepository extends JpaRepository<ValuationLayerEntity, Long> {

    /**
     * Find available layers for FIFO consumption.
     * Sorted by createdDate (oldest first).
     */
    List<ValuationLayerEntity> findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
            Long productId, Long containerId, BigDecimal remainingQuantity);

    /**
     * Find available layers for serialized items.
     */
    List<ValuationLayerEntity> findByProductIdAndContainerIdAndSerialNumberAndRemainingQuantityGreaterThan(
            Long productId, Long containerId, String serialNumber, BigDecimal remainingQuantity);
}

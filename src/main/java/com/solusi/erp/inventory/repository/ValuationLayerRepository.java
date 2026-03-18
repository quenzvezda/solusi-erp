package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.ValuationLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValuationLayerRepository extends JpaRepository<ValuationLayer, Long> {
    
    /**
     * Find available layers for FIFO consumption.
     * Sorted by createdDate (oldest first).
     */
    List<ValuationLayer> findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
            Long productId, Long containerId, java.math.BigDecimal remainingQuantity);

    /**
     * Find available layers for serialized items.
     */
    List<ValuationLayer> findByProductIdAndContainerIdAndSerialNumberAndRemainingQuantityGreaterThan(
            Long productId, Long containerId, String serialNumber, java.math.BigDecimal remainingQuantity);
}

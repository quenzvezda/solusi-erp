package com.solusi.erp.inventory.stock.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;

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

    List<ValuationLayerEntity> findByProductIdAndContainerIdAndReferenceTypeAndReferenceIdAndReferenceLineIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
            Long productId, Long containerId, ReferenceType referenceType, Long referenceId,
            Long referenceLineId, BigDecimal remainingQuantity);

    List<ValuationLayerEntity> findByProductIdAndContainerIdAndSerialNumberAndReferenceTypeAndReferenceIdAndReferenceLineIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
            Long productId, Long containerId, String serialNumber, ReferenceType referenceType,
            Long referenceId, Long referenceLineId, BigDecimal remainingQuantity);
}

package com.solusi.erp.inventory.stock.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for Inventory Movement.
 */
@Repository
public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementEntity, Long> {

    @Query("SELECT m FROM InventoryMovementEntity m " +
           "WHERE (:productId IS NULL OR m.productId = :productId) " +
           "AND (:containerId IS NULL OR m.containerId = :containerId) " +
           "AND (:startDate IS NULL OR m.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR m.transactionDate <= :endDate) " +
           "ORDER BY m.transactionDate DESC, m.id DESC")
    Page<InventoryMovementEntity> search(@Param("productId") Long productId,
                                         @Param("containerId") Long containerId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    boolean existsByContainerId(Long containerId);
}

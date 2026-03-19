package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.InventoryMovement;
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
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @Query("SELECT m FROM InventoryMovement m " +
           "WHERE (:productId IS NULL OR m.product.id = :productId) " +
           "AND (:containerId IS NULL OR m.container.id = :containerId) " +
           "AND (:startDate IS NULL OR m.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR m.transactionDate <= :endDate) " +
           "ORDER BY m.transactionDate DESC, m.id DESC")
    Page<InventoryMovement> search(@Param("productId") Long productId,
                                  @Param("containerId") Long containerId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);
}

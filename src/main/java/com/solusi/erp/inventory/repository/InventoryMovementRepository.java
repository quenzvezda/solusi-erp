package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Inventory Movement.
 */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
}

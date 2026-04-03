package com.solusi.erp.inventory.stock.infrastructure.adapter;

import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;

/**
 * Checks whether any InventoryMovement record references the given container.
 * Movement history must be preserved; container cannot be deleted if movements exist.
 */
public class InventoryMovementContainerUsageChecker implements ContainerUsageChecker {

    private final InventoryMovementJpaRepository movementRepository;

    public InventoryMovementContainerUsageChecker(InventoryMovementJpaRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    public boolean isUsed(Long containerId) {
        return movementRepository.existsByContainerId(containerId);
    }
}

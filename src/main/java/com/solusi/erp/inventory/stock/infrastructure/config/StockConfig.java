package com.solusi.erp.inventory.stock.infrastructure.config;

import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.adapter.InventoryMovementContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.adapter.StockBalanceContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers stock-slice consumer beans for cross-slice delete guards.
 */
@Configuration
public class StockConfig {

    @Bean
    public ContainerUsageChecker stockBalanceContainerUsageChecker(
            StockBalanceJpaRepository stockBalanceJpaRepository) {
        return new StockBalanceContainerUsageChecker(stockBalanceJpaRepository);
    }

    @Bean
    public ContainerUsageChecker inventoryMovementContainerUsageChecker(
            InventoryMovementJpaRepository inventoryMovementJpaRepository) {
        return new InventoryMovementContainerUsageChecker(inventoryMovementJpaRepository);
    }
}

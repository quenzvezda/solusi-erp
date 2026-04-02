package com.solusi.erp.inventory.stock.infrastructure.config;

import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.domain.repository.ValuationLayerRepository;
import com.solusi.erp.inventory.stock.domain.service.FifoValuationService;
import com.solusi.erp.inventory.stock.infrastructure.adapter.InventoryMovementContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.adapter.StockBalanceContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.adapter.StockBalanceRepositoryImpl;
import com.solusi.erp.inventory.stock.infrastructure.adapter.ValuationLayerRepositoryImpl;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalancePersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerPersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.service.StockServiceImpl;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Stock module.
 * Wires domain models, repositories, services, and cross-slice beans.
 */
@Configuration
public class StockConfig {

    @Bean
    public StockBalanceRepository stockBalanceDomainRepository(
            StockBalanceJpaRepository jpaRepository,
            StockBalancePersistenceMapper mapper) {
        return new StockBalanceRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public ValuationLayerRepository valuationLayerDomainRepository(
            ValuationLayerJpaRepository jpaRepository,
            ValuationLayerPersistenceMapper mapper) {
        return new ValuationLayerRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public FifoValuationService fifoValuationService(
            ValuationLayerRepository valuationLayerDomainRepository) {
        return new FifoValuationService(valuationLayerDomainRepository);
    }

    @Bean
    public StockService stockService(
            StockBalanceRepository stockBalanceDomainRepository,
            InventoryMovementJpaRepository inventoryMovementJpaRepository,
            JpaProductRepository productRepository,
            UomConversionService uomConversionService,
            FifoValuationService fifoValuationService,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        StockServiceImpl pure = new StockServiceImpl(
                stockBalanceDomainRepository, inventoryMovementJpaRepository,
                productRepository, uomConversionService,
                fifoValuationService, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (payload) -> tx.executeWithoutResult(status -> pure.adjust(payload));
    }

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

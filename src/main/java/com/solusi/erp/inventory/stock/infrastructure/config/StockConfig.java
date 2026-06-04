package com.solusi.erp.inventory.stock.infrastructure.config;

import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.domain.repository.InventoryReservationRepository;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.domain.repository.ValuationLayerRepository;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.stock.domain.service.FifoValuationService;
import com.solusi.erp.inventory.stock.infrastructure.adapter.InventoryMovementContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.adapter.InventoryReservationRepositoryImpl;
import com.solusi.erp.inventory.stock.infrastructure.adapter.StockBalanceContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.adapter.StockBalanceRepositoryImpl;
import com.solusi.erp.inventory.stock.infrastructure.adapter.ValuationLayerRepositoryImpl;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryReservationJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryReservationPersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalancePersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerPersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.service.StockServiceImpl;
import com.solusi.erp.inventory.stock.infrastructure.service.InventoryReservationServiceImpl;
import com.solusi.erp.inventory.stock.infrastructure.service.StockMovementReversalServiceImpl;
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
    public InventoryReservationRepository inventoryReservationDomainRepository(
            InventoryReservationJpaRepository jpaRepository,
            InventoryReservationPersistenceMapper mapper) {
        return new InventoryReservationRepositoryImpl(jpaRepository, mapper);
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
            UomConversionService uomConversionService,
            FifoValuationService fifoValuationService,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        StockServiceImpl pure = new StockServiceImpl(
                stockBalanceDomainRepository, inventoryMovementJpaRepository,
                uomConversionService,
                fifoValuationService, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (payload) -> tx.executeWithoutResult(status -> pure.adjust(payload));
    }

    @Bean
    public InventoryReservationService inventoryReservationService(
            InventoryReservationRepository inventoryReservationDomainRepository,
            StockService stockService,
            PlatformTransactionManager txManager) {
        InventoryReservationServiceImpl pure = new InventoryReservationServiceImpl(
                inventoryReservationDomainRepository, stockService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return new InventoryReservationService() {
            @Override
            public void reserve(com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType ownerType,
                                Long ownerId, String ownerCode,
                                java.util.List<com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest> requests) {
                tx.executeWithoutResult(status -> pure.reserve(ownerType, ownerId, ownerCode, requests));
            }

            @Override
            public void release(com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType ownerType, Long ownerId) {
                tx.executeWithoutResult(status -> pure.release(ownerType, ownerId));
            }

            @Override
            public void assertActiveCoverage(com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType ownerType,
                                             Long ownerId,
                                             java.util.List<com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest> requests) {
                pure.assertActiveCoverage(ownerType, ownerId, requests);
            }

            @Override
            public void consume(com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType ownerType, Long ownerId) {
                tx.executeWithoutResult(status -> pure.consume(ownerType, ownerId));
            }
        };
    }

    @Bean
    public StockMovementReversalService stockMovementReversalService(
            InventoryMovementJpaRepository inventoryMovementJpaRepository,
            ContainerJpaRepository containerJpaRepository,
            GridJpaRepository gridJpaRepository,
            StockBalanceRepository stockBalanceDomainRepository,
            StockService stockService,
            PlatformTransactionManager txManager) {
        StockMovementReversalServiceImpl pure = new StockMovementReversalServiceImpl(
                inventoryMovementJpaRepository,
                containerJpaRepository,
                gridJpaRepository,
                stockBalanceDomainRepository,
                stockService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return requests -> tx.executeWithoutResult(status -> pure.reverse(requests));
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

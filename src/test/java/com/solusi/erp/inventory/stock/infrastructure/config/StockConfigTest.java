package com.solusi.erp.inventory.stock.infrastructure.config;

import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.InventoryReservationRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryReservationJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryReservationPersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalancePersistenceMapper;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerPersistenceMapper;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StockConfig.class, StockConfigTest.MocksConfig.class})
class StockConfigTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private InventoryReservationService inventoryReservationService;

    @Test
    void wiresStockAndReservationBeans() {
        assertThat(stockService).isNotNull();
        assertThat(inventoryReservationRepository).isNotNull();
        assertThat(inventoryReservationService).isNotNull();
    }

    @Configuration
    static class MocksConfig {

        @Bean StockBalanceJpaRepository stockBalanceJpaRepository() { return mock(StockBalanceJpaRepository.class); }
        @Bean StockBalancePersistenceMapper stockBalancePersistenceMapper() { return mock(StockBalancePersistenceMapper.class); }
        @Bean ValuationLayerJpaRepository valuationLayerJpaRepository() { return mock(ValuationLayerJpaRepository.class); }
        @Bean ValuationLayerPersistenceMapper valuationLayerPersistenceMapper() { return mock(ValuationLayerPersistenceMapper.class); }
        @Bean InventoryReservationJpaRepository inventoryReservationJpaRepository() { return mock(InventoryReservationJpaRepository.class); }
        @Bean InventoryReservationPersistenceMapper inventoryReservationPersistenceMapper() { return mock(InventoryReservationPersistenceMapper.class); }
        @Bean InventoryMovementJpaRepository inventoryMovementJpaRepository() { return mock(InventoryMovementJpaRepository.class); }
        @Bean UomConversionService uomConversionService() { return mock(UomConversionService.class); }
        @Bean MessageSource messageSource() { return mock(MessageSource.class); }
        @Bean PlatformTransactionManager platformTransactionManager() { return mock(PlatformTransactionManager.class); }
    }
}

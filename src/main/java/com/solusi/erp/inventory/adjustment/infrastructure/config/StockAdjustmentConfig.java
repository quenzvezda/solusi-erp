package com.solusi.erp.inventory.adjustment.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.adjustment.application.usecase.command.*;
import com.solusi.erp.inventory.adjustment.application.usecase.query.*;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.adapter.AdjustmentFacilityUsageChecker;
import com.solusi.erp.inventory.adjustment.infrastructure.adapter.AdjustmentLineGridUsageChecker;
import com.solusi.erp.inventory.adjustment.infrastructure.adapter.StockAdjustmentRepositoryImpl;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentJpaRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentLineJpaRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentPersistenceMapper;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.facility.domain.port.FacilityUsageChecker;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for StockAdjustment module.
 * Each use case gets its own TransactionTemplate instance.
 */
@Configuration
public class StockAdjustmentConfig {

    @Bean
    public StockAdjustmentPersistenceMapper stockAdjustmentPersistenceMapper(
            com.solusi.erp.master.currency.domain.repository.CurrencyRepository currencyRepository,
            FacilityJpaRepository facilityJpaRepository,
            JpaProductRepository productJpaRepository,
            GridJpaRepository gridJpaRepository,
            ContainerJpaRepository containerJpaRepository,
            UomJpaRepository uomJpaRepository) {
        return new StockAdjustmentPersistenceMapper(currencyRepository, facilityJpaRepository,
                productJpaRepository, gridJpaRepository, containerJpaRepository, uomJpaRepository);
    }

    @Bean
    public FacilityUsageChecker adjustmentFacilityUsageChecker(
            StockAdjustmentJpaRepository adjustmentJpaRepository) {
        return new AdjustmentFacilityUsageChecker(adjustmentJpaRepository);
    }

    @Bean
    public GridUsageChecker adjustmentLineGridUsageChecker(
            StockAdjustmentLineJpaRepository lineJpaRepository) {
        return new AdjustmentLineGridUsageChecker(lineJpaRepository);
    }

    @Bean
    public StockAdjustmentRepository stockAdjustmentDomainRepository(
            StockAdjustmentJpaRepository jpaStockAdjustmentRepository,
            StockAdjustmentPersistenceMapper mapper) {
        return new StockAdjustmentRepositoryImpl(jpaStockAdjustmentRepository, mapper);
    }

    @Bean
    public CreateStockAdjustmentUseCase createStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            FacilityJpaRepository facilityJpaRepository,
            com.solusi.erp.master.currency.domain.repository.CurrencyRepository currencyRepository,
            SequenceGeneratorService sequenceGeneratorService,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        CreateStockAdjustmentUseCase pure = new CreateStockAdjustmentUseCaseImpl(
                stockAdjustmentDomainRepository, facilityJpaRepository, currencyRepository,
                sequenceGeneratorService, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (d, n, fId, cId, er, lines) -> tx.execute(s -> pure.execute(d, n, fId, cId, er, lines));
    }

    @Bean
    public UpdateStockAdjustmentUseCase updateStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            FacilityJpaRepository facilityJpaRepository,
            com.solusi.erp.master.currency.domain.repository.CurrencyRepository currencyRepository,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        UpdateStockAdjustmentUseCase pure = new UpdateStockAdjustmentUseCaseImpl(
                stockAdjustmentDomainRepository, facilityJpaRepository, currencyRepository, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, d, n, fId, cId, er, lines) -> tx.execute(s -> pure.execute(id, d, n, fId, cId, er, lines));
    }

    @Bean
    public DeleteStockAdjustmentUseCase deleteStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        DeleteStockAdjustmentUseCase pure = new DeleteStockAdjustmentUseCaseImpl(
                stockAdjustmentDomainRepository, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(s -> pure.execute(id));
    }

    @Bean
    public ProcessStockAdjustmentUseCase processStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            StockService stockService,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        ProcessStockAdjustmentUseCase pure = new ProcessStockAdjustmentUseCaseImpl(
                stockAdjustmentDomainRepository, stockService, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(s -> pure.execute(id));
    }

    @Bean
    public FindStockAdjustmentsUseCase findStockAdjustmentsUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            PlatformTransactionManager txManager) {
        FindStockAdjustmentsUseCase pure = new FindStockAdjustmentsUseCaseImpl(stockAdjustmentDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(s -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetStockAdjustmentUseCase getStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            PlatformTransactionManager txManager) {
        GetStockAdjustmentUseCase pure = new GetStockAdjustmentUseCaseImpl(stockAdjustmentDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(s -> pure.execute(id));
    }

    @Bean
    public GetStockAdjustmentEditViewUseCase getStockAdjustmentEditViewUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            PlatformTransactionManager txManager) {
        GetStockAdjustmentEditViewUseCase pure = new GetStockAdjustmentEditViewUseCaseImpl(stockAdjustmentDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(s -> pure.execute(id));
    }
}


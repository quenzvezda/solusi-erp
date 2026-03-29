package com.solusi.erp.inventory.adjustment.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.adjustment.application.usecase.command.*;
import com.solusi.erp.inventory.adjustment.application.usecase.query.*;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.adapter.StockAdjustmentRepositoryImpl;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentPersistenceMapper;
import com.solusi.erp.inventory.service.StockService;
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
            com.solusi.erp.master.currency.domain.repository.CurrencyRepository currencyRepository) {
        return new StockAdjustmentPersistenceMapper(currencyRepository);
    }

    @Bean
    public StockAdjustmentRepository stockAdjustmentDomainRepository(
            StockAdjustmentPersistenceMapper mapper,
            com.solusi.erp.inventory.repository.StockAdjustmentRepository jpaStockAdjustmentRepository,
            com.solusi.erp.inventory.repository.FacilityRepository facilityRepository,
            com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository productRepository,
            com.solusi.erp.inventory.repository.ContainerRepository containerRepository,
            com.solusi.erp.inventory.repository.GridRepository gridRepository,
            com.solusi.erp.inventory.repository.UnitOfMeasureRepository uomRepository) {
        return new StockAdjustmentRepositoryImpl(jpaStockAdjustmentRepository, facilityRepository,
                productRepository, containerRepository, gridRepository, uomRepository, mapper);
    }

    @Bean
    public CreateStockAdjustmentUseCase createStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            com.solusi.erp.inventory.repository.FacilityRepository facilityRepository,
            com.solusi.erp.master.currency.domain.repository.CurrencyRepository currencyRepository,
            SequenceGeneratorService sequenceGeneratorService,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        CreateStockAdjustmentUseCase pure = new CreateStockAdjustmentUseCaseImpl(
                stockAdjustmentDomainRepository, facilityRepository, currencyRepository,
                sequenceGeneratorService, messageSource);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (d, n, fId, cId, er, lines) -> tx.execute(s -> pure.execute(d, n, fId, cId, er, lines));
    }

    @Bean
    public UpdateStockAdjustmentUseCase updateStockAdjustmentUseCase(
            StockAdjustmentRepository stockAdjustmentDomainRepository,
            com.solusi.erp.inventory.repository.FacilityRepository facilityRepository,
            com.solusi.erp.master.currency.domain.repository.CurrencyRepository currencyRepository,
            MessageSource messageSource,
            PlatformTransactionManager txManager) {
        UpdateStockAdjustmentUseCase pure = new UpdateStockAdjustmentUseCaseImpl(
                stockAdjustmentDomainRepository, facilityRepository, currencyRepository, messageSource);
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


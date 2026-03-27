package com.solusi.erp.inventory.uomconversion.infrastructure.config;

import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.repository.ProductUomConversionRepository;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.uomconversion.application.usecase.command.*;
import com.solusi.erp.inventory.uomconversion.application.usecase.query.*;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.adapter.UomConversionRepositoryImpl;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for UomConversion module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class UomConversionConfig {

    @Bean
    public UomConversionRepository uomConversionDomainRepository(
            ProductUomConversionRepository jpaRepo,
            JpaProductRepository productRepo,
            UnitOfMeasureRepository uomRepo,
            UomConversionPersistenceMapper mapper) {
        return new UomConversionRepositoryImpl(jpaRepo, productRepo, uomRepo, mapper);
    }

    @Bean
    public CreateUomConversionUseCase createUomConversionUseCase(
            UomConversionRepository uomConversionDomainRepository,
            JpaProductRepository productRepo,
            UnitOfMeasureRepository uomRepo,
            PlatformTransactionManager txManager) {
        CreateUomConversionUseCase pure = new CreateUomConversionUseCaseImpl(
            uomConversionDomainRepository, productRepo, uomRepo);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (productId, fromUomId, factor) -> tx.execute(status -> pure.execute(productId, fromUomId, factor));
    }

    @Bean
    public UpdateUomConversionUseCase updateUomConversionUseCase(
            UomConversionRepository uomConversionDomainRepository,
            UnitOfMeasureRepository uomRepo,
            PlatformTransactionManager txManager) {
        UpdateUomConversionUseCase pure = new UpdateUomConversionUseCaseImpl(
            uomConversionDomainRepository, uomRepo);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, fromUomId, factor) -> tx.execute(status -> pure.execute(id, fromUomId, factor));
    }

    @Bean
    public DeleteUomConversionUseCase deleteUomConversionUseCase(
            UomConversionRepository uomConversionDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteUomConversionUseCase pure = new DeleteUomConversionUseCaseImpl(uomConversionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindUomConversionsUseCase findUomConversionsUseCase(
            UomConversionRepository uomConversionDomainRepository,
            PlatformTransactionManager txManager) {
        FindUomConversionsUseCase pure = new FindUomConversionsUseCaseImpl(uomConversionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetUomConversionEditViewUseCase getUomConversionEditViewUseCase(
            UomConversionRepository uomConversionDomainRepository,
            PlatformTransactionManager txManager) {
        GetUomConversionEditViewUseCase pure = new GetUomConversionEditViewUseCaseImpl(uomConversionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetUomConversionLookupUseCase getUomConversionLookupUseCase(
            UomConversionRepository uomConversionDomainRepository,
            JpaProductRepository productRepo,
            UnitOfMeasureRepository uomRepo,
            PlatformTransactionManager txManager) {
        GetUomConversionLookupUseCase pure = new GetUomConversionLookupUseCaseImpl(
            uomConversionDomainRepository, productRepo, uomRepo);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (productId) -> tx.execute(status -> pure.getConversionsForProduct(productId));
    }
}

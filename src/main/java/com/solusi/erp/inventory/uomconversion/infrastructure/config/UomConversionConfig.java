package com.solusi.erp.inventory.uomconversion.infrastructure.config;

import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.inventory.uomconversion.application.usecase.command.*;
import com.solusi.erp.inventory.uomconversion.application.usecase.query.*;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.adapter.UomConversionRepositoryImpl;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionJpaRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class UomConversionConfig {

    @Bean
    public UomConversionPersistenceMapper uomConversionPersistenceMapper(
            JpaProductRepository productRepo,
            UomJpaRepository uomRepo) {
        return new UomConversionPersistenceMapper(productRepo, uomRepo);
    }

    @Bean
    public UomConversionRepository uomConversionDomainRepository(
            UomConversionJpaRepository jpaRepo,
            UomConversionPersistenceMapper mapper) {
        return new UomConversionRepositoryImpl(jpaRepo, mapper);
    }

    @Bean
    public CreateUomConversionUseCase createUomConversionUseCase(
            UomConversionRepository uomConversionDomainRepository,
            JpaProductRepository productRepo,
            UomJpaRepository uomRepo,
            PlatformTransactionManager txManager) {
        CreateUomConversionUseCase pure = new CreateUomConversionUseCaseImpl(
            uomConversionDomainRepository, productRepo, uomRepo);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (productId, fromUomId, factor) -> tx.execute(status -> pure.execute(productId, fromUomId, factor));
    }

    @Bean
    public UpdateUomConversionUseCase updateUomConversionUseCase(
            UomConversionRepository uomConversionDomainRepository,
            UomJpaRepository uomRepo,
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
            UomJpaRepository uomRepo,
            PlatformTransactionManager txManager) {
        GetUomConversionLookupUseCase pure = new GetUomConversionLookupUseCaseImpl(
            uomConversionDomainRepository, productRepo, uomRepo);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (productId) -> tx.execute(status -> pure.getConversionsForProduct(productId));
    }
}

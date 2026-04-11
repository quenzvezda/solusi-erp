package com.solusi.erp.purchasing.supplierpricelist.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.adapter.SupplierPriceListRepositoryImpl;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SupplierPriceListConfig {

    @Bean
    public SupplierPriceListRepository supplierPriceListDomainRepository(
            SupplierPriceListJpaRepository jpaRepository,
            SupplierPriceListPersistenceMapper mapper) {
        return new SupplierPriceListRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateSupplierPriceListUseCase createSupplierPriceListUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateSupplierPriceListUseCase pure = new CreateSupplierPriceListUseCaseImpl(
            supplierPriceListDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (supplierId, productId, uomId, currencyId, unitPrice, minQuantity,
                effectiveFrom, effectiveTo, note, active) ->
            tx.execute(status -> pure.execute(supplierId, productId, uomId, currencyId,
                unitPrice, minQuantity, effectiveFrom, effectiveTo, note, active));
    }

    @Bean
    public UpdateSupplierPriceListUseCase updateSupplierPriceListUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateSupplierPriceListUseCase pure = new UpdateSupplierPriceListUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, productId, uomId, currencyId, unitPrice, minQuantity,
                effectiveFrom, effectiveTo, note, active) ->
            tx.execute(status -> pure.execute(id, productId, uomId, currencyId,
                unitPrice, minQuantity, effectiveFrom, effectiveTo, note, active));
    }

    @Bean
    public DeleteSupplierPriceListUseCase deleteSupplierPriceListUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteSupplierPriceListUseCase pure = new DeleteSupplierPriceListUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindSupplierPriceListsUseCase findSupplierPriceListsUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        FindSupplierPriceListsUseCase pure = new FindSupplierPriceListsUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetSupplierPriceListEditViewUseCase getSupplierPriceListEditViewUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        GetSupplierPriceListEditViewUseCase pure = new GetSupplierPriceListEditViewUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
